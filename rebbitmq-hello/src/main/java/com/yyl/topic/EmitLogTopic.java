package com.yyl.topic;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * javac EmitLogTopic routing_key
 * @author yyl
 * 2021/12/22 17:01
 */
public class EmitLogTopic {

    private static final String EXCHANGE_NAME = "topic_logs";
    private static final String[] severity = {"info", "warn", "error"};

    public static void main(String[] args) throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        String routingKey = getRoutingKey(args);
        String message = getMessage();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        AMQP.BasicProperties basicProperties = MessageProperties.TEXT_PLAIN.builder().contentEncoding(StandardCharsets.UTF_8.name()).build();

        while (true){
            channel.basicPublish(EXCHANGE_NAME, routingKey, basicProperties, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
            Thread.sleep(1000);
        }

    }

    private static String getMessage() {
        return String.format("[%s] %s 系统运行日志", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date()),
                severity[(int)(Math.random() * 10) % severity.length]);
    }

    private static String getRoutingKey(String[] args) {
        if (!Objects.equals(args[0], "")){
            return args[0];
        }
        System.out.println("usage: javac EmitLogTopic routing_key");
        System.exit(0);
        return null;
    }

}
