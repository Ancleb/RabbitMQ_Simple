package rabbitmq.boot.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyl
 * 2022/1/6 17:07
 */
@Configuration
public class RabbitConfig {

    /**
     * 指定RabbitTemplate将消息转换成byte[]的转换方式。
     *  模式SimpleMessageConverter，byte[]无需转换，String转换并将encoding写入properties，Serializable对象使用JDK的ByteOutputStream转换（性能慢，差）
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
