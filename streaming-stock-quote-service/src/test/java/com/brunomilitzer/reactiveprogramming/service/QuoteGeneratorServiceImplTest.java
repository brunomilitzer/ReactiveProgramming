package com.brunomilitzer.reactiveprogramming.service;

import com.brunomilitzer.reactiveprogramming.model.Quote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

class QuoteGeneratorServiceImplTest {

    private QuoteGeneratorService service;

    @BeforeEach
    void setUp() {

        this.service = new QuoteGeneratorServiceImpl();
    }

    @Test
    void fetchQuoteStream() throws InterruptedException {

        final Flux<Quote> quoteFlux = this.service.fetchQuoteStream( Duration.ofMillis( 100l ) );

        final Consumer<Quote> quoteConsumer = System.out::println;

        final Consumer<Throwable> throwableConsumer = e -> System.out.println( e.getMessage() );

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final Runnable done = countDownLatch::countDown;

        quoteFlux.take( 30 ).subscribe( quoteConsumer, throwableConsumer, done );

        countDownLatch.await();
    }

}