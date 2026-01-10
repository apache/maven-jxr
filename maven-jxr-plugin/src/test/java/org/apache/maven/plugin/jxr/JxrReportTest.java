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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.apache.maven.api.plugin.testing.MojoExtension.getTestFile;
import static org.apache.maven.api.plugin.testing.MojoExtension.getTestPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @author <a href="mailto:dennisl@apache.org">Dennis Lundberg</a>
 */
@MojoTest(realRepositorySession = true)
class JxrReportTest {

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
     * Test the plugin with original configuration
     */
    @Test
    @Basedir("/unit/default-configuration")
    @InjectMojo(goal = "jxr", pom = "default-configuration-plugin-config.xml")
    void defaultConfiguration(JxrReport mojo) throws Exception {
        File outputDir = getTestFile("target/site");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(getTestFile("javadoc-files"), outputDir);

        mojo.execute();

        // check if xref files were generated
        assertTrue(new File(xrefDir, "allclasses-frame.html").exists());
        assertTrue(new File(xrefDir, "index.html").exists());
        assertTrue(new File(xrefDir, "overview-frame.html").exists());
        assertTrue(new File(xrefDir, "overview-summary.html").exists());
        assertTrue(new File(xrefDir, "stylesheet.css").exists());
        assertTrue(new File(xrefDir, "def/configuration/App.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/AppSample.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-frame.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-summary.html").exists());

        // check if there's a link to the javadoc files
        String str = readFile(xrefDir, "def/configuration/AppSample.html");
        assertTrue(str.toLowerCase().contains("/apidocs/def/configuration/appsample.html\""));

        str = readFile(xrefDir, "def/configuration/App.html");
        assertTrue(str.toLowerCase().contains("/apidocs/def/configuration/app.html\"".toLowerCase()));

        // check if encoding is UTF-8, the default value
        assertTrue(str.contains("text/html; charset=UTF-8"));
    }

    /**
     * Test the plugin with jdk-4 configuration
     */
    @Test
    @Basedir("/unit/default-configuration")
    @InjectMojo(goal = "jxr", pom = "default-configuration-plugin-config-4.xml")
    void jdk4Configuration(JxrReport mojo) throws Exception {

        File outputDir = getTestFile("target/site/4");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(getTestFile("javadoc-files"), outputDir);

        mojo.execute();

        // check if xref files were generated
        assertTrue(new File(xrefDir, "allclasses-frame.html").exists());
        assertTrue(new File(xrefDir, "index.html").exists());
        assertTrue(new File(xrefDir, "overview-frame.html").exists());
        assertTrue(new File(xrefDir, "overview-summary.html").exists());
        assertTrue(new File(xrefDir, "stylesheet.css").exists());
        assertTrue(new File(xrefDir, "def/configuration/App.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/AppSample.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-frame.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-summary.html").exists());

        // check if there's a link to the javadoc files
        String str = readFile(xrefDir, "def/configuration/AppSample.html");
        assertTrue(str.toLowerCase(Locale.ENGLISH).contains("/apidocs/def/configuration/appsample.html\""));

        str = readFile(xrefDir, "def/configuration/App.html");
        assertTrue(str.toLowerCase(Locale.ENGLISH).contains("/apidocs/def/configuration/app.html\""));

        // check if encoding is UTF-8, the default value
        assertTrue(str.contains("text/html; charset=UTF-8"));
    }

    /**
     * Test the plugin with jdk-6 configuration
     */
    @Test
    @Basedir("/unit/default-configuration")
    @InjectMojo(goal = "jxr", pom = "default-configuration-plugin-config-6.xml")
    void jdk6Configuration(JxrReport mojo) throws Exception {

        File outputDir = getTestFile("target/site/6");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(getTestFile("javadoc-files"), outputDir);

        mojo.execute();

        // check if xref files were generated
        assertTrue(new File(xrefDir, "allclasses-frame.html").exists());
        assertTrue(new File(xrefDir, "index.html").exists());
        assertTrue(new File(xrefDir, "overview-frame.html").exists());
        assertTrue(new File(xrefDir, "overview-summary.html").exists());
        assertTrue(new File(xrefDir, "stylesheet.css").exists());
        assertTrue(new File(xrefDir, "def/configuration/App.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/AppSample.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-frame.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-summary.html").exists());

        // check if there's a link to the javadoc files
        String str = readFile(xrefDir, "def/configuration/AppSample.html");
        assertTrue(str.toLowerCase(Locale.ENGLISH).contains("/apidocs/def/configuration/appsample.html\""));

        str = readFile(xrefDir, "def/configuration/App.html");
        assertTrue(str.toLowerCase(Locale.ENGLISH).contains("/apidocs/def/configuration/app.html\""));

        // check if encoding is UTF-8, the default value
        assertTrue(str.contains("text/html; charset=UTF-8"));
    }

    /**
     * Test the plugin with jdk-7 configuration
     */
    @Test
    @Basedir("/unit/default-configuration")
    @InjectMojo(goal = "jxr", pom = "default-configuration-plugin-config-7.xml")
    void jdk7Configuration(JxrReport mojo) throws Exception {

        File outputDir = getTestFile("target/site/7");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(getTestFile("javadoc-files"), outputDir);

        mojo.execute();

        // check if xref files were generated
        assertTrue(new File(xrefDir, "allclasses-frame.html").exists());
        assertTrue(new File(xrefDir, "index.html").exists());
        assertTrue(new File(xrefDir, "overview-frame.html").exists());
        assertTrue(new File(xrefDir, "overview-summary.html").exists());
        assertTrue(new File(xrefDir, "stylesheet.css").exists());
        assertTrue(new File(xrefDir, "resources/background.gif").exists());
        assertTrue(new File(xrefDir, "resources/tab.gif").exists());
        assertTrue(new File(xrefDir, "resources/titlebar.gif").exists());
        assertTrue(new File(xrefDir, "resources/titlebar_end.gif").exists());
        assertTrue(new File(xrefDir, "def/configuration/App.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/AppSample.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-frame.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-summary.html").exists());

        // check if there's a link to the javadoc files
        String str = readFile(xrefDir, "def/configuration/AppSample.html");
        assertTrue(str.toLowerCase(Locale.ENGLISH).contains("/apidocs/def/configuration/appsample.html\""));

        str = readFile(xrefDir, "def/configuration/App.html");
        assertTrue(str.toLowerCase(Locale.ENGLISH).contains("/apidocs/def/configuration/app.html\""));

        // check if encoding is UTF-8, the default value
        assertTrue(str.contains("text/html; charset=UTF-8"));
    }

    /**
     * Test the plugin with jdk-8 configuration
     */
    @Test
    @Basedir("/unit/default-configuration")
    @InjectMojo(goal = "jxr", pom = "default-configuration-plugin-config-8.xml")
    void jdk8Configuration(JxrReport mojo) throws Exception {

        File outputDir = getTestFile("target/site/8");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(getTestFile("javadoc-files"), outputDir);

        mojo.execute();

        // check if xref files were generated
        assertTrue(new File(xrefDir, "allclasses-frame.html").exists());
        assertTrue(new File(xrefDir, "index.html").exists());
        assertTrue(new File(xrefDir, "overview-frame.html").exists());
        assertTrue(new File(xrefDir, "overview-summary.html").exists());
        assertTrue(new File(xrefDir, "stylesheet.css").exists());
        assertTrue(new File(xrefDir, "def/configuration/App.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/AppSample.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-frame.html").exists());
        assertTrue(new File(xrefDir, "def/configuration/package-summary.html").exists());

        // check if there's a link to the javadoc files
        String str = readFile(xrefDir, "def/configuration/AppSample.html");
        assertTrue(str.toLowerCase().contains("/apidocs/def/configuration/appsample.html\""));

        str = readFile(xrefDir, "def/configuration/App.html");
        assertTrue(str.toLowerCase().contains("/apidocs/def/configuration/app.html\"".toLowerCase()));

        // check if encoding is UTF-8, the default value
        assertTrue(str.contains("text/html; charset=UTF-8"));
    }

    /**
     * Test when javadocLink is disabled in the configuration
     */
    @Test
    @Basedir("/unit/nojavadoclink-configuration")
    @InjectMojo(goal = "jxr", pom = "nojavadoclink-configuration-plugin-config.xml")
    void noJavadocLink(JxrReport mojo) throws Exception {
        mojo.execute();

        File xrefDir = getTestFile("target/site/xref");

        // check if xref files were generated
        assertTrue(new File(xrefDir, "allclasses-frame.html").exists());
        assertTrue(new File(xrefDir, "index.html").exists());
        assertTrue(new File(xrefDir, "overview-frame.html").exists());
        assertTrue(new File(xrefDir, "overview-summary.html").exists());
        assertTrue(new File(xrefDir, "stylesheet.css").exists());
        assertTrue(new File(xrefDir, "nojavadoclink/configuration/App.html").exists());
        assertTrue(new File(xrefDir, "nojavadoclink/configuration/AppSample.html").exists());
        assertTrue(new File(xrefDir, "nojavadoclink/configuration/package-frame.html").exists());
        assertTrue(new File(xrefDir, "nojavadoclink/configuration/package-summary.html").exists());
        assertTrue(new File(xrefDir, "nojavadoclink/configuration/sample/package-summary.html").exists());
        assertTrue(new File(xrefDir, "nojavadoclink/configuration/sample/package-frame.html").exists());
        assertTrue(new File(xrefDir, "nojavadoclink/configuration/sample/Sample.html").exists());

        // check if there's a link to the javadoc files
        String str = readFile(xrefDir, "nojavadoclink/configuration/AppSample.html");
        assertEquals(
                -1, str.toLowerCase(Locale.ENGLISH).indexOf("/apidocs/nojavadoclink/configuration/appsample.html\""));

        str = readFile(xrefDir, "nojavadoclink/configuration/App.html");
        assertEquals(-1, str.toLowerCase(Locale.ENGLISH).indexOf("/apidocs/nojavadoclink/configuration/app.html\""));

        str = readFile(xrefDir, "nojavadoclink/configuration/sample/Sample.html");
        assertEquals(-1, str.toLowerCase().indexOf("/apidocs/nojavadoclink/configuration/sample/sample.html\""));

        // check if encoding is ISO-8859-1, like specified in the plugin configuration
        assertTrue(str.contains("text/html; charset=ISO-8859-1"));
    }

    @Nested
    class AggregateTest {

        @Inject
        private MavenProject project;

        @Inject
        private MavenSession session;

        @Inject
        private ArtifactHandlerManager artifactHandlerManager;

        @BeforeEach
        void setup() {
            project.setExecutionRoot(true);
            project.setArtifact(new DefaultArtifact(
                    project.getGroupId(),
                    project.getArtifactId(),
                    project.getVersion(),
                    "compile",
                    "pom",
                    null,
                    artifactHandlerManager.getArtifactHandler("pom")));

            MavenProject stub1 = new MavenProject();
            stub1.setArtifact(new DefaultArtifact(
                    project.getGroupId(),
                    "sub1",
                    project.getVersion(),
                    "compile",
                    "jr",
                    null,
                    artifactHandlerManager.getArtifactHandler("jar")));
            stub1.addCompileSourceRoot(getTestPath("submodule1"));

            MavenProject stub2 = new MavenProject();
            stub2.setArtifact(new DefaultArtifact(
                    project.getGroupId(),
                    "sub2",
                    project.getVersion(),
                    "compile",
                    "jar",
                    null,
                    artifactHandlerManager.getArtifactHandler("jar")));
            stub2.addCompileSourceRoot(getTestPath("submodule2"));

            when(session.getProjects()).thenReturn(Arrays.asList(project, stub1, stub2));
        }

        /**
         * Method for testing plugin when aggregate parameter is set to true
         */
        @Test
        @Basedir("/unit/aggregate-test")
        @InjectMojo(goal = "aggregate", pom = "aggregate-test-plugin-config.xml")
        void aggregate(AggregatorJxrReport mojo) throws Exception {

            mojo.execute();

            File xrefDir = getTestFile("target/site/xref");

            // check if xref files were generated for submodule1
            assertTrue(new File(xrefDir, "aggregate/test/submodule1/package-frame.html").exists());
            assertTrue(new File(xrefDir, "aggregate/test/submodule1/package-summary.html").exists());
            assertTrue(new File(xrefDir, "aggregate/test/submodule1/Submodule1App.html").exists());
            assertTrue(new File(xrefDir, "aggregate/test/submodule1/Submodule1AppSample.html").exists());

            // check if xref files were generated for submodule2
            assertTrue(new File(xrefDir, "aggregate/test/submodule2/package-frame.html").exists());
            assertTrue(new File(xrefDir, "aggregate/test/submodule2/package-summary.html").exists());
            assertTrue(new File(xrefDir, "aggregate/test/submodule2/Submodule2App.html").exists());
            assertTrue(new File(xrefDir, "aggregate/test/submodule2/Submodule2AppSample.html").exists());
        }
    }
    /**
     * Method for testing plugin when the specified javadocDir does not exist
     */
    @Test
    @Basedir("/unit/nojavadocdir-test")
    @InjectMojo(goal = "jxr", pom = "nojavadocdir-test-plugin-config.xml")
    void noJavadocDir(JxrReport mojo) throws Exception {
        mojo.execute();

        File xrefDir = getTestFile("target/site/xref");

        // check if there's a link to the javadoc files
        String str = readFile(xrefDir, "nojavadocdir/test/AppSample.html");
        assertTrue(str.toLowerCase(Locale.ENGLISH).contains("/apidocs/nojavadocdir/test/appsample.html"));

        str = readFile(xrefDir, "nojavadocdir/test/App.html");
        assertTrue(str.toLowerCase(Locale.ENGLISH).contains("/apidocs/nojavadocdir/test/app.html"));
    }

    /**
     * Test the plugin with an exclude configuration.
     */
    @Test
    @Basedir("/unit/exclude-configuration")
    @InjectMojo(goal = "jxr", pom = "exclude-configuration-plugin-config.xml")
    void exclude(JxrReport mojo) throws Exception {
        mojo.execute();

        Path xrefDir = getTestFile("target/site/xref").toPath();

        // check that the non-excluded xref files were generated
        assertTrue(Files.exists(xrefDir.resolve("exclude/configuration/App.html")));

        // check that the excluded xref files were not generated
        assertFalse(Files.exists(xrefDir.resolve("exclude/configuration/AppSample.html")));
    }

    /**
     * Test the plugin with an include configuration.
     */
    @Test
    @Basedir("/unit/include-configuration")
    @InjectMojo(goal = "jxr", pom = "include-configuration-plugin-config.xml")
    void include(JxrReport mojo) throws Exception {
        mojo.execute();

        Path xrefDir = getTestFile("target/site/xref").toPath();

        // check that the included xref files were generated
        assertTrue(Files.exists(xrefDir.resolve("include/configuration/App.html")));

        // check that the non-included xref files were not generated
        assertFalse(Files.exists(xrefDir.resolve("include/configuration/AppSample.html")));
    }

    @Test
    @Basedir("/unit/default-configuration")
    @InjectMojo(goal = "jxr", pom = "exception-test-plugin-config.xml")
    void exceptions(JxrReport mojo) {

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, mojo::execute);

        boolean hasExpectedMessage = false;
        Throwable throwable = exception;

        while (throwable != null) {
            if (throwable.getMessage().contains("Unable to find resource 'temp/index.vm'")) {
                hasExpectedMessage = true;
                break;
            }
            throwable = throwable.getCause();
        }

        if (!hasExpectedMessage) {
            fail(
                    "Expected exception message [Unable to find resource 'temp/index.vm'] not found in the exception chain.",
                    exception);
        }
    }

    /**
     * Read the contents of the specified file object into a string.
     */
    private String readFile(File xrefTestDir, String fileName) throws IOException {
        return new String(Files.readAllBytes(xrefTestDir.toPath().resolve(fileName)));
    }
}
