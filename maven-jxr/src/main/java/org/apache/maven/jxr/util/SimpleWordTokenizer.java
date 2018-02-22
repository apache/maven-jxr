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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * This is a small and fast word tokenizer. It has different characteristics from the normal Java tokenizer. It only
 * considers clear words that are only ended with spaces as strings. EX: "Flight" would be a word but "Flight()" would
 * not.
 */
public class SimpleWordTokenizer
{

    /**
     * Description of the Field
     */
    public static final char[] BREAKERS = { '(', ')', '[', ' ', '{', '}' };

    /**
     * Break the given line into multiple StringUtils
     */
    public static StringEntry[] tokenize( String line )
    {

        /*
         * determine where to start processing this String... this could either be the start of the line or just keep
         * going until the first
         */
        int start = getStart( line );

        // find the first non-BREAKER char and assume that is where you want to start

        if ( line == null || line.length() == 0 || start == -1 )
        {
            return new StringEntry[0];
        }

        return tokenize( line, start );
    }

    /**
     * Tokenize the given line but only return StringUtils that match the parameter find.
     *
     * @param line String to search in
     * @param find String to match.
     */
    public static StringEntry[] tokenize( String line, String find )
    {

        Vector<StringEntry> v = new Vector<StringEntry>();

        for ( StringEntry se : tokenize( line ) )
        {

            if ( se.toString().equals( find ) )
            {
                v.addElement( se );
            }

        }

        StringEntry[] found = new StringEntry[v.size()];
        Collections.sort( v );
        v.copyInto( found );
        return found;
    }

    /**
     * Internal impl. Specify the start and end.
     */
    private static StringEntry[] tokenize( String line, int start )
    {
        List<StringEntry> entries = new ArrayList<>();
        line = line.substring(start);

        for (char breaker : BREAKERS)
        {
            line = line.replace(breaker + "", " ");
        }

        String[] split = line.split("\\s+");
        entries.add(new StringEntry(split[0], line.indexOf(split[0],0)));

        for (int i = 1; i < split.length; i++)
        {
            int index = line.indexOf(split[i], line.indexOf(split[i - 1]) + split[i - 1].length());
            entries.add(new StringEntry(split[i], index));
        }

        return entries.toArray(new StringEntry[entries.size()]);
    }

    /**
     * Go through the entire String and if any character is not a Java identifier part (_, a, b, c, d, etc) then return
     * false.
     */
    private static boolean isWord( String string )
    {

        if ( string == null || string.length() == 0 )
        {

            return false;
        }

        for ( int i = 0; i < string.length(); ++i )
        {

            char c = string.charAt( i );

            if ( !Character.isJavaIdentifierPart( c ) && c != '.' )
            {
                return false;
            }

        }

        return true;
    }

    /**
     * Go through the list of BREAKERS and find the closes one.
     */
    private static int getNextBreak( String string, int start )
    {

        int breakPoint = -1;

        for ( int i = 0; i < BREAKERS.length; ++i )
        {

            int next = string.indexOf( BREAKERS[i], start );

            if ( breakPoint == -1 || next < breakPoint && next != -1 )
            {

                breakPoint = next;

            }

        }

        // if the breakPoint is still -1 go to the end of the string
        if ( breakPoint == -1 )
        {
            breakPoint = string.length();
        }

        return breakPoint;
    }

    /**
     * Go through the list of BREAKERS and find the closes one.
     */
    private static int getStart( String string )
    {

        for ( int i = 0; i < string.length(); ++i )
        {

            if ( !isBreaker( string.charAt( i ) ) )
            {
                return i;
            }

        }

        return -1;
    }

    /**
     * Return true if the given char is considered a breaker.
     */
    private static boolean isBreaker( char c )
    {

        for ( int i = 0; i < BREAKERS.length; ++i )
        {

            if ( BREAKERS[i] == c )
            {
                return true;
            }

        }

        return false;
    }

}
