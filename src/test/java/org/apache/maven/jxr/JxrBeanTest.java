package org.apache.maven.jxr;

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

import junit.framework.TestCase;

import java.util.Collections;

public class JxrBeanTest
    extends TestCase
{

    private JXR jxrBean;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        jxrBean = new JXR();
        jxrBean.setDest( System.getProperty( "basedir" ) + "/target" );
        jxrBean.setInputEncoding( "ISO-8859-1" );
        jxrBean.setOutputEncoding( "ISO-8859-1" );
        jxrBean.setJavadocLinkDir( "" );
        jxrBean.setLog( new DummyLog() );
    }

    public void testXref()
        throws Exception
    {
        jxrBean.xref( Collections.singletonList( System.getProperty( "basedir" ) + "/src/test/java" ), "templates",
                      "title", "title", "copyright" );
    }

}
