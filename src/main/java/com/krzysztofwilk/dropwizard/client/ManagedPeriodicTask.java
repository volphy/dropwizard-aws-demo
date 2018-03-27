package com.krzysztofwilk.dropwizard.client;

import com.google.common.util.concurrent.AbstractScheduledService;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedPeriodicTask implements Managed {

    @SuppressWarnings("squid:S1068")
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedPeriodicTask.class);

    private final AbstractScheduledService periodicTask;

    public ManagedPeriodicTask(AbstractScheduledService periodicTask) {
        this.periodicTask = periodicTask;
    }

    @Override
    public void start() {
        periodicTask.startAsync().awaitRunning();
    }

    @Override
    public void stop() {
        periodicTask.stopAsync().awaitTerminated();
    }
}
