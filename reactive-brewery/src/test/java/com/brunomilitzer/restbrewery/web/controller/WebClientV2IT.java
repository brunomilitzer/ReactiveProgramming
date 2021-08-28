package com.brunomilitzer.restbrewery.web.controller;

import com.brunomilitzer.restbrewery.bootstrap.BeerLoader;
import com.brunomilitzer.restbrewery.web.functional.BeerRouterConfig;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by jt on 3/7/21.
 */
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT )
public class WebClientV2IT {

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

        final Mono<BeerDto> beerDtoMono = this.webClient.get().uri( BeerRouterConfig.BEER_V2_URL + "/" + 1 )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerDto.class );

        beerDtoMono.subscribe( beer -> {
            assertThat( beer ).isNotNull();
            assertThat( beer.getBeerName() ).isNotNull();

            countDownLatch.countDown();
        } );

        countDownLatch.await( 2000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void getBeerByIdNotFound() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final Mono<BeerDto> beerDtoMono = this.webClient.get().uri( BeerRouterConfig.BEER_V2_URL + "/" + 1333 )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerDto.class );

        beerDtoMono.subscribe( beer -> {

        }, throwable -> {
            countDownLatch.countDown();
        } );

        countDownLatch.await( 2000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void getBeerByUpc() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final Mono<BeerDto> beerDtoMono = this.webClient.get().uri( BeerRouterConfig.BEER_V2_URL_UPC + "/" + BeerLoader.BEER_2_UPC )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerDto.class );

        beerDtoMono.subscribe( beer -> {
            assertThat( beer ).isNotNull();
            assertThat( beer.getUpc() ).isNotNull();

            countDownLatch.countDown();
        } );

        countDownLatch.await( 2000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void getBeerByUpcNotFound() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final Mono<BeerDto> beerDtoMono = this.webClient.get().uri( BeerRouterConfig.BEER_V2_URL_UPC + "/545454654654" )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerDto.class );

        beerDtoMono.subscribe( beer -> {

        }, throwable -> {
            countDownLatch.countDown();
        } );

        countDownLatch.await( 2000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void saveBeer() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final BeerDto beerDto = BeerDto.builder().beerName( "Eisenbhan" )
                .upc( "1234555" ).beerStyle( "LAGER" )
                .price( new BigDecimal( "8.99" ) ).build();

        final Mono<ResponseEntity<Void>> beerResponseMono = this.webClient.post().uri( BeerRouterConfig.BEER_V2_URL )
                .accept( MediaType.APPLICATION_JSON ).body( BodyInserters.fromValue( beerDto ) )
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn( Schedulers.parallel() ).subscribe( responseEntity -> {
            assertThat( responseEntity.getStatusCode().is2xxSuccessful() );

            countDownLatch.countDown();
        } );

        countDownLatch.await( 2000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );

    }

    @Test
    void saveBeerBadRequest() throws InterruptedException {

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        final BeerDto beerDto = BeerDto.builder().build();

        final Mono<ResponseEntity<Void>> beerResponseMono = this.webClient.post().uri( BeerRouterConfig.BEER_V2_URL )
                .accept( MediaType.APPLICATION_JSON ).body( BodyInserters.fromValue( beerDto ) )
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn( Schedulers.parallel() ).subscribe( responseEntity -> {

        }, throwable -> {
            if ( throwable.getClass().getName().equals( "org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest" ) ) {
                final WebClientResponseException ex = ( WebClientResponseException ) throwable;

                if ( ex.getStatusCode().equals( HttpStatus.BAD_REQUEST ) ) {
                    countDownLatch.countDown();
                }
            }
        } );

        countDownLatch.await( 2000, TimeUnit.MILLISECONDS );

        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void updateBeer() throws InterruptedException {

        final String newBeerName = "Pilser Urquel";
        final int beerId = 1;

        final CountDownLatch countDownLatch = new CountDownLatch( 2 );

        //update existing beer
        this.webClient.put().uri( BeerRouterConfig.BEER_V2_URL + "/" + beerId )
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue( BeerDto.builder()
                        .beerName( newBeerName )
                        .upc( "123455" )
                        .beerStyle( "PALE_ALE" )
                        .price( new BigDecimal( "8.99" ) )
                        .build() ) )
                .retrieve().toBodilessEntity()
                .subscribe( responseEntity -> {
                    assertThat( responseEntity.getStatusCode().is2xxSuccessful() );
                    countDownLatch.countDown();
                } );

        // wait for update thread to complete
        countDownLatch.await( 500, TimeUnit.MILLISECONDS );

        this.webClient.get().uri( BeerRouterConfig.BEER_V2_URL + "/" + beerId )
                .accept( MediaType.APPLICATION_JSON )
                .retrieve().bodyToMono( BeerDto.class )
                .subscribe( beer -> {
                    assertThat( beer ).isNotNull();
                    assertThat( beer.getBeerName() ).isNotNull();
                    assertThat( beer.getBeerName() ).isEqualTo( newBeerName );
                    countDownLatch.countDown();

                } );

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void updateBeerNotFound() throws InterruptedException {

        final String newBeerName = "Pilser Urquel";
        final int beerId = 999;

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        //update existing beer
        this.webClient.put().uri( BeerRouterConfig.BEER_V2_URL + "/" + beerId )
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue( BeerDto.builder()
                        .beerName( newBeerName )
                        .upc( "123455" )
                        .beerStyle( "PALE_ALE" )
                        .price( new BigDecimal( "8.99" ) )
                        .build() ) )
                .retrieve().toBodilessEntity()
                .subscribe( responseEntity -> {
                    assertThat( responseEntity.getStatusCode().is2xxSuccessful() );
                }, throwable -> {
                    countDownLatch.countDown();
                } );

        countDownLatch.await( 1000, TimeUnit.MILLISECONDS );
        assertThat( countDownLatch.getCount() ).isEqualTo( 0 );
    }

    @Test
    void deleteBeer() {

        final int beerId = 3;

        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        this.webClient.delete().uri( BeerRouterConfig.BEER_V2_URL + "/" + beerId )
                .retrieve().toBodilessEntity()
                .flatMap( responseEntity -> {
                    countDownLatch.countDown();

                    return this.webClient.get().uri( BeerRouterConfig.BEER_V2_URL + "/" + beerId )
                            .accept( MediaType.APPLICATION_JSON )
                            .retrieve().bodyToMono( BeerDto.class );
                } ).subscribe( savedDto -> {

        }, throwable -> {
            countDownLatch.countDown();
        } );
    }

    @Test
    void deleteBeerNotFound() {

        final int beerId = 4;

        this.webClient.delete().uri( BeerRouterConfig.BEER_V2_URL + "/" + beerId )
                .retrieve().toBodilessEntity().block();

        assertThrows( WebClientResponseException.NotFound.class, () -> {
            this.webClient.delete().uri( BeerRouterConfig.BEER_V2_URL + "/" + beerId )
                    .retrieve().toBodilessEntity().block();
        } );

    }

}
