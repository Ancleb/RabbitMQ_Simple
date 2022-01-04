package com.yyl.features.consumerPriority;

import com.rabbitmq.client.AMQP;
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
 * consumer优先级测试Publisher
 *
 * 粘贴messagePriority的消息Publisher
 *
 * @author: CoderWater
 * @create: 2022/1/3
 */
public class Publisher {

    private static Channel getChannel() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:2692440667@www.youngeryang.top/%2FAbstract");
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
        int maxPriority = 10;
        // 声明一个支持优先级的队列
        Map<String, Object> queueArgs = new HashMap<>();
        // 优先级范围是0-255，默认0。官方推荐使用0-10，因为大的可选优先级将耗费更多的线程，会有更多的内存消耗。
        // 只有支持priority的queue才支持消息的priority属性，否则不支持消息优先级的属性。
        queueArgs.put("x-max-priority", maxPriority);
        String priorityQueue = channel.queueDeclare("consumer_priority_queue", true, false, false, queueArgs).getQueue();

        // 先发送消息
        // 在managementUI中使用GetMessage查看消息是否是正常的先入先出。
        List<String> msgs = getMessage();
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();
        Random random = new Random();
        for (String msg : msgs) {
            // 使用随机消息优先级
            AMQP.BasicProperties msgProperties = basicProperties.builder().priority(random.nextInt(maxPriority + 1)).build();
            channel.basicPublish("", priorityQueue, msgProperties, msg.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("消息发送完成!");
        System.exit(0);
    }

}
