package com.brunomilitzer.restbrewery.services;

import com.brunomilitzer.restbrewery.web.model.BeerDto;
import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import com.brunomilitzer.restbrewery.web.model.BeerStyleEnum;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

/**
 * Created by jt on 2019-04-20.
 */
public interface BeerService {

    BeerPagedList listBeers( String beerName, BeerStyleEnum beerStyle, PageRequest pageRequest, Boolean showInventoryOnHand );

    BeerDto getById( UUID beerId, Boolean showInventoryOnHand );

    BeerDto saveNewBeer( BeerDto beerDto );

    BeerDto updateBeer( UUID beerId, BeerDto beerDto );

    BeerDto getByUpc( String upc );

    void deleteBeerById( UUID beerId );

}
