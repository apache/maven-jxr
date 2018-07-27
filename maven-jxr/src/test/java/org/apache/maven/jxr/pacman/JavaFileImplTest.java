package org.apache.maven.jxr.pacman;

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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class JavaFileImplTest
{
    @Test
    public void testNamesForInnerClasses()
        throws IOException
    {
        Path testFile = Paths.get( "src/test/java/org/apache/maven/jxr/pacman/FileWithInnerClassesTest.java" );
        JavaFile javaFile = new JavaFileImpl(testFile, "ISO-8859-1" );

        List<ClassType> classTypes = javaFile.getClassTypes();
        assertEquals(4, classTypes.size());
        assertEquals("FileWithInnerClassesTest", classTypes.get(0).getName());
        assertEquals("FileWithInnerClassesTest.Inner1", classTypes.get(1).getName());
        // next one ideally should be "FileWithInnerClassesTest.Inner1.Inner2" (but only one level is supported so far)
        assertEquals("FileWithInnerClassesTest.Inner2", classTypes.get(2).getName());
        assertEquals("FileWithInnerClassesTest.Inner3", classTypes.get(3).getName());
    }
}