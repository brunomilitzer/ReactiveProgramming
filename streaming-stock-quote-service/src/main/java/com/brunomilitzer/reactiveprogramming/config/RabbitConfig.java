package com.brunomilitzer.reactiveprogramming.config;

import com.rabbitmq.client.Connection;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

import javax.annotation.PreDestroy;


@Configuration
public class RabbitConfig {

    public static final String QUEUE = "quotes";

    @Autowired
    private Mono<Connection> connectionMono;

    @Bean
    public Mono<Connection> connectionMono( final CachingConnectionFactory connectionFactory ) {

        return Mono.fromCallable( () -> connectionFactory.getRabbitConnectionFactory().newConnection() );
    }

    @PreDestroy
    public void close() throws Exception {

        this.connectionMono.block().close();
    }

    @Bean
    public Sender sender( final Mono<Connection> mono ) {

        return RabbitFlux.createSender( new SenderOptions().connectionMono( mono ) );
    }

    @Bean
    public Receiver receiver( final Mono<Connection> mono ) {

        return RabbitFlux.createReceiver( new ReceiverOptions().connectionMono( mono ) );
    }

}
