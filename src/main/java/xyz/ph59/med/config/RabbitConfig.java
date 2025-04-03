package xyz.ph59.med.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String TOPIC = "EvalTask";
    public static final String EXCHANGE_NAME = "EvalExchangeDirect";
    public static final String ROUTING_KEY = "evalTaskDirectRoute";

    @Bean
    public Queue EvalTaskQueue() {
        return new Queue(TOPIC, true);
    }

    @Bean
    public DirectExchange EvalExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding Bind() {
        return BindingBuilder.bind(this.EvalTaskQueue())
                .to(this.EvalExchange())
                .with(ROUTING_KEY);
    }

}
