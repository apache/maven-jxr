package org.apache.maven.jxr.pacman;

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

import org.codehaus.plexus.util.DirectoryScanner;
import org.apache.maven.jxr.log.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Given a list of directories, parse them out and store them as rendered
 * packages, classes, imports, etc.
 */
public class PackageManager
{
    private final Log log;

    private Set<Path> directories = new HashSet<>();

    /**
     * All the packages that have been parsed
     */
    private Hashtable<String, PackageType> packages = new Hashtable<>();

    /**
     * The default Java package.
     */
    private PackageType defaultPackage = new PackageType();

    private FileManager fileManager;

    /**
     * The list of exclude patterns to use.
     */
    private String[] excludes = null;

    /**
     * The list of include patterns to use.
     */
    private String[] includes = { "**/*.java" };

    public PackageManager( Log log, FileManager fileManager )
    {
        this.log = log;
        this.fileManager = fileManager;
    }

    /**
     * Given the name of a package (Ex: org.apache.maven.util) obtain it from
     * the PackageManager
     */
    public PackageType getPackageType( String name )
    {

        //return the default package if the name is null.
        if ( name == null )
        {
            return defaultPackage;
        }

        return this.packages.get( name );
    }

    /**
     * Add a package to the PackageManager
     */
    public void addPackageType( PackageType packageType )
    {
        this.packages.put( packageType.getName(), packageType );
    }

    /**
     * Get all of the packages in the PackageManager
     */
    public Enumeration<PackageType> getPackageTypes()
    {
        return packages.elements();
    }

    /**
     * Parse out all the directories on which this depends.
     */
    private void parse( String directory )
    {
        // Go through each directory and get the java source 
        // files for this dir.
        log.debug( "Scanning " + directory );
        DirectoryScanner directoryScanner = new DirectoryScanner();
        Path baseDir = Paths.get( directory );
        directoryScanner.setBasedir( baseDir.toFile() );
        directoryScanner.setExcludes( excludes );
        directoryScanner.setIncludes( includes );
        directoryScanner.scan();

        for ( String file : directoryScanner.getIncludedFiles() )
        {
            log.debug( "parsing... " + file );

            //now parse out this file to get the packages/classname/etc
            try
            {
                Path fileName = baseDir.resolve( file );
                JavaFile jfi = fileManager.getFile( fileName );

                // now that we have this parsed out blend its information
                // with the current package structure
                PackageType jp = this.getPackageType( jfi.getPackageType().getName() );

                if ( jp == null )
                {
                    this.addPackageType( jfi.getPackageType() );
                    jp = jfi.getPackageType();
                }

                // Add the current file's class(es) to this global package.
                if ( jfi.getClassTypes() != null && !jfi.getClassTypes().isEmpty() )
                {
                    for ( ClassType ct : jfi.getClassTypes() )
                    {
                        jp.addClassType( ct );
                    }
                }

            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }

    }

    /**
     * Description of the Method
     */
    public void process( Path directory )
    {
        if ( this.directories.add( directory ) )
        {
            this.parse( directory.toString() );
        }
    }

    /**
     * Dump the package information to STDOUT. FOR DEBUG ONLY
     */
    public void dump()
    {

        log.debug( "Dumping out PackageManager structure" );

        Enumeration<PackageType> pts = this.getPackageTypes();

        while ( pts.hasMoreElements() )
        {

            //get the current package and print it.
            PackageType current = pts.nextElement();

            log.debug( current.getName() );

            //get the classes under the package and print those too.
            Enumeration<ClassType> classes = current.getClassTypes();

            while ( classes.hasMoreElements() )
            {

                ClassType currentClass = classes.nextElement();

                log.debug( "\t" + currentClass.getName() );

            }
        }
    }

    public FileManager getFileManager()
    {
        return fileManager;
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

