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

import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Creates an html-based, cross referenced version of Java source code
 * for a project.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard</a>
 * @version $Id$
 * @goal jxr
 */
public class JxrReport
    extends AbstractJxrReport
{
    /**
     * Source directories of the project.
     *
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List sourceDirs;

    /**
     * Specifies the source path where the java files are located.
     * The paths are separated by '<code>;</code>'.
     *
     * @parameter expression="${sourcePath}"
     */
    private String sourcePath;

    /**
     * Folder where the Xref files will be copied to.
     *
     * @parameter expression="${project.reporting.outputDirectory}/xref"
     */
    private String destDir;

    /**
     * Folder where Javadoc is generated for this project.
     *
     * @parameter expression="${project.reporting.outputDirectory}/apidocs"
     */
    private File javadocDir;

    /**
     * @see org.apache.maven.plugin.jxr.AbstractJxrReport#getDestinationDirectory()
     */
    protected String getDestinationDirectory()
    {
        return destDir;
    }

    /**
     * @see org.apache.maven.plugin.jxr.AbstractJxrReport#getSourceRoots()
     */
    protected List getSourceRoots()
    {
        if ( sourcePath != null )
        {
            String[] sourcePathArray = sourcePath.split( ";" );
            if ( sourcePathArray.length > 0 )
            {
                return Arrays.asList( sourcePathArray );
            }
        }

        List l = new ArrayList();

        if ( !"pom".equals( getProject().getPackaging().toLowerCase() ) )
        {
            l.addAll( sourceDirs );
        }

        if ( getProject().getExecutionProject() != null )
        {
            if ( !"pom".equals( getProject().getExecutionProject().getPackaging().toLowerCase() ) )
            {
                l.addAll( getProject().getExecutionProject().getCompileSourceRoots() );
            }
        }

        return l;
    }

    /**
     * @see org.apache.maven.plugin.jxr.AbstractJxrReport#getSourceRoots(org.apache.maven.project.MavenProject)
     */
    protected List getSourceRoots( MavenProject project )
    {
        List l = new ArrayList();

        if ( !"pom".equals( project.getPackaging().toLowerCase() ) )
        {
            l.addAll( project.getCompileSourceRoots() );
        }

        if ( project.getExecutionProject() != null )
        {
            if ( !"pom".equals( project.getExecutionProject().getPackaging().toLowerCase() ) )
            {
                l.addAll( project.getExecutionProject().getCompileSourceRoots() );
            }
        }

        return l;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.xref.main.description" );
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.xref.main.name" );
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "xref/index";
    }

    /**
     * @see org.apache.maven.plugin.jxr.AbstractJxrReport#getJavadocDir()
     */
    protected File getJavadocDir()
    {
        return javadocDir;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#setReportOutputDirectory(java.io.File)
     */
    public void setReportOutputDirectory( File reportOutputDirectory )
    {
        if ( ( reportOutputDirectory != null ) && ( !reportOutputDirectory.getAbsolutePath().endsWith( "xref" ) ) )
        {
            this.destDir = new File( reportOutputDirectory, "xref" ).getAbsolutePath();
        }
        else
        {
            this.destDir = reportOutputDirectory.getAbsolutePath();
        }
    }
}
