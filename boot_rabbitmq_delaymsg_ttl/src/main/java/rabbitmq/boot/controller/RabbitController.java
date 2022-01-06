package rabbitmq.boot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author: CoderWater
 * @create: 2022/1/5
 */
@RestController
@RequestMapping("ttl")
@Slf4j
public class RabbitController {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @GetMapping("sendMessage/{routingKey}/{msg}/{ttl}")
    public String sendMessage(@PathVariable("routingKey") String queueName, @PathVariable String msg, @PathVariable Integer ttl){
        amqpTemplate.convertAndSend("delay_normal_ex", queueName, msg, message -> {
            message.getMessageProperties().setExpiration(ttl.toString());
            // 内部的convertMessageIfNecessary中使用SimpleMessageConverter，对非String类型数据使用了默认的UTF-8编码进行编码成byte[]数据。 byte[]是MQ原生支持的发送数据类型。
            // message.getMessageProperties().setContentType(StandardCharsets.UTF_8.toString());
            return message;
        });
        return "{" + DateFormat.getTimeInstance().format(new Date()) + "}消息发送成功:" + msg + ", ttl:" + ttl / 1000 + "s";
    }
}
