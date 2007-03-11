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

/**
 * Represents an entry in a java "import" statement
 */
public class ImportType
    extends BaseType
{

    private boolean isclass = false;

    private boolean ispackage = false;

    private String packagename = null;

    /**
     * Create a new ImportType with the specified name
     *
     * @param name
     */
    public ImportType( String name )
    {
        this.setName( name );

        //compute member variables

        this.isclass = this.getName().indexOf( "*" ) == -1;

        this.ispackage = this.getName().indexOf( "*" ) != -1;

        int end = this.getName().lastIndexOf( "." );
        if ( end != -1 )
        {
            this.packagename = this.getName().substring( 0, end );
        }

    }

    /**
     * Return true if this is a class import. Ex: test.Test
     */
    public boolean isClass()
    {
        return this.isclass;
    }

    /**
     * Return true if this is a package import. Ex: test.*
     */
    public boolean isPackage()
    {
        return this.ispackage;
    }


    /**
     * Get the name of the package that this import is based on: EX: test.* will
     * return "test" EX: test.Test will return "test"
     */
    public String getPackage()
    {
        return this.packagename;
    }

}

