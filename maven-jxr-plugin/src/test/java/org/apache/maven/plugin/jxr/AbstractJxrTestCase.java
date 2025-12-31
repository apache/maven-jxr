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
package org.apache.maven.plugin.jxr;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.maven.execution.MavenSession;
import org.junit.jupiter.api.BeforeEach;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;

/**
 * Abstract class to test reports generation.
 */
abstract class AbstractJxrTestCase {

    @Inject
    private MavenSession session;

    @BeforeEach
    void setUp() {
        // Set a local repository path to common location for all tests
        session.getRequest()
                .setLocalRepositoryPath(Paths.get(getBasedir())
                        .getParent()
                        .resolve("local-repo-unit")
                        .toString());
    }

    /**
     * Read the contents of the specified file object into a string.
     */
    protected String readFile(File xrefTestDir, String fileName) throws IOException {
        return new String(Files.readAllBytes(xrefTestDir.toPath().resolve(fileName)));
    }
}
