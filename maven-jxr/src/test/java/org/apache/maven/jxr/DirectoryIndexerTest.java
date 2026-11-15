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
package org.apache.maven.jxr;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.jxr.DirectoryIndexer.ClassInfo;
import org.apache.maven.jxr.DirectoryIndexer.PackageInfo;
import org.apache.maven.jxr.DirectoryIndexer.ProjectInfo;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryIndexerTest {
    private PackageManager packageManager;

    private DirectoryIndexer directoryIndexer;

    @BeforeEach
    void setUp() {
        FileManager fileManager = new FileManager();
        packageManager = new PackageManager(fileManager);
        packageManager.process(Paths.get("src/test/resources/jxr68"));

        directoryIndexer = new DirectoryIndexer(packageManager, "");
    }

    /**
     * Parse the files in test/resources/jxr68 packages, ensure all are present in the allClasses Map,
     * in the correct order.
     */
    @Test
    void jxr68() {

        ProjectInfo packageInfo = directoryIndexer.getProjectInfo();
        final Map<String, PackageInfo> allPackages = packageInfo.getAllPackages();
        assertEquals(3, allPackages.size());
        assertTrue(allPackages.containsKey("(default package)"));
        assertTrue(allPackages.containsKey("pkga"));
        assertTrue(allPackages.containsKey("pkgb"));
        final Map<String, ClassInfo> allClasses = packageInfo.getAllClasses();
        assertEquals(6, allClasses.size());
        final Iterator<ClassInfo> iterator = allClasses.values().iterator();
        // #1: AClass
        assertEquals("AClass", iterator.next().getName());
        // #2: BClass
        assertEquals("BClass", iterator.next().getName());
        // #3: CClass
        assertEquals("CClass", iterator.next().getName());
        // #4: SomeClass in default package
        ClassInfo classInfo = iterator.next();
        assertEquals("SomeClass", classInfo.getName());
        assertEquals(".", classInfo.getDir());
        // #5: SomeClass in "pkga"
        classInfo = iterator.next();
        assertEquals("SomeClass", classInfo.getName());
        assertEquals("pkga", classInfo.getDir());
        // #6: SomeClass in "pkgb"
        classInfo = iterator.next();
        assertEquals("SomeClass", classInfo.getName());
        assertEquals("pkgb", classInfo.getDir());
    }

    /**
     * With frames, which is the default, the navigation is in pages of its own and there is nothing to search with.
     */
    @Test
    void frames(@TempDir Path root) throws Exception {
        createIndexer(root, "templates/jdk8").process();

        assertTrue(read(root, "index.html").contains("<frameset"));
        assertTrue(Files.exists(root.resolve("overview-frame.html")));
        assertTrue(Files.exists(root.resolve("allclasses-frame.html")));
        assertTrue(Files.exists(root.resolve("overview-summary.html")));
        assertTrue(Files.exists(root.resolve("pkga/package-frame.html")));
        assertTrue(Files.exists(root.resolve("pkga/package-summary.html")));

        assertFalse(Files.exists(root.resolve("allclasses-index.html")));
        assertFalse(Files.exists(root.resolve("search.js")));
        assertFalse(Files.exists(root.resolve("search-index.js")));
    }

    /**
     * Without frames every page has the navigation bar that the search script puts its box in.
     */
    @Test
    void noFrames(@TempDir Path root) throws Exception {
        DirectoryIndexer indexer = createIndexer(root, "templates/jdk13");
        indexer.setFrames(false);
        indexer.process();

        assertFalse(Files.exists(root.resolve("overview-frame.html")));
        assertFalse(Files.exists(root.resolve("allclasses-frame.html")));
        assertFalse(Files.exists(root.resolve("pkga/package-frame.html")));

        String index = read(root, "index.html");
        assertFalse(index.contains("<frameset"));
        assertTrue(index.contains("<script type=\"text/javascript\" src=\"search.js\"></script>"));
        assertTrue(index.contains("<a href=\"allclasses-index.html\">All Classes</a>"));
        assertTrue(index.contains("<span id=\"jxr_search\"></span>"));
        assertTrue(index.contains("<a href=\"pkga/package-summary.html\">pkga</a>"));

        // the page that the overview was with frames leads to its new place
        assertTrue(read(root, "overview-summary.html").contains("url=index.html"));

        String allClasses = read(root, "allclasses-index.html");
        assertTrue(allClasses.contains("<span id=\"jxr_search\"></span>"));
        assertTrue(
                allClasses.contains("<a href=\"pkga/SomeClass.html#SomeClass\" title=\"class in pkga\">SomeClass</a>"));
        assertTrue(allClasses.contains("<a href=\"pkga/package-summary.html\">pkga</a>"));
        assertFalse(allClasses.contains("target="));

        String packageSummary = read(root, "pkga/package-summary.html");
        assertTrue(packageSummary.contains("<script type=\"text/javascript\" src=\"../search.js\"></script>"));
        assertTrue(packageSummary.contains("<a href=\"../index.html\">Overview</a>"));
        assertTrue(packageSummary.contains("<a href=\"../allclasses-index.html\">All Classes</a>"));
        assertTrue(packageSummary.contains("<span id=\"jxr_search\"></span>"));
        assertFalse(packageSummary.contains("target="));

        // the script reads what the index is assigned to
        assertTrue(read(root, "search.js").contains("jxrSearchIndex"));
        assertEquals(
                "var jxrSearchIndex = {\"packages\":[\n"
                        + "{\"l\":\"(default package)\",\"u\":\"./package-summary.html\"},\n"
                        + "{\"l\":\"pkga\",\"u\":\"pkga/package-summary.html\"},\n"
                        + "{\"l\":\"pkgb\",\"u\":\"pkgb/package-summary.html\"}\n"
                        + "],\"types\":[\n"
                        + "{\"p\":\"pkgb\",\"l\":\"AClass\",\"u\":\"pkgb/AClass.html#AClass\"},\n"
                        + "{\"p\":\"pkga\",\"l\":\"BClass\",\"u\":\"pkga/BClass.html#BClass\"},\n"
                        + "{\"p\":\"(default package)\",\"l\":\"CClass\",\"u\":\"./CClass.html#CClass\"},\n"
                        + "{\"p\":\"(default package)\",\"l\":\"SomeClass\",\"u\":\"./SomeClass.html#SomeClass\"},\n"
                        + "{\"p\":\"pkga\",\"l\":\"SomeClass\",\"u\":\"pkga/SomeClass.html#SomeClass\"},\n"
                        + "{\"p\":\"pkgb\",\"l\":\"SomeClass\",\"u\":\"pkgb/SomeClass.html#SomeClass\"}\n"
                        + "]};\n",
                read(root, "search-index.js"));
    }

    /**
     * The names in the search index come from a lenient parser and get into pages of any encoding, so that all
     * but harmless ASCII has to be escaped.
     */
    @Test
    void searchIndexIsEscaped() {
        // an A with diaeresis, a double quote, a backslash, the characters that HTML is particular about, and a tab
        ClassInfo classInfo = new ClassInfo((char) 0xc4 + "\"\\<>&\t", "pkg");
        classInfo.setFilename("File");
        classInfo.setPackageName("pkg");
        ProjectInfo info = new ProjectInfo(
                Collections.<String, PackageInfo>emptyMap(), Collections.singletonMap("key", classInfo));

        String index = DirectoryIndexer.getSearchIndex(info);

        // one escape for each of the seven characters of the name
        String name = "\\u00c4" + "\\\"" + "\\\\" + "\\u003c" + "\\u003e" + "\\u0026" + "\\u0009";
        assertEquals(
                "var jxrSearchIndex = {\"packages\":[\n],\"types\":[\n"
                        + "{\"p\":\"pkg\",\"l\":\"" + name + "\",\"u\":\"pkg/File.html#" + name + "\"}\n"
                        + "]};\n",
                index);
    }

    private DirectoryIndexer createIndexer(Path root, String templateDir) {
        DirectoryIndexer indexer = new DirectoryIndexer(packageManager, root.toString());
        indexer.setOutputEncoding("UTF-8");
        indexer.setTemplateDir(templateDir);
        indexer.setWindowTitle("title");
        indexer.setDocTitle("title");
        indexer.setBottom("copyright");
        return indexer;
    }

    private String read(Path root, String file) throws Exception {
        return new String(Files.readAllBytes(root.resolve(file)), StandardCharsets.UTF_8);
    }
}
