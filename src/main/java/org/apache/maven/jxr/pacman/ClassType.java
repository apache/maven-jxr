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
 * Represents a Java class or interface.
 */
public class ClassType
    extends BaseType
{
    /**
     * The name of the file that this class resides in, without path or
     * extension. It might be different from the name of the class when dealing
     * with inner classes.
     */
    private String filename = null;

    /**
     * Create a new ClassType without a filename.
     *
     * @deprecated Please use {@link #ClassType( String, String )} instead
     * @param name
     */
    public ClassType( String name )
    {
        this.setName( name );
    }

    /**
     * Create a new ClassType.
     *
     * @param name
     * @param filename
     */
    public ClassType( String name , String filename)
    {
        this.setName( name );
        this.setFilename( filename );
    }


    public String getFilename()
    {
        return filename;
    }

    public void setFilename( String filename )
    {
        this.filename = filename;
    }
}
