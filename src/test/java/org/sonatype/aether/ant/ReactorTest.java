package org.sonatype.aether.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class ReactorTest
    extends AntBuildsTest
{

    private File pomDir;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        pomDir = new File( "src/test/ant/reactor" ).getAbsoluteFile();
        configureProject( "src/test/ant/Reactor.xml", Project.MSG_VERBOSE );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        ProjectWorkspaceReader.dropInstance();
    }

    private Artifact artifact( String coords )
    {
        return new DefaultArtifact( coords );
    }

    public void testPom()
        throws IOException
    {
        executeTarget( "testPom" );
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) );
        assertNotNull( found );
        assertEquals( new File( pomDir, "pom1.xml" ), found.getAbsoluteFile() );
    }

    public void testArtifact()
        throws IOException
    {
        executeTarget( "testArtifact" );
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) );
        assertNotNull( found );
        assertEquals( new File( pomDir, "pom1.xml" ), found.getAbsoluteFile() );

        found = reader.findArtifact( artifact( "test:test:xml:0.1-SNAPSHOT" ) );
        assertNotNull( found );
        assertEquals( new File( pomDir, "pom1.xml" ), found.getAbsoluteFile() );
    }

    public void testArtifactInMemoryPom()
        throws IOException
    {
        executeTarget( "testArtifactInMemoryPom" );
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) );
        assertNull( found );

        found = reader.findArtifact( artifact( "test:test:xml:0.1-SNAPSHOT" ) );
        assertNotNull( found );
        assertEquals( new File( pomDir, "pom1.xml" ), found.getAbsoluteFile() );
    }

    public void testResolveArtifact()
        throws IOException
    {
        executeTarget( "testResolveArtifact" );
        String prop = project.getProperty( "resolve.test:test:jar" );
        assertEquals( new File( "src/test/ant/reactor/pom1.xml" ).getAbsolutePath(), prop );
    }

    public void testResolveArtifactInMemoryPom()
        throws IOException
    {
        executeTarget( "testResolveArtifactInMemoryPom" );
        String prop = project.getProperty( "resolve.test:test:jar" );
        assertEquals( new File( "src/test/ant/reactor/pom1.xml" ).getAbsolutePath(), prop );
        assertLogContaining( "The POM for test:test:jar:0.1-SNAPSHOT is missing, no dependency information available" );
    }
}
