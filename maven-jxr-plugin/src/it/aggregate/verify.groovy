
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
assert new File( basedir, 'target/site' ).exists();

content = new File( basedir, 'target/site/project-reports.html' ).text;

assert content.contains( 'xref/index.html' );
assert content.contains( 'xref-test/index.html' );

assert new File( basedir, 'target/site/xref' ).exists();
assert new File( basedir, 'target/site/xref/index.html' ).exists();
assert new File( basedir, 'target/site/xref/org/apache/maven/jxr/it/App.html' ).exists();
assert new File( basedir, 'target/site/xref/org/apache/maven/jxr/it2/App.html' ).exists();

assert new File( basedir, 'target/site/xref-test' ).exists();
assert new File( basedir, 'target/site/xref-test/index.html' ).exists();
assert new File( basedir, 'target/site/xref-test/org/apache/maven/jxr/it/AppTest.html' ).exists();
assert new File( basedir, 'target/site/xref-test/org/apache/maven/jxr/it2/AppTest.html' ).exists();

content = new File( basedir, 'target/site/xref/org/apache/maven/jxr/it/App.html' ).text;
assert content.contains( 'App2.html' );

content = new File( basedir, 'target/site/xref/org/apache/maven/jxr/it2/App.html' ).text;
assert content.contains( 'App2.html' );

return true;