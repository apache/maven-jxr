package org.apache.maven.jxr.util;

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
 * 
 * A class in order to wrap the class name and provide a key. 
 * This will prevent TreeMap collection 
 * from replacing classes with the same name
 */
public class ClassName 
{
    private static final int HASH_CONSTANT = 47;
    
    private String packageName;
    private String className;

    public ClassName() 
    {
        //empty constructor
    }

    /**
     * 
     * @param packageName
     * @param className 
     */
    public ClassName( String packageName, String className ) 
    {
        this.packageName = packageName;
        this.className = className;
    }

    
    public String getPackageName() 
    {
        return packageName;
    }

    /**
     * 
     * @param packageName 
     */
    public void setPackageName( String packageName ) 
    {
        this.packageName = packageName;
    }

    public String getClassName() 
    {
        return className;
    }

    /**
     * 
     * @param className 
     */
    public void setClassName( String className ) 
    {
        this.className = className;
    }

    @Override
    public String toString() 
    {
        return "ClassName{" + "packageName=" + packageName + ", className=" + className + '}';
    }

    public String getKeyForComparison()
    {
        return this.className + this.packageName;
    }
    

}
