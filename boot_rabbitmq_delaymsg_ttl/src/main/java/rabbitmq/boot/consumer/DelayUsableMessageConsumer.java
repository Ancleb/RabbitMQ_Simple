package rabbitmq.boot.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 延迟队列，消费者
 *
 * @author: CoderWater
 * @create: 2022/1/5
 */
@Component
@Slf4j
public class DelayUsableMessageConsumer {

    @RabbitListener(queues = "delayUsableQueue")
    public void receivedDelayMsg(Channel channel, Message message){
        System.out.println(message);
    }

}
