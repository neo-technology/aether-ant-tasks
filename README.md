= Aether Ant Tasks

The Aether Ant tasks use the Aether library to resolve dependencies and install and deploy locally built artifacts.

== Settings

The Ant tasks are tightly integrated with the usual maven settings.xml. By default, the usual $HOME/.m2/settings.xml is used. The <settings/> definition is used to change that:

    <settings file="my-settings.xml"/>

Some settings defined in the settings file or in the POM can also be changed inside the ant file.

=== Proxy Settings

    <proxy host="" port="" type="http" nonProxyHosts="foo,bar" />

=== Authentication

    <authentication username="login" password="pw" id="auth"/>
    <authentication privateKeyFile="file.pk" passphrase="phrase" servers="distrepo" id="distauth"/>

=== Local Repository

    <localrepo dir="someDir"/>

=== Remote Repositories

Remote repositories may be defined directly:

    <remoterepo id="rso" url="http://repository.sonatype.org/" type="default" releases="true" snapshots="false" updates="always" checksums="fail"/>

    <remoterepo id="rao" url="http://repository.apache.org/">
        <repo:releases enabled="true" updates="daily" checksums="warn" />
        <repo:snapshots enabled="false" />
        <repo:authentication refid="auth" />
    </remoterepo>

    <remoterepo id="distrepo" url="..." authref="distauth"/>

Multiple repositories may be used as a group in every place that is legal for a remote repository:

    <remoterepos id="all">
        <remoterepo refid="rso"/>
        <remoterepo refid="rao"/>
        <remoterepo refid="distrepo"/>
    </remoterepos>

=== Mirrors

    <mirror id="" url="" mirrorOf=""/>

== Project

=== POM

The POM is the data type used to determine the target for the install and deploy tasks. They will use the POM that is set without an id by default.

    <pom file="pom.xml" id="pom"/>
    <pom groupId="g" artifactId="a" version="v"/>
    <pom coords="g:a:v"/>

==== Properties

If a POM is set via a file parameter and without any id, the properties interpolated from that POM are available for the ant build, e.g. ${pom.version}. User properties defined in that pom are mapped with "pom.properties." as prefix.

=== Attached Artifacts

    <artifact file="file-src.jar" type="jar" classifier="sources" id="src"/>

    <artifacts pomref="pom" id="producedArtifacts">
        <artifact refid="src"/>
	<artifact file="file-src.jar" />
    </artifacts>

=== Dependencies

    <dependency coords="g:a:v" />

    <dependency groupId="g" artifactId="a" version="v" classifier="c" type="jar" scope="runtime">
	<exclusion coords="g:a" />
	<exclusion groupId="g" artifactId="a" />
    </dependency>

    <dependencies id="deps">
    	<dependency refid="first"/>
    	<dependency refid="second"/>
    </dependencies>

== Tasks

=== Install

You need to set a POM that references a file for the install task to work.

    <install artifactsref="producedArtifacts"/>

=== Deploy

You need to set a POM that references a file for the deploy task to work.

    <deploy>
        <remoterepo refid="distrepo" authref="distauth"/>
    </deploy>

=== Resolve

The <resolve>-task is used to resolve and collect dependencies from remote servers. By default it only queries the remote repositories referenced by the name "aether.repositories" (only central).
It can filter the dependency tree by scope, enumerating included and/or excluded scope names ('provided,!system')

    <resolve>
        <dependencies>
            <dependency coords="org.apache.maven:maven-profile:2.0.6" />
            <exclusion artifactId="junit" />
            <exclusion groupId="org.codehaus.plexus" />
        </dependencies>
        <path refid="cp" classpath="compile" />
        <files refid="files" attachments="javadoc" dir="target/sources" layout="{artifactId}-{classifier}.{extension}" />
        <properties prefix="dep." scopes="provided,system"/>
    </resolve>

This task is able to assemble the collected dependencies in three different ways:

* Classpath: The <path> element defines a classpath with all resolved dependencies.
* Files: <files> will assemble a fileset containing all resolved dependencies.
* Properties: <properties> will set properties with the given prefix and the coordinates to the path to the resolved file.

    <resolve>
        <dependencies pomRef="pom"/>
        <remoterepositories refid="all"/>
        <path refid="cp" classpath="compile" />
    </resolve>

