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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Creates an html-based, cross referenced version of Java source code
 * for a project's test sources.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
@Mojo( name = "test-jxr" )
@Execute( phase = LifecyclePhase.GENERATE_TEST_SOURCES )
public class JxrTestReport
    extends AbstractJxrReport
{
    /**
     * Test directories of the project.
     */
    @Parameter( defaultValue = "${project.testCompileSourceRoots}", required = true, readonly = true )
    private List<String> sourceDirs;

    /**
     * Folder where the Xref files will be copied to.
     */
    @Parameter( defaultValue = "${project.reporting.outputDirectory}/xref-test" )
    private String destDir;

    /**
     * Folder where Test Javadoc is generated for this project.
     */
    @Parameter( defaultValue = "${project.reporting.outputDirectory}/testapidocs" )
    private File testJavadocDir;

    /**
     * @see org.apache.maven.plugin.jxr.AbstractJxrReport#getSourceRoots()
     */
    protected List<String> getSourceRoots()
    {
        List<String> l = new ArrayList<String>();

        if ( !"pom".equals( getProject().getPackaging().toLowerCase() ) )
        {
            l.addAll( sourceDirs );
        }

        if ( getProject().getExecutionProject() != null )
        {
            if ( !"pom".equals( getProject().getExecutionProject().getPackaging().toLowerCase() ) )
            {
                l.addAll( getProject().getExecutionProject().getTestCompileSourceRoots() );
            }
        }

        return l;
    }

    /**
     * @see org.apache.maven.plugin.jxr.AbstractJxrReport#getSourceRoots(org.apache.maven.project.MavenProject)
     */
    protected List<String> getSourceRoots( MavenProject project )
    {
        List<String> l = new ArrayList<String>();

        if ( project.getExecutionProject() != null )
        {
            if ( !"pom".equals( project.getExecutionProject().getPackaging().toLowerCase() ) )
            {
                l.addAll( project.getExecutionProject().getTestCompileSourceRoots() );
            }
        }

        return l;
    }

    /**
     * @see org.apache.maven.plugin.jxr.AbstractJxrReport#getDestinationDirectory()
     */
    protected String getDestinationDirectory()
    {
        return destDir;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.xref.test.description" );
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.xref.test.name" );
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "xref-test/index";
    }

    /**
     * @see org.apache.maven.plugin.jxr.AbstractJxrReport#getJavadocDir()
     */
    protected File getJavadocDir()
    {
        return testJavadocDir;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#setReportOutputDirectory(java.io.File)
     */
    public void setReportOutputDirectory( File reportOutputDirectory )
    {
        if ( ( reportOutputDirectory != null ) && ( !reportOutputDirectory.getAbsolutePath().endsWith( "xref-test" ) ) )
        {
            this.destDir = new File( reportOutputDirectory, "xref-test" ).getAbsolutePath();
        }
        else
        {
            this.destDir = reportOutputDirectory.getAbsolutePath();
        }
    }
}
