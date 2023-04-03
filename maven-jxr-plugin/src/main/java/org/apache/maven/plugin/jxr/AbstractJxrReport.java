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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.jxr.JXR;
import org.apache.maven.jxr.JavaCodeTransform;
import org.apache.maven.jxr.JxrException;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.languages.java.version.JavaVersion;
import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for the JXR reports.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public abstract class AbstractJxrReport
    extends AbstractMavenReport
{

    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    /**
     * Title of window of the Xref HTML files.
     */
    @Parameter( defaultValue = "${project.name} ${project.version} Reference" )
    private String windowTitle;

    /**
     * Title of main page of the Xref HTML files.
     */
    @Parameter( defaultValue = "${project.name} ${project.version} Reference" )
    private String docTitle;

    // CHECKSTYLE_OFF: LineLength
    /**
     * String used at the bottom of the Xref HTML files.
     */
    @Parameter( property = "bottom", defaultValue = "Copyright &#169; {inceptionYear}&#x2013;{currentYear} {organizationName}. All rights reserved." )
    private String bottom;

    // CHECKSTYLE_ON: LineLength

    /**
     * Directory where Velocity templates can be found to generate overviews, frames and summaries. Should not be used.
     * If used, should be an absolute path, like {@code "${basedir}/myTemplates"}.
     */
    @Parameter
    private String templateDir;

    /**
     * Style sheet used for the Xref HTML files. Should not be used. If used, should be an absolute path, like
     * {@code "${basedir}/myStyles.css"}.
     */
    @Parameter
    private String stylesheet;

    /**
     * A list of exclude patterns to use. By default no files are excluded.
     *
     * @since 2.1
     */
    @Parameter
    private ArrayList<String> excludes;

    /**
     * A list of include patterns to use. By default all .java files are included.
     *
     * @since 2.1
     */
    @Parameter
    private ArrayList<String> includes;

    /**
     * The projects in the reactor for aggregation report.
     */
    @Parameter( defaultValue = "${reactorProjects}", readonly = true )
    protected List<MavenProject> reactorProjects;

    /**
     * Whether to skip this execution.
     *
     * @since 2.3
     */
    @Parameter( property = "maven.jxr.skip", defaultValue = "false" )
    protected boolean skip;

    /**
     * Link the Javadoc from the Source XRef. Defaults to true and will link automatically if javadoc plugin is being
     * used.
     */
    @Parameter( defaultValue = "true" )
    private boolean linkJavadoc;

    /**
     * Version of the Javadoc templates to use.
     * The value should reflect `java.specification.version`, "1.4", "1.8", "9", "10",
     * by default this system property is used.
     */
    @Parameter( property = "javadocVersion" )
    private String javadocVersion;

    /**
     * Version of the Javadoc templates to use.
     */
    private JavaVersion javadocTemplatesVersion;

    /**
     * Compiles the list of directories which contain source files that will be included in the JXR report generation.
     *
     * @param sourceDirs the List of the source directories
     * @return a List of the directories that will be included in the JXR report generation
     */
    protected List<String> pruneSourceDirs( List<String> sourceDirs )
    {
        List<String> pruned = new ArrayList<>( sourceDirs.size() );
        for ( String dir : sourceDirs )
        {
            if ( !pruned.contains( dir ) && hasSources( new File( dir ) ) )
            {
                pruned.add( dir );
            }
        }
        return pruned;
    }

    /**
     * Initialize some attributes required during the report generation
     */
    protected void init()
    {
        // wanna know if Javadoc is being generated
        // TODO: what if it is not part of the site though, and just on the command line?
        if ( project.getModel().getReporting() != null )
        {
            for ( ReportPlugin reportPlugin : Collections.unmodifiableList(
                    project.getModel().getReporting().getPlugins() ) )
            {
                if ( "maven-javadoc-plugin".equals( reportPlugin.getArtifactId() ) )
                {
                    break;
                }
            }
        }
    }

    /**
     * Checks whether the given directory contains Java files.
     *
     * @param dir the source directory
     * @return true if the folder or one of its subfolders contains at least 1 Java file
     */
    private boolean hasSources( File dir )
    {
        if ( dir.exists() && dir.isDirectory() )
        {
            for ( File currentFile : dir.listFiles() )
            {
                if ( currentFile.isFile() )
                {
                    if ( currentFile.getName().endsWith( ".java" ) )
                    {
                        return true;
                    }
                }
                else
                {
                    if ( Character.isJavaIdentifierStart( currentFile.getName().charAt( 0 ) ) // avoid .svn directory
                        && hasSources( currentFile ) )
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates the Xref for the Java files found in the given source directory and puts them in the given destination
     * directory.
     *
     * @param locale The user locale to use for the Xref generation
     * @param destinationDirectory The output folder
     * @param sourceDirs The source directories
     * @throws java.io.IOException
     * @throws org.apache.maven.jxr.JxrException
     */
    private void createXref( Locale locale, String destinationDirectory, List<String> sourceDirs )
        throws IOException, JxrException
    {
        FileManager fileManager = new FileManager();
        PackageManager packageManager = new PackageManager( fileManager );
        JavaCodeTransform codeTransform = new JavaCodeTransform( packageManager, fileManager );

        JXR jxr = new JXR( packageManager, codeTransform );
        jxr.setDest( Paths.get( destinationDirectory ) );
        jxr.setInputEncoding( getInputEncoding() );
        jxr.setLocale( locale );
        jxr.setOutputEncoding( getOutputEncoding() );
        jxr.setRevision( "HEAD" );
        jxr.setJavadocLinkDir( getJavadocLocation() );
        // Set include/exclude patterns on the jxr instance
        if ( excludes != null && !excludes.isEmpty() )
        {
            jxr.setExcludes( excludes.toArray( new String[0] ) );
        }
        if ( includes != null && !includes.isEmpty() )
        {
            jxr.setIncludes( includes.toArray( new String[0] ) );
        }

        // avoid winding up using Velocity in two class loaders.
        ClassLoader savedTccl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            jxr.xref( sourceDirs, getTemplateDir(), windowTitle, docTitle, getBottomText() );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( savedTccl );
        }

        // and finally copy the stylesheet
        copyRequiredResources( destinationDirectory );
    }

    /**
     * Returns the bottom text to be displayed at the lower part of the generated JXR reports.
     */
    private String getBottomText()
    {
        int currentYear = Calendar.getInstance().get( Calendar.YEAR );
        String year = String.valueOf( currentYear );

        String inceptionYear = project.getInceptionYear();

        String theBottom = StringUtils.replace( this.bottom, "{currentYear}", year );

        if ( inceptionYear != null )
        {
            if ( inceptionYear.equals( year ) )
            {
                theBottom = StringUtils.replace( theBottom, "{inceptionYear}&#x2013;", "" );
            }
            else
            {
                theBottom = StringUtils.replace( theBottom, "{inceptionYear}", inceptionYear );
            }
        }
        else
        {
            theBottom = StringUtils.replace( theBottom, "{inceptionYear}&#x2013;", "" );
        }

        if ( project.getOrganization() == null )
        {
            theBottom = StringUtils.replace( theBottom, " {organizationName}", "" );
        }
        else
        {
            if ( StringUtils.isNotEmpty( project.getOrganization().getName() ) )
            {
                if ( StringUtils.isNotEmpty( project.getOrganization().getUrl() ) )
                {
                    // CHECKSTYLE_OFF: LineLength
                    theBottom =
                        StringUtils.replace( theBottom, "{organizationName}", "<a href=\""
                            + project.getOrganization().getUrl() + "\">" + project.getOrganization().getName() + "</a>" );
                    // CHECKSTYLE_ON: LineLength
                }
                else
                {
                    theBottom =
                        StringUtils.replace( theBottom, "{organizationName}", project.getOrganization().getName() );
                }
            }
            else
            {
                theBottom = StringUtils.replace( theBottom, " {organizationName}", "" );
            }
        }

        return theBottom;
    }

    /**
     * Copy some required resources (like the stylesheet) to the given directory
     *
     * @param dir the directory to copy the resources to
     */
    private void copyRequiredResources( String dir )
    {
        if ( StringUtils.isNotEmpty( stylesheet ) )
        {
            File stylesheetFile = new File( stylesheet );
            File destStylesheetFile = new File( dir, "stylesheet.css" );

            try
            {
                if ( stylesheetFile.isAbsolute() )
                {
                    FileUtils.copyFile( stylesheetFile, destStylesheetFile );
                }
                else
                {
                    URL stylesheetUrl = this.getClass().getClassLoader().getResource( stylesheet );
                    FileUtils.copyURLToFile( stylesheetUrl, destStylesheetFile );
                }
            }
            catch ( IOException e )
            {
                getLog().warn( "An error occured while copying the stylesheet to the target directory", e );
            }
        }
        else
        {
            if ( javadocTemplatesVersion.isAtLeast( "1.8" ) )
            {
                copyResources( dir, "jdk8/", "stylesheet.css" );
            }
            else if ( javadocTemplatesVersion.isAtLeast( "1.7" ) )
            {
                String[] jdk7Resources =
                {
                    "stylesheet.css",
                    "resources/background.gif",
                    "resources/tab.gif",
                    "resources/titlebar.gif",
                    "resources/titlebar_end.gif"
                };
                copyResources( dir, "jdk7/", jdk7Resources );
            }
            else if ( javadocTemplatesVersion.isAtLeast( "1.6" ) )
            {
                copyResources( dir, "jdk6/", "stylesheet.css" );
            }
            else if ( javadocTemplatesVersion.isAtLeast( "1.4" ) )
            {
                copyResources( dir, "jdk4/", "stylesheet.css" );
            }
            else
            {
                // Fallback to the original stylesheet
                copyResources( dir, "", "stylesheet.css" );
            }
        }
    }

    /**
     * Copy styles and related resources to the given directory
     *
     * @param dir the directory to copy the resources to
     * @param sourceFolder resources subfolder to copy from
     * @param files names of files to copy
     */
    private void copyResources( String dir, String sourceFolder, String... files )
    {
        try
        {
            for ( String file : files )
            {
                URL resourceUrl = this.getClass().getClassLoader().getResource( sourceFolder + file );
                File destResourceFile = new File( dir, file );
                FileUtils.copyURLToFile( resourceUrl, destResourceFile );
            }
        }
        catch ( IOException e )
        {
            getLog().warn( "An error occured while copying the resource to the target directory", e );
        }
    }

    @Override
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Returns the Maven session.
     * @return Maven session
     */
    protected MavenSession getSession()
    {
        return session;
    }

    /**
     * Returns the correct resource bundle according to the locale.
     *
     * @param locale the locale of the user
     * @return the bundle corresponding to the locale
     */
    protected ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "jxr-report", locale, this.getClass().getClassLoader() );
    }

    /**
     * Checks whether the report can be generated.
     *
     * @param sourceDirs list of source directories
     * @return true if the report could be generated
     */
    protected boolean canGenerateReport( List<String> sourceDirs )
    {
        boolean canGenerate = !sourceDirs.isEmpty();

        if ( isAggregate() && !project.isExecutionRoot() )
        {
            canGenerate = false;
        }
        return canGenerate;
    }

    @Override
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        if ( skip )
        {
            getLog().info( "Skipping JXR." );
            return;
        }
        List<String> sourceDirs = constructSourceDirs();
        if ( canGenerateReport( sourceDirs ) )
        {
            // init some attributes -- TODO (javadoc)
            init();

            // determine version of templates to use
            setJavadocTemplatesVersion();

            try
            {
                createXref( locale, getDestinationDirectory(), sourceDirs );
            }
            catch ( JxrException | IOException e )
            {
                throw new MavenReportException( "Error while generating the HTML source code of the project.", e );
            }
        }
    }


    /**
     * Determine the templateDir to use, given javadocTemplatesVersion
     *
     * @return
     */
    private String getTemplateDir()
    {
        // Check if overridden
        if ( StringUtils.isEmpty( templateDir ) )
        {
            if ( javadocTemplatesVersion.isAtLeast( "1.8" ) )
            {
                return "templates/jdk8";
            }
            else if ( javadocTemplatesVersion.isAtLeast( "1.7" ) )
            {
                return "templates/jdk7";
            }
            else if ( javadocTemplatesVersion.isAtLeast( "1.4" ) )
            {
                return "templates/jdk4";
            }
            else
            {
                getLog().warn( "Unsupported javadocVersion: " + javadocTemplatesVersion + ". Fallback to original" );
                return "templates";
            }
        }
        // use value specified by user
        return templateDir;
    }

    /**
     * Sets a new value for {@code javadocTemplatesVersion}.
     */
    private void setJavadocTemplatesVersion()
    {
        JavaVersion javaVersion = JavaVersion.JAVA_SPECIFICATION_VERSION;

        if ( StringUtils.isNotEmpty( javadocVersion ) )
        {
            javadocTemplatesVersion = JavaVersion.parse( javadocVersion );
        }
        else
        {
            javadocTemplatesVersion = javaVersion;
        }
    }

    /**
     * Gets the list of the source directories to be included in the JXR report generation
     *
     * @return a List of the source directories whose contents will be included in the JXR report generation
     */
    protected List<String> constructSourceDirs()
    {
        List<String> sourceDirs = new ArrayList<>( getSourceRoots() );
        if ( isAggregate() )
        {
            for ( MavenProject project : reactorProjects )
            {
                if ( "java".equals( project.getArtifact().getArtifactHandler().getLanguage() ) )
                {
                    sourceDirs.addAll( getSourceRoots( project ) );
                }
            }
        }

        sourceDirs = pruneSourceDirs( sourceDirs );
        return sourceDirs;
    }

    @Override
    public boolean canGenerateReport()
    {
        return canGenerateReport( constructSourceDirs() );
    }

    @Override
    public boolean isExternalReport()
    {
        return true;
    }

    /**
     * @return a String that contains the location of the javadocs
     */
    private Path getJavadocLocation()
        throws IOException
    {
        Path location = null;
        if ( linkJavadoc )
        {
            // We don't need to do the whole translation thing like normal, because JXR does it internally.
            // It probably shouldn't.
            if ( getJavadocDir().exists() )
            {
                // XRef was already generated by manual execution of a lifecycle binding
                location = getJavadocDir().toPath().toAbsolutePath();
            }
            else
            {
                // Not yet generated - check if the report is on its way

                // Special case: using the site:stage goal
                String stagingDirectory = System.getProperty( "stagingDirectory" );

                if ( StringUtils.isNotEmpty( stagingDirectory ) )
                {
                    String javadocDestDir = getJavadocDir().getName();
                    boolean javadocAggregate = JxrReportUtil.isJavadocAggregated( project );
                    String structureProject = JxrReportUtil.getStructure( project, false );

                    if ( isAggregate() && javadocAggregate )
                    {
                        location = Paths.get( stagingDirectory, structureProject, javadocDestDir );
                    }
                    if ( !isAggregate() && javadocAggregate )
                    {
                        location = Paths.get( stagingDirectory, javadocDestDir );

                        String hierarchy = project.getName();

                        MavenProject parent = project.getParent();
                        while ( parent != null )
                        {
                            hierarchy = parent.getName();
                            parent = parent.getParent();
                        }
                        location = Paths.get( stagingDirectory, hierarchy, javadocDestDir );
                    }
                    if ( isAggregate() && !javadocAggregate )
                    {
                        getLog().warn( "The JXR plugin is configured to build an aggregated report at the root, "
                                           + "not the Javadoc plugin." );
                    }
                    if ( !isAggregate() && !javadocAggregate )
                    {
                        location = Paths.get( stagingDirectory, structureProject, javadocDestDir );
                    }
                }
                else
                {
                    location = getJavadocDir().toPath();
                }
            }

            if ( location == null )
            {
                getLog().warn( "Unable to locate Javadoc to link to - DISABLED" );
            }
        }

        return location;
    }

    /**
     * Abstract method that returns the target directory where the generated JXR reports will be put.
     *
     * @return a String that contains the target directory name
     */
    protected abstract String getDestinationDirectory();

    /**
     * Abstract method that returns the specified source directories that will be included in the JXR report generation.
     *
     * @return a List of the source directories
     */
    protected abstract List<String> getSourceRoots();

    /**
     * Abstract method that returns the compile source directories of the specified project that will be included in the
     * JXR report generation
     *
     * @param project the MavenProject where the JXR report plugin will be executed
     * @return a List of the source directories
     */
    protected abstract List<String> getSourceRoots( MavenProject project );

    /**
     * Abstract method that returns the directory of the javadoc files.
     *
     * @return a File for the directory of the javadocs
     */
    protected abstract File getJavadocDir();

    /**
     * Is the current report aggregated?
     *
     * @return true if aggregate, false otherwise
     */
    protected boolean isAggregate()
    {
        return false;
    }
}
