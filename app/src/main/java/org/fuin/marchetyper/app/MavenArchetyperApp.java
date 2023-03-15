/**
 * Copyright (C) 2023 Future Invent IT Consulting GmbH. All rights reserved. 
 * http://www.fuin.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.marchetyper.app;

import java.io.File;

import org.fuin.ext4logback.LogbackStandalone;
import org.fuin.ext4logback.NewLogConfigFileParams;
import org.fuin.marchetyper.core.ConfigImpl;
import org.fuin.marchetyper.core.MavenArchetyper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven Archtyper console application.
 */
public final class MavenArchetyperApp {

    /**
     * Private constructor.
     */
    private MavenArchetyperApp() {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts the application.
     * 
     * @param args
     *            Only argument is the name and path of the configuration XML file.
     */
    public static void main(final String[] args) {

        if (args == null || args.length == 0) {
            System.err.println("Please provide the path and file name of the XML configuration");
            System.exit(1);
        }

        // Initialize logging
        try {
            new LogbackStandalone().init(new File("logback.xml"),
                    new NewLogConfigFileParams(MavenArchetyperApp.class.getPackage().getName(), "logback"));
        } catch (final RuntimeException ex) {
            System.err.println("Error initializing logging");
            ex.printStackTrace(System.err);
            System.exit(2);
        }

        final Logger log = LoggerFactory.getLogger(MavenArchetyperApp.class);

        // Load configuration
        ConfigImpl config = null;
        try {
            config = ConfigImpl.load(new File(args[0]));
        } catch (final RuntimeException ex) {
            System.err.println("Error loading configuration XML");
            ex.printStackTrace(System.err);
            System.exit(3);
        }

        // Start conversion
        try {
            new MavenArchetyper(config).execute();
        } catch (final RuntimeException ex) {
            log.error("Error executing application", ex);
            System.exit(4);
        }

    }

}
