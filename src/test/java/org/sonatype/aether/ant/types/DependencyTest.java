package org.sonatype.aether.ant.types;

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

import org.junit.Test;
import org.sonatype.aether.ant.types.Dependency;

/**
 * @author Benjamin Bentmann
 */
public class DependencyTest
{

    @Test
    public void testSetCoordsGidAidVer()
    {
        Dependency dep = new Dependency();
        dep.setCoords( "gid:aid:ver" );

        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "jar", dep.getType() );
        assertEquals( "", dep.getClassifier() );
        assertEquals( "compile", dep.getScope() );
    }

    @Test
    public void testSetCoordsGidAidVerScope()
    {
        Dependency dep = new Dependency();
        dep.setCoords( "gid:aid:ver:scope" );

        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "jar", dep.getType() );
        assertEquals( "", dep.getClassifier() );
        assertEquals( "scope", dep.getScope() );
    }

    @Test
    public void testSetCoordsGidAidVerTypeScope()
    {
        Dependency dep = new Dependency();
        dep.setCoords( "gid:aid:ver:type:scope" );

        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "type", dep.getType() );
        assertEquals( "", dep.getClassifier() );
        assertEquals( "scope", dep.getScope() );
    }

    @Test
    public void testSetCoordsGidAidVerTypeClsScope()
    {
        Dependency dep = new Dependency();
        dep.setCoords( "gid:aid:ver:type:cls:scope" );

        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "type", dep.getType() );
        assertEquals( "cls", dep.getClassifier() );
        assertEquals( "scope", dep.getScope() );
    }

}
