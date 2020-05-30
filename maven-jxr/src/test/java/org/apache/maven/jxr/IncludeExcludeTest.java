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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.Before;
import org.junit.Test;

/**
 * Test include/exclude patterns.
 *
 * @author <a href="mailto:dennisl@apache.org">Dennis Lundberg</a>
 * @version $Id$
 */
public class IncludeExcludeTest  
{
    private JXR jxr;
 
    @Before
    public void setUp() throws Exception
    {
        FileManager fileManager = new FileManager();
        PackageManager packageManager = new PackageManager( fileManager );
        JavaCodeTransform codeTransform = new JavaCodeTransform( packageManager, fileManager );
        
        jxr = new JXR( packageManager, codeTransform );
        jxr.setDest( Paths.get( "target" ) );
        jxr.setInputEncoding( "ISO-8859-1" );
        jxr.setOutputEncoding( "ISO-8859-1" );
        jxr.setJavadocLinkDir( Paths.get( "." ) );
    }

    @Test
    public void testIncludeExclude()
        throws Exception
    {
        String[] excludes = {"**/exclude/ExcludedClass.java"};
        jxr.setExcludes( excludes );
        String[] includes = {"**/exclude/*.java", "**/include/IncludedClass.java"};
        jxr.setIncludes( includes );
        jxr.xref( Collections.singletonList( "src/test/resources" ), "templates/jdk4",
                  "title", "title", "copyright" );
        Path excludedFile = Paths.get( "target/exclude/ExcludedClass.html" );
        assertFalse( Files.exists( excludedFile ) );
        Path includedFile = Paths.get( "target/include/IncludedClass.html" );
        assertTrue( Files.exists( includedFile ) );
        Path notIncludedFile = Paths.get( "target/include/NotIncludedClass.html" );
        assertFalse( Files.exists( notIncludedFile ) );
    }
}
