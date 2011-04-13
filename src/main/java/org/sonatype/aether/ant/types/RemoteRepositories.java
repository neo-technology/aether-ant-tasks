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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * @author Benjamin Bentmann
 */
public class RemoteRepositories
    extends DataType
    implements RemoteRepositoryContainer
{

    private List<RemoteRepositoryContainer> containers = new ArrayList<RemoteRepositoryContainer>();

    protected RemoteRepositories getRef()
    {
        return (RemoteRepositories) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            for ( RemoteRepositoryContainer container : containers )
            {
                container.validate( task );
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( !containers.isEmpty() )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public void addRemoterepo( RemoteRepository repository )
    {
        checkChildrenAllowed();
        containers.add( repository );
    }

    public void addRemoterepos( RemoteRepositories repositories )
    {
        checkChildrenAllowed();
        if ( repositories == this )
        {
            throw circularReference();
        }
        containers.add( repositories );
    }

    public List<RemoteRepository> getRepositories()
    {
        if ( isReference() )
        {
            return getRef().getRepositories();
        }
        List<RemoteRepository> repos = new ArrayList<RemoteRepository>();
        for ( RemoteRepositoryContainer container : containers )
        {
            repos.addAll( container.getRepositories() );
        }
        return repos;
    }

}
