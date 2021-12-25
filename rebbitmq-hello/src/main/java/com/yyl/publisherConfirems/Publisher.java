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
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
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
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
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
        // publishMessage2(channel);
        publishMessage3(channel);

        connection.close();
    }


    /**
     * 策略1：单独发布消息
     * 在这个示例中，每发布一个消息就要同步等待消息是否被确认。吞吐量低。
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
     * 在这个示例中一次性发布多个消息，并同步等待所有的消息被broker确认收到。提高了吞吐量。
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


    /**
     * 策略3：异步处理发布服务器确认
     * broker异步的确认已发布的消息，client端只需要注册一个回调函数，就可以收到broker具体的确认通知。
     * 并且可以被拒绝的消息进行其他操作
     */
    public static void publishMessage3(Channel channel) throws IOException, InterruptedException, TimeoutException {
        // 声明一个临时队列
        // String queue = channel.queueDeclare().getQueue();
        String queue = "not exist queue, in order to simulate message not confirm";
        // 存入发布的所有消息，等待被确认。Concurrent支持并发访问，因为发布回调是在多线程下调用的
        ConcurrentNavigableMap<Long, String> outStandingConfirms = new ConcurrentSkipListMap<>();

        channel.addConfirmListener((deliveryTag, multiple) -> {
            System.out.printf("broker确认消息成功回调：deliverTag:%s, multiple:%s%n", deliveryTag, multiple);
            if (multiple) {
                // 所有小于等于deliverTag的消息都被确认，因为deliverTag是递增的。
                outStandingConfirms.headMap(deliveryTag, true).clear();
            }else {
                outStandingConfirms.remove(deliveryTag);
            }
        }, (deliveryTag, multiple) -> {
            System.out.printf("broker nack-ed拒绝消息回调：deliverTag:%s, multiple:%s%n", deliveryTag, multiple);
            // 重新发布被nack-ed的消息虽然看起来很好，但是应该避免这样做。
            // 因为确认回调是在I/O线程中分派的，在回调中不应该使用channel执行操作。更好的解决方案是使用一个队列，在消息发布线程和回调线程之间进行通信。发布线程轮询读取队列中的消息。
        });



        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder().contentEncoding("text/plain").contentEncoding(StandardCharsets.UTF_8.name()).build();
        for (int i = 0; i < 10; i++) {
            String message = "策略1：单独发布消息, i = " + i;
            // 简单的发布一个消息
            long nextPublishSeqNo = channel.getNextPublishSeqNo(); // 在确认模式下，返回下一条要发布的消息的序列号。deliverTag
            outStandingConfirms.put(nextPublishSeqNo, message);
            channel.basicPublish("", queue, basicProperties, message.getBytes(StandardCharsets.UTF_8));
        }

        System.out.printf("正在等待自上次confirms后发送的消息被全部被确认%n");
        channel.waitForConfirmsOrDie(5_000);


        // broker没有成功确认的消息。
        System.out.println(outStandingConfirms);
    }
}
