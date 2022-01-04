package com.yyl.start.rpc;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author yyl
 * 2021/12/23 14:19
 */
public class RPCServer {
    private static final String RPC_QUEUE_NAME = "rpc_queue";

    public static Channel getChannel() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        Connection connection = connectionFactory.newConnection();
        return connection.createChannel();
    }

    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        channel.basicQos(1);
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

        // 下面要是用这个对象的锁
        // Object monitor = new Object();
        DeliverCallback deliverCallback = (consumerTag, message) -> {System.out.println("consumerTag = " + consumerTag);

            String response = "";
            try {
                String body = new String(message.getBody(), message.getProperties().getContentEncoding());
                System.out.println(" [.] fib(" + body + ")");
                response = String.valueOf(fib(Integer.parseInt(body)));
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e);
            } finally {
                // 其实完全可以使用message.getProperties()，不需要再创建一次。
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties().builder()
                        .correlationId(message.getProperties().getCorrelationId())
                        .contentType("text/plain").contentEncoding(StandardCharsets.UTF_8.name())
                        .build();
                channel.basicPublish("", message.getProperties().getReplyTo(), replyProps, response.getBytes(StandardCharsets.UTF_8));
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);

                // RabbitMq 消费者回调线程通知server主线程
                // synchronized (monitor){
                //     monitor.notify();
                // }
            }
        };

        channel.basicConsume(RPC_QUEUE_NAME, deliverCallback, consumerTag -> {});

        // 等待和准备从RPCClient中消费消息
        // while (true){
        //     synchronized (monitor){
        //         try {
        //             monitor.wait();
        //         } catch (InterruptedException e){
        //             e.printStackTrace();
        //         }
        //     }
        // }
    }


    /**
     * 斐波那契数列求和
     * @param n 前n个数字的和
     */
    public static int fib(int n){
        if (n == 0 || n == 1) return n;
        return fib(n - 1) + fib(n - 2);
    }
}
