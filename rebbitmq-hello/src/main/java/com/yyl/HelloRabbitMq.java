package com.yyl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.DefaultCredentialsProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Hello world!
 *
 */
public class HelloRabbitMq {

    private static final String QUEUE_NAME = "hello";
    public static void main( String[] args ) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider("abstract", "2692440667"));
        connectionFactory.setVirtualHost("/Abstract");
        // 默认ssl端口5671
        connectionFactory.setHost("www.youngeryang.top");
        // 默认端口5672,ssl方式默认端口5671
        connectionFactory.setPort(5672);
        // 创建TCP的connection
        // 通过connection创建逻辑连接channel
        // RabbitMQ的所有操作指令都是通过channel传输的。
        // 程序中有很多不同的线程需要从mq中消费或者生产消息，难么必然要建立很多个TCP的connection，建立和销毁TCP连接是非常昂贵的开销，如果遇到使用高峰，性能瓶颈也就随之显现出来。
        // RabbitMQ采用NIO（Non-blocking I/O），选择TCP连接复用，不仅可以减少性能的开销，同时也便于管理。
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            // 等价于和无参重载方法
            // AMQP.Queue.DeclareOk declareOk = channel.queueDeclare("", false, true, true, null);
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclare("", false, false, true, null);
            String message = "HelloWord!";
            // exchange为空， 则使用Broker启动默认创建的DefaultExchange（没有名称的DirectExchange）
            // routingKey：即队列的名称
            channel.basicPublish("", declareOk.getQueue(), null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println( "[x] Sent '" + message + "'" );
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }
}
