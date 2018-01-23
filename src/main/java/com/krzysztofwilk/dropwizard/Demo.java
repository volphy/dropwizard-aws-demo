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
import org.glassfish.jersey.logging.LoggingFeature;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Demo extends Application<DemoConfiguration> {

    public static void main(final String[] args) throws Exception {
        new Demo().run(args);
    }

    @Override
    public String getName() {
        return "Dropwizard - AWS Demo";
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

        environment.jersey().register(new LoggingFeature(Logger.getLogger("http-requests"),
                Level.INFO,
                LoggingFeature.Verbosity.PAYLOAD_ANY,
                8192));

        final SqsScheduledTask periodicTask = new SqsScheduledTask(configuration);
        final Managed managedImplementer = new ManagedPeriodicTask(periodicTask);
        environment.lifecycle().manage(managedImplementer);
    }

}
