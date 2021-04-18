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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class JavaFileImplTest {
    @Test
    public void testJXR_135_lotsOfNested() throws IOException
    {
        JavaFileImpl javaFile =
            new JavaFileImpl( Paths.get( "src/test/resources/jxr135/org/apache/maven/jxr/pacman/ClassWithNested.java" ),
                              StandardCharsets.UTF_8 );
        final Iterator<ClassType> classTypes = javaFile.getClassTypes().iterator();
        assertEquals( "ClassWithNested", classTypes.next().getName() );
        assertEquals( "ClassWithNested.NestedInterface", classTypes.next().getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum", classTypes.next().getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum.NestedEnum", classTypes.next().getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum.NestedClass2", classTypes.next().getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum2", classTypes.next().getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum2.NestedEnum", classTypes.next().getName() );
        assertEquals( "ClassWithNested.NestedClassWithEnum2.NestedClass2", classTypes.next().getName() );
        assertEquals( "NotNested", classTypes.next().getName() );
    }

}