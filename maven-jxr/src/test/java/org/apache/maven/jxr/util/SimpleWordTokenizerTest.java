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
package org.apache.maven.jxr.util;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleWordTokenizerTest {
    static Stream<Arguments> tokenShapes() {
        return Stream.of(
                Arguments.of("public void withApp1(App app)", 21, 25),
                Arguments.of("public void withApp2 ( App app)", 23, 27),
                Arguments.of("public void withApp3 (App app)", 22, 26),
                Arguments.of("public void withApp4( App app)", 22, 26));
    }

    @ParameterizedTest
    @MethodSource("tokenShapes")
    void tokenShape(String source, int appIndex, int appArgumentIndex) {
        List<StringEntry> entries = SimpleWordTokenizer.tokenize(source);
        assertEquals(5, entries.size());

        assertEquals("public", entries.get(0).toString());
        assertEquals(0, entries.get(0).getIndex());

        assertEquals("void", entries.get(1).toString());
        assertEquals(7, entries.get(1).getIndex());

        assertEquals(source.substring(12, 20), entries.get(2).toString());
        assertEquals(12, entries.get(2).getIndex());

        assertEquals("App", entries.get(3).toString());
        assertEquals(appIndex, entries.get(3).getIndex());

        assertEquals("app", entries.get(4).toString());
        assertEquals(appArgumentIndex, entries.get(4).getIndex());
    }

    @Test
    void withIndent() {
        List<StringEntry> entries = SimpleWordTokenizer.tokenize("    public void withApp3 (App app)", "App");
        assertEquals(1, entries.size());

        assertEquals(26, entries.get(0).getIndex());
    }
}
