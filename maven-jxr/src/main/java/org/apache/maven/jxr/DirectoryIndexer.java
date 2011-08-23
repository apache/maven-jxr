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

import org.apache.commons.io.IOUtils;
import org.apache.maven.jxr.pacman.ClassType;
import org.apache.maven.jxr.pacman.PackageManager;
import org.apache.maven.jxr.pacman.PackageType;
import org.apache.maven.jxr.log.VelocityLogger;
import org.apache.maven.jxr.log.Log;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class creates the navigational pages for jxr's cross-referenced source
 * files. The navigation is inspired by javadoc, so it should have a familiar feel.
 *
 * Creates the following files:
 * <ul>
 * <li><code>index.html</code>            main index containing the frameset</li>
 * <li><code>overview-frame.html</code>   list of the project's packages              (top left)</li>
 * <li><code>allclasses-frame.html</code> list of all classes in the project          (bottom left)</li>
 * <li><code>overview-summary.html</code> top-level listing of the project's packages (main frame)</li>
 *
 * <li>
 * Package specific:
 * <ul>
 * <li><code>package-summary.html</code> listing of all classes in this package    (main frame)</li>
 * <li><code>package-frame.html</code>   listing of all classes in this package    (bottom left)</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author <a href="mailto:bellingard@gmail.com">Fabrice Bellingard </a>
 * @author <a href="mailto:brian@brainslug.org">Brian Leonard</a>
 * @version $Id$
 */
public class DirectoryIndexer
{
    /*
     * JavaCodeTransform uses this to cross-reference package references
     * with that package's main summary page.
     */
    static final String INDEX = "package-summary.html";

    /*
     * Path to the root output directory.
     */
    private String root;

    /*
     * Package Manager for this project.
     */
    private PackageManager packageManager;

    /*
     * see the getter/setter docs for these properties
     */
    private String outputEncoding;

    private String templateDir;

    private String windowTitle;

    private String docTitle;

    private String bottom;

    /**
     * Constructor for the DirectoryIndexer object
     *
     * @param packageManager PackageManager for this project
     * @param root Path of the root output directory
     */
    public DirectoryIndexer( PackageManager packageManager, String root )
    {
        this.packageManager = packageManager;
        this.root = root;
    }

    /**
     * OutputEncoding is the encoding of output files.
     *
     * @param outputEncoding output Encoding
     */
    public void setOutputEncoding( String outputEncoding )
    {
        this.outputEncoding = outputEncoding;
    }

    /**
     * see setOutputEncoding(String)
     */
    public String getOutputEncoding()
    {
        return outputEncoding;
    }

    /**
     * TemplateDir is the location of the jelly template files used
     * to generate the navigation pages.
     *
     * @param templateDir location of the template directory
     */
    public void setTemplateDir( String templateDir )
    {
        this.templateDir = templateDir;
    }

    /**
     * see setTemplateDir(String)
     */
    public String getTemplateDir()
    {
        return templateDir;
    }

    /**
     * WindowTitle is used in the output's &lt;title&gt; tags
     * see the javadoc documentation for the property of the same name
     *
     * @param windowTitle the &lt;title&gt; attribute
     */
    public void setWindowTitle( String windowTitle )
    {
        this.windowTitle = windowTitle;
    }

    /**
     * see setWindowTitle(String)
     *
     * @see #setWindowTitle(String) setWindowTitle
     */
    public String getWindowTitle()
    {
        return windowTitle;
    }

    /**
     * DocTitle is used as a page heading for the summary files
     * see the javadoc documentation for the property of the same name
     *
     * @param docTitle major page heading
     */
    public void setDocTitle( String docTitle )
    {
        this.docTitle = docTitle;
    }

    /**
     * see setDocTitle(String)
     *
     * @see #setDocTitle(String) setDocTitle
     */
    public String getDocTitle()
    {
        return docTitle;
    }

    /**
     * Bottom is a footer for the navigation pages, usually a copyright
     * see the javadoc documentation for the property of the same name
     *
     * @param bottom page footer
     */
    public void setBottom( String bottom )
    {
        this.bottom = bottom;
    }

    /**
     * see setBottom(String)
     *
     * @see #setBottom(String) setBottom
     */
    public String getBottom()
    {
        return bottom;
    }

    /**
     * Does the actual indexing.
     *
     * @throws JxrException If something went wrong
     */
    public void process( Log log )
        throws JxrException
    {
        Map info = getPackageInfo();

        VelocityEngine engine = new VelocityEngine();
        setProperties( engine, log );
        try
        {
            engine.init();
        }
        catch ( Exception e )
        {
            throw new JxrException( "Error initializing Velocity", e );
        }

        VelocityContext context = new VelocityContext();
        context.put( "outputEncoding", getOutputEncoding() );
        context.put( "windowTitle", getWindowTitle() );
        context.put( "docTitle", getDocTitle() );
        context.put( "bottom", getBottom() );
        context.put( "info", info );

        doVelocity( "index", root, context, engine );
        doVelocity( "overview-frame", root, context, engine );
        doVelocity( "allclasses-frame", root, context, engine );
        doVelocity( "overview-summary", root, context, engine );

        Iterator iter = ( (Map) info.get( "allPackages" ) ).values().iterator();
        while ( iter.hasNext() )
        {
            Map pkgInfo = (Map) iter.next();

            VelocityContext subContext = new VelocityContext( context );
            subContext.put( "pkgInfo", pkgInfo );

            String outDir = root + "/" + (String) pkgInfo.get( "dir" );
            doVelocity( "package-summary", outDir, subContext, engine );
            doVelocity( "package-frame", outDir, subContext, engine );
        }
    }

    /*
     * Set Velocity properties to find templates
     */
    private void setProperties( VelocityEngine engine, Log log )
    {
        File templateDirFile = new File( getTemplateDir() );
        if ( templateDirFile.isAbsolute() )
        {
            // the property has been overridden: need to use a FileResourceLoader
            engine.setProperty( "resource.loader", "file" );
            engine.setProperty( "file.resource.loader.class",
                                "org.apache.velocity.runtime.resource.loader.FileResourceLoader" );
            engine.setProperty( "file.resource.loader.path", templateDirFile.toString() );
        }
        else
        {
            // use of the default templates
            engine.setProperty( "resource.loader", "classpath" );
            engine.setProperty( "classpath.resource.loader.class",
                                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        }
        // avoid "unable to find resource 'VM_global_library.vm' in any resource loader."
        engine.setProperty( "velocimacro.library", "" );
        engine.setProperty( Log.class.getName(), log );
        engine.setProperty( "runtime.log.logsystem.class", VelocityLogger.class.getName() );
    }

    /*
     * Generate the HTML file according to the Velocity template
     */
    private void doVelocity( String templateName, String outDir, VelocityContext context, VelocityEngine engine )
        throws JxrException
    {
        // output file
        File file = new File( outDir, templateName + ".html" );
        file.getParentFile().mkdirs();
        FileWriter writer = null;

        try
        {
            writer = new FileWriter( file );

            // template file
            StringBuffer templateFile = new StringBuffer();
            File templateDirFile = new File( getTemplateDir() );
            if ( !templateDirFile.isAbsolute() )
            {
                // default templates
                templateFile.append( getTemplateDir() );
                templateFile.append( "/" );
            }
            templateFile.append( templateName );
            templateFile.append( ".vm" );
            Template template = engine.getTemplate( templateFile.toString() );

            // do the merge
            template.merge( context, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            throw new JxrException( "Error merging velocity template", e );
        }
        finally
        {
            IOUtils.closeQuietly( writer );
        }
    }

    /*
     * Creates a Map of other Maps containing information about
     * this project's packages and classes, obtained from the PackageManager.
     *
     * allPackages collection of Maps with package info, with the following format
     *   {name}    package name (e.g., "org.apache.maven.jxr")
     *   {dir}     package dir relative to the root output dir (e.g., "org/apache/maven/jxr")
     *   {rootRef} relative link to root output dir (e.g., "../../../../") note trailing slash
     *   {classes} collection of Maps with class info
     *      {name}  class name (e.g., "DirectoryIndexer")
     *      {dir}   duplicate of package {dir}
     *
     * allClasses collection of Maps with class info, format as above
     *
     */
    private Map getPackageInfo()
    {
        TreeMap allPackages = new TreeMap();
        TreeMap allClasses = new TreeMap();
        Perl5Util perl = new Perl5Util();

        Enumeration packages = packageManager.getPackageTypes();
        while ( packages.hasMoreElements() )
        {
            PackageType pkg = (PackageType) packages.nextElement();
            String pkgName = pkg.getName();
            String pkgDir = perl.substitute( "s/\\./\\//g", pkgName );
            String rootRef = perl.substitute( "s/[^\\.]*(\\.|$)/..\\//g", pkgName );

            // special case for the default package
            // javadoc doesn't deal with it, but it's easy for us
            if ( pkgName.length() == 0 )
            {
                pkgName = "(default package)";
                pkgDir = ".";
                rootRef = "./";
            }

            TreeMap pkgClasses = new TreeMap();
            Enumeration classes = pkg.getClassTypes();
            while ( classes.hasMoreElements() )
            {
                ClassType clazz = (ClassType) classes.nextElement();

                String className = clazz.getName();
                Map classInfo = new HashMap();
                if ( clazz.getFilename() != null )
                {
                    classInfo.put( "filename", clazz.getFilename() );
                }
                else
                {
                    classInfo.put( "filename", "" );
                }
                classInfo.put( "name", className );
                classInfo.put( "dir", pkgDir );

                pkgClasses.put( className, classInfo );
                allClasses.put( className, classInfo );
            }

            Map pkgInfo = new HashMap();
            pkgInfo.put( "name", pkgName );
            pkgInfo.put( "dir", pkgDir );
            pkgInfo.put( "classes", pkgClasses );
            pkgInfo.put( "rootRef", rootRef );
            allPackages.put( pkgName, pkgInfo );
        }

        Map info = new HashMap();
        info.put( "allPackages", allPackages );
        info.put( "allClasses", allClasses );

        return info;
    }
}