package rabbitmq.boot.config;

import com.rabbitmq.client.BuiltinExchangeType;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yyl
 * 2022/1/6 17:11
 */
@Configuration
public class RabbitDeclare {

    /**
     * 声明自定义交换机
     *   插件提供的延迟交换机
     */
    @Bean
    public CustomExchange delayMessageExchange(){
        Map<String, Object> delayedMsgExArgs = new HashMap<>(2);
        delayedMsgExArgs.put("x-delayed-type", BuiltinExchangeType.DIRECT.getType());
        return new CustomExchange("delayMessageExchange", "x-delayed-message", true, false, delayedMsgExArgs);
    }

    /**
     * 声明一个队列
     */
    // @Bean // 不使用@Bean看看是否可以声明出来Queue
    public Queue delayMessageQueue(){
        return QueueBuilder.durable("delayPluginQueue").build();
    }

    /**
     * 声明绑定关系
     */
    @Bean
    public Binding delayBinding(){
        return BindingBuilder.bind(delayMessageQueue()).to(delayMessageExchange()).with(delayMessageQueue().getName()).noargs();
    }
}
