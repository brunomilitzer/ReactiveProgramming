package com.brunomilitzer.reactiveprogramming.service;

import com.brunomilitzer.reactiveprogramming.domain.QuoteHistory;
import com.brunomilitzer.reactiveprogramming.model.Quote;
import reactor.core.publisher.Mono;

public interface QuoteHistoryService {

    Mono<QuoteHistory> saveQuoteToMongo( Quote quote );

}
