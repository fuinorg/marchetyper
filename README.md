# marchetyper
Makes Maven Archetype creation a breeze

[![Java Maven Build](https://github.com/fuinorg/marchetyper/actions/workflows/maven.yml/badge.svg)](https://github.com/fuinorg/marchetyper/actions/workflows/maven.yml)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fuin/marchetyper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fuin/marchetyper/)
[![LGPLv3 License](http://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.gnu.org/licenses/lgpl.html)
[![Java Development Kit 11](https://img.shields.io/badge/JDK-11-green.svg)](https://openjdk.java.net/projects/jdk/11/)

## Why?
Creating a Maven Archetype from a project is already possible using the [create-from-project](https://maven.apache.org/archetype/maven-archetype-plugin/create-from-project-mojo.html) Mojo. Unfortunately it's pretty limited and does not allow much individual customization. That's where the **marchetyper** kicks in. It has a specialized configuration file and allows repeating the creation process every time your existing example project has changed.

## Plugin
The plugin uses a config file named 'marchetyper-config.xml' in the same directory where the 'pom.xml' is.

```xml
<plugin>
    <groupId>org.fuin.marchetyper</groupId>
    <artifactId>marchetyper-maven-plugin</artifactId>
    <version>0.3.0</version>
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
