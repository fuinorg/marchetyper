# marchetyper
Makes Maven Archetype creation a breeze

[![Java Maven Build](https://github.com/fuinorg/marchetyper/actions/workflows/maven.yml/badge.svg)](https://github.com/fuinorg/marchetyper/actions/workflows/maven.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fuin.marchetyper/marchetyper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fuin.marchetyper/marchetyper/)
[![LGPLv3 License](http://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.gnu.org/licenses/lgpl.html)
[![Java Development Kit 11](https://img.shields.io/badge/JDK-11-green.svg)](https://openjdk.java.net/projects/jdk/11/)

## Versions
- 0.5.x (or later) = **Java 11** with new **jakarta** namespace
- 0.4.x = **Java 11** before namespace change from 'javax' to 'jakarta'

## What is it?
You can think about the **marchetyper** as a specialized program that copies files from a source directory to a target directory.
During this process, names of files, directories and the content of text files will be changed by predefined rules.
The source directory contains a fully functional Java application that is used to create a [Maven Archetype](https://maven.apache.org/guides/introduction/introduction-to-archetypes.html) from it. The target directory will contain the Archetype after copying is finished.
It also creates the necessary `archetype-metadata.xml` file automatically.

## Why?
You may argue "Creating a Maven Archetype from a project is already possible using the [create-from-project](https://maven.apache.org/archetype/maven-archetype-plugin/create-from-project-mojo.html) Mojo". You are right. Unfortunately it's pretty limited and does not allow much individual customization. That's where the **marchetyper** kicks in. It has a specialized configuration file and allows **repeating the creation process** every time your existing example project has changed. As your example project will change over time, having an *automated build* that automatically updates your Archetype is the major benefit of **marchetyper**.

## Getting started
See [marchetyper-archetype](https://github.com/fuinorg/marchetyper-archetype) - An specialized Archetype that allows automated creation of the necessary Maven multi-module project structure for your own Archetype build.

## Plugin
The plugin uses a config file named 'marchetyper-config.xml' in the same directory where the 'pom.xml' is.

```xml
<plugin>
    <groupId>org.fuin.marchetyper</groupId>
    <artifactId>marchetyper-maven-plugin</artifactId>
    <version>0.7.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>generate</id>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
You can set a different location/filename by using the plugin configuration property `<configFile>../wherever/config.xml</configFile>`.


## Configuration
The configuration file defines the search/replace and generation process. See [marchetyper-config.xml](test/src/test/resources/test-project/marchetyper-config.xml) for an example.

```xml
<marchetyper-config 

    <!-- The directory to create an archetype from -->
    src-dir="../example" 

    <!-- The directory to write the generated archetype to -->
    dest-dir="../archetype"

    <!-- A regular expression of files that will be copied 1:1 without change -->
    binary-files=".*\.(jar|gitignore)"

    <!-- A regular expression of files where search/replace will be applied -->
    text-files="(.*\.(properties|md|java|yml|yaml|xml))|Dockerfile">

    <variables>
        <!-- Creates a variable (pkgPath) based on a property (pkgName) and executes
             some predefined conversion. Here all '.' will be replaced by '/'. -->
        <variable name="pkgPath" source="pkgName" transform="DOT_2_SLASH" />
    </variables>

    <!-- group-id/artifact-id/version are the coordinates for the generated archetype.
    <archetype group-id="org.fuin.example-archetype" 
               artifact-id="marchetyper-test-archetype" 
               version="0.1.0-SNAPSHOT">
        <!-- Defines a property that the user must provide when generating a project from the archetype.
             The 'default' value will be used in case nothing is provided.
             The 'test' value will be used for testing purposes. -->
        <property name="groupId" default="com.mycompany" test="org.fuin.examples" />
        <property name="artifactId" default="myapp" test="example-app" />
        <property name="version" default="0.1.0-SNAPSHOT" test="0.1.0-SNAPSHOT" />
        <property name="pkgName" default="com.mycompany.myapp" test="org.fuin.examples.app" />
        <property name="description" default="My cool application" 
                  test="A minimal Java standalone application with Maven build" />
        <property name="appName" default="MyApp" test="ExampleApp" />
    </archetype>
     
    <!-- Replacement definition for path elements -->
    <path-mappings>
        <!-- The 'search' path will be replaced by 'replace' path in any path of a copied file. -->
        <mapping search="org/fuin/examples/app" 
                 replace="__pkgPath__" />
        <mapping search="ExampleApp" 
                         replace="__appName__" />
    </path-mappings>

    <!-- Replacement definition for text files -->
    <text-file-mappings>
        <!-- If the 'search' text is found, it will be replaced with 'replace' text 
             for all files defined with the 'file' regular expression. -->
        <mapping search="&lt;groupId&gt;org.fuin.examples&lt;/groupId&gt;" 
                 replace="&lt;groupId&gt;${groupId}&lt;/groupId&gt;" 
                 file="pom\.xml" />
        <mapping search="&lt;artifactId&gt;example-app&lt;/artifactId&gt;" 
                 replace="&lt;artifactId&gt;${artifactId}&lt;/artifactId&gt;" 
                 file="pom\.xml" />
        <mapping search="&lt;description&gt;A minimal Java standalone application with Maven build&lt;/description&gt;" 
                 replace="&lt;description&gt;${description}&lt;/description&gt;" 
                 file="pom\.xml" />
        <mapping search="&lt;company&gt;fuinorg&lt;/company&gt;" 
                 replace="&lt;company&gt;${company}&lt;/company&gt;" 
                 file="pom\.xml" />
        <mapping search="org.fuin.examples.app" 
                 replace="${pkgName}"
                 file=".*\.java" />
        <mapping search="org/fuin/examples/app" 
                 replace="${pkgPath}" 
                 file=".*\.md" />
        <mapping search="My cool application" 
                 replace="${description}" 
                 file=".*\.md" />
        <mapping search="example-app"
                 replace="${artifactId}"
                 file=".*\.md" />        
        <mapping search="ExampleApp"
                 replace="${appName}"
                 file=".*\.md" />        
    </text-file-mappings>

    <!-- Directories and files that will always be copied (also if part of 'file-excludes') -->
    <file-includes>
        <filter file=".gitignore" />
        <filter path=".*/\.mvn/.*" />
    </file-includes>

    <!-- Directories and files that will not be copied -->
    <file-excludes>
        <filter path=".*/test-project/example/target/.*" />
        <filter path=".*/\..*/.*"/>
        <filter file="^\..*" />
        <filter file=".*\.log" />
    </file-excludes>

</marchetyper-config>
```
## Special configuration

### Tag "marchetyper-config"
There are some special attributes for "marchetyper-config" that allow further customization:

| Attrribute           | Suggested value                | Description   |
| -------------------- | ------------------------------ | ------------- |
| custom-pom-file      | custom-pom.xml                 | Normally the 'pom.xml' of the archetype is generated automatically with minimal settings. As an alternative you can provide a custom POM that should be copied to the archetype instead. |
| post-generate-file   | archetype-post-generate.groovy | Sometimes it's necessary to execute some script during the process of generating the archetype. This option allows you to include the given Groovy script into the archetype. See [archetype-post-generate.groovy](test/src/test/resources/test-project/marchetyper/archetype-post-generate.groovy) for an example. |
| mask-dot-file        | true                           | There is a bug ([ARCHETYPE-505](https://issues.apache.org/jira/browse/ARCHETYPE-505)) in Maven archetypes creation that prevents including files starting with a dot. This is especially bad as you may want to include some files like ".gitignore" into an archetype. Setting the flag to "true" will include such files, but with and underscore as first character. That's why you must use it along with a script that removes the underscore when generaing the artifact. See "post-generate-file" before. |

### Tag "archetype"
There are some special attributes for "archetype" that allow further customization:

| Attrribute           | Suggested value                | Description   |
| -------------------- | ------------------------------ | ------------- |
| version-pom-file     | pom.xml                        | As an alternative to using a fixed version, you can use the version from a pom.xml that will be extracted during creation of the archetype. This can be used in case the build modifies the versions in 'pom.xml' or simply to make your life easier during the release process. |


## Example
See [test-project](test/src/test/resources/test-project) for a fully functional Maven multi module project with the suggested structure for your archetype project. 
There is also a [README.md](test/src/test/resources/test-project/README.md) included.

## Snapshots

Snapshots can be found on the [OSS Sonatype Snapshots Repository](http://oss.sonatype.org/content/repositories/snapshots/org/fuin "Snapshot Repository"). 

Add the following to your .m2/settings.xml to enable snapshots in your Maven build:

```xml
<repository>
    <id>sonatype.oss.snapshots</id>
    <name>Sonatype OSS Snapshot Repository</name>
    <url>http://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
