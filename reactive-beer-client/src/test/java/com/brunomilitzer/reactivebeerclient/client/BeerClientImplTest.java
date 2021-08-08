package com.brunomilitzer.reactivebeerclient.client;

import com.brunomilitzer.reactivebeerclient.config.WebClientConfig;
import com.brunomilitzer.reactivebeerclient.model.BeerDto;
import com.brunomilitzer.reactivebeerclient.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeerClientImplTest {

    BeerClientImpl beerClient;

    @BeforeEach
    void setUp() {

        this.beerClient = new BeerClientImpl( new WebClientConfig().webClient() );
    }

    @Test
    void listBeers() {

        final Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers( null, null, null, null, null );

        final BeerPagedList pagedList = beerPagedListMono.block();

        assertThat( pagedList ).isNotNull();
        assertThat( pagedList.getContent().size() ).isGreaterThan( 0 );
    }

    @Test
    void listBeersPageSize10() {

        final Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers( 1, 10, null, null, null );

        final BeerPagedList pagedList = beerPagedListMono.block();

        assertThat( pagedList ).isNotNull();
        assertThat( pagedList.getContent().size() ).isEqualTo( 10 );
    }

    @Test
    void listBeersNoRecords() {

        final Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers( 10, 20, null, null, null );

        final BeerPagedList pagedList = beerPagedListMono.block();

        assertThat( pagedList ).isNotNull();
        assertThat( pagedList.getContent().size() ).isEqualTo( 0 );
    }

    @Disabled( "API returning inventory when should not be" )
    @Test
    void getBeerById() {

        final Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers( null, null, null, null, null );

        final BeerPagedList pagedList = beerPagedListMono.block();

        assertThat( pagedList ).isNotNull();
        final UUID beerId = pagedList.getContent().get( 0 ).getId();

        final Mono<BeerDto> beerDtoMono = this.beerClient.getBeerById( beerId, false );
        final BeerDto beerDto = beerDtoMono.block();

        assertThat( beerDto ).isNotNull();
        assertThat( beerDto.getId() ).isEqualTo( beerId );
        assertThat( beerDto.getQuantityOnHand() ).isNull();
    }

    @Test
    void functionalTestGetBeerById() throws InterruptedException {

        final AtomicReference<String> beerName = new AtomicReference<>();
        final CountDownLatch countDownLatch = new CountDownLatch( 1 );

        this.beerClient.listBeers( null, null, null, null, null )
                .map( beerPagedList -> beerPagedList.getContent().get( 0 ).getId() )
                .map( beerId -> this.beerClient.getBeerById( beerId, false ) )
                .flatMap( mono -> mono )
                .subscribe( beerDto -> {
                    System.out.println( beerDto.getBeerName() );
                    beerName.set( beerDto.getBeerName() );
                    assertThat( beerDto.getBeerName() ).isEqualTo( "Blessed" );
                    countDownLatch.countDown();
                } );
        
        countDownLatch.await();
        assertThat( beerName.get() ).isEqualTo( "Blessed" );
    }

    @Test
    void getBeerByIdShowInventoryTrue() {

        final Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers( null, null, null, null, null );

        final BeerPagedList pagedList = beerPagedListMono.block();

        assertThat( pagedList ).isNotNull();
        final UUID beerId = pagedList.getContent().get( 0 ).getId();

        final Mono<BeerDto> beerDtoMono = this.beerClient.getBeerById( beerId, true );
        final BeerDto beerDto = beerDtoMono.block();

        assertThat( beerDto ).isNotNull();
        assertThat( beerDto.getId() ).isEqualTo( beerId );
        assertThat( beerDto.getQuantityOnHand() ).isNotNull();
    }

    @Test
    void getBeerByUPC() {

        final Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers( null, null, null, null, null );

        final BeerPagedList pagedList = beerPagedListMono.block();

        assertThat( pagedList ).isNotNull();
        final String upc = pagedList.getContent().get( 0 ).getUpc();

        final Mono<BeerDto> beerDtoMono = this.beerClient.getBeerByUPC( upc );
        final BeerDto beerDto = beerDtoMono.block();

        assertThat( beerDto ).isNotNull();
        assertThat( beerDto.getUpc() ).isEqualTo( upc );
    }

    @Test
    void createBeer() {

        final BeerDto beerDto = BeerDto.builder()
                .beerName( "Guiness" )
                .beerStyle( "Stout" )
                .upc( "839849384934" )
                .price( new BigDecimal( "3.99" ) )
                .build();

        final Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.createBeer( beerDto );

        final ResponseEntity<Void> responseEntity = responseEntityMono.block();
        assertThat( responseEntity ).isNotNull();
        assertThat( responseEntity.getStatusCode() ).isEqualTo( HttpStatus.CREATED );
    }

    @Test
    void updateBeer() {

        final Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers( null, null, null, null, null );

        final BeerPagedList pagedList = beerPagedListMono.block();

        assertThat( pagedList ).isNotNull();
        final BeerDto beerDto = pagedList.getContent().get( 0 );

        final BeerDto updatedBeer = BeerDto.builder().beerName( "Hop House" ).beerStyle( "Lager" ).price( beerDto.getPrice() ).upc( beerDto.getUpc() ).build();

        final Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.updateBeer( beerDto.getId(), updatedBeer );
        final ResponseEntity<Void> responseEntity = responseEntityMono.block();
        assertThat( responseEntity ).isNotNull();
        assertThat( responseEntity.getStatusCode() ).isEqualTo( HttpStatus.NO_CONTENT );
    }

    @Test
    void deleteBeerById() {

        final Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers( null, null, null, null, null );

        final BeerPagedList pagedList = beerPagedListMono.block();

        assertThat( pagedList ).isNotNull();
        final BeerDto beerDto = pagedList.getContent().get( 0 );

        final Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.deleteBeerById( beerDto.getId() );
        final ResponseEntity<Void> responseEntity = responseEntityMono.block();
        assertThat( responseEntity ).isNotNull();
        assertThat( responseEntity.getStatusCode() ).isEqualTo( HttpStatus.NO_CONTENT );
    }

    @Test
    void testDeleteBeerHandleException() {

        final Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.deleteBeerById( UUID.randomUUID() );
        final ResponseEntity<Void> responseEntity = responseEntityMono.onErrorResume( throwable -> {
            if ( throwable instanceof WebClientResponseException ) {
                final WebClientResponseException exception = ( WebClientResponseException ) throwable;
                return Mono.just( ResponseEntity.status( exception.getStatusCode() ).build() );
            } else {
                throw new RuntimeException( throwable );
            }
        } ).block();

        assertThat( responseEntity ).isNotNull();
        assertThat( responseEntity.getStatusCode() ).isEqualTo( HttpStatus.NOT_FOUND );
    }

    @Test
    void deleteBeerNotFound() {

        final Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.deleteBeerById( UUID.randomUUID() );
        assertThrows( WebClientResponseException.class, () -> {
            final ResponseEntity<Void> responseEntity = responseEntityMono.block();
            assertThat( responseEntity ).isNotNull();
            assertThat( responseEntity.getStatusCode() ).isEqualTo( HttpStatus.NOT_FOUND );
        } );
    }

}