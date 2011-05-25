package org.sonatype.aether.ant;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpsWagon;
import org.sonatype.aether.connector.wagon.WagonProvider;

/**
 * @author Benjamin Bentmann
 */
class AntWagonProvider
    implements WagonProvider
{

    public Wagon lookup( String roleHint )
        throws Exception
    {
        if ( "file".equals( roleHint ) )
        {
            return new FileWagon();
        }
        else if ( "http".equals( roleHint ) )
        {
            return new LightweightHttpWagon();
        }
        else if ( "https".equals( roleHint ) )
        {
            return new LightweightHttpsWagon();
        }
        throw new IllegalArgumentException( "No wagon provider registered for protocol " + roleHint );
    }

    public void release( Wagon wagon )
    {
        // noop
    }

}
