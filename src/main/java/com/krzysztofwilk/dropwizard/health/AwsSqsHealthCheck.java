package com.krzysztofwilk.dropwizard.health;

import com.codahale.metrics.health.HealthCheck;
import software.amazon.awssdk.services.sqs.SQSClient;

public class AwsSqsHealthCheck extends HealthCheck {

    private static final SQSClient sqsClient = SQSClient.create();
    private final String sqsQueue;

    public AwsSqsHealthCheck(String sqsQueue) {
        this.sqsQueue = sqsQueue;
    }

    @Override
    protected Result check() {
        boolean queuePresent = sqsClient.listQueues()
                .queueUrls()
                .contains(sqsQueue);

        if (queuePresent) {
            return Result.healthy();
        } else {
            return Result.unhealthy(String.format("SQS queue: %s is not available", sqsQueue));
        }
    }
}
