package com.yyl.publisherConfirems;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * 用于实现可靠的消息发布。
 * 将使用publisherConfirms模式来确保已发布的消息安全抵达Broker。
 * @author: CoderWater
 * @create: 2021/12/24
 */
public class Publisher {

    private static Connection connection;

    private static Channel getChannel() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:2692440667@www.youngeryang.top/%2FAbstract");
        connection = connectionFactory.newConnection();
        return connection.createChannel();
    }

    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException, InterruptedException {
        Channel channel = getChannel();
        // 在此通道上启用PublisherConfirm模式。只需要启用1次，而不用对发布的每一个消息都启用。
        // 客户端发布的消息将由broker异步确认，这意味着他们已在服务器端得到处理。
        // 注意，是消息安全抵达Broker后Broker确认收到消息并且Broker表示我可以处理这个消息了。但是至于消息是否入正确的入队列和被成功消费了，publisherConfirms不会理会。
        channel.confirmSelect();

        // publishMessage1(channel);
        publishMessage2(channel);

        connection.close();
    }


    /**
     * 策略1：单独发布消息
     */
    public static void publishMessage1(Channel channel) throws IOException, InterruptedException, TimeoutException {
        // 声明一个临时队列
        // String queue = channel.queueDeclare().getQueue();
        String queue = "not exist queue, in order to simulate message not confirm";


        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder().contentType("text/plain").contentEncoding(StandardCharsets.UTF_8.name()).build();
        for (int i = 0; i < 10; i++) {
            String message = "策略1：单独发布消息, i = " + i;

            // 简单的发布一个消息
            channel.basicPublish("", queue, basicProperties, message.getBytes(StandardCharsets.UTF_8));

            System.out.printf("正在等待发送的消息被确认:【%s】%n", message);

            // 等待自上次调用这个方法以后，投递的所有消息都被确认或者拒接，如果超时，则抛出TimeoutException
            channel.waitForConfirmsOrDie(5000);
        }
    }
    /**
     * 策略2：批量发布消息
     */
    public static void publishMessage2(Channel channel) throws IOException, InterruptedException, TimeoutException {
        // 声明一个临时队列
        // String queue = channel.queueDeclare().getQueue();
        String queue = "not exist queue, in order to simulate message not confirm";


        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder().contentType("text/plain").contentEncoding(StandardCharsets.UTF_8.name()).build();
        for (int i = 0; i < 10; i++) {
            String message = "策略1：单独发布消息, i = " + i;

            // 简单的发布一个消息
            channel.basicPublish("", queue, basicProperties, message.getBytes(StandardCharsets.UTF_8));

        }
        System.out.printf("正在等待自上次confirms后发送的消息被全部被确认%n");
        // 等待自上次调用这个方法以后，投递的所有消息都被确认或者拒接，如果超时，则抛出TimeoutException
        channel.waitForConfirmsOrDie(5000);
    }
}
