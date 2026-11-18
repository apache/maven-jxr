<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

# Apache Maven JXR

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.apache.maven.plugins/maven-jxr-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.apache.maven.plugins/maven-jxr-plugin)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/org/apache/maven/jxr/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/org/apache/maven/jxr/README.md)
[![Verify](https://github.com/apache/maven-jxr/actions/workflows/maven-verify.yml/badge.svg)](https://github.com/apache/maven-jxr/actions/workflows/maven-verify.yml)

JXR (Java Cross Reference) renders Java source files as linked HTML pages: one page per file, an anchor on every
line, and links from each type name to its definition and to its Javadoc. Report plugins such as PMD and Checkstyle
link their findings to these pages.

This repository holds two modules:

| Module | Artifact | Role |
|---|---|---|
| `maven-jxr` | `org.apache.maven:maven-jxr` | The library that parses the sources and writes the HTML |
| `maven-jxr-plugin` | `org.apache.maven.plugins:maven-jxr-plugin` | The report plugin (`jxr:jxr`, `jxr:aggregate` and others) for the Maven site |

Both need Java 8 or later; the plugin needs Maven 3.9.0 or later.

- Documentation: <https://maven.apache.org/jxr/> (library) and
  <https://maven.apache.org/jxr/maven-jxr-plugin/> (plugin)
- Issues: <https://github.com/apache/maven-jxr/issues>

## Build

```shell
mvn verify
```

Run the plugin's integration tests (in `maven-jxr-plugin/src/it`) with:

```shell
mvn -Prun-its verify
```

Build the site, including this project's own cross-reference and Javadoc, with:

```shell
mvn install -DskipTests
mvn site site:stage -Preporting
```

The staged site is in `target/staging`.

## Contributing

Open an issue before a larger change, and a pull request against `master`. Run `mvn -Prun-its verify` before
you push; the build checks formatting with Spotless (`mvn spotless:apply` fixes it) and license headers with
Apache RAT. Contributions are licensed under the Apache License 2.0; regular contributors should file an
[Individual Contributor License Agreement](https://www.apache.org/licenses/contributor-agreements.html#clas).
