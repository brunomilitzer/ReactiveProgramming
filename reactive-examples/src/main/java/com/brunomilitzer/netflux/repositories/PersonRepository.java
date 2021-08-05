package com.brunomilitzer.netflux.repositories;

import com.brunomilitzer.netflux.domain.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonRepository {

    Mono<Person> getById( Integer id );

    Flux<Person> findAll();

}
