package org.apache.maven.jxr;

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

import junit.framework.TestCase;
import org.apache.maven.jxr.pacman.PackageManager;
import org.apache.maven.jxr.pacman.FileManager;

import java.io.File;
import java.util.Locale;

/**
 * JUnit test for {@link JavaCodeTransform}.
 */
public class JavaCodeTransformTest
    extends TestCase
{
    /** JavaCodeTransform object under test */
    private JavaCodeTransform codeTransform;

    /***/
    private PackageManager packageManager;

    /**
     * Set up this test.
     */
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        packageManager = new PackageManager( new DummyLog(), new FileManager() );
        codeTransform = new JavaCodeTransform( packageManager );
    }

    /**
     * Test basic transformation of a java source file.
     */
    public void testTransform()
        //test transforms its own sourcefile, so add some comments
        throws Exception // single line despite /*
    {
        File sourceFile = new File( System.getProperty( "user.dir" )
            + "/src/test/java/org/apache/maven/jxr/JavaCodeTransformTest.java" );
        assertTrue( /* mid-line comment */ sourceFile.exists() ); /*

        multiline comment text

        */ codeTransform.transform( sourceFile.getAbsolutePath(), System.getProperty( "user.dir" ) // additional comment
            + "/target/JavaCodeTransformTest.html", Locale.ENGLISH, "ISO-8859-1", "ISO-8859-1", "", "" );
        assertTrue( /**/ new File( System.getProperty( "user.dir" ), "/target/JavaCodeTransformTest.html" ).exists() );
    }

    /**
     * Test what happens with an empty sourcefile.
     */
    public void testTransformWithEmptyClassFile()
        throws Exception
    {
        File sourceFile = new File( System.getProperty( "user.dir" ) + "/src/test/resources/EmptyClass.java" );
        assertTrue( sourceFile.exists() );

        codeTransform.transform( sourceFile.getAbsolutePath(), System.getProperty( "user.dir" )
            + "/target/EmptyClass.html", Locale.ENGLISH, "ISO-8859-1", "ISO-8859-1", "", "" );
        assertTrue( new File( System.getProperty( "user.dir" ), "/target/EmptyClass.html" ).exists() );
    }

}
