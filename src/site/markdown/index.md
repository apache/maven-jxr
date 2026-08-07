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

Maven JXR project (formally Java Cross Reference) is a library to analyze a set of Java source files and produce documentation in HTML format _à la Javadoc_.

Take a look at this [JXR report](./xref/index.html) to see an example.

## Main Features

- Supports JDK 1.4+
- Complementary tool for Javadoc
- Easy configuration for color, style or template
- Fully integrated with [Maven](./maven-jxr-plugin/)
## Brief History

The original JXR code was merged in 2004 with the Javasrc project from the defunct [Jakarta Alexandria](http://jakarta.apache.org/alexandria) project. The code was first maintained within the Maven 1 JXR plugin. In September 2005, it was voted to fork the base and create a separate library.

## Examples

The following example shows how to use Maven JXR in more advanced use cases:

- [Using Maven JXR in Java](./examples/java.html)
