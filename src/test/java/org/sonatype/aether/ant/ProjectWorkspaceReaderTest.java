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

import static org.junit.Assert.*;

import java.io.File;

import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.ant.types.Pom;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class ProjectWorkspaceReaderTest
{

    private ProjectWorkspaceReader reader;

    private Project project;

    @Before
    public void setUp()
        throws Exception
    {
        this.reader = new ProjectWorkspaceReader();

        this.project = new Project();
        project.setProperty( "user.home", System.getProperty( "user.home" ) );
    }

    private Artifact artifact( String coords )
    {
        return new DefaultArtifact( coords );
    }

    @Test
    public void testFindPom()
    {
        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );
        
        reader.addPom( pom );
        
        assertEquals( new File( "src/test/ant/dummy-pom.xml" ),
                      reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) ) );
    }

    @Test
    public void testFindNoPom()
    {
        assertNull( reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) ) );

        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );

        reader.addPom( pom );

        assertNull( reader.findArtifact( artifact( "unavailable:test:pom:0.1-SNAPSHOT" ) ) );
    }

    @Test
    public void testFindNoArtifact()
    {
        assertNull( reader.findArtifact( artifact( "test:test:jar:0.1-SNAPSHOT" ) ) );

        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );

        reader.addPom( pom );

        org.sonatype.aether.ant.types.Artifact artifact = new org.sonatype.aether.ant.types.Artifact();
        artifact.setProject( project );
        artifact.addPom( pom );
        artifact.setFile( new File( "src/test/ant/common.xml" ) );

        reader.addArtifact( artifact );

        assertNull( reader.findArtifact( artifact( "unavailable:test:jar:0.1-SNAPSHOT" ) ) );
    }

    @Test
    public void testFindArtifact()
    {
        assertNull( reader.findArtifact( artifact( "test:test:jar:0.1-SNAPSHOT" ) ) );

        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );

        reader.addPom( pom );

        org.sonatype.aether.ant.types.Artifact artifact = new org.sonatype.aether.ant.types.Artifact();
        artifact.setProject( project );
        artifact.addPom( pom );
        artifact.setFile( new File( "src/test/ant/common.xml" ) );

        reader.addArtifact( artifact );

        assertNull( reader.findArtifact( artifact( "test:test:jar:0.1-SNAPSHOT" ) ) );
    }
}
