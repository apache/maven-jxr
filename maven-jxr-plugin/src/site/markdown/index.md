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

The JXR Plugin produces a cross-reference of the project's sources. The generated reports make it easier for the user to reference or find specific lines of code. It is also handy when used with the PMD Plugin for referencing errors found in the code.

## Goals Overview

The JXR Plugin has 6 goals:

- [jxr:jxr](./jxr-mojo.html) is used to generate a cross-reference page of the project's main sources. The generated JXR files can be linked to the javadocs of the project.
- [jxr:jxr-no-fork](./jxr-no-fork-mojo.html) is used to generate a cross-reference page of the project's main sources without forking the `generate-sources` phase again. Note that this goal does require generation of main sources before site generation, e.g. by invoking `mvn clean deploy site`.
- [jxr:aggregate](./aggregate-mojo.html) is used to generate an aggregated cross-reference page of the project's main sources. The generated JXR files can be linked to the javadocs of the project.
- [jxr:test-jxr](./test-jxr-mojo.html) on the other hand, is used to generate a cross-reference page of the project's test sources.
- [jxr:test-jxr-no-fork](./test-jxr-no-fork-mojo.html) on the other hand, is used to generate a cross-reference page of the project's test sources. without forking the `generate-test-sources` phase again. Note that this goal does require generation of test sources before site generation, e.g. by invoking `mvn clean deploy site`.
- [jxr:test-aggregate](./test-aggregate-mojo.html) on the other hand, is used to generate an aggregated cross-reference page of the project's test sources.
## Usage

General instructions on how to use the JXR Plugin can be found on the [usage page](./usage.html). Some more specific use cases are described in the examples given below. Last but not least, users occasionally contribute additional examples, tips or errata to the [plugin's wiki page](http://docs.codehaus.org/display/MAVENUSER/JXR+Plugin).

In case you still have questions regarding the plugin's usage, please have a look at the [FAQ](./faq.html) and feel free to contact the [user mailing list](./mailing-lists.html). The posts to the mailing list are archived and could already contain the answer to your question as part of an older thread. Hence, it is also worth browsing/searching the [mail archive](./mailing-lists.html).

If you feel like the plugin is missing a feature or has a defect, you can fill a feature request or bug report in our [issue tracker](./issue-management.html). When creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. For this reason, entire debug logs, POMs or most preferably little demo projects attached to the issue are very much appreciated. Of course, patches are welcome, too. Contributors can check out the project from our [source repository](./scm.html) and will find supplementary information in the [guide to helping with Maven](http://maven.apache.org/guides/development/guide-helping.html).

## Examples

To provide you with better understanding on some usages of the JXR Plugin, you can take a look into the following examples:

- [Aggregating JXR Reports for Multi-Projects](./examples/aggregate.html)
- [Linking JXR Files to Javadocs](./examples/linkjavadoc.html)
- [Generate JXR without duplicate execution of phase generate-sources](./examples/nofork.html)
