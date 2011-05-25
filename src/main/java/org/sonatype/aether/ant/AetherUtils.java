package org.sonatype.aether.ant;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import java.io.File;

import org.apache.tools.ant.Project;
import org.sonatype.aether.ant.types.RemoteRepositories;

class AetherUtils
{

    static File findGlobalSettings( Project project )
    {
        File file = new File( new File( project.getProperty( "ant.home" ), "etc" ), Names.SETTINGS_XML );
        if ( file.isFile() )
        {
            return file;
        }
        else
        {
            String mavenHome = getMavenHome( project );
            if ( mavenHome != null )
            {
                return new File( new File( mavenHome, "conf" ), Names.SETTINGS_XML );
            }
        }
    
        return null;
    }

    static String getMavenHome( Project project )
    {
        String mavenHome = project.getProperty( "maven.home" );
        if ( mavenHome != null )
        {
            return mavenHome;
        }
        return System.getenv( "M2_HOME" );
    }

    static File findUserSettings( Project project )
    {
        File userHome = new File( project.getProperty( "user.home" ) );
        File file = new File( new File( userHome, ".ant" ), Names.SETTINGS_XML );
        if ( file.isFile() )
        {
            return file;
        }
        else
        {
            return new File( new File( userHome, ".m2" ), Names.SETTINGS_XML );
        }
    }

    static RemoteRepositories getDefaultRepositories( Project project )
    {
        Object obj = project.getReference( Names.ID_DEFAULT_REPOS );
        if ( obj instanceof RemoteRepositories )
        {
            return (RemoteRepositories) obj;
        }
        return null;
    }

}
