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
 * put your documentation comment here
 *
 * @author jvanzyl
 * @created February 23, 2002
 */
public abstract class BaseType
{
    private String name = null;


    /**
     * Get the name for this type
     *
     * @return The name value
     */
    public String getName()
    {
        if ( name == null )
        {
            return "";
        }
        return this.name;
    }


    /**
     * Set the name for this type
     *
     * @param name The new name value
     */
    public void setName( String name )
    {
        this.name = name;
    }
}

