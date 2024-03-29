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
package org.apache.maven.plugin.jxr.stubs;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class AggregateSubmodule2MavenProjectStub extends MavenProjectStub {
    private List<ReportPlugin> reportPlugins = new ArrayList<>();

    public AggregateSubmodule2MavenProjectStub() {
        setArtifactId("aggregate-test-submodule2");
        setGroupId("aggregate.test");
        setVersion("1.0-SNAPSHOT");
        setPackaging("jar");
        setInceptionYear("2006");

        String basedir = getBasedir().getAbsolutePath();
        List<String> compileSourceRoots = new ArrayList<>();
        compileSourceRoots.add(
                basedir + "/src/test/resources/unit/aggregate-test/submodule2/aggregate/test/submodule2");
        setCompileSourceRoots(compileSourceRoots);

        // set the report plugins
        reportPlugins = new ArrayList<>();

        Artifact artifact = new JxrPluginArtifactStub(getGroupId(), getArtifactId(), getVersion(), getPackaging());
        artifact.setArtifactHandler(new DefaultArtifactHandlerStub());
        setArtifact(artifact);
    }

    @Override
    public List<ReportPlugin> getReportPlugins() {
        return reportPlugins;
    }
}
