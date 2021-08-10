package com.brunomilitzer.restbrewery.web.mappers;

import com.brunomilitzer.restbrewery.domain.Beer;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
import org.mapstruct.Mapper;

/**
 * Created by jt on 2019-05-25.
 */
@Mapper( uses = { DateMapper.class } )
public interface BeerMapper {

    BeerDto beerToBeerDto( Beer beer );

    BeerDto beerToBeerDtoWithInventory( Beer beer );

    Beer beerDtoToBeer( BeerDto dto );

}
