package boot.rabbitmq.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息投递回调接口
 *
 * @author yyl
 * 2022/1/7 14:56
 */
@Component
public class DeliverConfirmCallBack implements RabbitTemplate.ConfirmCallback{
    @Autowired
    public void setAmqpTemplate(RabbitTemplate rabbitTemplate){
        rabbitTemplate.setConfirmCallback(this);
    }
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData != null ? correlationData.getId() : "";
        if (ack) {
            System.out.println("broker确认消息。ack = true,  相关id: " + id + ",  cause:" + cause);
        } else {
            System.out.println("broker否定消息。ack = false,  消息相关id: " + id + ",  cause:" + cause);
        }
    }
}
