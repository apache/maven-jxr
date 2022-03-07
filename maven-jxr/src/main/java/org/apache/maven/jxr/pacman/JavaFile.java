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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interface for objects which wish to provide meta-info about a JavaFile.
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 * @version $Id$
 */
public abstract class JavaFile
{
    private Set<ImportType> imports = new HashSet<>();

    private List<ClassType> classTypes = new ArrayList<>();

    private PackageType packageType = new PackageType();

    private final Path path;

    private String filename;

    private final String encoding;
    
    protected JavaFile(  Path path, String encoding )
    {
        this.path = path;
        this.encoding = encoding;
        this.filename = getFilenameWithoutPathOrExtension( path );
    }

    /**
     * Get the imported packages/files that this package has.
     */
    public Set<ImportType> getImportTypes()
    {
        return Collections.unmodifiableSet( imports );
    }

    /**
     * Get the name of this class.
     */
    public ClassType getClassType()
    {
        if ( classTypes.isEmpty() )
        {
            return null;
        }
        else
        {
            // To retain backward compatibility, return the first class
            return this.classTypes.get( 0 );
        }
    }

    /**
     * Get the names of the classes in this file.
     */
    public List<ClassType> getClassTypes()
    {
        return this.classTypes;
    }

    /**
     * Get the package of this class.
     */
    public PackageType getPackageType()
    {
        return this.packageType;
    }


    /**
     * Add a ClassType to the current list of class types.
     */
    public void addClassType( ClassType classType )
    {
        this.classTypes.add( classType );
    }

    /**
     * Add an ImportType to the current imports.
     */
    public void addImportType( ImportType importType )
    {
        this.imports.add( importType );
    }

    /**
     * Set the name of this class.
     */
    public void setClassType( ClassType classType )
    {
        // To retain backward compatibility, make sure the list contains only the supplied classType
        this.classTypes.clear();
        this.classTypes.add( classType );
    }

    /**
     * Set the PackageType of this class.
     */
    public void setPackageType( PackageType packageType )
    {
        this.packageType = packageType;
    }


    /**
     * Gets the filename attribute of the JavaFile object
     */
    public Path getPath()
    {
        return this.path;
    }

    /**
     * File name without path and extension.
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Gets the encoding attribute of the JavaFile object
     */
    public String getEncoding()
    {
        return this.encoding;
    }

    /**
     * Remove the path and the ".java" extension from a filename.
     */
    protected static String getFilenameWithoutPathOrExtension( Path path )
    {
        String newFilename = path.getFileName().toString();
        // Remove the ".java" extension from the filename, if it exists
        int extensionIndex = newFilename.lastIndexOf( ".java" );
        if ( extensionIndex >= 0 )
        {
            newFilename = newFilename.substring( 0, extensionIndex );
        }
        return newFilename;
    }

}
