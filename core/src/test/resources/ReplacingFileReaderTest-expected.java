${javaDocCopyright}
package ${pkgName}.app;

import java.lang.management.ManagementFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.management.MBeanServer;
import jakarta.management.ObjectName;

import com.udojava.jmx.wrapper.JMXBeanWrapper;

/**
 * Provides server configuration information via JMX.
 */
@Singleton
@Startup
public class ${appName}JmxMonitoring {

    @Inject
    private ${appName}Config config;

    private MBeanServer platformMBeanServer;

    private ObjectName objectName = null;

    @PostConstruct
    public void registerInJMX() {
        try {
            final JMXBeanWrapper wrappedBean = new JMXBeanWrapper(config);
            objectName = new ObjectName(
                    this.getClass().getSimpleName() + ":type=" + ${appName}Config.class.getSimpleName());
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
