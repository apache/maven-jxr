package org.apache.maven.jxr.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleWordTokenizerTest
{
    @Test
    public void testCompact()
    {
        StringEntry[] entries = SimpleWordTokenizer.tokenize( "public void withApp1(App app)" );
        assertEquals( 5, entries.length );

        assertEquals( "public", entries[0].toString() );
        assertEquals( 0, entries[0].getIndex() );

        assertEquals( "void", entries[1].toString() );
        assertEquals( 7, entries[1].getIndex() );

        assertEquals( "withApp1", entries[2].toString() );
        assertEquals( 12, entries[2].getIndex() );

        assertEquals( "App", entries[3].toString() );
        assertEquals( 21, entries[3].getIndex() );

        assertEquals( "app", entries[4].toString() );
        assertEquals( 25, entries[4].getIndex() );
    }

    @Test
    public void testSpacesAroundParenOpen()
    {
        StringEntry[] entries = SimpleWordTokenizer.tokenize( "public void withApp2 ( App app)" );
        assertEquals( 5, entries.length );

        assertEquals( "public", entries[0].toString() );
        assertEquals( 0, entries[0].getIndex() );

        assertEquals( "void", entries[1].toString() );
        assertEquals( 7, entries[1].getIndex() );

        assertEquals( "withApp2", entries[2].toString() );
        assertEquals( 12, entries[2].getIndex() );

        assertEquals( "App", entries[3].toString() );
        assertEquals( 23, entries[3].getIndex() );

        assertEquals( "app", entries[4].toString() );
        assertEquals( 27, entries[4].getIndex() );
    }

    @Test
    public void testSpaceBeforeParenOpen()
    {
        StringEntry[] entries = SimpleWordTokenizer.tokenize( "public void withApp3 (App app)" );
        assertEquals( 5, entries.length );

        assertEquals( "public", entries[0].toString() );
        assertEquals( 0, entries[0].getIndex() );

        assertEquals( "void", entries[1].toString() );
        assertEquals( 7, entries[1].getIndex() );

        assertEquals( "withApp3", entries[2].toString() );
        assertEquals( 12, entries[2].getIndex() );

        assertEquals( "App", entries[3].toString() );
        assertEquals( 22, entries[3].getIndex() );

        assertEquals( "app", entries[4].toString() );
        assertEquals( 26, entries[4].getIndex() );
    }

    @Test
    public void testSpaceAfterParenOpen()
    {
        StringEntry[] entries = SimpleWordTokenizer.tokenize( "public void withApp4( App app)" );
        assertEquals( 5, entries.length );

        assertEquals( "public", entries[0].toString() );
        assertEquals( 0, entries[0].getIndex() );

        assertEquals( "void", entries[1].toString() );
        assertEquals( 7, entries[1].getIndex() );

        assertEquals( "withApp4", entries[2].toString() );
        assertEquals( 12, entries[2].getIndex() );

        assertEquals( "App", entries[3].toString() );
        assertEquals( 22, entries[3].getIndex() );

        assertEquals( "app", entries[4].toString() );
        assertEquals( 26, entries[4].getIndex() );
    }
}
