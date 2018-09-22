package org.apache.maven.plugin.jxr;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Site;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.repository.Repository;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility class for the jxr report.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 */
public class JxrReportUtil
{

    private static final String MAVEN_JAVADOC_PLUGIN_GROUP_ID = "org.apache.maven.plugins";

    private static final String MAVEN_JAVADOC_PLUGIN_ARTIFACT_ID = "maven-javadoc-plugin";

    /**
     * Determine if javadoc is aggregated in this project, paying attention to both TODO: take cognizance of javadoc
     * versus test-javadoc the old parameter and the new mojo.
     * 
     * @param project
     * @return
     * @throws IOException
     */
    protected static boolean isJavadocAggregated( MavenProject project )
        throws IOException
    {
        // first check conf for obsolete aggregate param.
        // CHECKSTYLE_OFF: LineLength
        boolean javadocAggregate =
            Boolean.valueOf( JxrReportUtil.getMavenJavadocPluginBasicOption( project, "aggregate", "false" ) ).booleanValue();
        // CHECKSTYLE_ON: LineLength

        if ( javadocAggregate )
        {
            return true;
        }
        for ( Object pluginObject : getMavenJavadocPlugins( project ) )
        {
            if ( pluginObject instanceof Plugin )
            {
                Plugin plugin = (Plugin) pluginObject;
                List<PluginExecution> executions = plugin.getExecutions();
                for ( PluginExecution pe : executions )
                {
                    List<String> goals = pe.getGoals();
                    for ( String goal : goals )
                    {
                        if ( "aggregate".equals( goal ) )
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return the <code>optionName</code> value defined in a project for the "maven-javadoc-plugin" plugin.
     *
     * @param project not null
     * @param optionName the option name wanted
     * @param defaultValue a default value
     * @return the value for the option name or the default value. Could be null if not found.
     * @throws IOException if any
     */
    protected static String getMavenJavadocPluginBasicOption( MavenProject project, String optionName,
                                                              String defaultValue )
        throws IOException
    {
        List<Object> plugins = new ArrayList<Object>();
        for ( Iterator<?> it = project.getModel().getReporting().getPlugins().iterator(); it.hasNext(); )
        {
            plugins.add( it.next() );
        }
        for ( Iterator<?> it = project.getModel().getBuild().getPlugins().iterator(); it.hasNext(); )
        {
            plugins.add( it.next() );
        }

        String pluginArtifactId = MAVEN_JAVADOC_PLUGIN_ARTIFACT_ID;
        for ( Object next : plugins )
        {
            Xpp3Dom pluginConf = null;

            if ( next instanceof Plugin )
            {
                Plugin plugin = (Plugin) next;

                // CHECKSTYLE_OFF: LineLength
                // using out-of-box Maven plugins
                if ( !isReportPluginMavenJavadoc( pluginArtifactId, plugin ) )
                // CHECKSTYLE_ON: LineLength
                {
                    continue;
                }

                pluginConf = (Xpp3Dom) plugin.getConfiguration();
            }

            if ( next instanceof ReportPlugin )
            {
                ReportPlugin reportPlugin = (ReportPlugin) next;

                // using out-of-box Maven plugins
                if ( !isReportPluginJavaDocPlugin( pluginArtifactId, reportPlugin ) )
                {
                    continue;
                }

                pluginConf = (Xpp3Dom) reportPlugin.getConfiguration();
            }

            if ( pluginConf == null )
            {
                continue;
            }

            try
            {
                StringInputStream stringInputStream = new StringInputStream( pluginConf.toString() );
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( stringInputStream );

                XObject obj = XPathAPI.eval( doc, "//configuration/" + optionName );

                if ( StringUtils.isNotEmpty( obj.toString() ) )
                {
                    return obj.toString();
                }
            }
            catch ( SAXException e )
            {
                throw new IOException( "SAXException: " + e.getMessage() );
            }
            catch ( ParserConfigurationException e )
            {
                throw new IOException( "ParserConfigurationException: " + e.getMessage() );
            }
            catch ( FactoryConfigurationError e )
            {
                throw new IOException( "FactoryConfigurationError: " + e.getMessage() );
            }
            catch ( TransformerException e )
            {
                throw new IOException( "TransformerException: " + e.getMessage() );
            }
        }

        return defaultValue;
    }

    /**
     * Return the plugin references for the javadoc plugin in a project.
     *
     * @param project not null
     * @throws IOException if any
     */
    protected static List<?> getMavenJavadocPlugins( MavenProject project )
        throws IOException
    {
        List<Object> plugins = new ArrayList<Object>();
        for ( Iterator<?> it = project.getModel().getReporting().getPlugins().iterator(); it.hasNext(); )
        {
            plugins.add( it.next() );
        }
        for ( Iterator<?> it = project.getModel().getBuild().getPlugins().iterator(); it.hasNext(); )
        {
            plugins.add( it.next() );
        }

        List<Object> result = new ArrayList<Object>();

        String pluginArtifactId = MAVEN_JAVADOC_PLUGIN_ARTIFACT_ID;
        for ( Object next : plugins )
        {
            if ( next instanceof Plugin )
            {
                Plugin plugin = (Plugin) next;

                // using out-of-box Maven plugins
                if ( !isReportPluginMavenJavadoc( pluginArtifactId, plugin ) )
                {
                    continue;
                }

                result.add( plugin );
            }

            if ( next instanceof ReportPlugin )
            {
                ReportPlugin reportPlugin = (ReportPlugin) next;

                // using out-of-box Maven plugins
                if ( !isReportPluginJavaDocPlugin( pluginArtifactId, reportPlugin ) )
                {
                    continue;
                }
                result.add( reportPlugin );
            }
        }
        return result;
    }

    private static boolean isReportPluginMavenJavadoc( String pluginArtifactId, Plugin plugin )
    {
        return ( plugin.getGroupId().equals( MAVEN_JAVADOC_PLUGIN_GROUP_ID ) )
            && ( plugin.getArtifactId().equals( pluginArtifactId ) );
    }

    private static boolean isReportPluginJavaDocPlugin( String pluginArtifactId, ReportPlugin reportPlugin )
    {
        return ( reportPlugin.getGroupId().equals( MAVEN_JAVADOC_PLUGIN_GROUP_ID ) )
            && ( reportPlugin.getArtifactId().equals( pluginArtifactId ) );
    }

    /**
     * Generates the site structure using the project hierarchy (project and its modules) or using the
     * distributionManagement elements from the pom.xml.
     *
     * @param project
     * @param ignoreMissingSiteUrl
     * @return the structure relative path
     * @throws IOException if any
     */
    protected static String getStructure( MavenProject project, boolean ignoreMissingSiteUrl )
        throws IOException
    {
        // @todo come from site plugin!
        // @see o.a.m.p.site.SiteStageMojo#getStructure(MavenProject project, boolean ignoreMissingSiteUrl )
        if ( project.getDistributionManagement() == null )
        {
            String hierarchy = project.getName();

            MavenProject parent = project.getParent();
            while ( parent != null )
            {
                hierarchy = parent.getName() + '/' + hierarchy;
                parent = parent.getParent();
            }

            return hierarchy;
        }

        Site site = project.getDistributionManagement().getSite();
        if ( site == null )
        {
            if ( !ignoreMissingSiteUrl )
            {
                throw new IOException( "Missing site information in the distribution management "
                    + "element in the project: '" + project.getName() + "'." );
            }

            return null;
        }

        if ( StringUtils.isEmpty( site.getUrl() ) )
        {
            if ( !ignoreMissingSiteUrl )
            {
                throw new IOException( "The URL in the site is missing in the project descriptor." );
            }

            return null;
        }

        Repository repository = new Repository( site.getId(), site.getUrl() );
        if ( StringUtils.isEmpty( repository.getBasedir() ) )
        {
            return repository.getHost();
        }

        if ( repository.getBasedir().startsWith( "/" ) )
        {
            return repository.getHost() + repository.getBasedir();
        }

        return repository.getHost() + '/' + repository.getBasedir();
    }
}
