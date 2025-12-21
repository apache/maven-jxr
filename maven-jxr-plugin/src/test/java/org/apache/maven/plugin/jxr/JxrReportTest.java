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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @author <a href="mailto:dennisl@apache.org">Dennis Lundberg</a>
 */
class JxrReportTest extends AbstractJxrTestCase {
    /**
     * Test the plugin with original configuration
     */
    @Test
    void defaultConfiguration() throws Exception {
        File resourcesDir = new File(getBasedir(), "src/test/resources/unit/default-configuration");

        File outputDir = new File(getBasedir(), "target/test/unit/default-configuration/target/site");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(new File(resourcesDir, "javadoc-files"), outputDir);

        generateReport(getGoal(), "default-configuration/default-configuration-plugin-config.xml");

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
    void jdk4Configuration() throws Exception {
        File resourcesDir = new File(getBasedir(), "src/test/resources/unit/default-configuration");

        File outputDir = new File(getBasedir(), "target/test/unit/default-configuration/target/site/4");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(new File(resourcesDir, "javadoc-files"), outputDir);

        generateReport(getGoal(), "default-configuration/default-configuration-plugin-config-4.xml");

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
    void jdk6Configuration() throws Exception {
        File resourcesDir = new File(getBasedir(), "src/test/resources/unit/default-configuration");

        File outputDir = new File(getBasedir(), "target/test/unit/default-configuration/target/site/6");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(new File(resourcesDir, "javadoc-files"), outputDir);

        generateReport(getGoal(), "default-configuration/default-configuration-plugin-config-6.xml");

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
    void jdk7Configuration() throws Exception {
        File resourcesDir = new File(getBasedir(), "src/test/resources/unit/default-configuration");

        File outputDir = new File(getBasedir(), "target/test/unit/default-configuration/target/site/7");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(new File(resourcesDir, "javadoc-files"), outputDir);

        generateReport(getGoal(), "default-configuration/default-configuration-plugin-config-7.xml");

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
    void jdk8Configuration() throws Exception {
        File resourcesDir = new File(getBasedir(), "src/test/resources/unit/default-configuration");

        File outputDir = new File(getBasedir(), "target/test/unit/default-configuration/target/site/8");
        File xrefDir = new File(outputDir, "xref");

        FileUtils.copyDirectory(new File(resourcesDir, "javadoc-files"), outputDir);

        generateReport(getGoal(), "default-configuration/default-configuration-plugin-config-8.xml");

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
    void noJavadocLink() throws Exception {
        generateReport(getGoal(), "nojavadoclink-configuration/nojavadoclink-configuration-plugin-config.xml");

        File xrefDir = new File(getBasedir(), "target/test/unit/nojavadoclink-configuration/target/site/xref");

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

    /**
     * Method for testing plugin when aggregate parameter is set to true
     */
    @Test
    void aggregate() throws Exception {
        generateReport(getGoal(), "aggregate-test/aggregate-test-plugin-config.xml");

        File xrefDir = new File(getBasedir(), "target/test/unit/aggregate-test/target/site/xref");

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

    /**
     * Method for testing plugin when the specified javadocDir does not exist
     */
    @Test
    void noJavadocDir() throws Exception {
        generateReport(getGoal(), "nojavadocdir-test/nojavadocdir-test-plugin-config.xml");

        File xrefDir = new File(getBasedir(), "target/test/unit/nojavadocdir-test/target/site/xref");

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
    void exclude() throws Exception {
        generateReport(getGoal(), "exclude-configuration/exclude-configuration-plugin-config.xml");

        Path xrefDir = new File(getBasedir(), "target/test/unit/exclude-configuration/target/site/xref").toPath();

        // check that the non-excluded xref files were generated
        assertTrue(Files.exists(xrefDir.resolve("exclude/configuration/App.html")));

        // check that the excluded xref files were not generated
        assertFalse(Files.exists(xrefDir.resolve("exclude/configuration/AppSample.html")));
    }

    /**
     * Test the plugin with an include configuration.
     */
    @Test
    void include() throws Exception {
        generateReport(getGoal(), "include-configuration/include-configuration-plugin-config.xml");

        Path xrefDir = new File(getBasedir(), "target/test/unit/include-configuration/target/site/xref").toPath();

        // check that the included xref files were generated
        assertTrue(Files.exists(xrefDir.resolve("include/configuration/App.html")));

        // check that the non-included xref files were not generated
        assertFalse(Files.exists(xrefDir.resolve("include/configuration/AppSample.html")));
    }

    @Test
    void exceptions() {
        try {
            generateReport(getGoal(), "default-configuration/exception-test-plugin-config.xml");

            fail("Must throw exception");
        } catch (Exception e) {
        }
    }

    /**
     * Test the jxr for a POM project.
     */
    @Test
    void pom() throws Exception {
        generateReport(getGoal(), "pom-test/pom-test-plugin-config.xml");

        assertFalse(new File(getBasedir(), "target/test/unit/pom-test").exists());
    }

    @Override
    protected String getGoal() {
        return "jxr";
    }
}
