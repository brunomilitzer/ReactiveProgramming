package com.brunomilitzer.restbrewery.web.controller;

import com.brunomilitzer.restbrewery.bootstrap.BeerLoader;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jt on 3/7/21.
 */
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT )
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
    void getBeerById() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final Mono<BeerDto> beerDtoMono = this.webClient.get().uri( "api/v1/beer/1" )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerDto.class );

        beerDtoMono.subscribe( beer -> {
            assertThat( beer ).isNotNull();
            assertThat( beer.getBeerName() ).isNotNull();

            countDownLatch.countDown();
        } );

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void getBeerByUpc() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final Mono<BeerDto> beerDtoMono = this.webClient.get().uri( "api/v1/beerUpc/" + BeerLoader.BEER_2_UPC )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerDto.class );

        beerDtoMono.subscribe( beer -> {
            assertThat( beer ).isNotNull();
            assertThat( beer.getUpc() ).isNotNull();

            countDownLatch.countDown();
        } );

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void testListBeers() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final Mono<BeerPagedList> beerPagedListMono = this.webClient.get().uri( "/api/v1/beer" )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerPagedList.class );

        beerPagedListMono.publishOn( Schedulers.parallel() ).subscribe( beerPagedList -> {

            beerPagedList.getContent().forEach( beerDto -> System.out.println( beerDto.toString() ) );

            countDownLatch.countDown();
        } );

        countDownLatch.await();
    }

    @Test
    void testSaveBeer() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final BeerDto beerDto = BeerDto.builder().beerName( "Eisenbhan" )
                .upc( "1234555" ).beerStyle( "LAGER" )
                .price( new BigDecimal( "8.99" ) ).build();

        final Mono<ResponseEntity<Void>> beerResponseMono = this.webClient.post().uri( "/api/v1/beer" )
                .accept( MediaType.APPLICATION_JSON ).body( BodyInserters.fromValue( beerDto ) )
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn( Schedulers.parallel() ).subscribe( responseEntity -> {
            assertThat( responseEntity.getStatusCode().is2xxSuccessful() );

            countDownLatch.countDown();
        } );

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );

    }

    @Test
    void testSaveBeerBadRequest() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final BeerDto beerDto = BeerDto.builder()
                .price( new BigDecimal( "8.99" ) ).build();

        final Mono<ResponseEntity<Void>> beerResponseMono = this.webClient.post().uri( "/api/v1/beer" )
                .accept( MediaType.APPLICATION_JSON ).body( BodyInserters.fromValue( beerDto ) )
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn( Schedulers.parallel() ).doOnError( responseEntity -> {
            countDownLatch.countDown();
        } ).subscribe( responseEntity -> {

        } );

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );

    }

    @Test
    void testUpdateBeer() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 3 );

        this.webClient.get().uri( "/api/v1/beer" )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve()
                .bodyToMono( BeerPagedList.class )
                .publishOn( Schedulers.single() )
                .subscribe( pagedList -> {
                    countDownLatch.countDown();

                    //get existing beer
                    final BeerDto beerDto = pagedList.getContent().get( 0 );

                    final BeerDto updatePayload = BeerDto.builder().beerName( "JTsUpdate" )
                            .beerStyle( beerDto.getBeerStyle() )
                            .upc( beerDto.getUpc() )
                            .price( beerDto.getPrice() )
                            .build();

                    //update existing beer
                    this.webClient.put().uri( "/api/v1/beer/" + beerDto.getId() )
                            .contentType( MediaType.APPLICATION_JSON )
                            .body( BodyInserters.fromValue( updatePayload ) )
                            .retrieve().toBodilessEntity()
                            .flatMap( responseEntity -> {
                                //get and verify update
                                countDownLatch.countDown();
                                return this.webClient.get().uri( "/api/v1/beer/" + beerDto.getId() )
                                        .accept( MediaType.APPLICATION_JSON )
                                        .retrieve().bodyToMono( BeerDto.class );
                            } ).subscribe( savedDto -> {
                        assertThat( savedDto.getBeerName() ).isEqualTo( "JTsUpdate" );
                        countDownLatch.countDown();
                    } );
                } );

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void testUpdateBeerNotFound() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 2 );

        final BeerDto updatePayload = BeerDto.builder().beerName( "JTsUpdate" )
                .beerStyle( "PALE ALE" )
                .upc( "12345555" )
                .price( new BigDecimal( "9.99" ) )
                .build();

        //update existing beer
        this.webClient.put().uri( "/api/v1/beer/" + 200 )
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue( updatePayload ) )
                .retrieve().toBodilessEntity()
                .subscribe( responseEntity -> {
                }, throwable -> {
                    if ( throwable.getClass().getName().equals( "org.springframework.web.reactive.function.client.WebClientResponseException$NotFound" ) ) {
                        final WebClientResponseException exception = ( WebClientResponseException ) throwable;

                        if ( exception.getStatusCode().equals( HttpStatus.NOT_FOUND ) ) {
                            countDownLatch.countDown();
                        }
                    }
                } );

        countDownLatch.countDown();

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void testDeleteBeer() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 3 );

        this.webClient.get().uri( "/api/v1/beer" )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve()
                .bodyToMono( BeerPagedList.class )
                .publishOn( Schedulers.single() )
                .subscribe( pagedList -> {
                    countDownLatch.countDown();

                    final BeerDto beerDto = pagedList.getContent().get( 0 );

                    this.webClient.delete().uri( "/api/v1/beer/" + beerDto.getId() )
                            .retrieve().toBodilessEntity()
                            .flatMap( responseEntity -> {
                                countDownLatch.countDown();

                                return this.webClient.get().uri( "/api/v1/beer/" + beerDto.getId() )
                                        .accept( MediaType.APPLICATION_JSON )
                                        .retrieve().bodyToMono( BeerDto.class );
                            } ).subscribe( savedDto -> {

                    }, throwable -> {
                        countDownLatch.countDown();
                    } );
                } );

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

}
