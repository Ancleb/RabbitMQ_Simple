package rabbitmq.boot.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyl
 * 2022/1/6 15:57
 */
@Configuration
public class RabbitMqConfig {


    /**
     * 指定RabbitTemplate的MessageConverter。
     *  默认是SimpleMessageConverter，对Serializable对象序列化方式采用JDK的ByteOutPutStream序列化。慢，差。
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
