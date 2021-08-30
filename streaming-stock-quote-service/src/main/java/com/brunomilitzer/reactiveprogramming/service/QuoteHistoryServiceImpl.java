package com.brunomilitzer.reactiveprogramming.service;

import com.brunomilitzer.reactiveprogramming.domain.QuoteHistory;
import com.brunomilitzer.reactiveprogramming.model.Quote;
import com.brunomilitzer.reactiveprogramming.repositories.QuoteHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QuoteHistoryServiceImpl implements QuoteHistoryService {

    private final QuoteHistoryRepository repository;

    @Override
    public Mono<QuoteHistory> saveQuoteToMongo( final Quote quote ) {

        return this.repository.save( QuoteHistory.builder()
                .ticker( quote.getTicker() )
                .price( quote.getPrice() )
                .instant( quote.getInstant() )
                .build()
        );
    }

}
