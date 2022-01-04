package com.yyl.features.lazyqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

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
 * 懒加载队列Publisher
 *
 * @author: CoderWater
 * @create: 2022/1/1
 */
public class LazyPublisher {

    public static Channel getChannel() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        return connectionFactory.newConnection().createChannel();
    }

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, IOException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        String queueName = channel.queueDeclare("lazyQueue", true, false, false, getQueueDeclareArg()).getQueue();
        // 批量发送消息
        List<String> messages = getMessage();
        for (String message : messages) {
            channel.basicPublish("", queueName, true, MessageProperties.PERSISTENT_BASIC, message.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("消息发送完成");
        // 查看消息是否被lazyQueue存储到磁盘中了。
    }

    /**
     * 产生投递消息
     */
    public static List<String> getMessage(){
        return Stream.iterate(0, pre -> pre + 1).limit(1000000).map(i -> "这是第" + i + "个消息").collect(Collectors.toList());
    }

    public static Map<String,Object> getQueueDeclareArg(){
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-mode", "lazy");
        return args;
    }
}
