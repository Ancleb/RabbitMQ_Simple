package boot.rabbitmq.consume;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 一个消费者
 * @author yyl
 * 2022/1/7 15:09
 */
@Component
public class Consumer {

    // bindings中出现的queue，直接listener监听，不需要在queues参数中再次声明要监听的队列。
    @RabbitListener(
            bindings = {
              @QueueBinding(
                      value = @Queue(name = "safe_confirm_return_queue", durable = "true", exclusive = "false", autoDelete = "false"),
                      exchange = @Exchange("safe_confirm_return_exchange"),
                      key = "routing_key"
              )
            },
            // concurrency: 当前Listener容器启动多少个线程去监听队列。
            concurrency = "2"
    )

    // org.springframework.messaging.Message 是通过amqpMessage生成的对象，其中有许多框架层面添加的参数，供框架内部使用。用户一般使用spring.amqp.core下的Message
    public void receiverMsg(Message message, Channel channel, org.springframework.messaging.Message msg) throws IOException {
        System.out.println("consumer" + Thread.currentThread().getName() + "收到消息：message = " + message);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
