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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.jxr.JXR;
import org.apache.maven.jxr.JavaCodeTransform;
import org.apache.maven.jxr.JxrException;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.languages.java.version.JavaVersion;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Base class for the JXR reports.
 *
 * @author <a href="mailto:bellingard.NO-SPAM@gmail.com">Fabrice Bellingard</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public abstract class AbstractJxrReport extends AbstractMavenReport {

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * Title of window of the Xref HTML files.
     */
    @Parameter(defaultValue = "${project.name} ${project.version} Reference")
    private String windowTitle;

    /**
     * Title of main page of the Xref HTML files.
     */
    @Parameter(defaultValue = "${project.name} ${project.version} Reference")
    private String docTitle;

    // CHECKSTYLE_OFF: LineLength
    /**
     * String used at the bottom of the Xref HTML files.
     */
    @Parameter(property = "bottom", defaultValue = "\u00A9 {inceptionYear}\u2013{currentYear} {organizationName}")
    private String bottom;

    // CHECKSTYLE_ON: LineLength

    /**
     * Directory where Velocity templates can be found to generate overviews, frames and summaries. Should not be used.
     * If used, should be an absolute path, like <code>{@literal "${basedir}/myTemplates"}</code>.
     */
    @Parameter
    private String templateDir;

    /**
     * Style sheet used for the Xref HTML files. Should not be used. If used, should be an absolute path, like
     * <code>{@literal "${basedir}/myStyles.css"}</code>.
     */
    @Parameter
    private String stylesheet;

    /**
     * A list of exclude patterns to use. By default no files are excluded.
     *
     * @since 2.1
     */
    @Parameter
    private ArrayList<String> excludes;

    /**
     * A list of include patterns to use. By default all .java files are included.
     *
     * @since 2.1
     */
    @Parameter
    private ArrayList<String> includes;

    /**
     * Whether to skip this execution.
     *
     * @since 2.3
     */
    @Parameter(property = "maven.jxr.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * Link the Javadoc from the Source XRef. Defaults to true and will link automatically if javadoc plugin is being
     * used.
     */
    @Parameter(defaultValue = "true")
    private boolean linkJavadoc;

    /**
     * Version of the Javadoc templates to use.
     * The value should reflect `java.specification.version`, "1.4", "1.8", "9", "10",
     * by default this system property is used.
     */
    @Parameter(property = "javadocVersion")
    private String javadocVersion;

    /**
     * Version of the Javadoc templates to use.
     */
    private JavaVersion javadocTemplatesVersion;

    /**
     * Compiles the list of directories which contain source files that will be included in the JXR report generation.
     *
     * @param sourceDirs the List of the source directories
     * @return a List of the directories that will be included in the JXR report generation
     */
    protected List<String> pruneSourceDirs(List<String> sourceDirs) {
        List<String> pruned = new ArrayList<>(sourceDirs.size());
        for (String dir : sourceDirs) {
            if (!pruned.contains(dir) && hasSources(new File(dir))) {
                pruned.add(dir);
            }
        }
        return pruned;
    }

    /**
     * Initialize some attributes required during the report generation
     */
    protected void init() {
        // wanna know if Javadoc is being generated
        // TODO: what if it is not part of the site though, and just on the command line?
        if (project.getModel().getReporting() != null) {
            for (ReportPlugin reportPlugin : Collections.unmodifiableList(
                    project.getModel().getReporting().getPlugins())) {
                if ("maven-javadoc-plugin".equals(reportPlugin.getArtifactId())) {
                    break;
                }
            }
        }
    }

    /**
     * Checks whether the given directory contains Java files.
     *
     * @param dir the source directory
     * @return true if the directory or one of its subdirectories contains at least 1 Java file
     */
    private boolean hasSources(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            for (File currentFile : dir.listFiles()) {
                if (currentFile.isFile()) {
                    if (currentFile.getName().endsWith(".java")) {
                        return true;
                    }
                } else {
                    if (Character.isJavaIdentifierStart(currentFile.getName().charAt(0)) // avoid .svn directory
                            && hasSources(currentFile)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates the Xref for the Java files found in the given source directory and puts them in the given output
     * directory.
     *
     * @param locale The user locale to use for the Xref generation
     * @param outputDirectory The output directory
     * @param sourceDirs The source directories
     * @throws java.io.IOException
     * @throws org.apache.maven.jxr.JxrException
     */
    private void createXref(Locale locale, File outputDirectory, List<String> sourceDirs)
            throws IOException, JxrException {
        FileManager fileManager = new FileManager();
        PackageManager packageManager = new PackageManager(fileManager);
        JavaCodeTransform codeTransform = new JavaCodeTransform(packageManager, fileManager);

        JXR jxr = new JXR(packageManager, codeTransform);
        jxr.setDest(outputDirectory.toPath());
        jxr.setInputEncoding(getInputEncoding());
        jxr.setLocale(locale);
        jxr.setOutputEncoding(getOutputEncoding());
        jxr.setRevision("HEAD");
        jxr.setJavadocLinkDir(constructJavadocLocation());
        // Set include/exclude patterns on the jxr instance
        if (excludes != null && !excludes.isEmpty()) {
            jxr.setExcludes(excludes.toArray(new String[0]));
        }
        if (includes != null && !includes.isEmpty()) {
            jxr.setIncludes(includes.toArray(new String[0]));
        }

        // avoid winding up using Velocity in two class loaders.
        ClassLoader savedTccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            jxr.xref(sourceDirs, getTemplateDir(), windowTitle, docTitle, getBottomText());
        } finally {
            Thread.currentThread().setContextClassLoader(savedTccl);
        }

        // and finally copy the stylesheet
        copyRequiredResources(outputDirectory);
    }

    /**
     * Returns the bottom text to be displayed at the lower part of the generated JXR report.
     */
    private String getBottomText() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String year = String.valueOf(currentYear);

        String inceptionYear = project.getInceptionYear();

        String theBottom = this.bottom == null || this.bottom.isEmpty() || year == null ? this.bottom : this.bottom.replace("{currentYear}", year);

        if (inceptionYear != null) {
            if (inceptionYear.equals(year)) {
                theBottom = theBottom == null || theBottom.isEmpty() ? theBottom : theBottom.replace("{inceptionYear}\u2013", "");
            } else {
                theBottom = theBottom == null || theBottom.isEmpty() || inceptionYear == null ? theBottom : theBottom.replace("{inceptionYear}", inceptionYear);
            }
        } else {
            theBottom = theBottom == null || theBottom.isEmpty() ? theBottom : theBottom.replace("{inceptionYear}\u2013", "");
        }

        if (project.getOrganization() == null) {
            theBottom = theBottom == null || theBottom.isEmpty() ? theBottom : theBottom.replace(" {organizationName}", "");
        } else {
            if (StringUtils.isNotEmpty(project.getOrganization().getName())) {
                if (StringUtils.isNotEmpty(project.getOrganization().getUrl())) {
                    // CHECKSTYLE_OFF: LineLength
                    theBottom = StringUtils.replace(
                            theBottom,
                            "{organizationName}",
                            "<a href=\"" + project.getOrganization().getUrl() + "\">"
                                    + project.getOrganization().getName() + "</a>");
                    // CHECKSTYLE_ON: LineLength
                } else {
                    theBottom = StringUtils.replace(
                            theBottom,
                            "{organizationName}",
                            project.getOrganization().getName());
                }
            } else {
                theBottom = theBottom == null || theBottom.isEmpty() ? theBottom : theBottom.replace(" {organizationName}", "");
            }
        }

        return theBottom;
    }

    /**
     * Copy some required resources (like the stylesheet) to the given target directory
     *
     * @param targetDirectory the directory to copy the resources to
     */
    private void copyRequiredResources(File targetDirectory) {
        if (stylesheet != null && !stylesheet.isEmpty()) {
            File stylesheetFile = new File(stylesheet);
            File targetStylesheetFile = new File(targetDirectory, "stylesheet.css");

            try {
                if (stylesheetFile.isAbsolute()) {
                    FileUtils.copyFile(stylesheetFile, targetStylesheetFile);
                } else {
                    URL stylesheetUrl = this.getClass().getClassLoader().getResource(stylesheet);
                    FileUtils.copyURLToFile(stylesheetUrl, targetStylesheetFile);
                }
            } catch (IOException e) {
                getLog().warn("An error occured while copying the stylesheet to the target directory", e);
            }
        } else {
            if (javadocTemplatesVersion.isAtLeast("1.8")) {
                copyResources(targetDirectory, "jdk8/", "stylesheet.css");
            } else if (javadocTemplatesVersion.isAtLeast("1.7")) {
                String[] jdk7Resources = {
                    "stylesheet.css",
                    "resources/background.gif",
                    "resources/tab.gif",
                    "resources/titlebar.gif",
                    "resources/titlebar_end.gif"
                };
                copyResources(targetDirectory, "jdk7/", jdk7Resources);
            } else if (javadocTemplatesVersion.isAtLeast("1.6")) {
                copyResources(targetDirectory, "jdk6/", "stylesheet.css");
            } else if (javadocTemplatesVersion.isAtLeast("1.4")) {
                copyResources(targetDirectory, "jdk4/", "stylesheet.css");
            } else {
                // Fallback to the original stylesheet
                copyResources(targetDirectory, "", "stylesheet.css");
            }
        }
    }

    /**
     * Copy styles and related resources to the given directory
     *
     * @param targetDirectory the target directory to copy the resources to
     * @param sourceDirectory resources subdirectory to copy from
     * @param files names of files to copy
     */
    private void copyResources(File targetDirectory, String sourceDirectory, String... files) {
        try {
            for (String file : files) {
                URL resourceUrl = this.getClass().getClassLoader().getResource(sourceDirectory + file);
                File targetResourceFile = new File(targetDirectory, file);
                FileUtils.copyURLToFile(resourceUrl, targetResourceFile);
            }
        } catch (IOException e) {
            getLog().warn("An error occured while copying the resource to the target directory", e);
        }
    }

    @Override
    protected MavenProject getProject() {
        return project;
    }

    protected MavenSession getSession() {
        return session;
    }

    protected List<MavenProject> getReactorProjects() {
        return reactorProjects;
    }

    protected MojoExecution getMojoExecution() {
        return mojoExecution;
    }

    /**
     * Returns the correct resource bundle according to the locale.
     *
     * @param locale the locale of the user
     * @return the bundle corresponding to the locale
     */
    protected ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("jxr-report", locale, this.getClass().getClassLoader());
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        // init some attributes -- TODO (javadoc)
        init();

        // determine version of templates to use
        setJavadocTemplatesVersion();

        try {
            createXref(locale, getPluginReportOutputDirectory(), constructSourceDirs());
        } catch (JxrException | IOException e) {
            throw new MavenReportException("Error while generating the HTML source code of the project.", e);
        }
    }

    /**
     * Determine the templateDir to use, given javadocTemplatesVersion
     *
     * @return
     */
    private String getTemplateDir() {
        // Check if overridden
        if (templateDir == null || templateDir.isEmpty()) {
            if (javadocTemplatesVersion.isAtLeast("1.8")) {
                return "templates/jdk8";
            } else if (javadocTemplatesVersion.isAtLeast("1.7")) {
                return "templates/jdk7";
            } else if (javadocTemplatesVersion.isAtLeast("1.4")) {
                return "templates/jdk4";
            } else {
                getLog().warn("Unsupported javadocVersion: " + javadocTemplatesVersion + ". Fallback to original");
                return "templates";
            }
        }
        // use value specified by user
        return templateDir;
    }

    /**
     * Sets a new value for {@code javadocTemplatesVersion}.
     */
    private void setJavadocTemplatesVersion() {
        JavaVersion javaVersion = JavaVersion.JAVA_SPECIFICATION_VERSION;

        if (javadocVersion != null && !javadocVersion.isEmpty()) {
            javadocTemplatesVersion = JavaVersion.parse(javadocVersion);
        } else {
            javadocTemplatesVersion = javaVersion;
        }
    }

    /**
     * Gets the list of the source directories to be included in the JXR report generation
     *
     * @return a List of the source directories whose contents will be included in the JXR report generation
     */
    protected List<String> constructSourceDirs() {
        List<String> sourceDirs = new ArrayList<>(getSourceRoots());
        if (isAggregate()) {
            for (MavenProject project : reactorProjects) {
                if ("java".equals(project.getArtifact().getArtifactHandler().getLanguage())) {
                    sourceDirs.addAll(getSourceRoots(project));
                }
            }
        }

        sourceDirs = pruneSourceDirs(sourceDirs);
        return sourceDirs;
    }

    @Override
    public boolean canGenerateReport() {
        if (skip) {
            return false;
        }

        if (constructSourceDirs().isEmpty()) {
            return false;
        }

        if (isAggregate() && !project.isExecutionRoot()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isExternalReport() {
        return true;
    }

    /**
     * @return a String that contains the location of the javadocs
     */
    private Path constructJavadocLocation() throws IOException {
        Path location = null;
        if (linkJavadoc) {
            // We don't need to do the whole translation thing like normal, because JXR does it internally.
            // It probably shouldn't.
            if (getJavadocLocation().exists()) {
                // XRef was already generated by manual execution of a lifecycle binding
                location = getJavadocLocation().toPath().toAbsolutePath();
            } else {
                // Not yet generated - check if the report is on its way

                // Special case: using the site:stage goal
                String stagingDirectory = System.getProperty("stagingDirectory");

                if (stagingDirectory != null && !stagingDirectory.isEmpty()) {
                    String javadocOutputDir = getJavadocLocation().getName();
                    boolean javadocAggregate = JxrReportUtil.isJavadocAggregated(project);
                    String structureProject = JxrReportUtil.getStructure(project, false);

                    if (isAggregate() && javadocAggregate) {
                        location = Paths.get(stagingDirectory, structureProject, javadocOutputDir);
                    }
                    if (!isAggregate() && javadocAggregate) {
                        location = Paths.get(stagingDirectory, javadocOutputDir);

                        String hierarchy = project.getName();

                        MavenProject parent = project.getParent();
                        while (parent != null) {
                            hierarchy = parent.getName();
                            parent = parent.getParent();
                        }
                        location = Paths.get(stagingDirectory, hierarchy, javadocOutputDir);
                    }
                    if (isAggregate() && !javadocAggregate) {
                        getLog().warn("The JXR plugin is configured to build an aggregated report at the root, "
                                + "not the Javadoc plugin.");
                    }
                    if (!isAggregate() && !javadocAggregate) {
                        location = Paths.get(stagingDirectory, structureProject, javadocOutputDir);
                    }
                } else {
                    location = getJavadocLocation().toPath();
                }
            }

            if (location == null) {
                getLog().warn("Unable to locate Javadoc to link to - DISABLED");
            }
        }

        return location;
    }

    /**
     * Abstract method that returns the plugin report output directory where the generated JXR report will be put
     * beneath {@link #getReportOutputDirectory()}.
     *
     * @return a File for the plugin's report output directory
     */
    protected abstract File getPluginReportOutputDirectory();

    /**
     * Abstract method that returns the specified source directories that will be included in the JXR report generation.
     *
     * @return a List of the source directories
     */
    protected abstract List<String> getSourceRoots();

    /**
     * Abstract method that returns the compile source directories of the specified project that will be included in the
     * JXR report generation
     *
     * @param project the MavenProject where the JXR report plugin will be executed
     * @return a List of the source directories
     */
    protected abstract List<String> getSourceRoots(MavenProject project);

    /**
     * Abstract method that returns the location where (Test) Javadoc is generated for this project.
     *
     * @return a File for the location of (test) javadoc
     */
    protected abstract File getJavadocLocation();

    /**
     * Is the current report aggregated?
     *
     * @return true if aggregate, false otherwise
     */
    protected boolean isAggregate() {
        return false;
    }
}
