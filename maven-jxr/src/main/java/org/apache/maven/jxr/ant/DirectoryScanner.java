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
package org.apache.maven.jxr.ant;

import java.io.File;

/**
 * Workaround to ignore package-info.java files.
 *
 * @author Carlos Sanchez
 */
public class DirectoryScanner extends org.codehaus.plexus.util.DirectoryScanner {

    @Override
    public void addDefaultExcludes() {
        super.addDefaultExcludes();
        int excludesLength = excludes == null ? 0 : excludes.length;
        String[] newExcludes;
        newExcludes = new String[excludesLength + 1];
        if (excludesLength > 0) {
            System.arraycopy(excludes, 0, newExcludes, 0, excludesLength);
        }
        newExcludes[excludesLength] = "**" + File.separatorChar + "package-info.java";
        excludes = newExcludes;
    }
}
