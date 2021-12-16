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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.jxr.DirectoryIndexer.ClassInfo;
import org.apache.maven.jxr.DirectoryIndexer.PackageInfo;
import org.apache.maven.jxr.DirectoryIndexer.ProjectInfo;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.Before;
import org.junit.Test;

public class DirectoryIndexerTest
{
    private DirectoryIndexer directoryIndexer;
    
    @Before
    public void setUp() 
    {
        FileManager fileManager = new FileManager();
        PackageManager packageManager = new PackageManager( fileManager );
        packageManager.process( Paths.get( "src/test/resources/jxr68" ) );

        directoryIndexer = new DirectoryIndexer( packageManager, "" );
    }
    
    /**
     * Parse the files in test/resources/jxr68 packages, ensure all are present in the allClasses Map,
     * in the correct order.
     */
    @Test
    public void testJXR_68()
    {

        ProjectInfo packageInfo = directoryIndexer.getProjectInfo();
        final Map<String, PackageInfo> allPackages = packageInfo.getAllPackages();
        assertEquals(3, allPackages.size());
        assertTrue( allPackages.containsKey( "(default package)" ) );
        assertTrue( allPackages.containsKey( "pkga" ) );
        assertTrue( allPackages.containsKey( "pkgb" ) );
        final Map<String, ClassInfo> allClasses = packageInfo.getAllClasses();
        assertEquals( 6, allClasses.size() );
        final Iterator<ClassInfo> iterator = allClasses.values().iterator();
        // #1: AClass
        assertEquals( "AClass", iterator.next().getName() );
        // #2: BClass
        assertEquals( "BClass", iterator.next().getName() );
        // #3: CClass
        assertEquals( "CClass", iterator.next().getName() );
        // #4: SomeClass in default package
        ClassInfo classInfo = iterator.next();
        assertEquals( "SomeClass", classInfo.getName() );
        assertEquals( ".", classInfo.getDir() );
        // #5: SomeClass in "pkga"
        classInfo = iterator.next();
        assertEquals( "SomeClass", classInfo.getName() );
        assertEquals( "pkga", classInfo.getDir() );
        // #6: SomeClass in "pkgb"
        classInfo = iterator.next();
        assertEquals( "SomeClass", classInfo.getName() );
        assertEquals( "pkgb", classInfo.getDir() );
    }

}