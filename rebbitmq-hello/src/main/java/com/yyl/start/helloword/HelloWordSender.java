package com.yyl.start.helloword;

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
public class HelloWordSender {

    private static final String QUEUE_NAME = "hello_queue";


    public static void main( String[] args ) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setCredentialsProvider(new DefaultCredentialsProvider("guest", "guest"));
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
            // 无参重载方法：不持久化，独占，自动删除。
            // 如果队列名称为空字符串，则broker将会我们自动生成一个队列名，比如:amq.gen-....
            // 声明的队列名称不能以amq.开头，因为amq.开头的队列是提供给broker内部使用的。如果违反此规则，将会抛出403ACCESS_REFUSED异常
            // 如果开启exclusive，则connection断开连接后，就删除这个队列。  程序结束connection断开连接
            // AMQP.Queue.DeclareOk declareOk = channel.queueDeclare("amq.noexcetion", false, false, true, null);
            // AMQP.Queue.DeclareOk declareOk = channel.queueDeclare("", false, true, true, null);
            AMQP.Queue.DeclareOk declareOk = channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            String message = "HelloWord!";
            // exchange为空字符串，则为defaultExchange，所有的队列都会默认绑定到这个defaultExchange。用队列名称作为routingKey，也就是绑定关系
            channel.basicPublish("", declareOk.getQueue(), null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println( "[x] Sent '" + message + "'" );
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }
}
