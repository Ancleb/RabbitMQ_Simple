package com.yyl.start.rpc;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
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
public class RPCClient implements AutoCloseable{

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";

    public RPCClient() throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
    }

    public static void main(String[] args) {
        try (RPCClient fibonacciRpc = new RPCClient()) {
            for (int i = 0; i < 50; i++) {
                System.out.println(" [x] Requesting fib(" + i + ")");
                String response = fibonacciRpc.call(Integer.toString(i));
                System.out.println(" [.] Got '" + response + "'");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 远程调用 一次rpc远程调用，将会产生一个consumer，用于等待rpc的结果返回，返回结果后，消费者自我删除。
     * @param message 调用参数
     */
    public String call(String message) throws IOException, InterruptedException {
        final String correlationId = UUID.randomUUID().toString();
        // rpc调用结果会投递到replyQueue队列中
        String replyQueueName = channel.queueDeclare().getQueue();
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder()
                .contentType("text/plain").contentEncoding(StandardCharsets.UTF_8.name())
                .correlationId(correlationId).replyTo(replyQueueName)
                .build();
        // 开始rpc远程调用
        // 因为上次server未能消费的大斐波那契数在队列中堆积，在RpcServer重启后，会继续接着消费，所以要清楚队列中的消息。
        channel.queuePurge(requestQueueName);
        channel.basicPublish("", requestQueueName, basicProperties, message.getBytes(StandardCharsets.UTF_8));

        // 存放rpcResponse的阻塞队列。（也可以使用对象锁的方式阻塞线程）
        final BlockingQueue<String> responses = new ArrayBlockingQueue<>(1);

        String curConsumerTag = channel.basicConsume(replyQueueName, (consumerTag, msg) -> {
            System.out.println("consumerTag = " + consumerTag);
            // 如果
            if (Objects.equals(msg.getProperties().getCorrelationId(), correlationId)){
                responses.offer(new String(msg.getBody(), msg.getProperties().getContentEncoding()));
            }
        }, consumerTag -> System.out.println("consumerTag:" + consumerTag + "：取消订阅"));

        // take：若首位为空，则线程阻塞，直到首位有对象，并将对象取出。
        String result = responses.take();
        // 消费者取消订阅
        // 用于监听rpc结果消息队列的消费者。   也就意味着一次rpc远程调用，将会产生一个consumer，用于等待rpc的结果返回，返回结果后，消费者自我删除。
        channel.basicCancel(curConsumerTag);
        return result;
    }

    @Override
    public void close() throws Exception {
        channel.close();
        connection.close();
    }
}
