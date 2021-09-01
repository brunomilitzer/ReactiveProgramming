package com.brunomilitzer.reactiveprogramming.service;

import com.brunomilitzer.reactiveprogramming.config.RabbitConfig;
import com.brunomilitzer.reactiveprogramming.model.Quote;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.OutboundMessageResult;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteMessageSender {

    private final ObjectMapper objectMapper;
    private final Sender sender;

    public Mono<Void> sendQuoteMessage( final Quote quote ) throws JsonProcessingException {

        final byte[] jsonBytes = this.objectMapper.writeValueAsBytes( quote );

        final Flux<OutboundMessageResult> confirmations = this.sender.sendWithPublishConfirms( Flux.just( new OutboundMessage( "", RabbitConfig.QUEUE, jsonBytes ) ) );

        this.sender.declareQueue( QueueSpecification.queue( RabbitConfig.QUEUE ) )
                .thenMany( confirmations ).doOnError( e -> log.error( "Send failed", e ) )
                .subscribe( r -> {
                    if ( r.isAck() ) {
                        log.info( "Message sent successfully {}", new String( r.getOutboundMessage().getBody() ) );
                    }
                } );

        return Mono.empty();
    }

}
