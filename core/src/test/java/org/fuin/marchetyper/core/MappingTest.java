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

import org.fuin.utils4j.JaxbUtils;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

/**
 * Test for the {@link Mapping} class.
 */
class MappingTest {

    @Test
    void testMarshal() throws Exception {

        // PREPARE
        final Mapping testee = new Mapping("ExampleApp", "${appName}", ".*/dir/.*", ".*\\.md");

        // TEST
        final String result = JaxbUtils.marshal(testee, Mapping.class);

        // VERIFY
        XmlAssert.assertThat(result).and("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<mapping search=\"ExampleApp\" replace=\"${appName}\" path=\".*/dir/.*\" file=\".*\\.md\" />").areIdentical();

    }

    @Test
    void testUnmarshal() throws Exception {

        // TEST
        final Mapping testee = JaxbUtils
                .unmarshal("<mapping search=\"ExampleApp\" replace=\"${appName}\" path=\".*/dir/.*\" file=\".*\\.md\" />", Mapping.class);

        // VERIFY
        assertThat(testee).isNotNull();
        assertThat(testee.getSearch()).isEqualTo("ExampleApp");
        assertThat(testee.getReplace()).isEqualTo("${appName}");
        assertThat(testee.getPathExpr()).isEqualTo(".*/dir/.*");
        assertThat(testee.getFileExpr()).isEqualTo(".*\\.md");

    }

    @Test
    void testAppliesFileAndPathExpr() {
        final Mapping testee = new Mapping("ExampleApp", "${appName}", ".*/b/.*", ".*\\.md");
        assertThat(testee.applies(null, new File("a/b/c/xyz.md"))).isTrue();
        assertThat(testee.applies(null, new File("x/y/z/xyz.md"))).isFalse();
    }

    @Test
    void testAppliesFileExpr() {
        final Mapping testee = new Mapping("ExampleApp", "${appName}", null, ".*\\.md");
        assertThat(testee.applies(null, new File("a/b/c/xyz.md"))).isTrue();
        assertThat(testee.applies(null, new File("xyz.md"))).isTrue();
        assertThat(testee.applies(null, new File(".md"))).isTrue();
        assertThat(testee.applies(null, new File("x/y/z/xyz.bat"))).isFalse();
    }

    @Test
    void testAppliesPathExpr() {
        final Mapping testee = new Mapping("ExampleApp", "${appName}", ".*/b/.*", null);
        assertThat(testee.applies(null, new File("a/b/c/xyz.md"))).isTrue();
        assertThat(testee.applies(null, new File("a/b/xyz.md"))).isTrue();
        assertThat(testee.applies(null, new File("x/y/z/xyz.md"))).isFalse();
    }

    @Test
    void testAppliesDefaultRegExFilenameSelector() {
        final Mapping testee = new Mapping("ExampleApp", "${appName}", null, null);
        assertThat(testee.applies(".*\\.md", new File("a/b/c/xyz.md"))).isTrue();
        assertThat(testee.applies(".*\\.md", new File("a/b/xyz.md"))).isTrue();
        assertThat(testee.applies(".*\\.md", new File("xyz.md"))).isTrue();
        assertThat(testee.applies(".*\\.md", new File("x/y/z/xyz.txt"))).isFalse();
    }

    @Test
    void testAppliesAllNull() {
        final Mapping testee = new Mapping("ExampleApp", "${appName}", null, null);
        assertThat(testee.applies(null, new File("a/b/c/xyz.md"))).isTrue();
        assertThat(testee.applies(null, new File("a/b/xyz.md"))).isTrue();
        assertThat(testee.applies(null, new File("xyz.md"))).isTrue();
    }

}
