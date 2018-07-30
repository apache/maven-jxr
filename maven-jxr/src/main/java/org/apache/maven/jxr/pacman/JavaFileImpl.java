package org.apache.maven.jxr.pacman;

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

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PacMan implementation of a JavaFile. This will parse out the file and
 * determine package, class, and imports
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 * @version $Id$
 */
public class JavaFileImpl
    extends JavaFile
{
    /**
     * Create a new JavaFileImpl that points to a given file...
     *
     * @param path
     * @param encoding
     * @throws IOException
     */
    public JavaFileImpl( Path path, String encoding )
        throws IOException
    {
        super( path, encoding );

        //always add java.lang.* to the package imports because the JVM always
        //does this implicitly.  Unless we add this to the ImportTypes JXR
        //won't pick up on this.
        this.addImportType( new ImportType( "java.lang.*" ) );

        //now parse out this file.
        this.parse();
    }

    /**
     * Open up the file and try to determine package, class and import
     * statements.
     */
    private void parse()
        throws IOException
    {
        StreamTokenizer stok = null;
        try ( Reader reader = getReader() )
        {
            stok = this.getTokenizer( reader );

            parseRecursive( "", stok );
        }
        finally
        {
            stok = null;
        }
    }

    private void parseRecursive( String nestedPrefix, StreamTokenizer stok )
            throws IOException
    {
        int openBracesCount = 0;

        while ( stok.nextToken() != StreamTokenizer.TT_EOF )
        {

            if ( stok.sval == null )
            {
                if ( stok.ttype == '{' )
                {
                    openBracesCount++;
                }
                else if ( stok.ttype == '}' )
                {
                    if ( --openBracesCount == 0 )
                    {
                        // break out of recursive
                        return;
                    }
                }
                continue;
            }

            //set the package
            if ( "package".equals( stok.sval ) && stok.ttype != '\"' )
            {
                stok.nextToken();
                this.setPackageType( new PackageType( stok.sval ) );
            }

            //set the imports
            if ( "import".equals( stok.sval )  && stok.ttype != '\"' )
            {
                stok.nextToken();

                String name = stok.sval;

                /*
                WARNING: this is a bug/non-feature in the current
                StreamTokenizer.  We needed to set the comment char as "*"
                and packages that are imported with this (ex "test.*") will be
                stripped( and become "test." ).  Here we need to test for this
                and if necessary re-add the char.
                */
                if ( name.charAt( name.length() - 1 ) == '.' )
                {
                    name = name + '*';
                }

                this.addImportType( new ImportType( name ) );
            }

            // Add the class or classes. There can be several classes in one file so
            // continue with the while loop to get them all.
            if ( ( "class".equals( stok.sval ) || "interface".equals( stok.sval ) || "enum".equals( stok.sval ) )
                    &&  stok.ttype != '\"' )
            {
                stok.nextToken();
                this.addClassType( new ClassType( nestedPrefix + stok.sval,
                        getFilenameWithoutPathOrExtension( this.getPath() ) ) );
                parseRecursive( nestedPrefix + stok.sval + ".", stok );
            }

        }
    }

    /**
     * Remove the path and the ".java" extension from a filename.
     */
    private static String getFilenameWithoutPathOrExtension( Path path )
    {
        String newFilename = path.getFileName().toString();
        // Remove the ".java" extension from the filename, if it exists
        int extensionIndex = newFilename.lastIndexOf( ".java" );
        if ( extensionIndex >= 0 )
        {
            newFilename = newFilename.substring( 0, extensionIndex );
        }
        return newFilename;
    }

    /**
     * Get a StreamTokenizer for this file.
     */
    private StreamTokenizer getTokenizer( Reader reader )
    {
        StreamTokenizer stok = new StreamTokenizer( reader );
        //int tok;

        stok.commentChar( '*' );
        stok.wordChars( '_', '_' );

        // set tokenizer to skip comments
        stok.slashStarComments( true );
        stok.slashSlashComments( true );

        return stok;
    }
    
    private Reader getReader()
        throws IOException
    {
        if ( Files.notExists( this.getPath() ) )
        {
            throw new IOException( this.getPath() + " does not exist!" );
        }

        if ( this.getEncoding() != null )
        {
            return new InputStreamReader( new FileInputStream( this.getPath().toFile() ), this.getEncoding() );
        }
        else
        {
            return new FileReader( this.getPath().toFile() );
        }
    }    
}
