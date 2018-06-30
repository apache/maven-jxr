package org.apache.maven.plugin.jxr;

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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @author <a href="mailto:dennisl@apache.org">Dennis Lundberg</a>
 */
public class JxrReportTest
    extends AbstractMojoTestCase
{
    /**
     * @see org.apache.maven.plugin.testing.AbstractMojoTestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }

    /**
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    protected void tearDown()
        throws Exception
    {
        // nop
    }

    /**
     * Test the plugin with default configuration
     *
     * @throws Exception
     */
    public void testDefaultConfiguration()
        throws Exception
    {
    	File resourcesDir = new File( getBasedir(), "src/test/resources/unit/default-configuration" );

    	File outputDir = new File( getBasedir(), "target/test/unit/default-configuration/target/site" );
    	File xrefDir = new File( outputDir, "xref" );

    	copyFilesFromDirectory( new File( resourcesDir, "javadoc-files" ), outputDir );

        File testPom = new File( resourcesDir, "default-configuration-plugin-config.xml" );
        JxrReport mojo = (JxrReport) lookupMojo( "jxr", testPom );
        mojo.execute();

        //check if xref files were generated
        assertTrue( new File( xrefDir, "allclasses-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "index.html" ).exists() );
        assertTrue( new File( xrefDir, "overview-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "overview-summary.html" ).exists() );
        assertTrue( new File( xrefDir, "stylesheet.css" ).exists() );
        assertTrue( new File( xrefDir, "def/configuration/App.html" ).exists() );
        assertTrue( new File( xrefDir, "def/configuration/AppSample.html" ).exists() );
        assertTrue( new File( xrefDir, "def/configuration/package-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "def/configuration/package-summary.html" ).exists() );

        //check if there's a link to the javadoc files
        String str = readFile( new File( xrefDir, "def/configuration/AppSample.html" ) );
        assertTrue( str.toLowerCase().indexOf( "/apidocs/def/configuration/AppSample.html\"".toLowerCase() ) != -1 );

        str = readFile( new File( xrefDir, "def/configuration/App.html" ) );
        assertTrue( str.toLowerCase().indexOf( "/apidocs/def/configuration/app.html\"".toLowerCase() ) != -1 );

        // check if encoding is UTF-8, the default value
        assertTrue( str.indexOf( "text/html; charset=UTF-8" ) != -1 );
    }

    /**
     * Test when javadocLink is disabled in the configuration
     *
     * @throws Exception
     */
    public void testNoJavadocLink()
        throws Exception
    {
        File testPom = new File( getBasedir(),
                                 "src/test/resources/unit/nojavadoclink-configuration/nojavadoclink-configuration-plugin-config.xml" );
        JxrReport mojo = (JxrReport) lookupMojo( "jxr", testPom );
        mojo.execute();

    	File xrefDir = new File( getBasedir(), "target/test/unit/nojavadoclink-configuration/target/site/xref" );

    	//check if xref files were generated
        assertTrue( new File( xrefDir, "allclasses-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "index.html" ).exists() );
        assertTrue( new File( xrefDir, "overview-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "overview-summary.html" ).exists() );
        assertTrue( new File( xrefDir, "stylesheet.css" ).exists() );
        assertTrue( new File( xrefDir, "nojavadoclink/configuration/App.html" ).exists() );
        assertTrue( new File( xrefDir, "nojavadoclink/configuration/AppSample.html" ).exists() );
        assertTrue( new File( xrefDir, "nojavadoclink/configuration/package-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "nojavadoclink/configuration/package-summary.html" ).exists() );
        assertTrue( new File( xrefDir, "nojavadoclink/configuration/sample/package-summary.html" ).exists() );
        assertTrue( new File( xrefDir, "nojavadoclink/configuration/sample/package-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "nojavadoclink/configuration/sample/Sample.html" ).exists() );

        //check if there's a link to the javadoc files
        String str = readFile( new File( xrefDir, "nojavadoclink/configuration/AppSample.html" ) );
        assertTrue(
            str.toLowerCase().indexOf( "/apidocs/nojavadoclink/configuration/AppSample.html\"".toLowerCase() ) == -1 );

        str = readFile( new File( xrefDir, "nojavadoclink/configuration/App.html" ) );
        assertTrue(
            str.toLowerCase().indexOf( "/apidocs/nojavadoclink/configuration/app.html\"".toLowerCase() ) == -1 );

        str = readFile( new File( xrefDir, "nojavadoclink/configuration/sample/Sample.html" ) );
        assertTrue( str.toLowerCase().indexOf(
            "/apidocs/nojavadoclink/configuration/sample/sample.html\"".toLowerCase() ) == -1 );

        // check if encoding is ISO-8859-1, like specified in the plugin configuration
        assertTrue( str.indexOf( "text/html; charset=ISO-8859-1" ) != -1 );
    }

    /**
     * Method for testing plugin when aggregate parameter is set to true
     *
     * @throws Exception
     */
    public void testAggregate()
        throws Exception
    {
        File testPom =
            new File( getBasedir(), "src/test/resources/unit/aggregate-test/aggregate-test-plugin-config.xml" );
        JxrReport mojo = (JxrReport) lookupMojo( "jxr", testPom );
        mojo.execute();

    	File xrefDir = new File( getBasedir(), "target/test/unit/aggregate-test/target/site/xref" );

        //check if xref files were generated for submodule1
        assertTrue( new File( xrefDir, "aggregate/test/submodule1/package-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "aggregate/test/submodule1/package-summary.html" ).exists() );
        assertTrue( new File( xrefDir, "aggregate/test/submodule1/Submodule1App.html" ).exists() );
        assertTrue( new File( xrefDir, "aggregate/test/submodule1/Submodule1AppSample.html" ).exists() );

        //check if xref files were generated for submodule2
        assertTrue( new File( xrefDir, "aggregate/test/submodule2/package-frame.html" ).exists() );
        assertTrue( new File( xrefDir, "aggregate/test/submodule2/package-summary.html" ).exists() );
        assertTrue( new File( xrefDir, "aggregate/test/submodule2/Submodule2App.html" ).exists() );
        assertTrue( new File( xrefDir, "aggregate/test/submodule2/Submodule2AppSample.html" ).exists() );

    }

    /**
     * Method for testing plugin when the specified javadocDir does not exist
     *
     * @throws Exception
     */
    public void testNoJavadocDir()
        throws Exception
    {
        File testPom =
            new File( getBasedir(), "src/test/resources/unit/nojavadocdir-test/nojavadocdir-test-plugin-config.xml" );
        JxrReport mojo = (JxrReport) lookupMojo( "jxr", testPom );
        mojo.execute();

    	File xrefDir = new File( getBasedir(), "target/test/unit/nojavadocdir-test/target/site/xref" );

    	//check if there's a link to the javadoc files
        String str = readFile( new File( xrefDir, "nojavadocdir/test/AppSample.html" ) );
        assertTrue( str.toLowerCase().indexOf( "/apidocs/nojavadocdir/test/AppSample.html".toLowerCase() ) != -1 );

        str = readFile( new File( xrefDir, "nojavadocdir/test/App.html" ) );
        assertTrue( str.toLowerCase().indexOf( "/apidocs/nojavadocdir/test/app.html".toLowerCase() ) != -1 );

    }

    /**
     * Test the plugin with an exclude configuration.
     *
     * @throws Exception
     */
    public void testExclude()
        throws Exception
    {
        File testPom = new File( getBasedir(),
                                 "src/test/resources/unit/exclude-configuration/exclude-configuration-plugin-config.xml" );
        JxrReport mojo = (JxrReport) lookupMojo( "jxr", testPom );
        mojo.execute();

    	File xrefDir = new File( getBasedir(), "target/test/unit/exclude-configuration/target/site/xref" );

    	// check that the non-excluded xref files were generated
        File generatedFile = new File( xrefDir, "exclude/configuration/App.html" );
        assertTrue( FileUtils.fileExists( generatedFile.getAbsolutePath() ) );

        // check that the excluded xref files were not generated
        generatedFile = new File( xrefDir, "exclude/configuration/AppSample.html" );
        assertFalse( FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
    }

    /**
     * Test the plugin with an include configuration.
     *
     * @throws Exception
     */
    public void testInclude()
        throws Exception
    {
        File testPom = new File( getBasedir(),
                                 "src/test/resources/unit/include-configuration/include-configuration-plugin-config.xml" );
        JxrReport mojo = (JxrReport) lookupMojo( "jxr", testPom );
        mojo.execute();

    	File xrefDir = new File( getBasedir(), "target/test/unit/include-configuration/target/site/xref" );

        // check that the included xref files were generated
        File generatedFile = new File( xrefDir, "include/configuration/App.html" );
        assertTrue( FileUtils.fileExists( generatedFile.getAbsolutePath() ) );

        // check that the non-included xref files were not generated
        generatedFile = new File( xrefDir, "include/configuration/AppSample.html" );
        assertFalse( FileUtils.fileExists( generatedFile.getAbsolutePath() ) );
    }

    public void testExceptions()
        throws Exception
    {
        try
        {
            File testPom =
                new File( getBasedir(), "src/test/resources/unit/default-configuration/exception-test-plugin-config.xml" );
            JxrReport mojo = (JxrReport) lookupMojo( "jxr", testPom );
            mojo.execute();

            fail( "Must throw exception");
        }
        catch ( Exception e )
        {
            assertTrue( true );
        }
    }

    /**
     * Test the jxr for a POM project.
     *
     * @throws Exception
     */
    public void testPom()
        throws Exception
    {
        File testPom = new File( getBasedir(),
                                 "src/test/resources/unit/pom-test/pom-test-plugin-config.xml" );
        JxrReport mojo = (JxrReport) lookupMojo( "jxr", testPom );
        mojo.execute();

        assertFalse( new File( getBasedir(), "target/test/unit/pom-test" ).exists() );
    }

    /**
     * Copy files from the specified source directory to the specified destination directory
     *
     * @param srcDir
     * @param destDir
     * @throws IOException
     */
    private static void copyFilesFromDirectory( File srcDir, File destDir )
        throws IOException
    {
        FileUtils.copyDirectoryStructure( srcDir, destDir );
    }

    /**
     * Read the contents of the specified file object into a string
     *
     * @param file the file to be read
     * @return a String object that contains the contents of the file
     * @throws IOException
     */
    private static String readFile( File file )
        throws IOException
    {
        String str = "", strTmp = "";
        BufferedReader in = new BufferedReader( new FileReader( file ) );

        while ( ( strTmp = in.readLine() ) != null )
        {
            str = str + ' ' + strTmp;
        }
        in.close();

        return str;
    }
}
