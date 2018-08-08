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
 * A StringEntry represents a value found by the tokenizer. The index is where
 * this StringEntry was found in the source string
 */
public class StringEntry
    implements Comparable<StringEntry>
{

    private String value = null;

    private int index = 0;

    /**
     * Constructor for the StringEntry object
     *
     * @param value
     * @param index
     */
    public StringEntry( String value, int index )
    {

        this.value = value;
        this.index = index;
    }

    /**
     * Gets the index attribute of the StringEntry object
     */
    public int getIndex()
    {
        return this.index;
    }

    /**
     * Description of the Method
     */
    @Override
    public String toString()
    {
        return this.value;
    }

    /**
     * Compare two objects for equality.
     */
    @Override
    public int compareTo( StringEntry se )
    {
        //right now only sort by the index.

        return Integer.compare( se.getIndex(), this.getIndex() );

    }

}

