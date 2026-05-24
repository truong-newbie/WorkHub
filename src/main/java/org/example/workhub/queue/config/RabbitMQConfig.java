package org.example.workhub.queue.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${workhub.queue.exchange:workhub.exchange}")
    private String exchangeName;

    @Value("${workhub.queue.email-queue:email.queue}")
    private String emailQueue;

    @Value("${workhub.queue.ats-screening-queue:ats.screening.queue}")
    private String atsScreeningQueue;

    @Value("${workhub.queue.resume-parsing-queue:resume.parsing.queue}")
    private String resumeParsingQueue;

    @Value("${workhub.queue.notification-queue:notification.queue}")
    private String notificationQueue;

    @Value("${workhub.queue.email-dlq:email.dlq}")
    private String emailDlq;

    @Value("${workhub.queue.ats-screening-dlq:ats.screening.dlq}")
    private String atsScreeningDlq;

    @Value("${workhub.queue.resume-parsing-dlq:resume.parsing.dlq}")
    private String resumeParsingDlq;

    @Value("${workhub.queue.notification-dlq:notification.dlq}")
    private String notificationDlq;

    @Value("${workhub.queue.retry.max-attempts:3}")
    private Integer maxAttempts;

    @Bean
    public DirectExchange workhubExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return queueWithDlq(emailQueue, QueueRoutingKeys.EMAIL_FAILED);
    }

    @Bean
    public Queue atsScreeningQueue() {
        return queueWithDlq(atsScreeningQueue, QueueRoutingKeys.ATS_SCREENING_FAILED);
    }

    @Bean
    public Queue resumeParsingQueue() {
        return queueWithDlq(resumeParsingQueue, QueueRoutingKeys.RESUME_PARSING_FAILED);
    }

    @Bean
    public Queue notificationQueue() {
        return queueWithDlq(notificationQueue, QueueRoutingKeys.NOTIFICATION_FAILED);
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(emailDlq).build();
    }

    @Bean
    public Queue atsScreeningDlq() {
        return QueueBuilder.durable(atsScreeningDlq).build();
    }

    @Bean
    public Queue resumeParsingDlq() {
        return QueueBuilder.durable(resumeParsingDlq).build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(notificationDlq).build();
    }

    @Bean
    public Binding emailBinding(DirectExchange workhubExchange, @Qualifier("emailQueue") Queue emailQueue) {
        return BindingBuilder.bind(emailQueue).to(workhubExchange).with(QueueRoutingKeys.EMAIL_SEND);
    }

    @Bean
    public Binding atsScreeningBinding(DirectExchange workhubExchange, @Qualifier("atsScreeningQueue") Queue atsScreeningQueue) {
        return BindingBuilder.bind(atsScreeningQueue).to(workhubExchange).with(QueueRoutingKeys.ATS_SCREENING_REQUEST);
    }

    @Bean
    public Binding resumeParsingBinding(DirectExchange workhubExchange, @Qualifier("resumeParsingQueue") Queue resumeParsingQueue) {
        return BindingBuilder.bind(resumeParsingQueue).to(workhubExchange).with(QueueRoutingKeys.RESUME_PARSING_REQUEST);
    }

    @Bean
    public Binding notificationBinding(DirectExchange workhubExchange, @Qualifier("notificationQueue") Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue).to(workhubExchange).with(QueueRoutingKeys.NOTIFICATION_SEND);
    }

    @Bean
    public Binding emailDlqBinding(DirectExchange workhubExchange, @Qualifier("emailDlq") Queue emailDlq) {
        return BindingBuilder.bind(emailDlq).to(workhubExchange).with(QueueRoutingKeys.EMAIL_FAILED);
    }

    @Bean
    public Binding atsScreeningDlqBinding(DirectExchange workhubExchange, @Qualifier("atsScreeningDlq") Queue atsScreeningDlq) {
        return BindingBuilder.bind(atsScreeningDlq).to(workhubExchange).with(QueueRoutingKeys.ATS_SCREENING_FAILED);
    }

    @Bean
    public Binding resumeParsingDlqBinding(DirectExchange workhubExchange, @Qualifier("resumeParsingDlq") Queue resumeParsingDlq) {
        return BindingBuilder.bind(resumeParsingDlq).to(workhubExchange).with(QueueRoutingKeys.RESUME_PARSING_FAILED);
    }

    @Bean
    public Binding notificationDlqBinding(DirectExchange workhubExchange, @Qualifier("notificationDlq") Queue notificationDlq) {
        return BindingBuilder.bind(notificationDlq).to(workhubExchange).with(QueueRoutingKeys.NOTIFICATION_FAILED);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(maxAttempts == null ? 3 : maxAttempts)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }

    private Queue queueWithDlq(String queueName, String deadLetterRoutingKey) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(exchangeName)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }
}
