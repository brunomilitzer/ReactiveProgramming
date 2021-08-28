package com.brunomilitzer.restbrewery.services;

import com.brunomilitzer.restbrewery.web.model.BeerDto;
import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import com.brunomilitzer.restbrewery.web.model.BeerStyleEnum;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;

/**
 * Created by jt on 2019-04-20.
 */
public interface BeerService {

    Mono<BeerPagedList> listBeers( String beerName, BeerStyleEnum beerStyle, PageRequest pageRequest, Boolean showInventoryOnHand );

    Mono<BeerDto> getById( Integer beerId, Boolean showInventoryOnHand );

    Mono<BeerDto> saveNewBeer( BeerDto beerDto );

    Mono<BeerDto> saveNewBeerMono( Mono<BeerDto> beerDto );

    Mono<BeerDto> updateBeer( Integer beerId, BeerDto beerDto );

    Mono<BeerDto> getByUpc( String upc );

    void deleteBeerById( Integer beerId );

    Mono<Void> reactiveDeleteById( Integer beerId );

}
