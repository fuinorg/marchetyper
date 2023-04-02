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

import org.fuin.utils4j.JaxbUtils;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj3.XmlAssert;

/**
 * Test for the {@link Variable} class.
 */
class TestVariable {

    @Test
    void testMarshal() throws Exception {

        // PREPARE
        final Variable testee = new Variable("pkgPath", "pkgName", TransformationType.DOT_2_SLASH);

        // TEST
        final String result = JaxbUtils.marshal(testee, Variable.class);

        // VERIFY
        XmlAssert.assertThat(result).and("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<variable name=\"pkgPath\" source=\"pkgName\" transform=\"DOT_2_SLASH\" />").areIdentical();

    }

    @Test
    void testUnmarshal() throws Exception {

        // TEST
        final Variable testee = JaxbUtils.unmarshal("<variable name=\"pkgPath\" source=\"pkgName\" transform=\"DOT_2_SLASH\" />",
                Variable.class);

        // VERIFY
        assertThat(testee).isNotNull();
        assertThat(testee.getName()).isEqualTo("pkgPath");
        assertThat(testee.getSource()).isEqualTo("pkgName");
        assertThat(testee.getTransformation()).isEqualTo(TransformationType.DOT_2_SLASH);

    }

}
