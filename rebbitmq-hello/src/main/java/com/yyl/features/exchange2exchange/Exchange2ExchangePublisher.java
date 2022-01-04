package com.yyl.features.exchange2exchange;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * exchange绑定到exchange
 *
 * @author: CoderWater
 * @create: 2022/1/3
 */
public class Exchange2ExchangePublisher {
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
        String sourceExchange = "exchange_A";
        String destinationExchange = "exchange_B";
        // 声明两个exchange
        channel.exchangeDeclare(sourceExchange, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(destinationExchange, BuiltinExchangeType.DIRECT);
        // 绑定两个交换机
        channel.exchangeBind(destinationExchange, sourceExchange, "");

        // 声明1个队列，并绑定到destinationExchange
        String destExchangeSomeQueue = channel.queueDeclare("exchange_B_some_queue", true, false, false, null).getQueue();
        channel.queueBind(destExchangeSomeQueue, destinationExchange, "");


        // 发送消息到source---exchange_A
        List<String> msgs = getMessage();
        for (String msg : msgs) {
            // 使用随机消息优先级
            channel.basicPublish(sourceExchange, destinationExchange, null, msg.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("消息发送完成!");
        System.exit(0);
    }


}
