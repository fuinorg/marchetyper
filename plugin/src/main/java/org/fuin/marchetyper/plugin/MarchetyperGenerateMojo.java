/**
 * Copyright (C) 2023 Future Invent IT Consulting GmbH. All rights
 * reserved. <http://www.fuin.org/>
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
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fuin.marchetyper.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.fuin.marchetyper.core.Config;
import org.fuin.marchetyper.core.ConfigImpl;
import org.fuin.marchetyper.core.MavenArchetyper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * A customizable source code generator plugin for maven.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.VERIFY)
public final class MarchetyperGenerateMojo extends AbstractMojo {

    private static final Logger LOG = LoggerFactory.getLogger(MarchetyperGenerateMojo.class);

    /**
     * The base directory.
     */
    @Parameter(property = "configFile", defaultValue = "${basedir}", readonly = true)
    private File baseDir;

    /**
     * The configuration file.
     */
    @Parameter(property = "configFile", defaultValue = "${basedir}/marchetyper-config.xml", readonly = true)
    private File configFile;

    @Override
    public void execute() throws MojoExecutionException {

        StaticLoggerBinder.getSingleton().setMavenLog(getLog());

        if (!configFile.exists()) {
            throw new MojoExecutionException(configFile + " does not exist");
        }
        LOG.info("Using config file: {}", configFile);

        final Config config = ConfigImpl.load(configFile);
        new MavenArchetyper(config).generate(baseDir);

    }

}
