package com.yyl.routing;

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
import java.util.concurrent.TimeoutException;

/**
 * @author yyl
 * 2021/12/22 13:35
 */
public class EmitLogDirect {

    private static final String EXCHANGE_NAME = "direct_logs";
    private static final Map<String, String> logs = new HashMap<>();

    static {
        logs.put("info", "系统运行中");
        logs.put("warn", "内存不足");
        logs.put("error", "OutOfMemory");
    }

    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException, InterruptedException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUri("amqp://abstract:2692440667@www.youngeryang.top/%2FAbstract");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // 声明一个Direct交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true, false, null);
        // 消息属性Properties
        AMQP.BasicProperties messageProperties = MessageProperties.TEXT_PLAIN.builder().contentEncoding(StandardCharsets.UTF_8.name()).build();

        while (true) {
            // 日志等级
            String severity = randomSeverity();
            channel.basicPublish(EXCHANGE_NAME, severity, messageProperties, generateLog(severity).getBytes(StandardCharsets.UTF_8));
            System.out.println("sent: " + severity);
            Thread.sleep(1000);
        }
    }


    private static String randomSeverity(){
        return (String) logs.keySet().toArray()[(int) (Math.random() * 10) % logs.size()];
    }
    private static String generateLog(String severity){
        return String.format("[%s] %s %s", DateFormat.getDateTimeInstance().format(new Date()),
                severity, logs.get(severity));
    }


}
