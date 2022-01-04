package com.yyl.features.deadletter;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 死信消息发布者
 *
 * @author: CoderWater
 * @create: 2022/1/2
 */
public class Publisher {

    private static Channel getChannel() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        return connectionFactory.newConnection().createChannel();
    }

    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        // 声明死信交换机
        String deadLetterExchange = "dead-letter-exchange";
        channel.exchangeDeclare(deadLetterExchange, BuiltinExchangeType.DIRECT, true, false, null);
        // 声明一个死信队列，用于存放死信消息
        String deadLetterQueue = channel.queueDeclare("dead-letter-queue", true, false, false, null).getQueue();
        // 死信队列绑定上死信交换机。死信将由死信交换机路由到死信队列。routingKey为空，注意:为空没有特殊含义
        channel.queueBind(deadLetterQueue, deadLetterExchange, "");

        // 声明一个用于发布和接收消息的常规队列
        Map<String, Object> queueArgs = new HashMap<>();
        queueArgs.put("x-dead-letter-exchange", deadLetterExchange);
        queueArgs.put("x-dead-letter-routing-key", "");
        String normalQueue = channel.queueDeclare("deadLetterNormalQueue", true, false, false, queueArgs).getQueue();
        List<String> msgs = getMessage();
        for (String msg : msgs) {
            channel.basicPublish("", normalQueue, true, new AMQP.BasicProperties.Builder().expiration("10000").build(), msg.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("消息发送完成");
    }

    /*
        生产随机消息
     */
    public static List<String> getMessage(){
        return Stream.iterate(0, pre -> pre + 1).limit(10).map(i -> "第" + i + "条消息").collect(Collectors.toList());
    }
}
