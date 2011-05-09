package org.sonatype.aether.ant;

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
