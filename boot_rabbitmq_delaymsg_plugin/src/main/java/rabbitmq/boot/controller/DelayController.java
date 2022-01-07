package rabbitmq.boot.controller;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author: CoderWater
 * @create: 2022/1/6
 */
@RestController
@RequestMapping("delayMsgEx")
public class DelayController {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @GetMapping("sendMessage/{msg}/{delay}")
    public String sendMessage(@PathVariable String msg, @PathVariable Integer delay){

        amqpTemplate.convertAndSend("delayMessageExchange", "delayPluginQueue", msg, message -> {
            message.getMessageProperties().setHeader("x-delay", 10000);
            return message;
        });
        return "{" + DateFormat.getTimeInstance().format(new Date()) + "}消息发送成功:" + msg + ", delay:" + delay / 1000 + "s";
    }
}
