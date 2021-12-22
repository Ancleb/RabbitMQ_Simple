package com.yyl.topic;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * @author yyl
 * 2021/12/22 17:01
 */
public class EmitLogTopic {

    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:guest@www.youngeryang.top/%2FAbstract");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        AMQP.BasicProperties basicProperties = MessageProperties.TEXT_PLAIN.builder().contentEncoding(StandardCharsets.UTF_8.name()).build();


        while (true){
            String routingKey = getRoutingKey();
            String message = getMessage(routingKey);
            channel.basicPublish(EXCHANGE_NAME, routingKey, basicProperties, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
            Thread.sleep(1000);
        }
    }

    private static String getMessage(String routingKey) {
        return String.format("[%s] %s",
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date()),
                logs.get(routingKey));
    }

    private static String getRoutingKey() {
        return (String) logs.keySet().toArray()[((int) (Math.random() * 10)) % logs.size()];
    }

    private static final Map<String, String> logs = new HashMap<>();

    static {
        logs.put("error.kern.critical", "一个内核错误");
        logs.put("info.kern.started", "一个内核启动日志");
        logs.put("warn.network.timeout", "一个网络超时警告");
    }



}
