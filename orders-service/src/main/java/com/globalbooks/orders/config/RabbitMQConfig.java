package com.globalbooks.orders.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String ORDER_EXCHANGE = "globalbooks.order.exchange";
    public static final String PAYMENT_EXCHANGE = "globalbooks.payment.exchange";
    public static final String SHIPPING_EXCHANGE = "globalbooks.shipping.exchange";
    public static final String DLX_EXCHANGE = "globalbooks.dlx.exchange";

    // Queue names
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_STATUS_QUEUE = "order.status.queue";
    public static final String PAYMENT_REQUEST_QUEUE = "payment.request.queue";
    public static final String PAYMENT_RESPONSE_QUEUE = "payment.response.queue";
    public static final String SHIPPING_STATUS_QUEUE = "shipping.status.queue";
    public static final String ORDER_DLQ = "order.dlq";

    // Routing keys
    public static final String ORDER_CREATED_KEY = "order.created";
    public static final String ORDER_STATUS_KEY = "order.status";
    public static final String PAYMENT_REQUEST_KEY = "payment.request";
    public static final String PAYMENT_RESPONSE_KEY = "payment.response";
    public static final String SHIPPING_STATUS_KEY = "shipping.status.*";
    public static final String ORDER_DLQ_KEY = "order.dead";

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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);

        // Confirm callback
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.println("Message not delivered: " + cause);
            }
        });

        // Return callback
        template.setReturnsCallback(returned -> {
            System.err.println("Message returned: " + returned.getMessage());
        });

        return template;
    }

    // Exchanges
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE, true, false);
    }

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
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_DLQ_KEY)
                .build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_DLQ_KEY)
                .build();
    }

    @Bean
    public Queue paymentResponseQueue() {
        return QueueBuilder.durable(PAYMENT_RESPONSE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_DLQ_KEY)
                .build();
    }

    @Bean
    public Queue shippingStatusQueue() {
        return QueueBuilder.durable(SHIPPING_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_DLQ_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(ORDER_DLQ).build();
    }

    // Bindings
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_KEY);
    }

    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder
                .bind(orderStatusQueue())
                .to(orderExchange())
                .with(ORDER_STATUS_KEY);
    }

    @Bean
    public Binding paymentResponseBinding() {
        return BindingBuilder
                .bind(paymentResponseQueue())
                .to(paymentExchange())
                .with(PAYMENT_RESPONSE_KEY);
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
                .with(ORDER_DLQ_KEY);
    }
}