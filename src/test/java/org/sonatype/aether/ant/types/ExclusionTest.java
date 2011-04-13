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
import org.sonatype.aether.ant.types.Exclusion;

/**
 * @author Benjamin Bentmann
 */
public class ExclusionTest
{

    @Test
    public void testSetCoordsGid()
    {
        Exclusion ex = new Exclusion();
        ex.setCoords( "gid" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "*", ex.getArtifactId() );
        assertEquals( "*", ex.getExtension() );
        assertEquals( "*", ex.getClassifier() );
    }

    @Test
    public void testSetCoordsGidAid()
    {
        Exclusion ex = new Exclusion();
        ex.setCoords( "gid:aid" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "aid", ex.getArtifactId() );
        assertEquals( "*", ex.getExtension() );
        assertEquals( "*", ex.getClassifier() );
    }

    @Test
    public void testSetCoordsGidAidExt()
    {
        Exclusion ex = new Exclusion();
        ex.setCoords( "gid:aid:ext" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "aid", ex.getArtifactId() );
        assertEquals( "ext", ex.getExtension() );
        assertEquals( "*", ex.getClassifier() );
    }

    @Test
    public void testSetCoordsGidAidExtCls()
    {
        Exclusion ex = new Exclusion();
        ex.setCoords( "gid:aid:ext:cls" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "aid", ex.getArtifactId() );
        assertEquals( "ext", ex.getExtension() );
        assertEquals( "cls", ex.getClassifier() );

        ex = new Exclusion();
        ex.setCoords( "gid:aid:ext:" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "aid", ex.getArtifactId() );
        assertEquals( "ext", ex.getExtension() );
        assertEquals( "", ex.getClassifier() );
    }

}
