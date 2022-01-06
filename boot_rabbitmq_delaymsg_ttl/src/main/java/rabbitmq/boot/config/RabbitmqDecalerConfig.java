package rabbitmq.boot.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * rabbitmq配置类
 * @author yyl
 * 2022/1/5 16:06
 */
@Configuration
public class RabbitmqDecalerConfig {

    // 普通交换机
    private static final String delayNormalEx = "delay_normal_ex";
    // 普通队列
    private static final String delayNormalQueue10 = "second_10";
    // 普通队列
    private static final String delayNormalQueue20 = "second_20";

    // 死信交换机
    private static final String delayDeadEx = "delay_dead_ex";
    // 死信队列
    private static final String delayUsableQueue = "delayUsableQueue";


    @Bean
    public DirectExchange directNormalEx(){
        return new DirectExchange(delayNormalEx, true, false);
    }
    @Bean
    public DirectExchange directDeadEx(){
        return new DirectExchange(delayDeadEx, true, false);
    }

    @Bean
    public Queue queueNormal10(){
        Map<String, Object> queueArgs = new HashMap<>();
        queueArgs.put("x-dead-letter-exchange", delayDeadEx);
        queueArgs.put("x-dead-letter-routing-key", "usableMessage");
        // queueArgs.put("x-message-ttl", 10000);
        return new Queue(delayNormalQueue10, true, false, false, queueArgs);
    }

    @Bean
    public Queue queueNormal20(){
        Map<String, Object> queueArgs = new HashMap<>(3);
        queueArgs.put("x-dead-letter-exchange", delayDeadEx);
        queueArgs.put("x-dead-letter-routing-key", "usableMessage");
        // queueArgs.put("x-message-ttl", 10000);
        return new Queue(delayNormalQueue20, true, false, false, queueArgs);
    }

    @Bean
    public Queue delayDeadQueue(){
        return QueueBuilder.durable(delayUsableQueue).build();
    }


    @Bean
    public Binding bindingNormal10(){
        return new Binding(delayNormalQueue10, Binding.DestinationType.QUEUE, delayNormalEx, delayNormalQueue10, null);
    }

    @Bean
    public Binding bindingNormal20(){
        return new Binding(delayNormalQueue20, Binding.DestinationType.QUEUE, delayNormalEx, delayNormalQueue20, null);
    }

    @Bean
    public Binding bindingDeadQueue(){
        return BindingBuilder.bind(delayDeadQueue()).to(directDeadEx()).with("usableMessage");
    }

}
