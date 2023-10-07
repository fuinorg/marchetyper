/**
 * Copyright (C) 2023 Future Invent IT Consulting GmbH. All rights reserved.
 * http://www.fuin.org/
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.marchetyper.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.fuin.utils4j.JaxbUtils;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

/**
 * Test for the {@link Archetype} class.
 */
class ArchetypeTest {

    @Test
    void testMarshal() throws Exception {

        // PREPARE
        final Property property = new Property("groupId", "com.mycompany", "org.fuin.examples");
        final Archetype testee = new Archetype.Builder().groupId("org.fuin.archetypes").artifactId("example-archetype")
                .version("0.1.0-SNAPSHOT").addProperty(property).build();

        // TEST
        final String actualXml = JaxbUtils.marshal(testee, Archetype.class);

        // VERIFY
        final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<archetype group-id=\"org.fuin.archetypes\" artifact-id=\"example-archetype\" version=\"0.1.0-SNAPSHOT\">"
                + "<property name=\"groupId\" default=\"com.mycompany\" test=\"org.fuin.examples\" />" + "</archetype>";
        XmlAssert.assertThat(actualXml).and(expectedXml).areIdentical();

    }

    @Test
    void testUnmarshal() throws Exception {

        // PREPARE
        final String xml = "<archetype group-id=\"org.fuin.archetypes\" artifact-id=\"example-archetype\" version=\"0.1.0-SNAPSHOT\">"
                + "<property name=\"groupId\" default=\"com.mycompany\" test=\"org.fuin.examples\" />" + "</archetype>";

        // TEST
        final Archetype testee = JaxbUtils.unmarshal(xml, Archetype.class);

        // VERIFY
        assertThat(testee).isNotNull();
        assertThat(testee.getGroupId()).isEqualTo("org.fuin.archetypes");
        assertThat(testee.getArtifactId()).isEqualTo("example-archetype");
        assertThat(testee.getVersion()).isEqualTo("0.1.0-SNAPSHOT");
        assertThat(testee.getProperties()).hasSize(1);
        final Property copy = testee.getProperties().get(0);
        assertThat(copy.getName()).isEqualTo("groupId");

    }

    @Test
    void testFind() throws Exception {

        // PREPARE
        final Property property = new Property("groupId", "com.mycompany", "org.fuin.examples");
        final Archetype testee = new Archetype.Builder().groupId("org.fuin.archetypes").artifactId("example-archetype")
                .version("0.1.0-SNAPSHOT").addProperty(property).build();

        // TEST
        final Property found = testee.findProperty("groupId");

        // VERIFY
        assertThat(found.getName()).isEqualTo("groupId");

        // TEST
        assertThat(testee.findProperty("foo")).isNull();

    }

    @Test
    void testToArchetypeGenerateArgs() {

        // PREPARE
        final Property property = new Property("groupId", "com.mycompany", "org.fuin.examples");
        final Archetype testee = new Archetype.Builder().groupId("org.fuin.archetypes").artifactId("example-archetype")
                .version("0.1.0-SNAPSHOT").addProperty(property).build();

        // TEST
        final List<String> result = testee.toArchetypeGenerateArgs(new File("."));

        // VERIFY
        assertThat(result).containsOnly("-DarchetypeGroupId=org.fuin.archetypes", "-DarchetypeArtifactId=example-archetype",
                "-DarchetypeVersion=0.1.0-SNAPSHOT", "-DgroupId=org.fuin.examples", "-DinteractiveMode=false");

    }

    @Test
    public void testRetrieveVersion() {
        assertThat(Archetype.retrieveVersion(null, "0.1.0")).isEqualTo("0.1.0");
        assertThat(Archetype.retrieveVersion(new File("src/test/resources/example-pom.xml"), null)).isEqualTo("1.0.0");
    }

}
