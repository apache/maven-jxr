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
File xref = new File( basedir, 'target/reports/xref' )

// there are no frames
assert !new File( xref, 'index.html' ).text.contains( '<frameset' )
assert !new File( xref, 'overview-frame.html' ).exists()
assert !new File( xref, 'allclasses-frame.html' ).exists()
assert !new File( xref, 'com/mycompany/app/package-frame.html' ).exists()

// the search script made it out of the plugin, and the index it reads lists packages and classes
assert new File( xref, 'search.js' ).text.contains( 'jxrSearchIndex' )

index = new File( xref, 'search-index.js' ).text
assert index.startsWith( 'var jxrSearchIndex = ' )
assert index.contains( '{"l":"com.mycompany.app","u":"com/mycompany/app/package-summary.html"}' )
assert index.contains( '{"l":"com.mycompany.app.util","u":"com/mycompany/app/util/package-summary.html"}' )
assert index.contains( '{"p":"com.mycompany.app","l":"App","u":"com/mycompany/app/App.html#App"}' )
assert index.contains( '{"p":"com.mycompany.app","l":"App.Nested","u":"com/mycompany/app/App.html' )
assert index.contains( '{"p":"com.mycompany.app.util","l":"Util","u":"com/mycompany/app/util/Util.html#Util"}' )

// every page loads the script from the root of the report, and has the element where its search box goes
[ 'index.html'                                  : 'search.js',
  'allclasses-index.html'                       : 'search.js',
  'com/mycompany/app/package-summary.html'      : '../../../search.js',
  'com/mycompany/app/App.html'                  : '../../../search.js',
  'com/mycompany/app/util/package-summary.html' : '../../../../search.js',
  'com/mycompany/app/util/Util.html'            : '../../../../search.js' ].each { page, script ->
    content = new File( xref, page ).text
    assert content.contains( '<script type="text/javascript" src="' + script + '"></script>' )
    assert content.contains( '<span id="jxr_search"></span>' )
}

// a class page leads back to the pages that list it
content = new File( xref, 'com/mycompany/app/util/Util.html' ).text
assert content.contains( '<a href="../../../../index.html">Overview</a>' )
assert content.contains( '<a href="package-summary.html">Package</a>' )
assert content.contains( '<a href="../../../../allclasses-index.html">All Classes</a>' )
