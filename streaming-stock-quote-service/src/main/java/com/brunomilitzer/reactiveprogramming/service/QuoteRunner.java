package com.brunomilitzer.reactiveprogramming.service;

import com.brunomilitzer.reactiveprogramming.config.RabbitConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteRunner implements CommandLineRunner {

    private final QuoteGeneratorService quoteGeneratorService;
    private final QuoteHistoryService quoteHistoryService;
    private final QuoteMessageSender quoteMessageSender;
    private final Receiver receiver;

    @Override
    public void run( final String... args ) throws Exception {

        final CountDownLatch countDownLatch = new CountDownLatch( 50 );

        this.quoteGeneratorService.fetchQuoteStream( Duration.ofMillis( 100l ) )
                .take( 50 )
                .log( "Got Quote: " )
                //.flatMap( this.quoteHistoryService::saveQuoteToMongo )
                .flatMap( message -> {
                    Mono<Void> quoteMessage = null;

                    try {
                        quoteMessage = this.quoteMessageSender.sendQuoteMessage( message );
                    } catch ( final JsonProcessingException e ) {
                        e.printStackTrace();
                    }
                    return quoteMessage;
                } )
                .subscribe( result -> {
                    log.debug( "Saved Quote: " + result );
                }, throwable -> {
                    // handle error here...
                    log.error( "Some error", throwable );
                }, () -> {
                    log.debug( "All done!!!!" );
                } );

        countDownLatch.await( 1, TimeUnit.SECONDS );

        final AtomicInteger receivedCount = new AtomicInteger();

        this.receiver.consumeAutoAck( RabbitConfig.QUEUE ).log( "Msg Receiver" ).subscribe( msg -> {
            log.debug( "Received Message # {} - {}", receivedCount.incrementAndGet(), new String( msg.getBody() ) );
        }, throwable -> {
            log.debug( "Error Receiving", throwable );
        }, () -> {
            log.debug( "Complete" );
        } );

    }

}
