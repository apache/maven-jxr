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

import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class JavaFileImplTest {
    @Test
    public void testJXR_135() throws Exception {
        JavaFileImpl javaFile = new JavaFileImpl( Paths.get( "src/test/java/org/apache/maven/jxr/ClassWithNested.java" ), "UTF-8" );
        final List<ClassType> classTypes = javaFile.getClassTypes();
        assertEquals( "ClassWithNested", classTypes.get(0).getName() );
        assertEquals( "ClassWithNested.NestedInterface", classTypes.get(1).getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum", classTypes.get(2).getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum.NestedEnum", classTypes.get(3).getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum.NestedClass2", classTypes.get(4).getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum2", classTypes.get(5).getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum2.NestedEnum", classTypes.get(6).getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum2.NestedClass2", classTypes.get(7).getName() );
        assertEquals( "NotNested", classTypes.get(8).getName() );
    }

}