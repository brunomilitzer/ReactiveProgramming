package com.brunomilitzer.reactivebeerclient.client;

import com.brunomilitzer.reactivebeerclient.config.WebClientConfig;
import com.brunomilitzer.reactivebeerclient.model.BeerDto;
import com.brunomilitzer.reactivebeerclient.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    }

    @Test
    void updateBeer() {

    }

    @Test
    void deleteBeerById() {

    }

}