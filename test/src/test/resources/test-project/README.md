# test-project
Example that converts an example project to a Maven Archetype.

- **[archetype](archetype)** - The generated Maven Archetype project.
- **[example](example)** - The source example project to create a Maven Archetype from.

See [pom.xml](pom.xml) that contains the converter plugin:

```xml
<plugin>
    <groupId>org.fuin.marchetyper</groupId>
    <artifactId>marchetyper-maven-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</plugin>
```

In the root directory simply run a maven build:

```
mvn org.fuin.marchetyper:marchetyper-maven-plugin:generate
```

This will delete the [archetype](archetype) directory and recreate it with the converted content of the [example](example) project.

The conversion is configured with the [marchetyper-config.xml](marchetyper-config.xml) file.
