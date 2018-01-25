package com.krzysztofwilk.dropwizard;

import com.krzysztofwilk.dropwizard.client.ManagedPeriodicTask;
import com.krzysztofwilk.dropwizard.client.SqsScheduledTask;
import com.krzysztofwilk.dropwizard.health.TemplateHealthCheck;
import com.krzysztofwilk.dropwizard.resources.HelloWorldResource;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo extends Application<DemoConfiguration> {

    private static final String APPLICATION_NAME = "Dropwizard - AWS Demo";

    private static final Logger LOGGER = LoggerFactory.getLogger(Demo.class);
    private static final java.util.logging.Logger JUL_LOGGER =
        java.util.logging.Logger.getLogger(Demo.class.getName() + "HTTP");

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Starting {}", APPLICATION_NAME);
        new Demo().run(args);
    }

    @Override
    public String getName() {
        return APPLICATION_NAME;
    }

    @Override
    public void initialize(final Bootstrap<DemoConfiguration> bootstrap) {
        bootstrap.addBundle(new TemplateConfigBundle());
    }

    @Override
    public void run(DemoConfiguration configuration,
                    Environment environment) {

        final HelloWorldResource resource = new HelloWorldResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );

        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);

        environment.jersey().register(resource);

        // Install SLF4J Bridge for Jersey
        org.slf4j.bridge.SLF4JBridgeHandler.install();

        // j.u.l.Level.FINE maps to Level.DEBUG
        environment.jersey().register(
                new org.glassfish.jersey.logging.LoggingFeature(JUL_LOGGER,
                    java.util.logging.Level.FINE,
                    org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_TEXT,
                    8192));

        final SqsScheduledTask periodicTask = new SqsScheduledTask(configuration);
        final Managed managedImplementer = new ManagedPeriodicTask(periodicTask);
        environment.lifecycle().manage(managedImplementer);
    }
}
