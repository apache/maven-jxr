package org.apache.maven.jxr.log;

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

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Logging interface for Velocity.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class VelocityLogger
    implements LogSystem
{
    private Log log;

    public void init( RuntimeServices runtimeServices )
    {
        log = (Log) runtimeServices.getProperty( Log.class.getName() );
    }

    public void logVelocityMessage( int level, String msg )
    {
        switch ( level )
        {
            // velocity info messages are too verbose, just consider them as debug messages...
            // case INFO_ID:
            //    log.info( msg );
            //    break;

            case WARN_ID:
                log.warn( msg );
                break;

            case ERROR_ID:
                log.error( msg );
                break;

            default:
                log.debug( msg );
                break;
        }
    }
}
