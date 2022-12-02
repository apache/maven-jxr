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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Creates an HTML-based, cross referenced version of Java source code
 * for a project.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard</a>
 */
@Mojo(name = "jxr")
@Execute(phase = LifecyclePhase.GENERATE_SOURCES)
public class JxrReport extends AbstractJxrReport {
    /**
     * Source directories of the project.
     */
    @Parameter(defaultValue = "${project.compileSourceRoots}", required = true, readonly = true)
    private List<String> sourceDirs;

    /**
     * Specifies the source path where the java files are located.
     * The paths are separated by '{@code ;}'.
     */
    @Parameter
    private String sourcePath;

    /**
     * Directory where Javadoc is generated for this project.
     */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/apidocs")
    private File javadocDir;

    @Override
    protected File getPluginReportOutputDirectory() {
        return new File(getReportOutputDirectory(), "xref");
    }

    @Override
    protected List<String> getSourceRoots() {
        if (sourcePath != null) {
            String[] sourcePathArray = sourcePath.split(";");
            if (sourcePathArray.length > 0) {
                return Arrays.asList(sourcePathArray);
            }
        }

        List<String> l = new ArrayList<>();

        if (!"pom".equals(getProject().getPackaging().toLowerCase(Locale.ENGLISH))) {
            l.addAll(sourceDirs);
        }

        if (getProject().getExecutionProject() != null) {
            if (!"pom".equals(getProject().getExecutionProject().getPackaging().toLowerCase(Locale.ENGLISH))) {
                l.addAll(getProject().getExecutionProject().getCompileSourceRoots());
            }
        }

        return l;
    }

    @Override
    protected List<String> getSourceRoots(MavenProject project) {
        List<String> l = new ArrayList<>();

        if (!"pom".equals(project.getPackaging().toLowerCase(Locale.ENGLISH))) {
            l.addAll(project.getCompileSourceRoots());
        }

        if (project.getExecutionProject() != null) {
            if (!"pom".equals(project.getExecutionProject().getPackaging().toLowerCase(Locale.ENGLISH))) {
                l.addAll(project.getExecutionProject().getCompileSourceRoots());
            }
        }

        return l;
    }

    @Override
    public String getDescription(Locale locale) {
        return getBundle(locale).getString("report.xref.main.description");
    }

    @Override
    public String getName(Locale locale) {
        return getBundle(locale).getString("report.xref.main.name");
    }

    @Override
    public String getOutputName() {
        return "xref/index";
    }

    @Override
    protected File getJavadocDir() {
        return javadocDir;
    }
}
