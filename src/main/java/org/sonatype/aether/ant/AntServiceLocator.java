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

import org.apache.tools.ant.Project;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;

/**
 * @author Benjamin Bentmann
 */
class AntServiceLocator
    extends DefaultServiceLocator
{

    private Project project;

    public AntServiceLocator( Project project )
    {
        this.project = project;
    }

    @Override
    protected void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception )
    {
        String msg = "Could not initialize repository system";
        if ( !RepositorySystem.class.equals( type ) )
        {
            msg += ", service " + type.getName() + " (" + impl.getName() + ") failed to initialize";
        }
        msg += ": " + exception.getMessage();
        project.log( msg, exception, Project.MSG_ERR );
    }

}
