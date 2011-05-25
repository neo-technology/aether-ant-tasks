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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.ant.types.Authentication;
import org.sonatype.aether.ant.types.Dependency;
import org.sonatype.aether.ant.types.Exclusion;
import org.sonatype.aether.ant.types.Proxy;
import org.sonatype.aether.ant.types.RemoteRepositories;
import org.sonatype.aether.ant.types.RemoteRepository;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.DefaultArtifactType;

public class ConverterUtils
{

    private static org.sonatype.aether.artifact.Artifact toArtifact( Dependency dependency, ArtifactTypeRegistry types )
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

    static org.sonatype.aether.repository.Authentication toAuthentication( Authentication auth )
    {
        if ( auth == null )
        {
            return null;
        }
        return new org.sonatype.aether.repository.Authentication( auth.getUsername(), auth.getPassword(),
                                                                  auth.getPrivateKeyFile(), auth.getPassphrase() );
    }

    public static org.sonatype.aether.graph.Dependency toDependency( Dependency dependency, List<Exclusion> exclusions,
                                                                     RepositorySystemSession session )
    {
        return new org.sonatype.aether.graph.Dependency( toArtifact( dependency, session.getArtifactTypeRegistry() ),
                                                         dependency.getScope(), false,
                                                         toExclusions( dependency.getExclusions(), exclusions ) );
    }

    /**
     * Converts the given ant repository type to an Aether repository instance with authentication and proxy filled in
     * via the sessions' selectors.
     */
    public static org.sonatype.aether.repository.RemoteRepository toDistRepository( RemoteRepository repo,
                                                                       RepositorySystemSession session )
    {
        org.sonatype.aether.repository.RemoteRepository result = toRepository( repo );
        result.setAuthentication( session.getAuthenticationSelector().getAuthentication( result ) );
        result.setProxy( session.getProxySelector().getProxy( result ) );
        return result;
    }

    private static org.sonatype.aether.graph.Exclusion toExclusion( Exclusion exclusion )
    {
        return new org.sonatype.aether.graph.Exclusion( exclusion.getGroupId(), exclusion.getArtifactId(),
                                                        exclusion.getClassifier(), exclusion.getExtension() );
    }

    private static Collection<org.sonatype.aether.graph.Exclusion> toExclusions( Collection<Exclusion> exclusions1,
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

    private static RepositoryPolicy toPolicy( RemoteRepository.Policy policy, boolean enabled, String updates,
                                              String checksums )
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

    /**
     * Adds every &lt;String, String>-entry in the map as a property to the given Properties.
     */
    static Properties addProperties( Properties props, Map<?, ?> map )
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

    static org.sonatype.aether.repository.Proxy toProxy( Proxy proxy )
    {
        if ( proxy == null )
        {
            return null;
        }
        return new org.sonatype.aether.repository.Proxy( proxy.getType(), proxy.getHost(), proxy.getPort(),
                                                         toAuthentication( proxy.getAuthentication() ) );
    }

    private static org.sonatype.aether.repository.RemoteRepository toRepository( RemoteRepository repo )
    {
        org.sonatype.aether.repository.RemoteRepository result = new org.sonatype.aether.repository.RemoteRepository();
        result.setId( repo.getId() );
        result.setContentType( repo.getType() );
        result.setUrl( repo.getUrl() );
        result.setPolicy( true,
                          toPolicy( repo.getSnapshotPolicy(), repo.isSnapshots(), repo.getUpdates(),
                                    repo.getChecksums() ) );
        result.setPolicy( false,
                          toPolicy( repo.getReleasePolicy(), repo.isReleases(), repo.getUpdates(), repo.getChecksums() ) );
        result.setAuthentication( toAuthentication( repo.getAuthentication() ) );
        return result;
    }

    static List<org.sonatype.aether.repository.RemoteRepository> toRepositories( Project project,
                                                                          RepositorySystemSession session,
                                                                          RemoteRepositories repos, RemoteRepositoryManager remoteRepositoryManager )
    {
        List<RemoteRepository> repositories;

        if ( repos != null )
        {
            repositories = repos.getRepositories();
        }
        else
        {
            repositories = new ArrayList<RemoteRepository>();
        }

        List<org.sonatype.aether.repository.RemoteRepository> results =
            new ArrayList<org.sonatype.aether.repository.RemoteRepository>();
        for ( RemoteRepository repo : repositories )
        {
            results.add( toRepository( repo ) );
        }

        results =
            remoteRepositoryManager.aggregateRepositories( session,
                                                      Collections.<org.sonatype.aether.repository.RemoteRepository> emptyList(),
                                                      results, true );

        return results;
    }

}
