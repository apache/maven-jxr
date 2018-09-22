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

import org.apache.maven.jxr.ant.DirectoryScanner;
import org.apache.maven.jxr.log.Log;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Main entry point into Maven used to kick off the XReference code building.
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 * @version $Id$
 */
public class JXR
{
    /**
     * The Log.
     */
    private Log log;

    /**
     * The default list of include patterns to use.
     */
    private static final String[] DEFAULT_INCLUDES = { "**/*.java" };

    /**
     * Path to destination.
     */
    private Path destDir;

    private Locale locale;

    private String inputEncoding;

    private String outputEncoding;

    /**
     * Relative path to javadocs, suitable for hyperlinking.
     */
    private Path javadocLinkDir;

    /**
     * Handles taking .java files and changing them into html. "More than meets
     * the eye!" :)
     */
    private JavaCodeTransform transformer;

    /**
     * The revision of the module currently being processed.
     */
    private String revision;

    /**
     * The list of exclude patterns to use.
     */
    private String[] excludes = null;

    /**
     * The list of include patterns to use.
     */
    private String[] includes = DEFAULT_INCLUDES;

    /**
     * Now that we have instantiated everything. Process this JXR task.
     *
     * @param packageManager
     * @param sourceDir
     * @param bottom
     * @throws IOException
     */
    public void processPath( PackageManager packageManager, Path sourceDir, String bottom )
        throws IOException
    {
        this.transformer = new JavaCodeTransform( packageManager );

        DirectoryScanner ds = new DirectoryScanner();
        // I'm not sure why we don't use the directoryScanner in packageManager,
        // but since we don't we need to set includes/excludes here as well
        ds.setExcludes( excludes );
        ds.setIncludes( includes );
        ds.addDefaultExcludes();

        ds.setBasedir( sourceDir.toString() );
        ds.scan();

        //now get the list of included files

        String[] files = ds.getIncludedFiles();

        for ( String file : files )
        {
            Path sourceFile = sourceDir.resolve( file );

            if ( isJavaFile( sourceFile.toString() ) )
            {
                String newFileName = file.replaceFirst( ".java$", ".html" );
                
                transform( sourceFile, this.destDir.resolve( newFileName ), bottom );
            }
        }
    }

    /**
     * Check to see if the file is a Java source file.
     *
     * @param filename The name of the file to check
     * @return <code>true</code> if the file is a Java file
     */
    public static boolean isJavaFile( String filename )
    {
        return filename.endsWith( ".java" );
    }

    /**
     * Check to see if the file is an HTML file.
     *
     * @param filename The name of the file to check
     * @return <code>true</code> if the file is an HTML file
     */
    public static boolean isHtmlFile( String filename )
    {
        return filename.endsWith( ".html" );
    }

    /**
     * @param dest
     */
    public void setDest( Path dest )
    {
        this.destDir = dest;
    }

    /**
     * @param locale
     */
    public void setLocale( Locale locale )
    {
        this.locale = locale;
    }

    /**
     * @param inputEncoding
     */
    public void setInputEncoding( String inputEncoding )
    {
        this.inputEncoding = inputEncoding;
    }

    /**
     * @param outputEncoding
     */
    public void setOutputEncoding( String outputEncoding )
    {
        this.outputEncoding = outputEncoding;
    }

    /**
     * @param javadocLinkDir
     */
    public void setJavadocLinkDir( Path javadocLinkDir )
    {
        // get a relative link to the javadocs
        this.javadocLinkDir = javadocLinkDir;
    }

    /**
     * @param transformer
     */
    public void setTransformer( JavaCodeTransform transformer )
    {
        this.transformer = transformer;
    }

    /**
     * @param revision
     */
    public void setRevision( String revision )
    {
        this.revision = revision;
    }

    /**
     * @param log
     */
    public void setLog( Log log )
    {
        this.log = log;
    }

    /**
     * @param sourceDirs
     * @param templateDir
     * @param windowTitle
     * @param docTitle
     * @param bottom
     * @throws IOException
     * @throws JxrException
     */
    public void xref( List<String> sourceDirs, String templateDir, String windowTitle, String docTitle, String bottom )
        throws IOException, JxrException
    {
        // first collect package and class info
        FileManager fileManager = new FileManager();
        fileManager.setEncoding( inputEncoding );

        PackageManager pkgmgr = new PackageManager( log, fileManager );
        pkgmgr.setExcludes( excludes );
        pkgmgr.setIncludes( includes );

        // go through each source directory and xref the java files
        for ( String dir : sourceDirs )
        {
            Path path = Paths.get( dir ).toRealPath();

            pkgmgr.process( path );

            processPath( pkgmgr, path, bottom );
        }

        // once we have all the source files xref'd, create the index pages
        DirectoryIndexer indexer = new DirectoryIndexer( pkgmgr, destDir.toString() );
        indexer.setOutputEncoding( outputEncoding );
        indexer.setTemplateDir( templateDir );
        indexer.setWindowTitle( windowTitle );
        indexer.setDocTitle( docTitle );
        indexer.setBottom( bottom );
        indexer.process( log );
    }

    // ----------------------------------------------------------------------
    // private methods
    // ----------------------------------------------------------------------
    /**
     * Given a source file transform it into HTML and write it to the
     * destination (dest) file.
     *
     * @param sourceFile The java source file
     * @param destFile The directory to put the HTML into
     * @param bottom The bottom footer text just as in the package pages
     * @throws IOException Thrown if the transform can't happen for some reason.
     */
    private void transform( Path sourceFile, Path destFile, String bottom )
        throws IOException
    {
        log.debug( sourceFile + " -> " + destFile );

        // get a relative link to the javadocs
        Path javadoc = javadocLinkDir != null ? getRelativeLink( destFile.getParent(), javadocLinkDir ) : null;
        transformer.transform( sourceFile, destFile, locale, inputEncoding, outputEncoding, javadoc,
            this.revision, bottom );
    }

    /**
     * Creates a relative link from one directory to another.
     *
     * Example:
     * given <code>/foo/bar/baz/oink</code>
     * and <code>/foo/bar/schmoo</code>
     *
     * this method will return a string of <code>"../../schmoo/"</code>
     *
     * @param fromDir The directory from which the link is relative.
     * @param toDir The directory into which the link points.
     * @return a String of format <code>"../../schmoo/"</code>
     */
    private static Path getRelativeLink( Path fromDir, Path toDir )
    {
        return fromDir.relativize( toDir );
    }

    public void setExcludes( String[] excludes )
    {
        this.excludes = excludes;
    }


    public void setIncludes( String[] includes )
    {
        if ( includes == null )
        {
            // We should not include non-java files, so we use a sensible default pattern
            this.includes = DEFAULT_INCLUDES;
        }
        else
        {
            this.includes = includes;
        }
    }
}
