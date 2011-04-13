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
import org.sonatype.aether.spi.log.Logger;

/**
 * @author Benjamin Bentmann
 */
class AntLogger
    implements Logger
{

    private Project project;

    public AntLogger( Project project )
    {
        this.project = project;
    }

    public void debug( String msg )
    {
        project.log( msg, Project.MSG_DEBUG );
    }

    public void debug( String msg, Throwable error )
    {
        project.log( msg, error, Project.MSG_DEBUG );
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

    public boolean isWarnEnabled()
    {
        return true;
    }

    public void warn( String msg )
    {
        project.log( msg, Project.MSG_WARN );
    }

    public void warn( String msg, Throwable error )
    {
        project.log( msg, error, Project.MSG_WARN );
    }

}
