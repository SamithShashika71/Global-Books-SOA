package com.globalbooks.shipping.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String SHIPPING_EXCHANGE = "globalbooks.shipping.exchange";
    public static final String PAYMENT_EXCHANGE = "globalbooks.payment.exchange";
    public static final String ORDER_EXCHANGE = "globalbooks.order.exchange";
    public static final String DLX_EXCHANGE = "globalbooks.dlx.exchange";

    // Queue names
    public static final String SHIPPING_REQUEST_QUEUE = "shipping.request.queue";
    public static final String SHIPPING_STATUS_QUEUE = "shipping.status.queue";
    public static final String PAYMENT_STATUS_QUEUE = "payment.status.queue";
    public static final String SHIPPING_DLQ = "order.dlq";

    // Routing keys
    public static final String SHIPPING_REQUEST_KEY = "shipping.request";
    public static final String SHIPPING_STATUS_KEY = "shipping.status.*";
    public static final String PAYMENT_COMPLETED_KEY = "payment.status.completed";
    public static final String SHIPPING_DLQ_KEY = "order.dead";

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Configure ObjectMapper to handle Java 8 time types
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pass the configured ObjectMapper to the constructor
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        return converter;
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);

        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("Message not delivered: " + cause);
            }
        });

        template.setReturnsCallback(returned -> {
            System.err.println("Message returned: " + returned.getMessage());
        });

        return template;
    }

    // Exchanges
    @Bean
    public TopicExchange shippingExchange() {
        return new TopicExchange(SHIPPING_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    // Queues
    @Bean
    public Queue shippingRequestQueue() {
        return QueueBuilder.durable(SHIPPING_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SHIPPING_DLQ_KEY)
                .withArgument("x-message-ttl", 600000) // 10 minutes TTL
                .build();
    }

    @Bean
    public Queue shippingStatusQueue() {
        return QueueBuilder.durable(SHIPPING_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SHIPPING_DLQ_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("order.dlq").build();
    }

    // Bindings
    @Bean
    public Binding shippingRequestBinding() {
        return BindingBuilder
                .bind(shippingRequestQueue())
                .to(shippingExchange())
                .with(SHIPPING_REQUEST_KEY);
    }

    @Bean
    public Binding shippingStatusBinding() {
        return BindingBuilder
                .bind(shippingStatusQueue())
                .to(shippingExchange())
                .with(SHIPPING_STATUS_KEY);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("order.dead");
    }
}