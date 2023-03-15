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
package org.fuin.marchetyper.core;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.maven.shared.invoker.CommandLineConfigurationException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenCommandLineBuilder;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.utils.cli.Commandline;
import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.common.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a Maven build.
 */
public final class MavenExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MavenExecutor.class);

    private final File baseDir;

    private final String[] goals;

    private final Properties properties;

    /**
     * Constructor with all mandatory data.
     * 
     * @param baseDir
     *            Base directory.
     * @param goals
     *            Goals to execute.
     */
    public MavenExecutor(final File baseDir, final String... goals) {
        this(baseDir, null, goals);
    }

    /**
     * Constructor with all mandatory data.
     * 
     * @param baseDir
     *            Base directory.
     * @param properties
     *            Arguments.
     * @param goals
     *            Goals to execute.
     */
    public MavenExecutor(@NotNull final File baseDir, @Nullable final Properties properties, @NotNull final String... goals) {
        super();
        Contract.requireArgNotNull("baseDir", baseDir);
        Contract.requireArgNotNull("goals", goals);
        this.baseDir = baseDir;
        this.goals = goals;
        this.properties = properties;
    }

    /**
     * Runs the build.
     */
    public void execute() {

        try {

            final InvocationRequest request = new DefaultInvocationRequest();
            request.setBaseDirectory(baseDir);
            request.setGoals(Arrays.asList(goals));
            if (properties != null) {
                request.setProperties(properties);
            }
            log(request);

            final Invoker invoker = new DefaultInvoker();
            final InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new IllegalStateException("Build failed.");
            }

        } catch (final MavenInvocationException ex) {
            throw new RuntimeException("Failure executing the archetype test", ex);
        }
    }

    private void log(final InvocationRequest request) {
        try {
            final MavenCommandLineBuilder builder = new MavenCommandLineBuilder();
            final Commandline cl = builder.build(request);
            LOG.debug(replaceEscapes(cl.toString()));
        } catch (final CommandLineConfigurationException ex) {
            LOG.error("Couln't log Maven invocation request", ex);
        }
    }

    private String replaceEscapes(final String str) {
        String replaced = str;
        replaced = replaced.replace("\r", "\\r");
        replaced = replaced.replace("\n", "\\n");
        replaced = replaced.replace("\t", "\\t");
        return replaced;
    }

}
