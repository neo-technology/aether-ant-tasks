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
import org.apache.tools.ant.types.Reference;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.AntRepoSys;
import org.sonatype.aether.ant.types.RemoteRepository;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;

/**
 * @author Benjamin Bentmann
 */
public class Deploy
    extends AbstractDistTask
{

    private RemoteRepository repository;

    private RemoteRepository snapshotRepository;

    @Override
    protected void validate()
    {
        super.validate();

        if ( repository == null )
        {
            throw new BuildException( "You must specify the <repo id=\"...\" url=\"...\"> element"
                + " to denote the target repository for the deployment" );
        }
        else
        {
            repository.validate( this );
        }
        if ( snapshotRepository != null )
        {
            snapshotRepository.validate( this );
        }
    }

    public void addRepo( RemoteRepository repository )
    {
        if ( this.repository != null )
        {
            throw new BuildException( "You must not specify multiple <repo> elements" );
        }
        this.repository = repository;
    }

    public void setRepoRef( Reference ref )
    {
        if ( repository == null )
        {
            repository = new RemoteRepository();
            repository.setProject( getProject() );
        }
        repository.setRefid( ref );
    }

    public void addSnapshotRepo( RemoteRepository snapshotRepository )
    {
        if ( this.snapshotRepository != null )
        {
            throw new BuildException( "You must not specify multiple <snapshotRepo> elements" );
        }
        this.snapshotRepository = snapshotRepository;
    }

    public void setSnapshotRepoRef( Reference ref )
    {
        if ( snapshotRepository == null )
        {
            snapshotRepository = new RemoteRepository();
            snapshotRepository.setProject( getProject() );
        }
        snapshotRepository.setRefid( ref );
    }

    @Override
    public void execute()
        throws BuildException
    {
        validate();

        AntRepoSys sys = AntRepoSys.getInstance( getProject() );

        RepositorySystemSession session = sys.getSession( this, null );
        RepositorySystem system = sys.getSystem();

        DeployRequest request = new DeployRequest();

        request.setArtifacts( toArtifacts( session ) );

        boolean snapshot = request.getArtifacts().iterator().next().isSnapshot();
        RemoteRepository distRepo = ( snapshot && snapshotRepository != null ) ? snapshotRepository : repository;
        request.setRepository( sys.toDistRepo( distRepo, session ) );

        try
        {
            system.deploy( session, request );
        }
        catch ( DeploymentException e )
        {
            throw new BuildException( "Could not deploy artifacts: " + e.getMessage(), e );
        }
    }

}
