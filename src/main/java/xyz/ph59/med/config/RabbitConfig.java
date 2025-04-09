package xyz.ph59.med.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.ph59.med.util.FastJsonMessageConverter;

@Configuration
public class RabbitConfig {
    public static final String TOPIC = "EvalTask";
    public static final String EXCHANGE_NAME = "EvalExchangeDirect";
    public static final String ROUTING_KEY = "evalTaskDirectRoute";

    public static final String RESULT_TOPIC = "EvalTaskResult";
    public static final String RESULT_EXCHANGE_NAME = "EvalExchangeDirect";
    public static final String RESULT_ROUTING_KEY = "evalTaskDirectRoute";

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

    @Bean
    public Queue EvalTaskResultQueue() {
        return new Queue(RESULT_TOPIC, true);
    }

    @Bean
    public DirectExchange EvalResultExchange() {
        return new DirectExchange(RESULT_EXCHANGE_NAME, true, false);
    }

    public Binding EvalResultQueueBind() {
        return BindingBuilder.bind(this.EvalTaskResultQueue())
                .to(this.EvalResultExchange())
                .with(RESULT_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new FastJsonMessageConverter());
        template.setReplyTimeout(60000);
        return template;
    }

}
