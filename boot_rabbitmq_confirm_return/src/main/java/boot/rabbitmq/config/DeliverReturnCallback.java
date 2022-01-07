package boot.rabbitmq.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yyl
 * 2022/1/7 16:45
 */
@Component
public class DeliverReturnCallback implements RabbitTemplate.ReturnsCallback {

    @Autowired
    public void init(RabbitTemplate rabbitTemplate){
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        System.out.println("消息没有被正确路由到queue中：returnedCallBack: "  + returned);
    }
}
