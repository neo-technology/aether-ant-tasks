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


import java.lang.reflect.Field;

import org.apache.maven.settings.crypto.DefaultSettingsDecrypter;

/**
 * @author Benjamin Bentmann
 */
class AntSettingsDecryptorFactory
{

    public DefaultSettingsDecrypter newInstance()
    {
        AntSecDispatcher secDispatcher = new AntSecDispatcher();

        DefaultSettingsDecrypter decrypter = new DefaultSettingsDecrypter();

        try
        {
            Field field = decrypter.getClass().getDeclaredField( "securityDispatcher" );
            field.setAccessible( true );
            field.set( decrypter, secDispatcher );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( e );
        }

        return decrypter;
    }

}
