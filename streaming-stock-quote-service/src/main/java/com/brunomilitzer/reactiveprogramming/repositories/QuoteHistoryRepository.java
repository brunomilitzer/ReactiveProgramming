package com.brunomilitzer.reactiveprogramming.repositories;

import com.brunomilitzer.reactiveprogramming.domain.QuoteHistory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface QuoteHistoryRepository extends ReactiveMongoRepository<QuoteHistory, String> {

}
