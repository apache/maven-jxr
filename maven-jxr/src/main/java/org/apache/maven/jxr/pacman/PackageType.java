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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Represents a Java package and its subclasses.
 */
public class PackageType
    extends BaseType
{

    private Hashtable classes = new Hashtable();

    /**
     * Create a Java package
     *
     * @param name
     */
    public PackageType( String name )
    {
        this.setName( name );
    }

    /**
     * Create a Java package with no name IE the default Java package.
     */
    public PackageType()
    {
    }


    /**
     * Get all the known classes
     */
    public Enumeration getClassTypes()
    {

        return classes.elements();
    }

    /**
     * Add a class to this package.
     */
    public void addClassType( ClassType classType )
    {

        this.classes.put( classType.getName(), classType );

    }

    /**
     * Given the name of a class, get it from this package or null if it does
     * not exist
     */
    public ClassType getClassType( String classType )
    {

        return (ClassType) this.classes.get( classType );
    }

}
