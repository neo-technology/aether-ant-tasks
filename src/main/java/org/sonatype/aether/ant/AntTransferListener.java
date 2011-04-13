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
import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.transfer.TransferCancelledException;
import org.sonatype.aether.transfer.TransferEvent;

/**
 * @author Benjamin Bentmann
 */
class AntTransferListener
    extends AbstractTransferListener
{

    private Task task;

    public AntTransferListener( Task task )
    {
        this.task = task;
    }

    @Override
    public void transferInitiated( TransferEvent event )
        throws TransferCancelledException
    {
        String msg = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";
        msg += " " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        task.log( msg );
    }

    @Override
    public void transferSucceeded( TransferEvent event )
    {
        String msg = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded";
        msg += " " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        task.log( msg );
    }

    @Override
    public void transferFailed( TransferEvent event )
    {
        String msg = "Failed to ";
        msg += event.getRequestType() == TransferEvent.RequestType.PUT ? "upload" : "download";
        msg += " " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        msg += ": " + event.getException().getMessage();
        task.log( msg, event.getException(), Project.MSG_ERR );
    }

}
