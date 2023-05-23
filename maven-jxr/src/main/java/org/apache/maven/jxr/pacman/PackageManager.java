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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a list of directories, parse them out and store them as rendered
 * packages, classes, imports, etc.
 */
public class PackageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageManager.class);

    private final FileManager fileManager;

    private Set<Path> directories = new HashSet<>();

    /**
     * All the packages that have been parsed
     */
    private Map<String, PackageType> packages = new HashMap<>();

    /**
     * The default Java package.
     */
    private PackageType defaultPackage = new PackageType();

    /**
     * The list of exclude patterns to use.
     */
    private String[] excludes = null;

    /**
     * The list of include patterns to use.
     */
    private String[] includes = {"**/*.java"};

    public PackageManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * Given the name of a package (Ex: org.apache.maven.util) obtain it from
     * the package manager.
     *
     * @param name name of package
     * @return package type if found or default package type
     */
    public PackageType getPackageType(String name) {
        // return the default package if the name is null.
        if (name == null) {
            return defaultPackage;
        }

        return this.packages.get(name);
    }

    /**
     * Add a package to this package manager.
     *
     * @param packageType package type to add
     */
    public void addPackageType(PackageType packageType) {
        this.packages.put(packageType.getName(), packageType);
    }

    /**
     * Gets all of the packages in this package manager.
     *
     * @return package types
     */
    public Collection<PackageType> getPackageTypes() {
        return packages.values();
    }

    /**
     * Parse out all the directories on which this depends.
     */
    private void parse(Path baseDir) {
        // Go through each directory and get the java source
        // files for this dir.
        LOGGER.debug("Scanning " + baseDir);
        DirectoryScanner directoryScanner = new DirectoryScanner();
        directoryScanner.setBasedir(baseDir.toFile());
        directoryScanner.setExcludes(excludes);
        directoryScanner.setIncludes(includes);
        directoryScanner.scan();

        for (String file : directoryScanner.getIncludedFiles()) {
            LOGGER.debug("parsing... " + file);

            // now parse out this file to get the packages/classname/etc
            try {
                Path fileName = baseDir.resolve(file);
                JavaFile jfi = fileManager.getFile(fileName);

                // now that we have this parsed out blend its information
                // with the current package structure
                PackageType jp = this.getPackageType(jfi.getPackageType().getName());

                if (jp == null) {
                    this.addPackageType(jfi.getPackageType());
                    jp = jfi.getPackageType();
                }

                // Add the current file's class(es) to this global package.
                if (jfi.getClassTypes() != null && !jfi.getClassTypes().isEmpty()) {
                    for (ClassType ct : jfi.getClassTypes()) {
                        jp.addClassType(ct);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void process(Path directory) {
        if (this.directories.add(directory)) {
            this.parse(directory);
        }
    }

    /**
     * Dump the package information to STDOUT. FOR DEBUG ONLY
     */
    public void dump() {

        LOGGER.debug("Dumping out PackageManager structure");

        for (PackageType current : getPackageTypes()) {
            LOGGER.debug(current.getName());

            // get the classes under the package and print those too.
            for (ClassType currentClass : current.getClassTypes()) {
                LOGGER.debug('\t' + currentClass.getName());
            }
        }
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }
}
