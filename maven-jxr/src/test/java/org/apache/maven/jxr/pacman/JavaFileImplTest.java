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
package org.apache.maven.jxr.pacman;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaFileImplTest {
    @Test
    void jxr_135LotsOfNested() throws IOException {
        JavaFileImpl javaFile = new JavaFileImpl(
                Paths.get("src/test/resources/jxr135/org/apache/maven/jxr/pacman/ClassWithNested.java"), "UTF-8");
        final Iterator<ClassType> classTypes = javaFile.getClassTypes().iterator();
        assertEquals("ClassWithNested", classTypes.next().getName());
        assertEquals("ClassWithNested.NestedInterface", classTypes.next().getName());
        assertEquals("ClassWithNested.NestedClassWithEnum", classTypes.next().getName());
        assertEquals(
                "ClassWithNested.NestedClassWithEnum.NestedEnum",
                classTypes.next().getName());
        assertEquals(
                "ClassWithNested.NestedClassWithEnum.NestedClass2",
                classTypes.next().getName());
        assertEquals("ClassWithNested.NestedClassWithEnum2", classTypes.next().getName());
        assertEquals(
                "ClassWithNested.NestedClassWithEnum2.NestedEnum",
                classTypes.next().getName());
        assertEquals(
                "ClassWithNested.NestedClassWithEnum2.NestedClass2",
                classTypes.next().getName());
        assertEquals("NotNested", classTypes.next().getName());
    }

    @Test
    void jxr_170MultiLineString() throws IOException {
        JavaFileImpl javaFile = new JavaFileImpl(
                Paths.get("src/test/resources/jxr170/org/apache/maven/jxr/pacman/ClassWithMultiLineString.java"),
                "UTF-8");
        assertEquals(1, javaFile.getClassTypes().size());
        assertEquals("ClassWithMultiLineString", javaFile.getClassTypes().get(0).getName());
        assertEquals("[ImportType[name=java.lang.*]]", javaFile.getImportTypes().toString());
    }

    @Test
    void jxr_175Java14Record() throws IOException {
        JavaFileImpl javaFile = new JavaFileImpl(
                Paths.get("src/test/resources/jxr175/org/apache/maven/jxr/pacman/Java14Record.java"), "UTF-8");
        assertEquals(1, javaFile.getClassTypes().size());
        assertEquals("Java14Record", javaFile.getClassTypes().get(0).getName());
    }
}
