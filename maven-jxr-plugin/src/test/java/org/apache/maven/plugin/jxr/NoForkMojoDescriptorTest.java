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
package org.apache.maven.plugin.jxr;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoForkMojoDescriptorTest {

    @Test
    void aggregateNoForkGoalsDoNotForkLifecycle() throws Exception {
        InputStream descriptorStream = getClass().getClassLoader().getResourceAsStream("META-INF/maven/plugin.xml");
        assertNotNull(descriptorStream);

        try (InputStream input = descriptorStream) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document descriptor = factory.newDocumentBuilder().parse(input);

            assertNoForkPhase(descriptor, "aggregate-no-fork");
            assertNoForkPhase(descriptor, "test-aggregate-no-fork");
        }
    }

    private void assertNoForkPhase(Document descriptor, String expectedGoal) {
        NodeList mojos = descriptor.getElementsByTagName("mojo");
        boolean found = false;

        for (int index = 0; index < mojos.getLength(); index++) {
            Element mojo = (Element) mojos.item(index);
            String goal = mojo.getElementsByTagName("goal").item(0).getTextContent();
            if (expectedGoal.equals(goal)) {
                found = true;
                NodeList executePhase = mojo.getElementsByTagName("executePhase");
                String phase = executePhase.getLength() == 0
                        ? null
                        : executePhase.item(0).getTextContent();
                assertTrue(phase == null || "None".equals(phase), "Unexpected fork phase for " + expectedGoal);
            }
        }

        assertTrue(found, "Missing goal in plugin descriptor: " + expectedGoal);
    }
}
