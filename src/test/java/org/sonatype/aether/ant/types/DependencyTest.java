package org.sonatype.aether.ant.types;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

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
