package com.brunomilitzer.reactiveprogramming.repository;

import com.brunomilitzer.reactiveprogramming.domain.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonRepositoryImpl implements PersonRepository {

    private final Person bruno = new Person( 1, "Bruno", "Militzer" );
    private final Person vanessa = new Person( 2, "Vanessa", "de Garcez" );
    private final Person tales = new Person( 3, "Tales", "de Garcez" );
    private final Person jose = new Person( 4, "José", "Garcez" );

    @Override
    public Mono<Person> getById( final Integer id ) {

        return this.findAll().filter( person -> person.getId().equals( id ) ).next();
    }

    @Override
    public Flux<Person> findAll() {

        return Flux.just( this.bruno, this.vanessa, this.tales, this.jose );
    }

}
