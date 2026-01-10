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

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.execution.MavenSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.apache.maven.api.plugin.testing.MojoExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
@MojoTest(realRepositorySession = true)
class JxrTestReportTest {

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
     * Method to test when the source dir is the test source dir
     */
    @Test
    @Basedir("/unit/testsourcedir-test")
    @InjectMojo(goal = "test-jxr", pom = "testsourcedir-test-plugin-config.xml")
    void sourceDir(JxrTestReport mojo) throws Exception {
        mojo.execute();

        File xrefTestDir = getTestFile("target/site/xref-test");

        // check if the jxr docs were generated
        assertTrue(new File(xrefTestDir, "testsourcedir/test/AppSampleTest.html").exists());
        assertTrue(new File(xrefTestDir, "testsourcedir/test/AppTest.html").exists());
        assertTrue(new File(xrefTestDir, "testsourcedir/test/package-frame.html").exists());
        assertTrue(new File(xrefTestDir, "testsourcedir/test/package-summary.html").exists());
        assertTrue(new File(xrefTestDir, "allclasses-frame.html").exists());
        assertTrue(new File(xrefTestDir, "index.html").exists());
        assertTrue(new File(xrefTestDir, "overview-frame.html").exists());
        assertTrue(new File(xrefTestDir, "overview-summary.html").exists());
        assertTrue(new File(xrefTestDir, "stylesheet.css").exists());

        // check if there's a link to the javadoc files
        String str = readFile(xrefTestDir, "testsourcedir/test/AppSampleTest.html");
        assertFalse(str.toLowerCase().contains("/apidocs/testsourcedir/test/AppSample.html\"".toLowerCase()));

        str = readFile(xrefTestDir, "testsourcedir/test/AppTest.html");
        assertFalse(str.toLowerCase().contains("/apidocs/testsourcedir/test/App.html\"".toLowerCase()));
    }

    /**
     * Read the contents of the specified file object into a string.
     */
    private String readFile(File xrefTestDir, String fileName) throws IOException {
        return new String(Files.readAllBytes(xrefTestDir.toPath().resolve(fileName)));
    }
}
