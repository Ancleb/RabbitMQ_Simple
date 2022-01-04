package com.yyl.features.consumerPriority;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * consumer优先级测试
 *
 * @author: CoderWater
 * @create: 2022/1/3
 */
public class Consumer_2 {
    public static Channel getChannel() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:2692440667@www.youngeryang.top/%2FAbstract");
        return connectionFactory.newConnection().createChannel();
    }

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, IOException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        // 应尽量避免一批消息到达queue之后立马被一个consumer全部接收，这样queue将无法对这一批消息使用优先级策略进行排序。
        channel.basicQos(1);
        System.out.println("队列中的消息数量：" + channel.messageCount("consumer_priority_queue"));
        Map<String, Object> consumerArgs = new HashMap<>();
        consumerArgs.put("x-priority", 10);
        channel.basicConsume("consumer_priority_queue", false, consumerArgs,(consumerTag, message) -> {
            System.out.println("message = " + new String(message.getBody()) + "  priority：" + message.getProperties().getPriority());
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        }, (consumerTag, sig) -> {});
    }
}
