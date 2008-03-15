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

import java.io.File;
import java.util.Collections;

/**
 * Test include/exclude patterns.
 *
 * @author <a href="mailto:dennisl@apache.org">Dennis Lundberg</a>
 * @version $Id$
 */
public class IncludeExcludeTest extends TestCase
{
    private JXR jxr;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        jxr = new JXR();
        jxr.setDest( System.getProperty( "basedir" ) + "/target" );
        jxr.setInputEncoding( "ISO-8859-1" );
        jxr.setOutputEncoding( "ISO-8859-1" );
        jxr.setJavadocLinkDir( "" );
        jxr.setLog( new DummyLog() );
    }

    public void testIncludeExclude()
        throws Exception
    {
        String[] excludes = {"**/exclude/ExcludedClass.java"};
        jxr.setExcludes( excludes );
        String[] includes = {"**/exclude/*.java", "**/include/IncludedClass.java"};
        jxr.setIncludes( includes );
        jxr.xref( Collections.singletonList( System.getProperty( "basedir" ) + "/src/test/resources" ), "templates",
                  "title", "title", "copyright" );
        File excludedFile = new File( System.getProperty( "basedir" ) + "/target/exclude/ExcludedClass.html" );
        assertFalse( excludedFile.exists() );
        File includedFile = new File( System.getProperty( "basedir" ) + "/target/include/IncludedClass.html" );
        assertTrue( includedFile.exists() );
        File notIncludedFile = new File( System.getProperty( "basedir" ) + "/target/include/NotIncludedClass.html" );
        assertFalse( notIncludedFile.exists() );
    }
}
