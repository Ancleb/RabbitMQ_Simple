package rabbitmq.boot.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq配置类
 * @author yyl
 * 2022/1/5 16:06
 */
@Configuration
public class RabbitmqConfig {

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
    public Queue queueNormal(){
        return new Queue()
    }
}
