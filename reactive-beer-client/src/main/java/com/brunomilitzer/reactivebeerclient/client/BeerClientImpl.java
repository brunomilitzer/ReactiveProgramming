package com.brunomilitzer.reactivebeerclient.client;

import com.brunomilitzer.reactivebeerclient.model.BeerDto;
import com.brunomilitzer.reactivebeerclient.model.BeerPagedList;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class BeerClientImpl implements BeerClient {

    @Override
    public Mono<BeerDto> getBeerById( final UUID id, final Boolean showInventoryOnHand ) {

        return null;
    }

    @Override
    public Mono<BeerPagedList> listBeers( final Integer pageNumber, final Integer pageSize, final String beerName, final String beerStyle, final Boolean showInventoryOnHand ) {

        return null;
    }

    @Override
    public Mono<ResponseEntity> createBeer( final BeerDto beerDto ) {

        return null;
    }

    @Override
    public Mono<ResponseEntity> updateBeer( final BeerDto beerDto ) {

        return null;
    }

    @Override
    public Mono<ResponseEntity> deleteBeerById( final UUID id ) {

        return null;
    }

    @Override
    public Mono<BeerDto> getBeerByUPC( final String upc ) {

        return null;
    }

}
