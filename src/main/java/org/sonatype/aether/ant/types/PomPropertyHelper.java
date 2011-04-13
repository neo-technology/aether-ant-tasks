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

import org.apache.tools.ant.PropertyHelper;

/**
 * @author Benjamin Bentmann
 */
@SuppressWarnings( "deprecation" )
class PomPropertyHelper
    extends PropertyHelper
{

    private final ModelValueExtractor extractor;

    public static void register( ModelValueExtractor extractor, PropertyHelper propertyHelper )
    {
        PomPropertyHelper helper = new PomPropertyHelper( extractor );
        helper.setNext( propertyHelper.getNext() );
        propertyHelper.setNext( helper );
    }

    public PomPropertyHelper( ModelValueExtractor extractor )
    {
        if ( extractor == null )
        {
            throw new IllegalArgumentException( "no model value exractor specified" );
        }
        this.extractor = extractor;
        setProject( extractor.getProject() );
    }

    @Override
    public Object getPropertyHook( String ns, String name, boolean user )
    {
        Object value = extractor.getValue( name );
        if ( value != null )
        {
            return value;
        }
        else if ( extractor.isApplicable( name ) )
        {
            return null;
        }
        return super.getPropertyHook( ns, name, user );
    }

}
