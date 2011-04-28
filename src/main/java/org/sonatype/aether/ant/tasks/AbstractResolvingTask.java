package org.sonatype.aether.ant.tasks;

import java.util.List;

import org.apache.maven.model.Model;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.AntRepoSys;
import org.sonatype.aether.ant.types.Dependencies;
import org.sonatype.aether.ant.types.Dependency;
import org.sonatype.aether.ant.types.Exclusion;
import org.sonatype.aether.ant.types.LocalRepository;
import org.sonatype.aether.ant.types.RemoteRepositories;
import org.sonatype.aether.ant.types.RemoteRepository;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.DependencyNode;

public abstract class AbstractResolvingTask
    extends Task
{

    protected Dependencies dependencies;

    protected RemoteRepositories remoteRepositories;

    protected LocalRepository localRepository;

    public void addDependencies( Dependencies dependencies )
    {
        if ( this.dependencies != null )
        {
            throw new BuildException( "You must not specify multiple <dependencies> elements" );
        }
        this.dependencies = dependencies;
    }

    public void setDependenciesRef( Reference ref )
    {
        if ( dependencies == null )
        {
            dependencies = new Dependencies();
            dependencies.setProject( getProject() );
        }
        dependencies.setRefid( ref );
    }

    public LocalRepository createLocalRepo()
    {
        if ( localRepository != null )
        {
            throw new BuildException( "You must not specify multiple <localRepo> elements" );
        }
        localRepository = new LocalRepository( this );
        return localRepository;
    }

    private RemoteRepositories getRemoteRepos()
    {
        if ( remoteRepositories == null )
        {
            remoteRepositories = new RemoteRepositories();
            remoteRepositories.setProject( getProject() );
        }
        return remoteRepositories;
    }

    public void addRemoteRepo( RemoteRepository repository )
    {
        getRemoteRepos().addRemoterepo( repository );
    }

    public void addRemoteRepos( RemoteRepositories repositories )
    {
        getRemoteRepos().addRemoterepos( repositories );
    }

    public void setRemoteReposRef( Reference ref )
    {
        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject( getProject() );
        repos.setRefid( ref );
        getRemoteRepos().addRemoterepos( repos );
    }

    protected CollectResult collectDependencies()
    {
        AntRepoSys sys = AntRepoSys.getInstance( getProject() );
        RepositorySystem system = sys.getSystem();
        RepositorySystemSession session = sys.getSession( this, localRepository );

        List<org.sonatype.aether.repository.RemoteRepository> repos = sys.toRepos( remoteRepositories, session );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRequestContext( "project" );

        for ( org.sonatype.aether.repository.RemoteRepository repo : repos )
        {
            log( "Using remote repository " + repo, Project.MSG_VERBOSE );
            collectRequest.addRepository( repo );
        }

        if ( dependencies != null )
        {

            List<Exclusion> globalExclusions = dependencies.getExclusions();
            for ( Dependency dep : dependencies.getDependencies() )
            {
                collectRequest.addDependency( sys.toDependency( dep, globalExclusions, session ) );
            }

            if ( dependencies.getPom() != null )
            {
                Model model = dependencies.getPom().getModel( this );
                for ( org.apache.maven.model.Dependency dep : model.getDependencies() )
                {
                    Dependency dependency = new Dependency();
                    dependency.setArtifactId( dep.getArtifactId() );
                    dependency.setClassifier( dep.getClassifier() );
                    dependency.setGroupId( dep.getGroupId() );
                    dependency.setScope( dep.getScope() );
                    dependency.setVersion( dep.getVersion() );
                    collectRequest.addDependency( sys.toDependency( dependency, globalExclusions, session ) );
                }
            }
        }

        log( "Collecting dependencies", Project.MSG_VERBOSE );

        DependencyNode root;
        CollectResult result;
        try
        {
            result = system.collectDependencies( session, collectRequest );
            root = result.getRoot();
        }
        catch ( DependencyCollectionException e )
        {
            throw new BuildException( "Could not collect dependencies: " + e.getMessage(), e );
        }

        root.accept( new DependencyGraphLogger( this ) );
        return result;
    }

}