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

import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JXR352Test {
    @Test
    void indexesModuleSourcesWithoutDefaultPackageAndLinksModuleJavadocs() throws Exception {
        Path sourceDirectory = Paths.get("src/test/resources/jxr352/src/main/java");
        Path outputDirectory = Paths.get("target/jxr-352");
        FileManager fileManager = new FileManager();
        PackageManager packageManager = new PackageManager(fileManager);
        JXR jxr = new JXR(packageManager, new JavaCodeTransform(packageManager, fileManager));
        jxr.setDest(outputDirectory);
        jxr.setOutputEncoding("UTF-8");
        jxr.setJavadocLinkDir(Paths.get("target/apidocs"));
        jxr.xref(Collections.singletonList(sourceDirectory.toString()), "templates/jdk4", "title", "title", "");

        assertFalse(Files.exists(outputDirectory.resolve("package-summary.html")));
        assertFalse(Files.exists(outputDirectory.resolve("module-info.html")));
        String moduleClass = new String(
                Files.readAllBytes(outputDirectory.resolve("org/apache/maven/jxr/testmodule/ModuleClass.html")),
                StandardCharsets.UTF_8);
        assertTrue(moduleClass.contains(
                "org.apache.maven.jxr.testmodule/org/apache/maven/jxr/testmodule/ModuleClass.html"));
    }
}
