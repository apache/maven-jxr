---
title: Introduction
author: 
  - Jason van Zyl
  - Vincent Siveton
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

# Maven JXR

Maven JXR (Java Cross Reference) turns a tree of Java source files into a set of linked HTML pages, much like
Javadoc does for API documentation. Every class, package and line number gets its own anchor, and type names in the
code link to the page of the type they refer to, so you can browse a code base or point someone at a single line.

To see the result, open the [JXR report of this project](./xref/index.html).

JXR comes in two parts:

- The [Maven JXR Plugin](./maven-jxr-plugin/) (`org.apache.maven.plugins:maven-jxr-plugin`) adds the cross-reference
  to a Maven site, or writes it on its own with `mvn jxr:jxr`. Most users need only the plugin.
- The Maven JXR library (`org.apache.maven:maven-jxr`) does the parsing and the HTML rendering. Call it directly
  from Java code; see [Using Maven JXR in Java](./examples/java.html).

## Main Features

- Requires Java 8 or later.
- One HTML page per source file, with line-number anchors (for example `Greeter.html#L42`) and keyword and comment
  highlighting.
- Links from a type name to the page that defines it, and from each page to the matching Javadoc page.
- Package and class index pages in the style of Javadoc, rendered from [Velocity](https://velocity.apache.org/)
  templates that you can replace.
- Other report plugins, such as the [PMD Plugin](https://maven.apache.org/plugins/maven-pmd-plugin/) and the
  [Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/), link their findings to the
  matching JXR line.

## Brief History

The original JXR code was merged in 2004 with the Javasrc project from the retired
[Jakarta Alexandria](https://jakarta.apache.org/alexandria/) project. The code was first maintained within the Maven 1
JXR plugin. In September 2005, it was voted to fork the base and create a separate library.

## Examples

The following example shows how to use the Maven JXR library directly:

- [Using Maven JXR in Java](./examples/java.html)
