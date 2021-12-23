package com.yyl.rpc;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * 我们建立连接和通道。
 * 我们的call方法发出实际的 RPC 请求。
 * 在这里，我们首先生成一个唯一的correlationId 编号并保存它——我们的消费者回调将使用这个值来匹配适当的响应。
 * 然后，我们为回复创建一个专用的排他队列并订阅它。
 * 接下来，我们发布具有两个属性的请求消息： replyTo和correlationId。
 * 在这一点上，我们可以坐下来等待正确的响应到来。
 * 由于我们的消费者交付处理发生在一个单独的线程中，我们需要在响应到达之前暂停主线程。使用BlockingQueue是一种可能的解决方案。在这里，我们正在创建 容量设置为 1 的ArrayBlockingQueue，因为我们只需要等待一个响应。
 * 消费者正在做一项非常简单的工作，对于每条消费的响应消息，它都会检查相关性ID 是否 是我们正在寻找的。如果是这样，它将响应放入BlockingQueue。
 * 同时主线程正在等待响应从BlockingQueue 中获取它。
 * 最后我们将响应返回给用户。
 * @author yyl
 * 2021/12/23 11:28
 */
public class RPCClient {

    private static final String RPC_QUEUE = "rpc_queue";
    private static String REPLAY_QUEUE = null;
    private static BlockingQueue<String> responses = new ArrayBlockingQueue<>(1);


    private static Channel getChannel() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:2692440667@www.youngeryang.top/%2FAbstract");
        Connection connection = connectionFactory.newConnection();
        return connection.createChannel();
    }

    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        Channel channel = getChannel();
        // 任务要发送到的队列
        channel.queueDeclare(RPC_QUEUE, true, true, true, null);
        // client监听任务结果的回调队列
        REPLAY_QUEUE = channel.queueDeclare().getQueue();


        startReceiveResult(channel);
        startSendTask(channel);
    }

    /**
     * 开始接收结果
     */
    private static void startReceiveResult(Channel channel) throws IOException {
        
    }


    /**
     * 开始发送任务
     */
    private static void startSendTask(Channel channel) throws IOException {

        // 和此次消息相关联的ID
        String correlationId = UUID.randomUUID().toString();

        // 创建消息属性对象，包含correlationId和replyQueue
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder().correlationId(correlationId).replyTo(REPLAY_QUEUE).contentType("text/plain").contentEncoding(StandardCharsets.UTF_8.name()).build();

        // 发送32个rpc，让客户端去消费消息
        for (int i = 0; i < 32; i++) {
            System.out.println(" [x] Requesting fib(" + i + ")");
            channel.basicPublish("", RPC_QUEUE, basicProperties, String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
    }








}
