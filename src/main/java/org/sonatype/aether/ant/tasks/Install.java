package org.sonatype.aether.ant.tasks;

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

import org.apache.tools.ant.BuildException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.AntRepoSys;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;

/**
 * @author Benjamin Bentmann
 */
public class Install
    extends AbstractDistTask
{

    @Override
    public void execute()
        throws BuildException
    {
        validate();

        AntRepoSys sys = AntRepoSys.getInstance( getProject() );

        RepositorySystemSession session = sys.getSession( this, null );
        RepositorySystem system = sys.getSystem();

        InstallRequest request = new InstallRequest();
        request.setArtifacts( toArtifacts( session ) );

        try
        {
            system.install( session, request );
        }
        catch ( InstallationException e )
        {
            throw new BuildException( "Could not install artifacts: " + e.getMessage(), e );
        }
    }

}
