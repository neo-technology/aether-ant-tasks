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
import org.apache.tools.ant.Task;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;

/**
 * @author Benjamin Bentmann
 */
class AntRepositoryListener
    extends AbstractRepositoryListener
{

    private Task task;

    public AntRepositoryListener( Task task )
    {
        this.task = task;
    }

    @Override
    public void artifactInstalling( RepositoryEvent event )
    {
        task.log( "Installing " + event.getFile() );
    }

    @Override
    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        task.log( "The POM for " + event.getArtifact() + " is invalid"
            + ", transitive dependencies (if any) will not be available: " + event.getException().getMessage(),
                  event.getException(), Project.MSG_WARN );
    };

    @Override
    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        task.log( "The POM for " + event.getArtifact() + " is missing, no dependency information available",
                  Project.MSG_WARN );
    };

}
