package com.brunomilitzer.reactiveprogramming.service;

import com.brunomilitzer.reactiveprogramming.model.Quote;
import reactor.core.publisher.Flux;

import java.time.Duration;

public interface QuoteGeneratorService {

    Flux<Quote> fetchQuoteStream( Duration period );

}
