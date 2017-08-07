package com.krzysztofwilk.dropwizard.client;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.krzysztofwilk.dropwizard.DemoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SqsScheduledTask extends AbstractScheduledService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsScheduledTask.class);

    private static final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();

    private final String sqsQueue;

    public SqsScheduledTask(DemoConfiguration configuration) {
        this.sqsQueue = configuration.getSqsQueue();
    }

    @Override
    protected void runOneIteration() throws Exception {
        LOGGER.info("Checking SQS messages");

        ReceiveMessageResult messageResult = sqsClient.receiveMessage(sqsQueue);

        List<Message> messages = messageResult.getMessages();

        if (! messages.isEmpty()) {

            List<DeleteMessageBatchRequestEntry> entries = new LinkedList<>();
            messages.forEach(m -> entries.add(new DeleteMessageBatchRequestEntry(m.getMessageId(), m.getReceiptHandle
                            ())));

            sqsClient.deleteMessageBatch(sqsQueue, entries);

            LOGGER.info("SQS messages={}", messages);
        }
    }

    @Override
    protected AbstractScheduledService.Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, 3, TimeUnit.SECONDS);
    }
}