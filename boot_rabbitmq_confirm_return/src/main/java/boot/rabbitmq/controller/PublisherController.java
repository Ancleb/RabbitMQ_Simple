package boot.rabbitmq.controller;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author yyl
 * 2022/1/7 15:42
 */
@RestController
@RequestMapping("confirmAndReturn")
public class PublisherController {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @GetMapping("sendMessage/{msg}")
    public String sendMessage(@PathVariable String msg) {
        // amqpTemplate.convertAndSend("safe_confirm_return_exchange", "routing_key_error", msg);
        amqpTemplate.convertAndSend("safe_confirm_return_exchange", "routing_key", msg);
        return String.format("消息发送成功：【%s】 %s", DateFormat.getTimeInstance().format(new Date()), msg);
    }
}
