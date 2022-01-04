package com.yyl.features.alternateExchange;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AE测试Publisher
 *
 * @author: CoderWater
 * @create: 2022/1/3
 */
public class AlternateExchangePublisher {

    private static Channel getChannel() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        return connectionFactory.newConnection().createChannel();
    }

    /*
        生产随机消息
     */
    public static List<String> getMessage(){
        // 使用1个消息和多个消息能对比出来。1个消息每次都是优先级高的接收消息。
        // 如果优先级高的状态处于非空闲，则优先级低的consumer接收消息。
        return Stream.iterate(0, pre -> pre + 1).limit(1).map(i -> "第" + i + "条消息").collect(Collectors.toList());
    }

    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        String alternateExchange = "AE";
        String normalExchange = "AE-normal";
        // 声明一个alternate-exchange,  这里使用direct，根据业务场景来,常用fanout。
        channel.exchangeDeclare(alternateExchange, BuiltinExchangeType.DIRECT);
        String unRoutedQueue = channel.queueDeclare("AE-unrouted", true, false, false, null).getQueue();
        channel.queueBind(unRoutedQueue, alternateExchange, "bind_key_ae");


        // 声明一个普通的exchange，和queue
        channel.exchangeDeclare(normalExchange, BuiltinExchangeType.DIRECT, true, false, Collections.singletonMap("alternate-exchange", alternateExchange));
        String msgQueue = channel.queueDeclare("AE-normal-queue", true, false, false, null).getQueue();
        channel.queueBind(msgQueue, normalExchange, "bind_key_normal");

        List<String> msgs = getMessage();
        for (String msg : msgs) {
            // 使用随机消息优先级
            channel.basicPublish(normalExchange, "bind_key_ae", null, msg.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("消息发送完成!");
        System.exit(0);
    }

}
