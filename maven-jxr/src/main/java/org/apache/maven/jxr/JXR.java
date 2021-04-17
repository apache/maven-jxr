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
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Main entry point into Maven used to kick off the XReference code building.
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 */
public class JXR
{
    private static final Logger LOGGER = LoggerFactory.getLogger( JXR.class );
    
    private final PackageManager pkgmgr;
    
    private final FileManager fileManager;

    /**
     * Handles taking .java files and changing them into html. "More than meets
     * the eye!" :)
     */
    private Map<String, CodeTransformer> transformers;
    
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
     * The list of exclude patterns to use.
     */
    private String[] excludes;

    /**
     * The list of include patterns to use.
     */
    private String[] includes;
    
    public JXR( PackageManager pkgmgr, FileManager fileManager, Map<String, CodeTransformer> transformers )
    {
        this.pkgmgr = pkgmgr;
        this.fileManager = fileManager;
        this.transformers = transformers;
    }

    /**
     * Now that we have instantiated everything. Process this JXR task.
     *
     * @param packageManager
     * @param sourceDir
     * @param bottom
     * @throws IOException
     */
    public void processPath( Path sourceDir, String bottom )
        throws IOException
    {
        
        DirectoryScanner ds = new DirectoryScanner();
        // I'm not sure why we don't use the directoryScanner in packageManager,
        // but since we don't we need to set includes/excludes here as well
        ds.setExcludes( excludes );
        if ( includes != null )
        {
            ds.setIncludes( includes );
        }
        else
        {
            Set<String> transformerIncludes = new HashSet<>();
            for ( CodeTransformer transformer: transformers.values() )
            {
               transformerIncludes.addAll( transformer.getDefaultIncludes() ); 
            }
            ds.setIncludes( transformerIncludes.toArray( new String[0] ) );
        }
        ds.addDefaultExcludes();

        ds.setBasedir( sourceDir.toString() );
        ds.scan();

        //now get the list of included files
        String[] files = ds.getIncludedFiles();
        
        // < file-extension, named key >, this way we will let DI decide if we get the singleton or a new instance 
        Map<String, String> transformerForExtension = new HashMap<>();
        
        for ( String file : files )
        {
            Path sourceFile = sourceDir.resolve( file );
            
            String fileExtension = getExtension( sourceFile );

            String transformerName = transformerForExtension.get( fileExtension );
            if ( !transformerForExtension.containsKey( fileExtension ) )
            {
                for ( Map.Entry<String, CodeTransformer> ct : transformers.entrySet() )
                {
                    if ( ct.getValue().canTransform( fileExtension ) )
                    {
                        transformerName = ct.getKey();
                        break;
                    }
                }
                transformerForExtension.put( fileExtension, transformerName );
            }
            
            if ( transformerName != null )
            {
                String newFileName = file.replaceFirst( fileExtension + '$', ".html" );
                
                transform( transformers.get( transformerName ), sourceFile, this.destDir.resolve( newFileName ),
                           bottom );
            }
        }
    }
    
    private String getExtension( Path file ) 
    {
        String fileName = file.getFileName().toString(); 
        return fileName.substring( fileName.indexOf( '.' ) );
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
        pkgmgr.setExcludes( excludes );
        pkgmgr.setIncludes( includes );

        // go through each source directory and xref the java files
        for ( String dir : sourceDirs )
        {
            Path path = Paths.get( dir ).toRealPath();

            pkgmgr.process( path );

            processPath( path, bottom );
        }

        // once we have all the source files xref'd, create the index pages
        DirectoryIndexer indexer = new DirectoryIndexer( pkgmgr, destDir.toString() );
        indexer.setOutputEncoding( outputEncoding );
        indexer.setTemplateDir( templateDir );
        indexer.setWindowTitle( windowTitle );
        indexer.setDocTitle( docTitle );
        indexer.setBottom( bottom );
        indexer.process();
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
    private void transform( CodeTransformer transformer, Path sourceFile, Path destFile, String bottom )
        throws IOException
    {
        LOGGER.debug( sourceFile + " -> " + destFile );
        
        // get a relative link to the javadocs
        Path javadoc = javadocLinkDir != null ? getRelativeLink( destFile.getParent(), javadocLinkDir ) : null;
        transformer.transform( fileManager.getFile( sourceFile ), destFile, locale, outputEncoding, javadoc, bottom );
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
        this.includes = includes;
    }
}
