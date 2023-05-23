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
package org.apache.maven.jxr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.jxr.pacman.ClassType;
import org.apache.maven.jxr.pacman.PackageManager;
import org.apache.maven.jxr.pacman.PackageType;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * This class creates the navigational pages for jxr's cross-referenced source
 * files. The navigation is inspired by javadoc, so it should have a familiar feel.
 *
 * Creates the following files:
 * <ul>
 * <li>{@code index.html}            main index containing the frameset</li>
 * <li>{@code overview-frame.html}   list of the project's packages              (top left)</li>
 * <li>{@code allclasses-frame.html} list of all classes in the project          (bottom left)</li>
 * <li>{@code overview-summary.html} top-level listing of the project's packages (main frame)</li>
 *
 * <li>
 * Package specific:
 * <ul>
 * <li>{@code package-summary.html} listing of all classes in this package    (main frame)</li>
 * <li>{@code package-frame.html}   listing of all classes in this package    (bottom left)</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author <a href="mailto:bellingard@gmail.com">Fabrice Bellingard </a>
 * @author <a href="mailto:brian@brainslug.org">Brian Leonard</a>
 */
public class DirectoryIndexer {
    /*
     * JavaCodeTransform uses this to cross-reference package references
     * with that package's main summary page.
     */
    static final String INDEX = "package-summary.html";

    /*
     * Path to the root output directory.
     */
    private String root;

    /*
     * Package Manager for this project.
     */
    private PackageManager packageManager;

    /*
     * see the getter/setter docs for these properties
     */
    private String outputEncoding;

    private String templateDir;

    private String windowTitle;

    private String docTitle;

    private String bottom;

    /**
     * Constructor for the DirectoryIndexer object
     *
     * @param packageManager PackageManager for this project
     * @param root Path of the root output directory
     */
    public DirectoryIndexer(PackageManager packageManager, String root) {
        this.packageManager = packageManager;
        this.root = root;
    }

    /**
     * OutputEncoding is the encoding of output files.
     *
     * @param outputEncoding output encoding
     */
    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    /**
     * Gets the output encoding.
     * @return output encoding
     */
    public String getOutputEncoding() {
        return outputEncoding;
    }

    /**
     * {@code templateDir} is the location of the jelly template files used
     * to generate the navigation pages.
     *
     * @param templateDir location of the template directory
     */
    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    /**
     * Gets the template directory.
     *
     * @return location of the template directory
     */
    public String getTemplateDir() {
        return templateDir;
    }

    /**
     * {@code windowTitle} is used in the output's &lt;title&gt; tags
     * see the javadoc documentation for the property of the same name
     *
     * @param windowTitle the &lt;title&gt; attribute
     */
    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    /**
     * Gets the window title.
     *
     * @return window title
     */
    public String getWindowTitle() {
        return windowTitle;
    }

    /**
     * {@code docTitle} is used as a page heading for the summary files.<br>
     * See the javadoc documentation for the property of the same name
     *
     * @param docTitle major page heading
     */
    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    /**
     * Gets the major page heading.
     *
     * @return major page heading
     */
    public String getDocTitle() {
        return docTitle;
    }

    /**
     * Bottom is a footer for the navigation pages, usually a copyright.<br>
     * See the javadoc documentation for the property of the same name.
     *
     * @param bottom page footer
     */
    public void setBottom(String bottom) {
        this.bottom = bottom;
    }

    /**
     * Gets the footer for the navigation pages.
     *
     * @return bottom page footer
     */
    public String getBottom() {
        return bottom;
    }

    /**
     * Does the actual indexing.
     *
     * @throws JxrException If something went wrong
     */
    public void process() throws JxrException {
        ProjectInfo info = getProjectInfo();

        VelocityEngine engine = new VelocityEngine();
        setProperties(engine);
        try {
            engine.init();
        } catch (Exception e) {
            throw new JxrException("Error initializing Velocity", e);
        }

        VelocityContext context = new VelocityContext();
        context.put("outputEncoding", getOutputEncoding());
        context.put("windowTitle", getWindowTitle());
        context.put("docTitle", getDocTitle());
        context.put("bottom", getBottom());
        context.put("info", info);

        doVelocity("index", root, context, engine);
        doVelocity("overview-frame", root, context, engine);
        doVelocity("allclasses-frame", root, context, engine);
        doVelocity("overview-summary", root, context, engine);

        for (PackageInfo pkgInfo : info.getAllPackages().values()) {
            VelocityContext subContext = new VelocityContext(context);
            subContext.put("pkgInfo", pkgInfo);

            String outDir = root + '/' + pkgInfo.getDir();
            doVelocity("package-summary", outDir, subContext, engine);
            doVelocity("package-frame", outDir, subContext, engine);
        }
    }

    /*
     * Sets Velocity properties to find templates.
     */
    private void setProperties(VelocityEngine engine) {
        Path templateDirFile = Paths.get(getTemplateDir());
        if (templateDirFile.isAbsolute()) {
            // the property has been overridden: need to use a FileResourceLoader
            engine.setProperty("resource.loader", "file");
            engine.setProperty(
                    "file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            engine.setProperty("file.resource.loader.path", templateDirFile.toString());
        } else {
            // use of the default templates
            engine.setProperty("resource.loader", "classpath");
            engine.setProperty(
                    "classpath.resource.loader.class",
                    "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        }
        // avoid "unable to find resource 'VM_global_library.vm' in any resource loader."
        engine.setProperty("velocimacro.library", "");
        //        engine.setProperty( Log.class.getName(), log );
        //        engine.setProperty( "runtime.log.logsystem.class", VelocityLogger.class.getName() );
    }

    /*
     * Generate the HTML file according to the Velocity template
     */
    private void doVelocity(String templateName, String outDir, VelocityContext context, VelocityEngine engine)
            throws JxrException {
        // output file
        File file = new File(outDir, templateName + ".html");
        file.getParentFile().mkdirs();

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), getOutputEncoding())) {
            // template file
            StringBuilder templateFile = new StringBuilder();
            File templateDirFile = new File(getTemplateDir());
            if (!templateDirFile.isAbsolute()) {
                // default templates
                templateFile.append(getTemplateDir());
                templateFile.append('/');
            }
            templateFile.append(templateName);
            templateFile.append(".vm");
            Template template = engine.getTemplate(templateFile.toString());

            // do the merge
            template.merge(context, writer);
            writer.flush();
        } catch (Exception e) {
            throw new JxrException("Error merging velocity template", e);
        }
    }

    /*
     * Creates a Map of other Maps containing information about
     * this project's packages and classes, obtained from the PackageManager.
     *
     * allPackages collection of Maps with package info, with the following format
     *   {name}    package name (e.g., "org.apache.maven.jxr")
     *   {dir}     package dir relative to the root output dir (e.g., "org/apache/maven/jxr")
     *   {rootRef} relative link to root output dir (e.g., "../../../../") note trailing slash
     *   {classes} collection of Maps with class info
     *      {name}  class name (e.g., "DirectoryIndexer")
     *      {dir}   duplicate of package {dir}
     *
     * allClasses collection of Maps with class info, format as above
     *
     */
    ProjectInfo getProjectInfo() {
        Map<String, PackageInfo> allPackages = new TreeMap<>();
        Map<String, ClassInfo> allClasses = new TreeMap<>();

        for (PackageType pkg : packageManager.getPackageTypes()) {
            String pkgName = pkg.getName();
            String pkgDir = pkgName.replace('.', '/');
            String rootRef = pkgName.replaceAll("[^\\.]+(\\.|$)", "../");

            // special case for the default package
            // javadoc doesn't deal with it, but it's easy for us
            if (pkgName.length() == 0) {
                pkgName = "(default package)";
                pkgDir = ".";
                rootRef = "./";
            }

            Map<String, ClassInfo> pkgClasses = new TreeMap<>();
            for (ClassType clazz : pkg.getClassTypes()) {
                String className = clazz.getName();

                ClassInfo classInfo = new ClassInfo(className, pkgDir);

                classInfo.setFilename(clazz.getFilename());

                pkgClasses.put(className, classInfo);

                // Adding package name to key in order to ensure classes with identical names in different packages are
                // all included.
                allClasses.put(className + "#" + pkgName, classInfo);
            }

            PackageInfo pkgInfo = new PackageInfo(pkgName, pkgDir);
            pkgInfo.setClasses(pkgClasses);
            pkgInfo.setRootRef(rootRef);

            allPackages.put(pkgName, pkgInfo);
        }

        return new ProjectInfo(allPackages, allClasses);
    }

    /**
     *
     * @author Robert Scholte
     * @since 3.2.0
     */
    public static class ProjectInfo {
        private final Map<String, PackageInfo> allPackages;

        private final Map<String, ClassInfo> allClasses;

        public ProjectInfo(Map<String, PackageInfo> allPackages, Map<String, ClassInfo> allClasses) {
            this.allPackages = allPackages;
            this.allClasses = allClasses;
        }

        public Map<String, PackageInfo> getAllPackages() {
            return allPackages;
        }

        public Map<String, ClassInfo> getAllClasses() {
            return allClasses;
        }
    }

    /**
     *
     * @author Robert Scholte
     * @since 3.2.0
     */
    public static class PackageInfo {
        private final String name;

        private final String dir;

        Map<String, ClassInfo> classes;

        private String rootRef;

        public PackageInfo(String name, String dir) {
            this.name = name;
            this.dir = dir;
        }

        public String getName() {
            return name;
        }

        public String getDir() {
            return dir;
        }

        public void setClasses(Map<String, ClassInfo> classes) {
            this.classes = classes;
        }

        public Map<String, ClassInfo> getClasses() {
            return classes;
        }

        public void setRootRef(String rootRef) {
            this.rootRef = rootRef;
        }

        public String getRootRef() {
            return rootRef;
        }
    }

    /**
     * Holds class information
     *
     * @author Robert Scholte
     * @since 3.2.0
     */
    public static class ClassInfo {
        private final String name;

        private final String dir;

        private String filename;

        public ClassInfo(String name, String dir) {
            super();
            this.name = name;
            this.dir = dir;
        }

        public String getName() {
            return name;
        }

        public String getDir() {
            return dir;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }
    }
}
