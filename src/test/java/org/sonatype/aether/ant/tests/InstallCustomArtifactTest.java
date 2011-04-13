package org.sonatype.aether.ant.tests;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;

import org.sonatype.aether.ant.AntBuildsTest;

public class InstallCustomArtifactTest
    extends AntBuildsTest
{

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        configureProject( "src/test/ant/InstallCustomArtifact.xml" );
    }

    public void testDefaultRepo()
    {
        executeTarget( "testDefaultRepo" );
        long tstamp = System.currentTimeMillis();

        assertLogContaining( "Installing" );
        File defaultRepoPath = new File( System.getProperty( "user.home" ), ".m2/repository/" );

        assertUpdatedFile( tstamp, defaultRepoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
        assertUpdatedFile( tstamp, defaultRepoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-ant.xml" );
    }



    public void testCustomRepo()
    {
        executeTarget( "testCustomRepo" );
        long tstamp = System.currentTimeMillis();

        System.out.println( getLog() );
        assertLogContaining( "Installing" );
        File defaultRepoPath = new File( "target/local-repo-custom" );

        assertUpdatedFile( tstamp, defaultRepoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
        assertUpdatedFile( tstamp, defaultRepoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-ant.xml" );
    }

    private void assertUpdatedFile( long tstamp, File repoPath, String path )
    {
        File file = new File( repoPath, path );
        assertThat( "Files do not exist in default repo", file.exists() );
        assertThat( "Files was not updated for 2s before/after timestamp", file.lastModified(),
                    allOf( greaterThanOrEqualTo( tstamp - 2000 ), lessThanOrEqualTo( tstamp ) ) );
    }
}
