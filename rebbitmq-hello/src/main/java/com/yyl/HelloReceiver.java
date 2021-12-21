package com.yyl;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.CredentialsProvider;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author yyl
 * 2021/12/21 9:03
 */
public class HelloReceiver {
    private static final String QUEUE_NAME = "hello_queue";

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        // 默认host：localhost
        connectionFactory.setHost("www.youngeryang.top");
        // 默认端口， 默认ssl端口5671
        connectionFactory.setPort(5672);
        // 默认virtualHost：/
        connectionFactory.setVirtualHost("/Abstract");
        connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider("abstract", "2692440667"));
        Connection connection = connectionFactory.newConnection();
        System.out.println("connection.getChannelMax() = " + connection.getChannelMax());
        Channel channel = connection.createChannel();
        System.out.println("channel.getChannelNumber() = " + channel.getChannelNumber());
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        DeliverCallback deliverCallback = (consumerTag, message) -> {
            System.out.println("consumerTag = " + consumerTag);
            System.out.println("message = " + message);
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println(body);

            // 手动确认 manual acknowledgement
            // channel.basicAck(message.getEnvelope().getDeliveryTag(), true);
        };
        // channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {});
        channel.basicConsume(QUEUE_NAME, true, "自定义ConsumerTag", deliverCallback, consumerTag -> {});

    }
}
