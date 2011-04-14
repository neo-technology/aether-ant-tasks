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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.AntRepoSys;
import org.sonatype.aether.ant.types.Artifact;
import org.sonatype.aether.ant.types.Artifacts;
import org.sonatype.aether.ant.types.Pom;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * @author Benjamin Bentmann
 */
public abstract class AbstractDistTask
    extends Task
{

    private Pom pom;

    private Artifacts artifacts;

    protected void validate()
    {
        getArtifacts().validate( this );

        Map<String, File> duplicates = new HashMap<String, File>();
        for ( Artifact artifact : getArtifacts().getArtifacts() )
        {
            String key = artifact.getType() + ':' + artifact.getClassifier();
            if ( "pom:".equals( key ) )
            {
                throw new BuildException( "You must not specify an <artifact> with type=pom"
                    + ", please use the <pom> element instead." );
            }
            else if ( duplicates.containsKey( key ) )
            {
                throw new BuildException( "You must not specify two or more artifacts with the same type ("
                    + artifact.getType() + ") and classifier (" + artifact.getClassifier() + ")" );
            }
            else
            {
                duplicates.put( key, artifact.getFile() );
            }
        }

        Pom defaultPom = AntRepoSys.getInstance( getProject() ).getDefaultPom();
        pom = pom != null ? pom : defaultPom;

        if ( pom == null )
        {
            throw new BuildException( "You must specify the <pom file=\"...\"> element"
                + " to denote the descriptor for the artifacts" );
        }
        if ( pom.getFile() == null )
        {
            throw new BuildException( "You must specify a <pom> element that has the 'file' attribute set" );
        }
    }

    protected List<org.sonatype.aether.artifact.Artifact> toArtifacts( RepositorySystemSession session )
    {
        Model model = getPom().getModel( this );
        File pomFile = getPom().getFile();

        List<org.sonatype.aether.artifact.Artifact> results = new ArrayList<org.sonatype.aether.artifact.Artifact>();

        org.sonatype.aether.artifact.Artifact pomArtifact =
            new DefaultArtifact( model.getGroupId(), model.getArtifactId(), "pom", model.getVersion() ).setFile( pomFile );
        results.add( pomArtifact );

        for ( Artifact artifact : getArtifacts().getArtifacts() )
        {
            org.sonatype.aether.artifact.Artifact buildArtifact =
                new DefaultArtifact( model.getGroupId(), model.getArtifactId(), artifact.getClassifier(),
                                     artifact.getType(), model.getVersion() ).setFile( artifact.getFile() );
            results.add( buildArtifact );
        }

        return results;
    }

    protected Artifacts getArtifacts()
    {
        if ( artifacts == null )
        {
            artifacts = new Artifacts();
            artifacts.setProject( getProject() );
        }
        return artifacts;
    }

    public void addArtifact( Artifact artifact )
    {
        getArtifacts().addArtifact( artifact );
    }

    public void addArtifacts( Artifacts artifacts )
    {
        getArtifacts().addArtifacts( artifacts );
    }

    public void setArtifactsRef( Reference ref )
    {
        Artifacts artifacts = new Artifacts();
        artifacts.setProject( getProject() );
        artifacts.setRefid( ref );
        getArtifacts().addArtifacts( artifacts );
    }

    protected Pom getPom()
    {
        if ( pom == null )
        {
            return AntRepoSys.getInstance( getProject() ).getDefaultPom();
        }

        return pom;
    }

    public void addPom( Pom pom )
    {
        if ( this.pom != null )
        {
            throw new BuildException( "You must not specify multiple <pom> elements" );
        }
        this.pom = pom;
    }

    public void setPomRef( Reference ref )
    {
        if ( this.pom != null )
        {
            throw new BuildException( "You must not specify multiple <pom> elements" );
        }
        pom = new Pom();
        pom.setProject( getProject() );
        pom.setRefid( ref );
    }

}
