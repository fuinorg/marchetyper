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
package org.fuin.jee7restswagquick.app;

import java.lang.management.ManagementFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.udojava.jmx.wrapper.JMXBeanWrapper;

/**
 * Provides server configuration information via JMX.
 */
@Singleton
@Startup
public class QuickstartJmxMonitoring {

    @Inject
    private QuickstartConfig config;

    private MBeanServer platformMBeanServer;

    private ObjectName objectName = null;

    @PostConstruct
    public void registerInJMX() {
        try {
            final JMXBeanWrapper wrappedBean = new JMXBeanWrapper(config);
            objectName = new ObjectName(
                    this.getClass().getSimpleName() + ":type=" + QuickstartConfig.class.getSimpleName());
            platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            platformMBeanServer.registerMBean(wrappedBean, objectName);
        } catch (final Exception ex) {
            throw new IllegalStateException("Error registering JMX monitoring bean", ex);
        }
    }

    @PreDestroy
    public void unregisterFromJMX() {
        if (objectName != null) {
            try {
                platformMBeanServer.unregisterMBean(objectName);
            } catch (final Exception ex) {
                throw new IllegalStateException("Error unregistering JMX monitoring bean", ex);
            }
        }
    }

}
