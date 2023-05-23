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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interface for objects which wish to provide meta-info about a JavaFile.
 *
 * @author <a href="mailto:burton@apache.org">Kevin A. Burton</a>
 */
public abstract class JavaFile {
    private Set<ImportType> imports = new HashSet<>();

    private List<ClassType> classTypes = new ArrayList<>();

    private PackageType packageType = new PackageType();

    private final Path path;

    private String filename;

    private final String encoding;

    protected JavaFile(Path path, String encoding) {
        this.path = path;
        this.encoding = encoding;
        this.filename = getFilenameWithoutPathOrExtension(path);
    }

    /**
     * Gets the imported packages/files that this package has.
     *
     * @return import types
     */
    public Set<ImportType> getImportTypes() {
        return Collections.unmodifiableSet(imports);
    }

    /**
     * Gets the name of this class.
     * @return class type
     */
    public ClassType getClassType() {
        if (classTypes.isEmpty()) {
            return null;
        } else {
            // to retain backward compatibility, return the first class
            return this.classTypes.get(0);
        }
    }

    /**
     * Gets the names of the classes in this file.
     *
     * @return list of class types
     */
    public List<ClassType> getClassTypes() {
        return this.classTypes;
    }

    /**
     * Gets the package of this class.
     *
     * @return package type
     */
    public PackageType getPackageType() {
        return this.packageType;
    }

    /**
     * Add a class type to the current list of class types.
     *
     * @param classType class type
     */
    public void addClassType(ClassType classType) {
        this.classTypes.add(classType);
    }

    /**
     * Add an import type.
     *
     * @param importType import type
     */
    public void addImportType(ImportType importType) {
        this.imports.add(importType);
    }

    /**
     * Sets the name of this class.
     *
     * @param classType class type
     */
    public void setClassType(ClassType classType) {
        // to retain backward compatibility, make sure the list contains only the supplied classType
        this.classTypes.clear();
        this.classTypes.add(classType);
    }

    /**
     * Sets the package type of this class.
     *
     * @param packageType package type
     */
    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    /**
     * Gets the path attribute.
     *
     * @return path
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * Gets the file name without path and extension.
     *
     * @return file name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Gets the encoding attribute.
     *
     * @return encoding
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Remove the path and the ".java" extension from a filename.
     *
     * @param path path to modify
     * @return modified path
     */
    protected static String getFilenameWithoutPathOrExtension(Path path) {
        String newFilename = path.getFileName().toString();
        // Remove the ".java" extension from the filename, if it exists
        int extensionIndex = newFilename.lastIndexOf(".java");
        if (extensionIndex >= 0) {
            newFilename = newFilename.substring(0, extensionIndex);
        }
        return newFilename;
    }
}
