package org.sonatype.aether.ant;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.sonatype.aether.test.util.TestFileUtils;

public class ResolveTest
    extends AntBuildsTest
{

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        configureProject( "src/test/ant/Resolve.xml", Project.MSG_VERBOSE );
    }

    public void testResolveGlobalPom()
    {
        executeTarget( "testResolveGlobalPom" );

        String prop = getProject().getProperty( "test.resolve.path.org.sonatype.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop, containsString( ".m2/repository" ) );
    }

    public void testResolveOverrideGlobalPom()
    {
        executeTarget( "testResolveOverrideGlobalPom" );

        String prop = getProject().getProperty( "test.resolve.path.org.sonatype.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop, containsString( ".m2/repository" ) );
    }

    public void testResolveGlobalPomIntoOtherLocalRepo()
    {
        executeTarget( "testResolveGlobalPomIntoOtherLocalRepo" );

        String prop = getProject().getProperty( "test.resolve.path.org.sonatype.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop,
                    containsString( "resolvetest-local-repo" ) );
    }

    public void testResolveCustomFileLayout()
        throws IOException
    {
        File dir = new File( "target/resolve-custom-layout" );
        TestFileUtils.delete( dir );
        executeTarget( "testResolveCustomFileLayout" );

        assertThat( "aether-api was not saved with custom file layout",
                    new File( dir, "org.sonatype.aether/aether-api/org/sonatype/aether/jar" ).exists() );

        TestFileUtils.delete( dir );
    }
}
