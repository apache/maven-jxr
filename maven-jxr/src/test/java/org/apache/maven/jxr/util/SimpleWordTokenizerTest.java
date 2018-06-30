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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class SimpleWordTokenizerTest
{
    @Test
    public void testCompact()
    {
        List<StringEntry> entries = SimpleWordTokenizer.tokenize( "public void withApp1(App app)" );
        assertEquals( 5, entries.size() );

        assertEquals( "public", entries.get(0).toString() );
        assertEquals( 0, entries.get(0).getIndex() );

        assertEquals( "void", entries.get(1).toString() );
        assertEquals( 7, entries.get(1).getIndex() );

        assertEquals( "withApp1", entries.get(2).toString() );
        assertEquals( 12, entries.get(2).getIndex() );

        assertEquals( "App", entries.get(3).toString() );
        assertEquals( 21, entries.get(3).getIndex() );

        assertEquals( "app", entries.get(4).toString() );
        assertEquals( 25, entries.get(4).getIndex() );
    }
    
    @Test
    public void testSpacesAroundParenOpen()
    {
        List<StringEntry> entries = SimpleWordTokenizer.tokenize( "public void withApp2 ( App app)" );
        assertEquals( 5, entries.size() );

        assertEquals( "public", entries.get(0).toString() );
        assertEquals( 0, entries.get(0).getIndex() );

        assertEquals( "void", entries.get(1).toString() );
        assertEquals( 7, entries.get(1).getIndex() );

        assertEquals( "withApp2", entries.get(2).toString() );
        assertEquals( 12, entries.get(2).getIndex() );

        assertEquals( "App", entries.get(3).toString() );
        assertEquals( 23, entries.get(3).getIndex() );

        assertEquals( "app", entries.get(4).toString() );
        assertEquals( 27, entries.get(4).getIndex() );        
    }

    @Test
    public void testSpaceBeforeParenOpen()
    {
        List<StringEntry> entries = SimpleWordTokenizer.tokenize( "public void withApp3 (App app)" );
        assertEquals( 5, entries.size() );

        assertEquals( "public", entries.get(0).toString() );
        assertEquals( 0, entries.get(0).getIndex() );

        assertEquals( "void", entries.get(1).toString() );
        assertEquals( 7, entries.get(1).getIndex() );

        assertEquals( "withApp3", entries.get(2).toString() );
        assertEquals( 12, entries.get(2).getIndex() );

        assertEquals( "App", entries.get(3).toString() );
        assertEquals( 22, entries.get(3).getIndex() );

        assertEquals( "app", entries.get(4).toString() );
        assertEquals( 26, entries.get(4).getIndex() );        
    }

    @Test
    public void testSpaceAfterParenOpen()
    {
        List<StringEntry> entries = SimpleWordTokenizer.tokenize( "public void withApp4( App app)" );
        assertEquals( 5, entries.size() );

        assertEquals( "public", entries.get(0).toString() );
        assertEquals( 0, entries.get(0).getIndex() );

        assertEquals( "void", entries.get(1).toString() );
        assertEquals( 7, entries.get(1).getIndex() );

        assertEquals( "withApp4", entries.get(2).toString() );
        assertEquals( 12, entries.get(2).getIndex() );

        assertEquals( "App", entries.get(3).toString() );
        assertEquals( 22, entries.get(3).getIndex() );

        assertEquals( "app", entries.get(4).toString() );
        assertEquals( 26, entries.get(4).getIndex() );         
    }
    
    @Test
    public void testWithIndent()
    {
        List<StringEntry> entries = SimpleWordTokenizer.tokenize( "    public void withApp3 (App app)", "App" );
        assertEquals( 1, entries.size() );
        
        assertEquals( 26, entries.get(0).getIndex() );
    }
}