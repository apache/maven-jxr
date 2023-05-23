/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.jxr.pacman;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * PacMan implementation of a JavaFile. This will parse out the file and
 * determine package, class, and imports
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 */
public class JavaFileImpl extends JavaFile {

    private final List<String> classTypes = Arrays.asList("class", "interface", "enum", "record");

    /**
     * Constructor of a new object that points to a given file.
     *
     * @param path path of the file
     * @param encoding encoding of the file
     * @throws IOException on parsing failure
     */
    public JavaFileImpl(Path path, String encoding) throws IOException {
        super(path, encoding);

        // always add java.lang.* to the package imports because the JVM always
        // does this implicitly.  Unless we add this to the ImportTypes JXR
        // won't pick up on this.
        this.addImportType(new ImportType("java.lang.*"));

        // now parse out this file.
        this.parse();
    }

    /**
     * Opens up the file and try to determine package, class and import statements.
     */
    private void parse() throws IOException {
        StreamTokenizer stok = null;
        try (Reader reader = getReader()) {
            stok = this.getTokenizer(reader);

            parseRecursive("", stok);
        } finally {
            stok = null;
        }
    }

    private void parseRecursive(String nestedPrefix, StreamTokenizer stok) throws IOException {
        int openBracesCount = 0;

        char prevttype = Character.MIN_VALUE; // previous token type
        boolean inTripleQuote = false; // used to toggle between inside/outside triple-quoted multi-line strings

        while (stok.nextToken() != StreamTokenizer.TT_EOF) {

            if (stok.sval == null) {
                if (stok.ttype == '{') {
                    openBracesCount++;
                } else if (stok.ttype == '}') {
                    if (--openBracesCount == 0) {
                        // break out of recursive
                        return;
                    }
                }
                continue;
            } else {
                if ('"' == stok.ttype && '"' == prevttype) {
                    inTripleQuote = !inTripleQuote;
                }
                prevttype = (char) stok.ttype;
                if (inTripleQuote) {
                    // skip content found inside triple-quoted multi-line Java 15 String
                    continue;
                }
            }

            // set the package
            if ("package".equals(stok.sval) && stok.ttype != '\"') {
                stok.nextToken();
                if (stok.sval != null) {
                    this.setPackageType(new PackageType(stok.sval));
                }
            }

            // set the imports
            if ("import".equals(stok.sval) && stok.ttype != '\"') {
                stok.nextToken();

                String name = stok.sval;

                /*
                WARNING: this is a bug/non-feature in the current
                StreamTokenizer.  We needed to set the comment char as "*"
                and packages that are imported with this (ex "test.*") will be
                stripped( and become "test." ).  Here we need to test for this
                and if necessary re-add the char.
                */
                if (name != null) {
                    if (name.charAt(name.length() - 1) == '.') {
                        name = name + '*';
                    }
                    this.addImportType(new ImportType(name));
                }
            }

            // Add the class or classes. There can be several classes in one file so
            // continue with the while loop to get them all.
            if (classTypes.contains(stok.sval) && stok.ttype != '"') {
                stok.nextToken();
                if (stok.sval != null) {
                    this.addClassType(
                            new ClassType(nestedPrefix + stok.sval, getFilenameWithoutPathOrExtension(this.getPath())));
                    parseRecursive(nestedPrefix + stok.sval + ".", stok);
                }
            }
        }
    }

    /**
     * Gets a {@link StreamTokenizer} for this file.
     */
    private StreamTokenizer getTokenizer(Reader reader) {
        StreamTokenizer stok = new StreamTokenizer(reader);

        stok.commentChar('*');
        stok.wordChars('_', '_');

        // set tokenizer to skip comments
        stok.slashStarComments(true);
        stok.slashSlashComments(true);

        return stok;
    }

    private Reader getReader() throws IOException {
        if (Files.notExists(this.getPath())) {
            throw new IOException(this.getPath() + " does not exist!");
        }

        if (this.getEncoding() != null) {
            return new InputStreamReader(new FileInputStream(this.getPath().toFile()), this.getEncoding());
        } else {
            return new FileReader(this.getPath().toFile());
        }
    }
}
