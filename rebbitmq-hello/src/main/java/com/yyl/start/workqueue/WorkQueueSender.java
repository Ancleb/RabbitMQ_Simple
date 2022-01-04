package com.yyl.start.workqueue;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author yyl
 * 2021/12/21 14:01
 */
public class WorkQueueSender {

    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setVirtualHost("/Abstract");
        connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider("abstract", "guest"));
        connectionFactory.setHost("www.youngeryang.top");
        connectionFactory.setPort(5672);
        try (Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel()) {
            sendMessage(channel);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }



    private static void sendMessage(Channel channel) throws IOException {
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        String[] messages = new String[]{"First message.", "Second message..", "Third message...", "Fourth message....","Fifth message....."};
        for (String message : messages) {
            // channel.basicPublish("", declareOk.getQueue(), null, message.getBytes(StandardCharsets.UTF_8));
            channel.basicPublish("", declareOk.getQueue(), MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
