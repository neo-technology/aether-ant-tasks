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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Reference;
import org.sonatype.aether.ConfigurationProperties;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.types.Authentication;
import org.sonatype.aether.ant.types.Dependency;
import org.sonatype.aether.ant.types.Exclusion;
import org.sonatype.aether.ant.types.LocalRepository;
import org.sonatype.aether.ant.types.Mirror;
import org.sonatype.aether.ant.types.Pom;
import org.sonatype.aether.ant.types.Proxy;
import org.sonatype.aether.ant.types.RemoteRepositories;
import org.sonatype.aether.ant.types.RemoteRepository;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.repository.AuthenticationSelector;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.MirrorSelector;
import org.sonatype.aether.repository.ProxySelector;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.spi.log.Logger;
import org.sonatype.aether.util.DefaultRepositoryCache;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.DefaultArtifactType;
import org.sonatype.aether.util.repository.ConservativeAuthenticationSelector;
import org.sonatype.aether.util.repository.DefaultAuthenticationSelector;
import org.sonatype.aether.util.repository.DefaultMirrorSelector;
import org.sonatype.aether.util.repository.DefaultProxySelector;

/**
 * @author Benjamin Bentmann
 */
public class AntRepoSys
{

    private static final String ID = "aether";

    private static final String ID_CENTRAL = "central";

    private static final String ID_DEFAULT_REPOS = ID + ".repositories";

    private static final String PROPERTY_OFFLINE = "aether.offline";

    private static final String SETTINGS_XML = "settings.xml";

    private static boolean OS_WINDOWS = Os.isFamily( "windows" );

    private static final ModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();

    private static final SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();

    private static final SettingsDecrypter settingsDecrypter = new AntSettingsDecryptorFactory().newInstance();

    private Project project;

    private AntServiceLocator locator;

    private RepositorySystem repoSys;

    private RemoteRepositoryManager remoteRepoMan;

    private File userSettings;

    private File globalSettings;

    private Settings settings;

    private List<Mirror> mirrors = new CopyOnWriteArrayList<Mirror>();

    private List<Proxy> proxies = new CopyOnWriteArrayList<Proxy>();

    private List<Authentication> authentications = new CopyOnWriteArrayList<Authentication>();

    private LocalRepository localRepository;

    private Pom defaultPom;

    private static <T> boolean eq( T o1, T o2 )
    {
        return ( o1 == null ) ? o2 == null : o1.equals( o2 );
    }

    public static synchronized AntRepoSys getInstance( Project project )
    {
        Object obj = project.getReference( ID );
        if ( obj instanceof AntRepoSys )
        {
            return (AntRepoSys) obj;
        }
        AntRepoSys instance = new AntRepoSys( project );
        project.addReference( ID, instance );
        instance.initDefaults();
        return instance;
    }

    private AntRepoSys( Project project )
    {
        this.project = project;

        locator = new AntServiceLocator( project );
        locator.setServices( Logger.class, new AntLogger( project ) );
        locator.setServices( ModelBuilder.class, modelBuilder );
        locator.setServices( WagonProvider.class, new AntWagonProvider() );
        locator.addService( ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class );
        locator.addService( ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class );
        locator.addService( VersionResolver.class, DefaultVersionResolver.class );
        locator.addService( VersionRangeResolver.class, DefaultVersionRangeResolver.class );
        locator.addService( MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class );
        locator.addService( MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class );
        locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );
    }

    private void initDefaults()
    {
        RemoteRepository repo = new RemoteRepository();
        repo.setProject( project );
        repo.setId( "central" );
        repo.setUrl( "http://repo1.maven.org/maven2/" );
        project.addReference( ID_CENTRAL, repo );

        repo = new RemoteRepository();
        repo.setProject( project );
        repo.setRefid( new Reference( project, ID_CENTRAL ) );
        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject( project );
        repos.addRemoterepo( repo );
        project.addReference( ID_DEFAULT_REPOS, repos );
    }

    public synchronized RepositorySystem getSystem()
    {
        if ( repoSys == null )
        {
            repoSys = locator.getService( RepositorySystem.class );
            if ( repoSys == null )
            {
                throw new BuildException( "The repository system could not be initialized" );
            }
        }
        return repoSys;
    }

    private synchronized RemoteRepositoryManager getRemoteRepoMan()
    {
        if ( remoteRepoMan == null )
        {
            remoteRepoMan = locator.getService( RemoteRepositoryManager.class );
            if ( remoteRepoMan == null )
            {
                throw new BuildException( "The repository system could not be initialized" );
            }
        }
        return remoteRepoMan;
    }

    public RepositorySystemSession getSession( Task task, LocalRepository localRepo )
    {
        DefaultRepositorySystemSession session = new MavenRepositorySystemSession();

        session.getConfigProperties().put( ConfigurationProperties.USER_AGENT,
                                           "Apache-Ant/" + project.getProperty( "ant.version" ) + " Aether" );

        session.setNotFoundCachingEnabled( false );
        session.setTransferErrorCachingEnabled( false );

        session.setOffline( isOffline() );
        session.setUserProps( project.getUserProperties() );

        session.setLocalRepositoryManager( getLocalRepoMan( localRepo ) );

        session.setProxySelector( getProxySelector() );
        session.setMirrorSelector( getMirrorSelector() );
        session.setAuthenticationSelector( getAuthSelector() );

        session.setCache( new DefaultRepositoryCache() );

        session.setRepositoryListener( new AntRepositoryListener( task ) );
        session.setTransferListener( new AntTransferListener( task ) );

        return session;
    }

    private boolean isOffline()
    {
        String prop = project.getProperty( PROPERTY_OFFLINE );
        if ( prop != null )
        {
            return Boolean.parseBoolean( prop );
        }
        return getSettings().isOffline();
    }

    private File getDefaultLocalRepoDir()
    {
        Settings settings = getSettings();
        if ( settings.getLocalRepository() != null )
        {
            return new File( settings.getLocalRepository() );
        }
        return new File( new File( project.getProperty( "user.home" ), ".m2" ), "repository" );
    }

    private LocalRepositoryManager getLocalRepoMan( LocalRepository localRepo )
    {
        if ( localRepo == null )
        {
            localRepo = localRepository;
        }

        File repoDir;
        if ( localRepo != null && localRepo.getDir() != null )
        {
            repoDir = localRepo.getDir();
        }
        else
        {
            repoDir = getDefaultLocalRepoDir();
        }

        org.sonatype.aether.repository.LocalRepository repo =
            new org.sonatype.aether.repository.LocalRepository( repoDir );

        return getSystem().newLocalRepositoryManager( repo );
    }

    private synchronized Settings getSettings()
    {
        if ( settings == null )
        {
            DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
            request.setUserSettingsFile( getUserSettings() );
            request.setGlobalSettingsFile( getGlobalSettings() );
            request.setSystemProperties( getSystemProperties() );
            request.setUserProperties( getUserProperties() );

            try
            {
                settings = settingsBuilder.build( request ).getEffectiveSettings();
            }
            catch ( SettingsBuildingException e )
            {
                project.log( "Could not process settings.xml: " + e.getMessage(), e, Project.MSG_WARN );
            }

            SettingsDecryptionResult result =
                settingsDecrypter.decrypt( new DefaultSettingsDecryptionRequest( settings ) );
            settings.setServers( result.getServers() );
            settings.setProxies( result.getProxies() );
        }
        return settings;
    }

    private ProxySelector getProxySelector()
    {
        DefaultProxySelector selector = new DefaultProxySelector();

        for ( Proxy proxy : proxies )
        {
            selector.add( toProxy( proxy ), proxy.getNonProxyHosts() );
        }

        Settings settings = getSettings();
        for ( org.apache.maven.settings.Proxy proxy : settings.getProxies() )
        {
            org.sonatype.aether.repository.Authentication auth = null;
            if ( proxy.getUsername() != null || proxy.getPassword() != null )
            {
                auth = new org.sonatype.aether.repository.Authentication( proxy.getUsername(), proxy.getPassword() );
            }
            selector.add( new org.sonatype.aether.repository.Proxy( proxy.getProtocol(), proxy.getHost(),
                                                                    proxy.getPort(), auth ),
                          proxy.getNonProxyHosts() );
        }

        return selector;
    }

    private MirrorSelector getMirrorSelector()
    {
        DefaultMirrorSelector selector = new DefaultMirrorSelector();

        for ( Mirror mirror : mirrors )
        {
            selector.add( mirror.getId(), mirror.getUrl(), mirror.getType(), false, mirror.getMirrorOf(), null );
        }

        Settings settings = getSettings();
        for ( org.apache.maven.settings.Mirror mirror : settings.getMirrors() )
        {
            selector.add( String.valueOf( mirror.getId() ), mirror.getUrl(), mirror.getLayout(), false,
                          mirror.getMirrorOf(), mirror.getMirrorOfLayouts() );
        }

        return selector;
    }

    private AuthenticationSelector getAuthSelector()
    {
        DefaultAuthenticationSelector selector = new DefaultAuthenticationSelector();

        Collection<String> ids = new HashSet<String>();
        for ( Authentication auth : authentications )
        {
            List<String> servers = auth.getServers();
            if ( !servers.isEmpty() )
            {
                org.sonatype.aether.repository.Authentication a = toAuth( auth );
                for ( String server : servers )
                {
                    if ( ids.add( server ) )
                    {
                        selector.add( server, a );
                    }
                }
            }
        }

        Settings settings = getSettings();
        for ( Server server : settings.getServers() )
        {
            org.sonatype.aether.repository.Authentication auth =
                new org.sonatype.aether.repository.Authentication( server.getUsername(), server.getPassword(),
                                                        server.getPrivateKey(), server.getPassphrase() );
            selector.add( server.getId(), auth );
        }

        return new ConservativeAuthenticationSelector( selector );
    }

    public org.sonatype.aether.graph.Dependency toDependency( Dependency dependency, List<Exclusion> exclusions,
                                                        RepositorySystemSession session )
    {
        return new org.sonatype.aether.graph.Dependency( toArtifact( dependency, session.getArtifactTypeRegistry() ),
                                                   dependency.getScope(), false,
                                                   toExclusions( dependency.getExclusions(), exclusions ) );
    }

    private Collection<org.sonatype.aether.graph.Exclusion> toExclusions( Collection<Exclusion> exclusions1,
                                                                    Collection<Exclusion> exclusions2 )
    {
        Collection<org.sonatype.aether.graph.Exclusion> results =
            new LinkedHashSet<org.sonatype.aether.graph.Exclusion>();
        if ( exclusions1 != null )
        {
            for ( Exclusion exclusion : exclusions1 )
            {
                results.add( toExclusion( exclusion ) );
            }
        }
        if ( exclusions2 != null )
        {
            for ( Exclusion exclusion : exclusions2 )
            {
                results.add( toExclusion( exclusion ) );
            }
        }
        return results;
    }

    private org.sonatype.aether.graph.Exclusion toExclusion( Exclusion exclusion )
    {
        return new org.sonatype.aether.graph.Exclusion( exclusion.getGroupId(), exclusion.getArtifactId(),
                                                  exclusion.getClassifier(), exclusion.getExtension() );
    }

    private org.sonatype.aether.artifact.Artifact toArtifact( Dependency dependency, ArtifactTypeRegistry types )
    {
        ArtifactType type = types.get( dependency.getType() );
        if ( type == null )
        {
            type = new DefaultArtifactType( dependency.getType() );
        }

        Artifact artifact =
            new DefaultArtifact( dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), null,
                                 dependency.getVersion(), type );

        if ( "system".equals( dependency.getScope() ) )
        {
            artifact = artifact.setFile( dependency.getSystemPath() );
        }

        return artifact;
    }

    private org.sonatype.aether.repository.Proxy toProxy( Proxy proxy )
    {
        if ( proxy == null )
        {
            return null;
        }
        return new org.sonatype.aether.repository.Proxy( proxy.getType(), proxy.getHost(), proxy.getPort(),
                                              toAuth( proxy.getAuthentication() ) );
    }

    private org.sonatype.aether.repository.Authentication toAuth( Authentication auth )
    {
        if ( auth == null )
        {
            return null;
        }
        return new org.sonatype.aether.repository.Authentication( auth.getUsername(), auth.getPassword(),
                                                       auth.getPrivateKeyFile(), auth.getPassphrase() );
    }

    private RemoteRepositories getRepos( RemoteRepositories repos )
    {
        if ( repos == null )
        {
            Object obj = project.getReference( ID_DEFAULT_REPOS );
            if ( obj instanceof RemoteRepositories )
            {
                repos = (RemoteRepositories) obj;
            }
        }
        return repos;
    }

    public org.sonatype.aether.repository.RemoteRepository toDistRepo( RemoteRepository repo,
                                                                       RepositorySystemSession session )
    {
        org.sonatype.aether.repository.RemoteRepository result = toRepo( repo );
        result.setAuthentication( session.getAuthenticationSelector().getAuthentication( result ) );
        result.setProxy( session.getProxySelector().getProxy( result ) );
        return result;
    }

    public List<org.sonatype.aether.repository.RemoteRepository> toRepos( RemoteRepositories repos,
                                                                          RepositorySystemSession session )
    {
        repos = getRepos( repos );

        List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();
        if ( repos != null )
        {
            repositories = repos.getRepositories();
        }

        List<org.sonatype.aether.repository.RemoteRepository> results =
            new ArrayList<org.sonatype.aether.repository.RemoteRepository>();
        for ( RemoteRepository repo : repositories )
        {
            results.add( toRepo( repo ) );
        }

        results =
            getRemoteRepoMan().aggregateRepositories( session,
                                                      Collections.<org.sonatype.aether.repository.RemoteRepository> emptyList(),
                                                      results, true );

        return results;
    }

    private org.sonatype.aether.repository.RemoteRepository toRepo( RemoteRepository repo )
    {
        org.sonatype.aether.repository.RemoteRepository result = new org.sonatype.aether.repository.RemoteRepository();
        result.setId( repo.getId() );
        result.setContentType( repo.getType() );
        result.setUrl( repo.getUrl() );
        result.setPolicy( true, toPolicy( repo.getSnapshotPolicy(), repo.isSnapshots(), repo.getUpdates(),
                                          repo.getChecksums() ) );
        result.setPolicy( false, toPolicy( repo.getReleasePolicy(), repo.isReleases(), repo.getUpdates(),
                                           repo.getChecksums() ) );
        result.setAuthentication( toAuth( repo.getAuthentication() ) );
        return result;
    }

    private RepositoryPolicy toPolicy( RemoteRepository.Policy policy, boolean enabled, String updates, String checksums )
    {
        if ( policy != null )
        {
            enabled = policy.isEnabled();
            if ( policy.getChecksums() != null )
            {
                checksums = policy.getChecksums();
            }
            if ( policy.getUpdates() != null )
            {
                updates = policy.getUpdates();
            }
        }
        return new RepositoryPolicy( enabled, updates, checksums );
    }

    public synchronized void setUserSettings( File file )
    {
        if ( !eq( this.userSettings, file ) )
        {
            settings = null;
        }
        this.userSettings = file;
    }

    /* UT */File getUserSettings()
    {
        if ( userSettings == null )
        {
            File userHome = new File( project.getProperty( "user.home" ) );
            File file = new File( new File( userHome, ".ant" ), SETTINGS_XML );
            if ( file.isFile() )
            {
                return file;
            }
            else
            {
                return new File( new File( userHome, ".m2" ), SETTINGS_XML );
            }

        }
        return userSettings;
    }

    public void setGlobalSettings( File file )
    {
        if ( !eq( this.globalSettings, file ) )
        {
            settings = null;
        }
        this.globalSettings = file;
    }

    /* UT */File getGlobalSettings()
    {
        if ( globalSettings == null )
        {
            File file = new File( new File( project.getProperty( "ant.home" ), "etc" ), SETTINGS_XML );
            if ( file.isFile() )
            {
                return file;
            }
            else
            {
                String mavenHome = getMavenHome();
                if ( mavenHome != null )
                {
                    return new File( new File( mavenHome, "conf" ), SETTINGS_XML );
                }
            }

        }
        return globalSettings;
    }

    private String getMavenHome()
    {
        String mavenHome = project.getProperty( "maven.home" );
        if ( mavenHome != null )
        {
            return mavenHome;
        }
        List<?> env = Execute.getProcEnvironment();
        for ( Object obj : env )
        {
            String var = obj.toString();
            if ( var.startsWith( "M2_HOME=" ) )
            {
                mavenHome = var.substring( "M2_HOME=".length() );
                return mavenHome;
            }
        }
        return null;
    }

    public void addProxy( Proxy proxy )
    {
        proxies.add( proxy );
    }

    public void addMirror( Mirror mirror )
    {
        mirrors.add( mirror );
    }

    public void addAuthentication( Authentication authentication )
    {
        authentications.add( authentication );
    }

    public void setLocalRepository( LocalRepository localRepository )
    {
        this.localRepository = localRepository;
    }

    public Model loadModel( Task task, File pomFile, boolean local, RemoteRepositories repos )
    {
        RepositorySystemSession session = getSession( task, null );

        List<org.sonatype.aether.repository.RemoteRepository> repositories = toRepos( repos, session );

        ModelResolver modelResolver =
            new AntModelResolver( session, "project", getSystem(), getRemoteRepoMan(), repositories );

        Settings settings = getSettings();

        try
        {
            DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
            request.setLocationTracking( true );
            request.setProcessPlugins( false );
            if ( local )
            {
                request.setPomFile( pomFile );
                request.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_STRICT );
            }
            else
            {
                request.setModelSource( new FileModelSource( pomFile ) );
                request.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
            }
            request.setSystemProperties( getSystemProperties() );
            request.setUserProperties( getUserProperties() );
            request.setProfiles( SettingsUtils.convert( settings.getProfiles() ) );
            request.setActiveProfileIds( settings.getActiveProfiles() );
            request.setModelResolver( modelResolver );
            return modelBuilder.build( request ).getEffectiveModel();
        }
        catch ( ModelBuildingException e )
        {
            throw new BuildException( "Could not load POM " + pomFile + ": " + e.getMessage(), e );
        }
    }

    private Properties getSystemProperties()
    {
        Properties props = new Properties();
        getEnvProperties( props );
        props.putAll( System.getProperties() );
        toProps( props, project.getProperties() );
        return props;
    }

    private Properties getEnvProperties( Properties props )
    {
        if ( props == null )
        {
            props = new Properties();
        }
        boolean envCaseInsensitive = OS_WINDOWS;
        for ( Map.Entry<String, String> entry : System.getenv().entrySet() )
        {
            String key = entry.getKey();
            if ( envCaseInsensitive )
            {
                key = key.toUpperCase( Locale.ENGLISH );
            }
            key = "env." + key;
            props.put( key, entry.getValue() );
        }
        return props;
    }

    private Properties getUserProperties()
    {
        return toProps( null, project.getUserProperties() );
    }

    private Properties toProps( Properties props, Map<?, ?> map )
    {
        if ( props == null )
        {
            props = new Properties();
        }
        for ( Map.Entry<?, ?> entry : map.entrySet() )
        {
            if ( entry.getKey() instanceof String && entry.getValue() instanceof String )
            {
                props.put( entry.getKey(), entry.getValue() );
            }
        }
        return props;
    }

    public void setDefaultPom( Pom pom )
    {
        this.defaultPom = pom;
    }

    public Pom getDefaultPom()
    {
        return defaultPom;
    }

}
