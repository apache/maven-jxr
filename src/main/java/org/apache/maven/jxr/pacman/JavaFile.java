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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Interface for objects which wish to provide metainfo about a JavaFile.
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 * @version $Id$
 */
public abstract class JavaFile
{

    private Vector imports = new Vector();

    private ArrayList classTypes = new ArrayList();

    private PackageType packageType = new PackageType();

    private String filename = null;

    private String encoding = null;

    /**
     * Get the imported packages/files that this package has.
     */
    public ImportType[] getImportTypes()
    {

        ImportType[] it = new ImportType[this.imports.size()];
        this.imports.copyInto( it );
        return it;
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
            return (ClassType) this.classTypes.get( 0 );
        }
    }

    /**
     * Get the names of the classes in this file.
     */
    public List getClassTypes()
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
        this.imports.addElement( importType );
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
    public String getFilename()
    {
        return this.filename;
    }

    /**
     * Sets the filename attribute of the JavaFile object
     */
    public void setFilename( String filename )
    {
        this.filename = filename;
    }


    /**
     * Gets the encoding attribute of the JavaFile object
     */
    public String getEncoding()
    {
        return this.encoding;
    }

    /**
     * Sets the encoding attribute of the JavaFile object
     */
    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }
}
