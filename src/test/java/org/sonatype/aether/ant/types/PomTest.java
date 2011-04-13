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

/**
 * @author Benjamin Bentmann
 */
public class PomTest
{

    @Test
    public void testSetCoordsGid()
    {
        Pom pom = new Pom();
        pom.setCoords( "gid:aid:ver" );

        assertEquals( "gid", pom.getGroupId() );
        assertEquals( "aid", pom.getArtifactId() );
        assertEquals( "ver", pom.getVersion() );
    }

}
