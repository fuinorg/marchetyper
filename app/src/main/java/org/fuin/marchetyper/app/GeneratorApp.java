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
package org.fuin.marchetyper.app;

import org.fuin.marchetyper.core.Config;
import org.fuin.marchetyper.core.ConfigImpl;
import org.fuin.marchetyper.core.MavenArchetyper;

import java.io.File;
import java.io.IOException;

/**
 * Generates an archetype based on an example.
 */
public class GeneratorApp {

    /**
     * Main entry point.
     * @param args Path and name of the config file (marchetyper-config.xml) as only argument expected.
     */
    public static void main(String[] args) throws IOException {

        if (args == null || args.length != 1) {
            System.err.println("Please provide the config file path and name as only parameter");
            System.exit(1);
        }
        final File configFile = new File(args[0]).getCanonicalFile();
        if (!configFile.exists()) {
            System.err.println("Config file does not exist: " + configFile);
            System.exit(1);
        }

        final Config config = ConfigImpl.load(configFile);
        final File targetDir = new File("target/example-archetype").getCanonicalFile();
        final File srcDir = config.getSrcDir(configFile.getParentFile());

        new MavenArchetyper(config).generate(srcDir, targetDir);

    }

}
