package com.brunomilitzer.reactiveprogramming.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteRunner implements CommandLineRunner {

    private final QuoteGeneratorService quoteGeneratorService;
    private final QuoteHistoryService quoteHistoryService;

    @Override
    public void run( final String... args ) throws Exception {

        this.quoteGeneratorService.fetchQuoteStream( Duration.ofMillis( 100l ) )
                .take( 50 )
                .log( "Got Quote: " )
                .flatMap( this.quoteHistoryService::saveQuoteToMongo )
                .subscribe( savedQuote -> {
                    log.debug( "Saved Quote: " + savedQuote );
                }, throwable -> {
                    // handle error here...
                    log.error( "Some error", throwable );
                }, () -> {
                    log.debug( "All done!!!!" );
                } );
    }

}
