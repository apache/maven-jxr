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

import java.io.IOException;

import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Site;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test {@link JxrReportUtil}.
 */
class JxrReportUtilTest {

    /**
     * The expectations are the values Wagon's {@code Repository} produced for the same URLs, with the exception of
     * the {@code dav:} one, see {@link #davUrlKeepsItsHost()}.
     */
    @Test
    void structureFollowsTheSiteUrl() throws IOException {
        assertStructure(
                "maven.apache.org/plugins/maven-jxr-plugin/", "https://maven.apache.org/plugins/maven-jxr-plugin/");
        assertStructure(
                "maven.apache.org/plugins/maven-jxr-plugin", "https://maven.apache.org/plugins/maven-jxr-plugin");
        assertStructure("maven.apache.org/", "https://maven.apache.org");
        assertStructure("maven.apache.org/plugins/x", "https://maven.apache.org:8080/plugins/x");
        assertStructure(
                "svn.apache.org/repos/asf/maven/website/components/x",
                "scm:svn:https://svn.apache.org/repos/asf/maven/website/components/x");
        assertStructure(
                "people.apache.org/www/maven.apache.org/plugins/x",
                "scp://user@people.apache.org/www/maven.apache.org/plugins/x");
        assertStructure("localhost/tmp/site", "file:///tmp/site");
    }

    /**
     * Wagon parsed {@code dav:https://maven.apache.org/plugins/x} into host {@code https} and base directory
     * {@code .apache.org/plugins/x}. The {@code dav:} prefix is now peeled off like the SCM one.
     */
    @Test
    void davUrlKeepsItsHost() throws IOException {
        assertEquals(
                "maven.apache.org/plugins/x",
                JxrReportUtil.getStructure(projectWithSiteUrl("dav:https://maven.apache.org/plugins/x"), false));
    }

    @Test
    void unparseableUrlIsReported() {
        assertThrows(
                IOException.class,
                () -> JxrReportUtil.getStructure(projectWithSiteUrl("https://maven.apache.org/a b"), false));
    }

    @Test
    void missingSiteUrlIsIgnoredWhenAsked() throws IOException {
        assertNull(JxrReportUtil.getStructure(projectWithSiteUrl(""), true));
    }

    @Test
    void missingSiteUrlIsReportedOtherwise() {
        assertThrows(IOException.class, () -> JxrReportUtil.getStructure(projectWithSiteUrl(""), false));
    }

    /**
     * Without distributionManagement the structure falls back to the project name hierarchy.
     */
    @Test
    void structureFallsBackToTheProjectHierarchy() throws IOException {
        Model model = new Model();
        model.setName("Child");
        MavenProject project = new MavenProject(model);

        Model parentModel = new Model();
        parentModel.setName("Parent");
        project.setParent(new MavenProject(parentModel));

        assertEquals("Parent/Child", JxrReportUtil.getStructure(project, false));
    }

    private static void assertStructure(String expected, String siteUrl) throws IOException {
        assertEquals(expected, JxrReportUtil.getStructure(projectWithSiteUrl(siteUrl), false), siteUrl);
    }

    private static MavenProject projectWithSiteUrl(String siteUrl) {
        Site site = new Site();
        site.setId("site");
        site.setUrl(siteUrl);

        DistributionManagement distributionManagement = new DistributionManagement();
        distributionManagement.setSite(site);

        Model model = new Model();
        model.setDistributionManagement(distributionManagement);

        return new MavenProject(model);
    }
}
