package com.yyl.publishAndSubScribe;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * 日志接收者Consumer
 * 在我们的日志系统中，每个正在运行的接收程序副本都会收到消息。这样，我们就可以运行一个接收器并将日志定向到磁盘；同时，我们可以运行另一个接收器并在屏幕上看到日志。
 *
 * @author: CoderWater
 * @create: 2021/12/21
 */
public class ReceiveLogs {

    private static final String EXCHANGES_NAME = "logs";

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:2692440667@www.youngeryang.top/%2FAbstract");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // 声明一个no-durable exclusive auto-delete的队列，三个属性合在一起又叫临时队列
        // 为什么要是用临时队列？因为我们指向关注的是现在当前产生的日志，而不是老日志。而临时队列在disconnect或者队列中的没有消息后就会自动删除。这就很好的满足了我们的需求，只在启动消费者时才会创建并绑定队列，然后开始消费队列。当消费者关闭时，则自动删除队列，不再继续接收消息
        String queueName = channel.queueDeclare().getQueue();
        // 将临时队列绑定到logsExchange上。
        channel.queueBind(queueName, EXCHANGES_NAME, "");

        DeliverCallback deliverCallback = (consumerTag, message) -> {
            System.out.println("consumerTag = " + consumerTag);
            String body = new String(message.getBody(), message.getProperties().getContentEncoding());
            System.out.println(body);

            // manual acknowledgement
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        };

        channel.basicConsume(queueName, deliverCallback, consumerTag -> {});
    }
}
