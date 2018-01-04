package com.krzysztofwilk.dropwizard.client;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.krzysztofwilk.dropwizard.DemoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.sqs.SQSClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SqsScheduledTask extends AbstractScheduledService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsScheduledTask.class);

    private static final SQSClient sqsClient = SQSClient.create();
    private final String sqsQueue;

    public SqsScheduledTask(DemoConfiguration configuration) {
        this.sqsQueue = configuration.getSqsQueue();
        }

    @Override
    protected void runOneIteration() {
        LOGGER.info("Checking SQS messages");

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(sqsQueue)
                .waitTimeSeconds(1)
                .build();

        ReceiveMessageResponse  messageResponse = sqsClient.receiveMessage(request);
        LOGGER.info("SQS response: {}" ,  messageResponse);

        List<Message> messages = Optional.ofNullable(messageResponse.messages()).orElse(Collections.emptyList());

        if (!messages.isEmpty()) {
            List<DeleteMessageBatchRequestEntry> entries = new LinkedList<>();
            messages.forEach(m ->
                entries.add(DeleteMessageBatchRequestEntry.builder()
                        .id(m.messageId())
                        .receiptHandle(m.receiptHandle())
                        .build()
                )
            );

            DeleteMessageBatchRequest deleteRequest = DeleteMessageBatchRequest.builder()
                    .entries(entries)
                    .build();
            sqsClient.deleteMessageBatch(deleteRequest);

            LOGGER.info("SQS messages: {}", messages);
        }
    }

    @Override
    protected AbstractScheduledService.Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 3, TimeUnit.SECONDS);
    }
}