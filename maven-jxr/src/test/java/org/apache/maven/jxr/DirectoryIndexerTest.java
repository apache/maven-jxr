package org.apache.maven.jxr;

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

import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class DirectoryIndexerTest {
    /**
     * Parse the files in test/resources/java packages, ensure all are present in the allClasses Map
     *
     * @throws Exception
     */
    @Test
    public void testJXR_68() throws Exception {
        FileManager fileManager = FileManager.getInstance();
        PackageManager packageManager = new PackageManager(new DummyLog(), fileManager);
        packageManager.process(Paths.get("src/test/resources/java"));
        DirectoryIndexer directoryIndexer = new DirectoryIndexer(packageManager, "");

        final Map<String, Object> packageInfo = directoryIndexer.getPackageInfo();
        final Map<String, String> allPackages = (Map<String, String>) packageInfo.get("allPackages");
        assertTrue(allPackages.containsKey("pkga"));
        assertTrue(allPackages.containsKey("pkgb"));
        final Map<String, String> allClasses = (Map<String, String>) packageInfo.get("allClasses");
        assertTrue(allClasses.containsKey("pkga.SomeClass"));
        assertTrue(allClasses.containsKey("pkgb.SomeClass"));
    }

}