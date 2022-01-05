package com.yyl.features.delayMessage.ttl;

import com.rabbitmq.client.BuiltinExchangeType;
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
 * 基于TTL的消息发布。
 * 可能存在一种情况：未过期的消息存在于队列头，已过期的消息在后面紧跟着，但是却没有死掉，直到到达队列头时才会被死信或丢弃。并且这个过期消息参与队列的统计。
 * @author yyl
 * 2022/1/5 11:05
 */
public class TtlDelayPublisher {

    private static Channel getChannel() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        return connectionFactory.newConnection().createChannel();
    }

    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        String delayNormalEx = "delay_normal_ex";
        String delayDeadEx = "delay_dead_ex";
        channel.exchangeDeclare(delayNormalEx, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(delayDeadEx, BuiltinExchangeType.DIRECT);
        Map<String, Object> queueArgs = new HashMap<>();
        // 设置队列中的所有消息都是10ttl。
        // queueArgs.put("x-message-ttl", 10000);
        queueArgs.put("x-dead-letter-exchange", delayDeadEx);
        queueArgs.put("x-dead-letter-routing-key", "usableMessage");
        String second10 = channel.queueDeclare("second_10", true, false, false, queueArgs).getQueue();
        String second20 = channel.queueDeclare("second_20", true, false, false, queueArgs).getQueue();
        channel.queueBind(second10, delayNormalEx, second10); // defaultExchange绑定的queue默认才是队列名。  手动声明ex需要手动指定binding_key
        channel.queueBind(second20, delayNormalEx, second20); // defaultExchange绑定的queue默认才是队列名。  手动声明ex需要手动指定binding_key
        // 声名死信队列，绑定上死信交换机。
        String delayUsableQueue = channel.queueDeclare("delayUsableQueue", true, false, false, null).getQueue();
        channel.queueBind(delayUsableQueue, delayDeadEx, "usableMessage");
    }

}
