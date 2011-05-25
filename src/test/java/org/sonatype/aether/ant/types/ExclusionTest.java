package org.sonatype.aether.ant.types;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

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
