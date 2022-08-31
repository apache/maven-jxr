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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a small and fast word tokenizer. It has different characteristics from the normal Java tokenizer. It only
 * considers clear words that are only ended with spaces as strings. EX: "Flight" would be a word but "Flight()" would
 * not.
 */
public class SimpleWordTokenizer
{

    private static final Pattern NONBREAKERS = Pattern.compile( "([^()\\[ {}]+)" );

    private static final char[] BREAKERS = { '(', ')', '[', ' ', '{', '}' };

    /**
     * Breaks the given line into multiple tokens.
     *
     * @param line line to tokenize
     * @return list of tokens
     */
    public static List<StringEntry> tokenize( String line )
    {

        /*
         * determine where to start processing this String... this could either be the start of the line or just keep
         * going until the first
         */
        int start = getStart( line );

        // find the first non-BREAKER char and assume that is where you want to start

        if ( line == null || line.length() == 0 || start == -1 )
        {
            return Collections.emptyList();
        }

        return tokenize( line, start );
    }

    /**
     * Tokenize the given line but only return those tokens that match the parameter {@code find}.
     *
     * @param line line to search in
     * @param find String to match
     * @return list of matching tokens
     */
    public static List<StringEntry> tokenize( String line, String find )
    {

        List<StringEntry> foundTokens = new ArrayList<>();

        for ( StringEntry se : tokenize( line ) )
        {

            if ( se.toString().equals( find ) )
            {
                foundTokens.add( se );
            }

        }

        return foundTokens;
    }

    /**
     * Internal impl. Specify the start and end.
     */
    private static List<StringEntry> tokenize( String line, int start )
    {
        Matcher matcher = NONBREAKERS.matcher( line.substring( start ) );

        List<StringEntry> words = new ArrayList<>();

        while ( matcher.find() )
        {
            StringEntry entry = new StringEntry( matcher.group( 1 ), matcher.start() + start );
            words.add( entry );
        }

        return words;
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

        for ( char breaker : BREAKERS )
        {

            if ( breaker == c )
            {
                return true;
            }

        }

        return false;
    }

}
