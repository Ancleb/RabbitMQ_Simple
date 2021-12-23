package com.yyl.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author yyl
 * 2021/12/22 17:12
 */
public class ReceiveLogsTopic {

    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // 声明一临时队列
        String queueName = channel.queueDeclare().getQueue();
        if (args.length < 1) {
            System.err.println("Usage: ReceiveLogsTopic [binding_key]...");
            System.exit(1);
        }

        // 用指定的Binding_key，将queue绑定到exchange
        for (String bindingKey : args) {
            channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
        }
        DeliverCallback deliverCallback = (consumerTag, message) -> {
            System.out.println("consumerTag = " + consumerTag);
            String body = new String(message.getBody(), message.getProperties().getContentEncoding());
            System.out.println(body);

            // manual acknowledgement
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        };


        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.basicConsume(queueName, deliverCallback, (consumerTag, sig) -> {});
    }
}
