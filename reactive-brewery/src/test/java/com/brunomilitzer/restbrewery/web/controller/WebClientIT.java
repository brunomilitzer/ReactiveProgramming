package com.brunomilitzer.restbrewery.web.controller;

import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.CountDownLatch;

/**
 * Created by jt on 3/7/21.
 */
public class WebClientIT {

    public static final String BASE_URL = "http://localhost:8080";

    WebClient webClient;

    @BeforeEach
    void setUp() {

        this.webClient = WebClient.builder()
                .baseUrl( BASE_URL )
                .clientConnector( new ReactorClientHttpConnector( HttpClient.create().wiretap( true ) ) )
                .build();
    }

    @Test
    void testListBeers() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final Mono<BeerPagedList> beerPagedListMono = this.webClient.get().uri( "/api/v1/beer" )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerPagedList.class );


//        BeerPagedList pagedList = beerPagedListMono.block();
//        pagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));
        beerPagedListMono.publishOn( Schedulers.parallel() ).subscribe( beerPagedList -> {

            beerPagedList.getContent().forEach( beerDto -> System.out.println( beerDto.toString() ) );

            countDownLatch.countDown();
        } );

        countDownLatch.await();
    }

}
