package com.yyl.publishAndSubScribe;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * 日志生产者
 * 在我们的日志系统中，每个正在运行的接收程序副本都会收到消息。这样，我们就可以运行一个接收器并将日志定向到磁盘；同时，我们可以运行另一个接收器并在屏幕上看到日志。
 *
 * @author: CoderWater
 * @create: 2021/12/21
 */
public class EmitLog {

    private static final String EXCHANGES_NAME = "logs";

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:2692440667@www.youngeryang.top/%2FAbstract");

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            //创建exchange
            channel.exchangeDeclare(EXCHANGES_NAME, BuiltinExchangeType.FANOUT, true, false, null);



            while (true){
                String message = "Hello RabbitMQ By Fanout Exchange. " + DateFormat.getDateTimeInstance().format(new Date());
                AMQP.BasicProperties basicProperties = new AMQP.BasicProperties().builder().contentEncoding(StandardCharsets.UTF_8.name()).contentType("text/plain").build();
                channel.basicPublish(EXCHANGES_NAME, "", basicProperties, message.getBytes(StandardCharsets.UTF_8));

                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
