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

import java.nio.file.Paths;
import java.util.Collections;

import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.Before;
import org.junit.Test;

public class JxrBeanTest
{
    private JXR jxrBean;

    @Before
    public void setUp()
    {   FileManager fileManager = new FileManager();
        PackageManager packageManager = new PackageManager( fileManager );
        CodeTransformer codeTransform = new JavaCodeTransform( packageManager, fileManager );
        jxrBean = new JXR( packageManager, fileManager, Collections.singletonMap( "java", codeTransform ) );
        jxrBean.setDest( Paths.get( "target" ) );
        jxrBean.setInputEncoding( "ISO-8859-1" );
        jxrBean.setOutputEncoding( "ISO-8859-1" );
        jxrBean.setJavadocLinkDir( Paths.get( "." ) );
    }

    @Test
    public void testXref()
        throws Exception
    {
        jxrBean.xref( Collections.singletonList( "src/test/java" ), "templates/jdk4",
                      "title", "title", "copyright" );
    }

}
