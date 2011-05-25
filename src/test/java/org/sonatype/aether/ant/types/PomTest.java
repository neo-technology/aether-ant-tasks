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
