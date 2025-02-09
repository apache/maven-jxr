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

import java.nio.file.Paths;
import java.util.Collections;

import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JxrBeanTest {
    private JXR jxrBean;

    @BeforeEach
    void setUp() {
        FileManager fileManager = new FileManager();
        PackageManager packageManager = new PackageManager(fileManager);
        JavaCodeTransform codeTransform = new JavaCodeTransform(packageManager, fileManager);
        jxrBean = new JXR(packageManager, codeTransform);
        jxrBean.setDest(Paths.get("target"));
        jxrBean.setInputEncoding("ISO-8859-1");
        jxrBean.setOutputEncoding("ISO-8859-1");
        jxrBean.setJavadocLinkDir(Paths.get("."));
    }

    @Test
    void xref() throws Exception {
        jxrBean.xref(Collections.singletonList("src/test/java"), "templates/jdk4", "title", "title", "copyright");
    }
}
