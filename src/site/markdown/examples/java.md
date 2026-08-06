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

# Using Maven JXR in Java

The cross-referencing API is pretty basic. You can generate xref for a given Java package or single Java source class. The whole generated file is xref'd by line number.

## Transforming Java Packages

JXR handles several options like an input/output encoding. See the [API](../apidocs/org/apache/maven/jxr/JXR.html) for more information.

```unknown
JXR jxr = new JXR();
jxr.setDest( "/target/jxr" );
jxr.setLog( new DummyLog() );

jxr.xref( Collections.singletonList( "/src/main/java/" ), "templateDir",
                                     "WindowsTitle", "DocTitle", "Bottom" );
```

**Note**: the _templateDir_ is a directory with several [Velocity](http://velocity.apache.org/) templates. Maven JXR uses its own [templates](https://github.com/apache/maven-jxr/tree/master/maven-jxr/src/main/resources/templates).

The generated JXR structure should be like the following:

```
/target/jxr
 |- allclasses-frame.html
 |- index.html
 |- overview-frame.html
 |- overview-summary.html
 ...
```

## Transforming a Single Java Source File

You can transform a single Java source file with the following:

```unknown
File sourceFile = new File( "/src/main/java/Test.java" );

PackageManager packageManager = new PackageManager( new DummyLog(),
                                                    new FileManager() );
JavaCodeTransform codeTransform = new JavaCodeTransform( packageManager );

codeTransform.transform( sourceFile.getAbsolutePath(), "/target/jxr/Test.html",
                         Locale.ENGLISH, "ISO-8859-1", "ISO-8859-1", "", "" );
```
