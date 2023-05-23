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
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * Singleton that handles holding references to JavaFiles. This allows
 * Alexandria to lookup and see if a file has already been parsed out and then
 * it can load the information from memory instead of reparsing the file. </p>
 * <p>
 *
 * Note. This assumes that the file will not be modified on disk while
 * Alexandria is running. </p>
 */
public class FileManager {
    private Map<Path, JavaFile> files = new HashMap<>();

    private String encoding = null;

    /**
     * Gets a file from its name.<br>
     * If the file does not exist within the FileManager, creates a new one and returns it.
     *
     * @param path path of the file
     * @return the {@link JavaFile} meta object for the specified file
     * @throws IOException on parsing failure
     */
    public JavaFile getFile(Path path) throws IOException {

        JavaFile real = this.files.get(path);

        if (real == null) {
            real = new JavaFileImpl(path, this.getEncoding());
            this.addFile(real);
        }

        return real;
    }

    /**
     * Add a file to this file manager.
     * @param file file to add
     */
    public void addFile(JavaFile file) {
        this.files.put(file.getPath(), file);
    }

    /**
     * Sets the encoding of source files.
     *
     * @param encoding encoding of source files
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Gets the encoding of source files.
     *
     * @return encoding
     */
    public String getEncoding() {
        return encoding;
    }
}
