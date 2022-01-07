package boot.rabbitmq.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyl
 * 2022/1/7 14:53
 */
@Configuration
public class RabbitConfig {

    /**
     * 指定RabbitTemplate将消息转换成byte[]的转换方式。
     *  默认RabbitmqTemplate的消息转换器是SimpleMessageConverter。byte[]不理会，String转byte[]并将字符编码写入properties中。Serializable对象采用ByteOutputStream进行序列化（效率慢）
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
