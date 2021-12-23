package com.yyl.routing;

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
 * 2021/12/22 14:15
 */
public class ReceiveLogsDirect {

    private static final String EXCHANGE_NAME = "direct_logs";

    // private static final String detail_levels = "info";
    // private static final String detail_levels = "warn";
    // private static final String detail_levels = "error";
    private static final String detail_levels = "info,warn,error";

    public static void main(String[] args) throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        channel.basicQos(1);
        String queueName = channel.queueDeclare().getQueue();

        for (String detail_level : detail_levels.split(",")) {
            channel.queueBind(queueName, EXCHANGE_NAME, detail_level);
        }

        DeliverCallback deliverCallback = (consumerTag, message) -> {
            System.out.println("consumerTag = " + consumerTag);
            String body = new String(message.getBody(), message.getProperties().getContentEncoding());
            System.out.println(body);

            try {
                // 模拟处理消息很满，故意造成queue中消息堆积
                Thread.sleep(2000);
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        channel.basicConsume(queueName, deliverCallback, consumerTag -> {});
    }
}
