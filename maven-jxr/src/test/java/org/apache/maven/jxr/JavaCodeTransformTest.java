package org.apache.maven.jxr;

import static org.junit.Assert.assertTrue;

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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for {@link JavaCodeTransform}.
 */
public class JavaCodeTransformTest
{
    /** JavaCodeTransform object under test */
    private JavaCodeTransform codeTransform;

    /**
     * Set up this test.
     */
    @Before
    public void setUp() throws Exception
    {
        FileManager fileManager = new FileManager();
        codeTransform = new JavaCodeTransform( new PackageManager( fileManager ), fileManager);
    }

    /**
     * Test basic transformation of a java source file.
     */
    @Test
    public void testTransform()
        //test transforms its own sourcefile, so add some comments
        throws Exception // single line despite /*
    {
        Path sourceFile = Paths.get( "src/test/java/org/apache/maven/jxr/JavaCodeTransformTest.java" );
        assertTrue( /* mid-line comment */ Files.exists( sourceFile ) ); /*

        multiline comment text

        */ codeTransform.transform( sourceFile, Paths.get( "target/JavaCodeTransformTest.html" ) // additional comment
           , Locale.ENGLISH, "ISO-8859-1", "ISO-8859-1", Paths.get( "." ), "", "" );
        assertTrue( /**/ Files.exists( Paths.get( "target/JavaCodeTransformTest.html" ) ) );
    }

    /**
     * Test what happens with an empty sourcefile.
     */
    @Test
    public void testTransformWithEmptyClassFile()
        throws Exception
    {
        Path sourceFile = Paths.get( "src/test/resources/EmptyClass.java" );
        assertTrue( Files.exists( sourceFile ) );

        codeTransform.transform( sourceFile, Paths.get( "target/EmptyClass.html" )
            , Locale.ENGLISH, "ISO-8859-1", "ISO-8859-1", Paths.get( "." ), "", "" );
        assertTrue( Files.exists( Paths.get( "target/EmptyClass.html" ) ) );
    }

    /**
     * Test proper handling of link
     */
    @Test
    public void testLinkHandling()
        throws Exception
    {
        Path sourceFile = Paths.get( "src/test/resources/ClassWithLink.java" );
        assertTrue( Files.exists( sourceFile ) );

        codeTransform.transform( sourceFile, Paths.get( "target/ClassWithLink.html" )
            , Locale.ENGLISH, "ISO-8859-1", "ISO-8859-1", Paths.get( "." ), "", "" );
        assertTrue( Files.exists( Paths.get( "target/ClassWithLink.html" ) ) );

        byte[] bytes = Files.readAllBytes( Paths.get( "target/ClassWithLink.html" ) );
        String content = new String( bytes, StandardCharsets.ISO_8859_1 );
        // The proper link in its full length
        assertTrue( content.contains(
            "<a href=\"http://www.apache.org/licenses/LICENSE-2.0\" " +
            "target=\"alexandria_uri\">http://www.apache.org/licenses/LICENSE-2.0</a></em>" ) );
        // ...and the same link with https protocol
        assertTrue( content.contains(
            "<a href=\"https://www.apache.org/licenses/LICENSE-2.0\" " +
            "target=\"alexandria_uri\">https://www.apache.org/licenses/LICENSE-2.0</a></em>" ) );

    }
}
