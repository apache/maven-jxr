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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Java package and its subclasses.
 */
public class PackageType
    extends BaseType
{

    private Map<String, ClassType> classes = new HashMap<>();

    /**
     * Create a Java package
     *
     * @param name
     */
    public PackageType( String name )
    {
        super( name );
    }

    /**
     * Create a Java package with no name IE the default Java package.
     */
    public PackageType()
    {
        super( "" );
    }

    /**
     * Get all the known classes
     */
    public Collection<ClassType> getClassTypes()
    {
        return classes.values();
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

        return this.classes.get( classType );
    }

}
