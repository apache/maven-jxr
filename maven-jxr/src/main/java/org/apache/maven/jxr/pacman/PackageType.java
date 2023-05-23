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
package org.apache.maven.jxr.pacman;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Java package and its subclasses.
 */
public class PackageType extends BaseType {

    private Map<String, ClassType> classes = new HashMap<>();

    /**
     * Creates a Java package.
     *
     * @param name name of package
     */
    public PackageType(String name) {
        super(name);
    }

    /**
     * Creates a Java package with no name IE the default Java package.
     */
    public PackageType() {
        super("");
    }

    /**
     * Gets all the known classes
     *
     * @return collection of class types
     */
    public Collection<ClassType> getClassTypes() {
        return classes.values();
    }

    /**
     * Adds a class to this package.
     *
     * @param classType class type to add
     */
    public void addClassType(ClassType classType) {

        this.classes.put(classType.getName(), classType);
    }

    /**
     * Given the name of a class, get it from this package or null if it does
     * not exist.
     *
     * @param classType class type String
     * @return class type object
     */
    public ClassType getClassType(String classType) {

        return this.classes.get(classType);
    }
}
