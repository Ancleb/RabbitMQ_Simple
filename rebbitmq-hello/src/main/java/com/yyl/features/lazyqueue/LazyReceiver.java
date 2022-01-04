package com.yyl.features.lazyqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * 懒加载队列消费者
 *
 * @author: CoderWater
 * @create: 2022/1/2
 */
public class LazyReceiver {

    public static Channel getChannel() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        return connectionFactory.newConnection().createChannel();
    }

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, IOException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        // channel.basicQos(10000);
        System.out.println("队列中的消息数量：" + channel.messageCount("lazyQueue"));
        channel.basicConsume("lazyQueue", (consumerTag, message) -> {
            System.out.println("message = " + new String(message.getBody()));
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        }, (consumerTag, sig) -> {});
    }

}
