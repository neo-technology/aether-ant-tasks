= Aether Ant Tasks

The Aether Ant tasks use the Aether library to resolve dependencies and install and deploy locally built artifacts.

== Settings

The Ant tasks are tightly integrated with the usual maven settings.xml. By
default, the usual $HOME/.m2/settings.xml is used for user settings. 

For the global settings, different paths will be tried:

* ${ant.home}/etc/settings.xml
* ${maven.home}/conf/settings.xml
* $M2_HOME/conf/settings.xml

The <settings/> definition is used to change that:

    <settings file="my-settings.xml" globalfile="myglobal-settings.xml" />

Some settings defined in the settings file or in the POM can also be changed inside the ant file.

=== Proxy Settings

Proxy definitions are used throughout the whole session. There may be multiple
proxies set. The proxy to use will be chosen by evaluating the nonProxyHosts on
each proxy definition.

    <proxy host="" port="" type="http" nonProxyHosts="foo,bar" />

=== Authentication

Authentication elements are used with the deploy and resolve tasks to access
the remote repositories. Every authentication definition will be added globally
and chosen based on the 'servers' attribute. If this attribute is not set, an
authentication has to be referenced explicitely to be used.

    <authentication username="login" password="pw" id="auth"/>
    <authentication privateKeyFile="file.pk" passphrase="phrase" servers="distrepo" id="distauth"/>

=== Local Repository

Only one local repository can be used at a time.

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

Multiple repositories may be used as a group in every place that is legal for a
remote repository:

    <remoterepos id="all">
        <remoterepo refid="rso"/>
        <remoterepo refid="rao"/>
        <remoterepo refid="distrepo"/>
    </remoterepos>

=== Mirrors

    <mirror id="" url="" mirrorOf=""/>

== Project

Project settings deal with locally availabe information about the build.

=== POM

The POM is the data type used to determine the target for the install and
deploy tasks. If you define a POM without an id based on a full pom.xml file,
that POM will be used by default for install and deploy.

    <pom file="pom.xml" id="pom"/>
    <pom groupId="g" artifactId="a" version="v"/>
    <pom coords="g:a:v"/>

==== Properties

If a POM is set via a file parameter and without any id, the properties
interpolated from that POM are available for the ant build, e.g.
${pom.version}. User properties defined in that pom are mapped with
"pom.properties." as prefix.

=== Attached Artifacts

<artifact> elements define the artifacts produced by this build that should be installed or deployed.

    <artifact file="file-src.jar" type="jar" classifier="sources" id="src"/>

    <artifacts pomref="pom" id="producedArtifacts">
        <artifact refid="src"/>
	<artifact file="file-src.jar" />
    </artifacts>

=== Dependencies

Dependencies are used to to create classpaths or filesets. They are used by
the <resolve>-task, which collects the artifacts belonging to the dependencies
transitively.

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

You need to set a POM that references a file for the deploy task to work, as that POM file will be deployed to repository.

    <deploy artifactsref="producedArtifacts">
        <repo refid="distrepo" authref="distauth"/>
	<snapshotrepo refid="snaprepo">
    </deploy>

=== Resolve

The <resolve>-task is used to resolve and collect dependencies from remote
servers. If no repositories are set explicitely for the task, the repositories
referenced by "aether.repositories" are used. This contains only central by
default, but can be overridden by supplying another repository definition with
this id. 


This task is able to assemble the collected dependencies in three different ways:

* Classpath: The <path> element defines a classpath with all resolved dependencies.
* Files: <files> will assemble a fileset containing all resolved dependencies.
* Properties: <properties> will set properties with the given prefix and the coordinates to the path to the resolved file.

These targets may also be mentioned more than once for the same resolve task,
but only one set of <dependencies/> is allowed.

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

    <resolve dependenciesref="deps">
        <path refid="cp.compile" classpath="compile" />
        <path refid="cp.test" classpath="test" />
    </resolve>

Scope filters can be set on every target, enumerating included and/or excluded
scope names ('provided,!system').

The classpath attribute is a shortcut for the scope filters (e.g.
classpath="compile" equals scope="provided,system,compile"). Valid values are
"compile", "runtime", "test".

    <resolve>
        <dependencies pomRef="pom"/>
        <remoterepositories refid="all"/>
        <path refid="cp" classpath="compile" />
    </resolve>

