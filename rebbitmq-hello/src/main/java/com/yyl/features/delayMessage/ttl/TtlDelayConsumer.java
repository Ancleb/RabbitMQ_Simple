package com.yyl.features.delayMessage.ttl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * 基于TTL的消息发布。
 * 可能存在一种情况：未过期的消息存在于队列头，已过期的消息在后面紧跟着，但是却没有死掉，直到到达队列头时才会被死信或丢弃。并且这个过期消息参与队列的统计。
 * @author yyl
 * 2022/1/5 11:50
 */
public class TtlDelayConsumer {
    public static Channel getChannel() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        return connectionFactory.newConnection().createChannel();
    }

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, IOException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        String delayUsableQueue = channel.queueDeclare("delayUsableQueue", true, false, false, null).getQueue();
        channel.basicConsume(delayUsableQueue, (consumerTag, message) -> {
            System.out.println("message = " + new String(message.getBody()) + "; headers = " + message.getProperties().getHeaders());
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        }, consumerTag -> {});
    }
}
