package com.yyl.features.maxLimit;

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
 * 队列长度或容量达到最大值时的表现形式
 * broker默认使用drop-head模式作为队列溢出的解决方案。
 *  如果开启了发布者确认模式，broker会回调ack结果给publisher。
 * reject-publish作为队列溢出的解决方案时，broker会拒绝新消息。
 *  如果开启了发布者确认模式，broker会回调ack或者nack给publisher。
 *
 * @author: CoderWater
 * @create: 2022/1/1
 */
public class LimitPublisher {

    public static Channel getChannel() throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        return connectionFactory.newConnection().createChannel();
    }

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, IOException, KeyManagementException, TimeoutException, InterruptedException {
        Channel channel = getChannel();

        // 开启消息发布确认模式。
        // 方便查看哪些消息发送成功了，哪些消息因为达到queue的限制没有被broker接收
        channel.confirmSelect();
        Map<Long, String> msg = new HashMap<>();
        channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
            System.out.printf("returnCallback： 【replyCode:%s, replyText:%s,exchange:%s, routingKey:%s, properties:%s, body:%s 】", replyCode, replyText, exchange, routingKey, properties, new String(body));
        });
        channel.addConfirmListener(new ConfirmListener() {
            // broker使用ack的异步回调。
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("broker使用Ack的异步回调, deliveryTag:" + msg.get(deliveryTag));
            }
            // broker使用nack的异步回调。
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("broker使用nack的异步回调, deliveryTag:" + msg.get(deliveryTag));
            }
        });

        String queueName = channel.queueDeclare("limitQueue", true, false, false, getQueueDeclareArg()).getQueue();

        // 批量发送消息
        List<String> messages = getMessage();
        for (String message : messages) {
            msg.put(channel.getNextPublishSeqNo(), message);
            channel.basicPublish("", queueName, true, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("waitForConfirms = " + channel.waitForConfirms(5000));
        }
        System.out.println("消息发送完成");
    }

    /**
     * 产生投递消息
     */
    public static List<String> getMessage(){
        return Stream.iterate(0, pre -> pre + 1).limit(5).map(i -> "这是第" + i + "个消息").collect(Collectors.toList());
    }

    public static Map<String,Object> getQueueDeclareArg(){
        Map<String, Object> args = new HashMap<>();
        args.put("x-max-length", 10); // 长度只能容纳10条message
        args.put("x-overflow", "reject-publish"); // 长度只能容纳10条message
        return args;
    }
}
