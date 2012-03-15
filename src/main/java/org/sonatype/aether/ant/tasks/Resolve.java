package org.sonatype.aether.ant.tasks;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.AntRepoSys;
import org.sonatype.aether.ant.Names;
import org.sonatype.aether.ant.types.Dependencies;
import org.sonatype.aether.ant.types.Pom;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.SubArtifact;
import org.sonatype.aether.util.filter.ScopeDependencyFilter;

/**
 * @author Benjamin Bentmann
 */
public class Resolve
    extends AbstractResolvingTask
{

    private final List<ArtifactConsumer> consumers = new ArrayList<ArtifactConsumer>();

    private boolean failOnMissingAttachments;
    private boolean transitive = true;

    public void setFailOnMissingAttachments( boolean failOnMissingAttachments )
    {
        this.failOnMissingAttachments = failOnMissingAttachments;
    }

    public void setTransitive( boolean transitive )
    {
        this.transitive = transitive;
    }

    public Path createPath()
    {
        Path path = new Path();
        consumers.add( path );
        return path;
    }

    public Files createFiles()
    {
        Files files = new Files();
        consumers.add( files );
        return files;
    }

    public Props createProperties()
    {
        Props props = new Props();
        consumers.add( props );
        return props;
    }

    private void validate()
    {
        for ( ArtifactConsumer consumer : consumers )
        {
            consumer.validate();
        }

        Pom pom = AntRepoSys.getInstance( getProject() ).getDefaultPom();
        if ( dependencies == null && pom != null )
        {
            log( "Using default pom for dependency resolution (" + pom.toString() + ")", Project.MSG_INFO );
            dependencies = new Dependencies();
            dependencies.setProject( getProject() );
            getProject().addReference( Names.ID_DEFAULT_POM, pom );
            dependencies.setPomRef( new Reference( getProject(), Names.ID_DEFAULT_POM ) );
        }

        if ( dependencies != null )
        {
            dependencies.validate( this );
        }
        else
        {
            throw new BuildException( "No <dependencies> set for resolution" );
        }
    }

    @Override
    public void execute()
        throws BuildException
    {
        validate();


        AntRepoSys sys = AntRepoSys.getInstance( getProject() );

        RepositorySystemSession session = sys.getSession( this, localRepository );
        RepositorySystem system = sys.getSystem();
        log( "Using local repository " + session.getLocalRepository(), Project.MSG_VERBOSE );

        DependencyNode root = collectDependencies().getRoot();
        root.accept( new DependencyGraphLogger( this ) );

        Map<String, Group> groups = new HashMap<String, Group>();
        for ( ArtifactConsumer consumer : consumers )
        {
            String classifier = consumer.getClassifier();
            Group group = groups.get( classifier );
            if ( group == null )
            {
                group = new Group( classifier );
                groups.put( classifier, group );
            }
            group.add( consumer );
        }

        for ( Group group : groups.values() )
        {
            group.createRequests( root, transitive );
        }

        log( "Resolving artifacts", Project.MSG_INFO );

        for ( Group group : groups.values() )
        {
            List<ArtifactResult> results;
            try
            {
                results = system.resolveArtifacts( session, group.getRequests() );
            }
            catch ( ArtifactResolutionException e )
            {
                if ( !group.isAttachments() || failOnMissingAttachments )
                {
                    throw new BuildException( "Could not resolve artifacts: " + e.getMessage(), e );
                }
                results = e.getResults();
                for ( ArtifactResult result : results )
                {
                    if ( result.isMissing() )
                    {
                        log( "Ignoring missing attachment " + result.getRequest().getArtifact(), Project.MSG_VERBOSE );
                    }
                    else if ( !result.isResolved() )
                    {
                        throw new BuildException( "Could not resolve artifacts: " + e.getMessage(), e );
                    }
                }
            }

            group.processResults( results );
        }
    }

    public static abstract class ArtifactConsumer
        extends ProjectComponent
    {

        private DependencyFilter filter;

        public boolean accept( org.sonatype.aether.graph.DependencyNode node, List<DependencyNode> parents )
        {
            return filter == null || filter.accept( node, parents );
        }

        public String getClassifier()
        {
            return null;
        }

        public void validate()
        {

        }

        public abstract void process( Artifact artifact );

        public void setScopes( String scopes )
        {
            if ( filter != null )
            {
                throw new BuildException( "You must not specify both 'scopes' and 'classpath'" );
            }

            Collection<String> included = new HashSet<String>();
            Collection<String> excluded = new HashSet<String>();

            String[] split = scopes.split( "[, ]" );
            for ( String scope : split )
            {
                scope = scope.trim();
                Collection<String> dst;
                if ( scope.startsWith( "-" ) || scope.startsWith( "!" ) )
                {
                    dst = excluded;
                    scope = scope.substring( 1 );
                }
                else
                {
                    dst = included;
                }
                if ( scope.length() > 0 )
                {
                    dst.add( scope );
                }
            }

            filter = new ScopeDependencyFilter( included, excluded );
        }

        public void setClasspath( String classpath )
        {
            if ( "compile".equals( classpath ) )
            {
                setScopes( "provided,system,compile" );
            }
            else if ( "runtime".equals( classpath ) )
            {
                setScopes( "compile,runtime" );
            }
            else if ( "test".equals( classpath ) )
            {
                setScopes( "provided,system,compile,runtime,test" );
            }
            else
            {
                throw new BuildException( "The classpath '" + classpath + "' is not defined"
                    + ", must be one of 'compile', 'runtime' or 'test'" );
            }
        }

    }

    public class Path
        extends ArtifactConsumer
    {

        private String refid;

        private org.apache.tools.ant.types.Path path;

        public void setRefId( String refId )
        {
            this.refid = refId;
        }

        @Override
        public void validate()
        {
            if ( refid == null )
            {
                throw new BuildException( "You must specify the 'refid' for the path" );
            }
        }

        @Override
        public void process( Artifact artifact )
        {
            if ( path == null )
            {
                path = new org.apache.tools.ant.types.Path( getProject() );
                getProject().addReference( refid, path );
            }
            path.setLocation( artifact.getFile() );
        }

    }

    public class Files
        extends ArtifactConsumer
    {

        private static final String DEFAULT_LAYOUT = Layout.GID_DIRS + "/" + Layout.AID + "/" + Layout.BVER + "/"
            + Layout.AID + "-" + Layout.VER + "-" + Layout.CLS + "." + Layout.EXT;

        private String refid;

        private String classifier;

        private File dir;

        private Layout layout = new Layout( DEFAULT_LAYOUT );

        private FileSet fileset;

        public void setRefId( String refId )
        {
            this.refid = refId;
        }

        @Override
        public String getClassifier()
        {
            return classifier;
        }

        public void setAttachments( String attachments )
        {
            if ( "sources".equals( attachments ) )
            {
                classifier = "*-sources";
            }
            else if ( "javadoc".equals( attachments ) )
            {
                classifier = "*-javadoc";
            }
            else
            {
                throw new BuildException( "The attachment type '" + attachments
                    + "' is not defined, must be one of 'sources' or 'javadoc'" );
            }
        }

        public void setDir( File dir )
        {
            this.dir = dir;
        }

        public void setLayout( String layout )
        {
            this.layout = new Layout( layout );
        }

        @Override
        public void validate()
        {
            if ( refid == null && dir == null )
            {
                throw new BuildException( "You must either specify the 'refid' for the resource collection"
                    + " or a 'dir' to copy the files to" );
            }
        }

        @Override
        public void process( Artifact artifact )
        {
            if ( dir != null )
            {
                if ( refid != null && fileset == null )
                {
                    fileset = new FileSet();
                    fileset.setProject( getProject() );
                    fileset.setDir( dir );
                    getProject().addReference( refid, fileset );
                }

                String path = layout.getPath( artifact );

                if ( fileset != null )
                {
                    fileset.createInclude().setName( path );
                }

                File src = artifact.getFile();
                File dst = new File( dir, path );

                if ( src.lastModified() != dst.lastModified() || src.length() != dst.length() )
                {
                    try
                    {
                        Resolve.this.log( "Copy " + src + " to " + dst, Project.MSG_VERBOSE );
                        FileUtils.getFileUtils().copyFile( src, dst, null, true, true );
                    }
                    catch ( IOException e )
                    {
                        throw new BuildException( "Failed to copy artifact file " + src + " to " + dst + ": "
                            + e.getMessage(), e );
                    }
                }
                else
                {
                    Resolve.this.log( "Omit to copy " + src + " to " + dst + ", seems unchanged", Project.MSG_VERBOSE );
                }
            }
        }

    }

    public class Props
        extends ArtifactConsumer
    {

        private String prefix;

        private String classifier;

        public void setPrefix( String prefix )
        {
            this.prefix = prefix;
        }

        @Override
        public String getClassifier()
        {
            return classifier;
        }

        public void setAttachments( String attachments )
        {
            if ( "sources".equals( attachments ) )
            {
                classifier = "*-sources";
            }
            else if ( "javadoc".equals( attachments ) )
            {
                classifier = "*-javadoc";
            }
            else
            {
                throw new BuildException( "The attachment type '" + attachments
                    + "' is not defined, must be one of 'sources' or 'javadoc'" );
            }
        }

        @Override
        public void process( Artifact artifact )
        {
            StringBuilder buffer = new StringBuilder( 256 );
            if ( prefix != null && prefix.length() > 0 )
            {
                buffer.append( prefix );
                if ( !prefix.endsWith( "." ) )
                {
                    buffer.append( '.' );
                }
            }
            buffer.append( artifact.getGroupId() );
            buffer.append( ':' );
            buffer.append( artifact.getArtifactId() );
            buffer.append( ':' );
            buffer.append( artifact.getExtension() );
            if ( artifact.getClassifier().length() > 0 )
            {
                buffer.append( ':' );
                buffer.append( artifact.getClassifier() );
            }

            String path = artifact.getFile().getAbsolutePath();

            getProject().setProperty( buffer.toString(), path );
        }

    }

    private static class Group
    {

        private final String classifier;

        private final List<ArtifactConsumer> consumers = new ArrayList<ArtifactConsumer>();

        private final List<ArtifactRequest> requests = new ArrayList<ArtifactRequest>();

        public Group( String classifier )
        {
            this.classifier = classifier;
        }

        public boolean isAttachments()
        {
            return classifier != null;
        }

        public void add( ArtifactConsumer consumer )
        {
            consumers.add( consumer );
        }

        public void createRequests( DependencyNode node, boolean transitive )
        {
            createRequests( node, transitive, new LinkedList<DependencyNode>() );
        }

        private void createRequests( DependencyNode node, boolean transitive, LinkedList<DependencyNode> parents )
        {
            if ( node.getDependency() != null )
            {
                for ( ArtifactConsumer consumer : consumers )
                {
                    if ( consumer.accept( node, parents ) )
                    {
                        ArtifactRequest request = new ArtifactRequest( node );
                        if ( classifier != null )
                        {
                            request.setArtifact( new SubArtifact( request.getArtifact(), classifier, "jar" ) );
                        }
                        requests.add( request );
                        break;
                    }
                }
            }

            if ( transitive || parents.isEmpty() )
            {
                parents.addFirst( node );

                for ( DependencyNode child : node.getChildren() )
                {
                    createRequests( child, transitive, parents );
                }

                parents.removeFirst();
            }
        }

        public List<ArtifactRequest> getRequests()
        {
            return requests;
        }

        public void processResults( List<ArtifactResult> results )
        {
            for ( ArtifactResult result : results )
            {
                if ( !result.isResolved() )
                {
                    continue;
                }
                for ( ArtifactConsumer consumer : consumers )
                {
                    if ( consumer.accept( result.getRequest().getDependencyNode(),
                                          Collections.<DependencyNode> emptyList() ) )
                    {
                        consumer.process( result.getArtifact() );
                    }
                }
            }
        }

    }

}
