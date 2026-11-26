---
title: Introduction
author: 
  - Maria Odea Ching
date: 2010-01-20
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Maven JXR Plugin

The JXR Plugin produces a cross-reference of the project's sources: one HTML page per Java source file, with an
anchor on every line and links between types. The pages make it easy to browse the code from the project site or to
point at a specific line. Report plugins such as the
[PMD Plugin](https://maven.apache.org/plugins/maven-pmd-plugin/) and the
[Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/) link each finding to its line in
the cross-reference when one is present.

The plugin requires Maven 3.9.0 or later and Java 8 or later. It is built on the
[Maven JXR library](../index.html).

## Common Tasks

Add the cross-reference to the project site by declaring the plugin in the `<reporting>` section of your `pom.xml`,
as shown on the [usage page](./usage.html), and then run:

```shell
mvn site
```

The site gets two reports, `xref/index.html` for the main sources and `xref-test/index.html` for the test sources.

Generate the cross-reference of the main sources without building the site:

```shell
mvn jxr:jxr
```

The pages go to `target/reports/xref`. Run `mvn jxr:test-jxr` for the test sources, which go to
`target/reports/xref-test`.

In a multi-module project, build one cross-reference for all modules from the parent project, as shown in
[Aggregating JXR Reports for Multi-module Projects](./examples/aggregate.html).

## Goals Overview

The JXR Plugin has 7 goals:

- [jxr:jxr](./jxr-mojo.html) generates a cross-reference of the project's main sources. It runs the lifecycle up to
  `generate-sources` first, so generated sources are included. The pages can link to the project's Javadoc.
- [jxr:test-jxr](./test-jxr-mojo.html) generates a cross-reference of the project's test sources, after running the
  lifecycle up to `generate-test-sources`.
- [jxr:jxr-no-fork](./jxr-no-fork-mojo.html) does the same as `jxr:jxr` without running `generate-sources` again.
  Generate the sources before the site in the same build, for example with `mvn clean deploy site`.
- [jxr:test-jxr-no-fork](./test-jxr-no-fork-mojo.html) does the same as `jxr:test-jxr` without running
  `generate-test-sources` again. Generate the test sources before the site in the same build.
- [jxr:aggregate](./aggregate-mojo.html) generates one cross-reference of the main sources of all modules in a
  multi-module build. It runs the lifecycle up to `compile` first.
- [jxr:test-aggregate](./test-aggregate-mojo.html) generates one cross-reference of the test sources of all modules
  in a multi-module build. It runs the lifecycle up to `test-compile` first.
- [jxr:help](./help-mojo.html) displays help information on the plugin. Run
  `mvn jxr:help -Ddetail=true -Dgoal=<goal-name>` to see the parameters of a goal.

## Usage

General instructions on how to use the JXR Plugin can be found on the [usage page](./usage.html). Some more specific
use cases are described in the examples given below.

If you still have questions, have a look at the [FAQ](./faq.html) and ask on the
[user mailing list](./mailing-lists.html). The posts to the mailing list are archived and could already contain the
answer to your question.

If the plugin is missing a feature or has a defect, open an issue in the [issue tracker](./issue-management.html).
For a bug, include the plugin and Maven versions, the relevant part of your POM and, if you can, a small project that
reproduces the problem. Contributors can check out the project from the [source repository](./scm.html); see the
[guide to helping with Maven](https://maven.apache.org/guides/development/guide-helping.html).

## Examples

To provide you with better understanding on some usages of the JXR Plugin, you can take a look into the following
examples:

- [Aggregating JXR Reports for Multi-module Projects](./examples/aggregate.html)
- [Including/Excluding Source Files](./examples/include-exclude.html)
- [Linking JXR Files to Javadocs](./examples/linkjavadoc.html)
- [Generate JXR without duplicate execution of phase generate-sources](./examples/nofork.html)
