package org.sonatype.aether.ant;

public class InstallTest
    extends AntBuildsTest
{

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        configureProject( "src/test/ant/Install.xml" );
    }

    public void testInstallGlobalPom()
    {
        executeTarget( "testInstallGlobalPom" );

    }

}
