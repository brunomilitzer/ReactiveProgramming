package com.brunomilitzer.netflux.repositories;

import com.brunomilitzer.netflux.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class PersonRepositoryImplTest {

    private PersonRepository repository;

    @BeforeEach
    void setUp() {

        this.repository = new PersonRepositoryImpl();
    }

    @Test
    void getByIdBlock() {

        final Mono<Person> personMono = this.repository.getById( 1 );

        final Person person = personMono.block();

        System.out.println( person.toString() );
    }

    @Test
    void testByIdSubscribe() {

        final Mono<Person> personMono = this.repository.getById( 1 );

        StepVerifier.create( personMono ).expectNextCount( 1 ).verifyComplete();

        personMono.subscribe( person -> {
            System.out.println( person.toString() );
        } );
    }

    @Test
    void testByIdSubscribeNotFound() {

        final Mono<Person> personMono = this.repository.getById( 9 );

        StepVerifier.create( personMono ).expectNextCount( 0 ).verifyComplete();

        personMono.subscribe( person -> {
            System.out.println( person.toString() );
        } );
    }

    @Test
    void getByIdMapFunction() {

        final Mono<Person> personMono = this.repository.getById( 1 );

        personMono.map( person -> {
            System.out.println( person.toString() );

            return person.getFirstName();
        } ).subscribe( firstName -> {
            System.out.println( "from map: " + firstName );
        } );

    }

    @Test
    void fluxTestBlockFirst() {

        final Flux<Person> personFlux = this.repository.findAll();

        final Person person = personFlux.blockFirst();

        System.out.println( person.toString() );

    }

    @Test
    void testFluxSubscribe() {

        final Flux<Person> personFlux = this.repository.findAll();

        StepVerifier.create( personFlux ).expectNextCount( 4 ).verifyComplete();

        personFlux.subscribe( person -> {
            System.out.println( person.toString() );
        } );
    }

    @Test
    void testFluxToListMono() {

        final Flux<Person> personFlux = this.repository.findAll();

        final Mono<List<Person>> personListMono = personFlux.collectList();
        personListMono.subscribe( list -> {
            list.forEach( person -> System.out.println( person.toString() ) );
        } );

    }

    @Test
    void testFindPersonById() {

        final Flux<Person> personFlux = this.repository.findAll();

        final Integer id = 3;

        final Mono<Person> personListMono = personFlux.filter( person -> person.getId().equals( id ) ).next();

        personListMono.subscribe( person -> System.out.println( person.toString() ) );

    }

    @Test
    void testFindPersonByIdNotFound() {

        final Flux<Person> personFlux = this.repository.findAll();

        final Integer id = 9;

        final Mono<Person> personListMono = personFlux.filter( person -> person.getId().equals( id ) ).next();

        personListMono.subscribe( person -> System.out.println( person.toString() ) );

    }

    @Test
    void testFindPersonByIdNotFoundWithException() {

        final Flux<Person> personFlux = this.repository.findAll();

        final Integer id = 9;

        final Mono<Person> personListMono = personFlux.filter( person -> person.getId().equals( id ) ).single();

        personListMono.doOnError( throwable -> System.out.println( "No Person found!" ) )
                .onErrorReturn( Person.builder().id( id ).build() ).subscribe( person -> System.out.println( person.toString() ) );

    }

}