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

import java.io.File;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class JxrTestReportTest extends AbstractJxrTestCase {
    /**
     * Method to test when the source dir is the test source dir
     *
     * @throws Exception
     */
    public void testSourceDir() throws Exception {
        generateReport(getGoal(), "testsourcedir-test/testsourcedir-test-plugin-config.xml");

        File xrefTestDir = new File(getBasedir(), "target/test/unit/testsourcedir-test/target/site/xref-test");

        // check if the jxr docs were generated
        assertTrue(new File(xrefTestDir, "testsourcedir/test/AppSampleTest.html").exists());
        assertTrue(new File(xrefTestDir, "testsourcedir/test/AppTest.html").exists());
        assertTrue(new File(xrefTestDir, "testsourcedir/test/package-frame.html").exists());
        assertTrue(new File(xrefTestDir, "testsourcedir/test/package-summary.html").exists());
        assertTrue(new File(xrefTestDir, "allclasses-frame.html").exists());
        assertTrue(new File(xrefTestDir, "index.html").exists());
        assertTrue(new File(xrefTestDir, "overview-frame.html").exists());
        assertTrue(new File(xrefTestDir, "overview-summary.html").exists());
        assertTrue(new File(xrefTestDir, "stylesheet.css").exists());

        // check if there's a link to the javadoc files
        String str = readFile(xrefTestDir, "testsourcedir/test/AppSampleTest.html");
        assertFalse(str.toLowerCase().contains("/apidocs/testsourcedir/test/AppSample.html\"".toLowerCase()));

        str = readFile(xrefTestDir, "testsourcedir/test/AppTest.html");
        assertFalse(str.toLowerCase().contains("/apidocs/testsourcedir/test/App.html\"".toLowerCase()));
    }

    @Override
    protected String getGoal() {
        return "test-jxr";
    }
}
