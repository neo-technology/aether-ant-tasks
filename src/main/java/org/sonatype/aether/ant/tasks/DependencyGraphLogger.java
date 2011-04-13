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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

/**
 * @author Benjamin Bentmann
 */
class DependencyGraphLogger
    implements DependencyVisitor
{

    private Task task;

    private String indent = "";

    public DependencyGraphLogger( Task task )
    {
        this.task = task;
    }

    public boolean visitEnter( DependencyNode node )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( indent );
        org.sonatype.aether.graph.Dependency dep = node.getDependency();
        if ( dep != null )
        {
            org.sonatype.aether.artifact.Artifact art = dep.getArtifact();

            buffer.append( art );
            buffer.append( ':' ).append( dep.getScope() );

            if ( node.getPremanagedScope() != null && !node.getPremanagedScope().equals( dep.getScope() ) )
            {
                buffer.append( " (scope managed from " ).append( node.getPremanagedScope() ).append( ")" );
            }

            if ( node.getPremanagedVersion() != null && !node.getPremanagedVersion().equals( art.getVersion() ) )
            {
                buffer.append( " (version managed from " ).append( node.getPremanagedVersion() ).append( ")" );
            }
        }
        else
        {
            buffer.append( "Resolved Dependency Graph:" );
        }

        task.log( buffer.toString(), Project.MSG_VERBOSE );
        indent += "   ";
        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        indent = indent.substring( 0, indent.length() - 3 );
        return true;
    }

}
