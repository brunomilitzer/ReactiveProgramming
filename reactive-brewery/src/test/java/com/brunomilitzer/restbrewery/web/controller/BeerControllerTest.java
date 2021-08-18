package com.brunomilitzer.restbrewery.web.controller;

import com.brunomilitzer.restbrewery.bootstrap.BeerLoader;
import com.brunomilitzer.restbrewery.services.BeerService;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest( BeerController.class )
class BeerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BeerService beerService;

    private BeerDto validBeer;

    @BeforeEach
    void setUp() {

        this.validBeer = BeerDto.builder().beerName( "Test beer" )
                .beerStyle( "PALE_ALE" )
                .upc( BeerLoader.BEER_1_UPC ).build();
    }

    @Test
    void getBeerByUpc() {

        given( this.beerService.getByUpc( any() ) )
                .willReturn( Mono.just( this.validBeer ) );

        this.webTestClient.get().uri( "/api/v1/beerUpc/" + this.validBeer.getUpc() )
                .accept( MediaType.APPLICATION_JSON )
                .exchange().expectStatus().isOk()
                .expectBody( BeerDto.class )
                .value( BeerDto::getBeerName, equalTo( this.validBeer.getBeerName() ) );

    }

    @Test
    public void listBeers() {

        final List<BeerDto> beerList = Arrays.asList( this.validBeer );

        final BeerPagedList beerPagedList = new BeerPagedList( beerList, PageRequest.of( 1, 1 ), beerList.size() );
        given( this.beerService.listBeers( any(), any(), any(), any() ) ).willReturn( Mono.just( beerPagedList ) );

        this.webTestClient.get().uri( "/api/v1/beer" ).accept( MediaType.APPLICATION_JSON )
                .exchange().expectStatus().isOk()
                .expectBody( BeerPagedList.class );
    }

    @Test
    void getBeerById() {

        final Integer beerId = 1;
        given( this.beerService.getById( any(), any() ) )
                .willReturn( Mono.just( this.validBeer ) );

        this.webTestClient.get().uri( "/api/v1/beer/" + beerId )
                .accept( MediaType.APPLICATION_JSON )
                .exchange().expectStatus().isOk()
                .expectBody( BeerDto.class )
                .value( BeerDto::getBeerName, equalTo( this.validBeer.getBeerName() ) );

    }

}