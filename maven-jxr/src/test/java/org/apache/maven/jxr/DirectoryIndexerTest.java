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

import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DirectoryIndexerTest {
    /**
     * Parse the files in test/resources/jxr68 packages, ensure all are present in the allClasses Map,
     * in the correct order.
     *
     * @throws Exception
     */
    @Test
    public void testJXR_68() throws Exception
    {
        FileManager fileManager = FileManager.getInstance();
        PackageManager packageManager = new PackageManager( new DummyLog(), fileManager );
        packageManager.process(Paths.get( "src/test/resources/jxr68" ));
        DirectoryIndexer directoryIndexer = new DirectoryIndexer( packageManager, "" );

        final Map<String, Map<String, ?>> packageInfo = directoryIndexer.getPackageInfo();
        final Map<String, ?> allPackages = packageInfo.get( "allPackages" );
        assertEquals(3, allPackages.size());
        assertTrue( allPackages.containsKey( "(default package)" ) );
        assertTrue( allPackages.containsKey( "pkga" ) );
        assertTrue( allPackages.containsKey( "pkgb" ) );
        final Map<String, Map<String, String>> allClasses = (Map<String, Map<String, String>>) packageInfo.get( "allClasses" );
        assertEquals( 6, allClasses.size() );
        final Iterator<Map<String, String>> iterator = allClasses.values().iterator();
        // #1: AClass
        assertEquals( "AClass", iterator.next().get( "name" ) );
        // #2: BClass
        assertEquals( "BClass", iterator.next().get( "name" ) );
        // #3: CClass
        assertEquals( "CClass", iterator.next().get( "name" ) );
        // #4: SomeClass in default package
        Map<String, String> classInfo = iterator.next();
        assertEquals( "SomeClass", classInfo.get( "name" ) );
        assertEquals( ".", classInfo.get( "dir" ) );
        // #5: SomeClass in "pkga"
        classInfo = iterator.next();
        assertEquals( "SomeClass", classInfo.get( "name" ) );
        assertEquals( "pkga", classInfo.get( "dir" ) );
        // #6: SomeClass in "pkgb"
        classInfo = iterator.next();
        assertEquals( "SomeClass", classInfo.get( "name" ) );
        assertEquals( "pkgb", classInfo.get( "dir" ) );
    }

}