package com.yyl.workqueue;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * 重要点：
 *  公平调度：
 *      您可能已经注意到，调度仍然没有完全按照我们的要求工作。
 *      例如，在有两名工人的情况下，当所有奇数信息都很重，而偶数信息又很轻时，一名工人会一直忙着，而另一名工人几乎不做任何工作。
 *      好吧，RabbitMQ对此一无所知，仍然会均匀地发送消息。
 *
 *      这是因为RabbitMQ只是在消息进入队列时发送消息。它不查看消费者未确认消息的数量。它只是盲目地向第n个消费者发送每一条信息。
 *
 *  为了解决它，我们可以使用基本Qos方法的预取计数 = 1背景。这告诉RabbitMQ不要一次给工人多个消息。或者，换句话说，在员工处理并确认上一条消息之前，不要向它发送新消息。相反，它将把它分派给下一个不忙的工人。
 *
 * @author yyl
 * 2021/12/21 14:22
 */
public class WorkQueueWorker {

    private static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:2692440667@www.youngeryang.top/%2FAbstract");
        try{
            Connection connection = connectionFactory.newConnection();

            Channel channel = connection.createChannel();
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            /*
            设置服务质量（Qos）：某些消息的任务重，某些消息的任务轻，就会导致不同的consumer的工作强度不均匀，极端情况下可能会导致一部分闲着，一部分总是在做重活。这不是我们想要的。
            prefetchCount:欲获取数量，告诉broker不要一次给我发送太多的消息。在消息ack或reject之前，每次只能持有指定数量的消息。剩下的消息在队列中分配给其他空闲或（对某一个小消息消费完后空闲的）的消费者。
            这样broker就能做到让每个消费者均匀分摊消息的重量。而不是傻乎乎的按消息数量分配给消费者
             */
            int prefetchCount = 1;
            channel.basicQos(prefetchCount);
            DeliverCallback deliverCallback = (consumerTag, message) -> {
                System.out.println("consumerTag = " + consumerTag);
                System.out.println("message = " + message);
                String body = new String(message.getBody());
                System.out.println("body = " + body);
                try {
                    doWork(body);
                } finally {
                    System.out.println(" [" + message.getEnvelope().getDeliveryTag() + "] Done");
                }
                // manual acknowledgement
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                // 拒绝消息并且重新入队,只有一个消费者时会陷入死循环。
                // channel.basicReject(message.getEnvelope().getDeliveryTag(), true);
            };
            channel.basicConsume(declareOk.getQueue(), false, deliverCallback, consumerTag -> {});
            System.out.println("[*] Waiting for messages. To exit press CTRL+C");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }


    private static void doWork(String taskBody) {
        for (char ch : taskBody.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException _ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
