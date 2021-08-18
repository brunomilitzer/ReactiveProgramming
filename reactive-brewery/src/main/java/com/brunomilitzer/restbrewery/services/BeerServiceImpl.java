package com.brunomilitzer.restbrewery.services;

import com.brunomilitzer.restbrewery.domain.Beer;
import com.brunomilitzer.restbrewery.repositories.BeerRepository;
import com.brunomilitzer.restbrewery.web.mappers.BeerMapper;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import com.brunomilitzer.restbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.empty;
import static org.springframework.data.relational.core.query.Query.query;

/**
 * Created by jt on 2019-04-20.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    private final R2dbcEntityTemplate template;

    @Cacheable( cacheNames = "beerListCache", condition = "#showInventoryOnHand == false " )
    @Override
    public Mono<BeerPagedList> listBeers( final String beerName, final BeerStyleEnum beerStyle, final PageRequest pageRequest, final Boolean showInventoryOnHand ) {

        Query query = empty();

        if ( !StringUtils.isEmpty( beerName ) && !StringUtils.isEmpty( beerStyle ) ) {
            //search both
            query = query( where( "beerName" ).is( beerName ).and( "beerStyle" ).is( beerStyle ) );
        } else if ( !StringUtils.isEmpty( beerName ) && StringUtils.isEmpty( beerStyle ) ) {
            //search beer_service name
            query = query( where( "beerName" ).is( beerName ) );
        } else if ( StringUtils.isEmpty( beerName ) && !StringUtils.isEmpty( beerStyle ) ) {
            //search beer_service style
            query = query( where( "beerStyle" ).is( beerStyle ) );
        }

        return this.template.select( Beer.class )
                .matching( query.with( pageRequest ) )
                .all()
                .map( this.beerMapper::beerToBeerDto )
                .collect( Collectors.toList() )
                .map( beers -> new BeerPagedList( beers, PageRequest.of( pageRequest.getPageNumber(), pageRequest.getPageSize() ), beers.size() ) );
    }

    @Cacheable( cacheNames = "beerCache", key = "#beerId", condition = "#showInventoryOnHand == false " )
    @Override
    public Mono<BeerDto> getById( final Integer beerId, final Boolean showInventoryOnHand ) {

        if ( showInventoryOnHand ) {
            return this.beerRepository.findById( beerId ).map( this.beerMapper::beerToBeerDtoWithInventory );
        } else {
            return this.beerRepository.findById( beerId ).map( this.beerMapper::beerToBeerDto );
        }
    }

    @Override
    public Mono<BeerDto> saveNewBeer( final BeerDto beerDto ) {

        return this.beerRepository.save( this.beerMapper.beerDtoToBeer( beerDto ) ).map( this.beerMapper::beerToBeerDto );
    }

    @Override
    public Mono<BeerDto> updateBeer( final Integer beerId, final BeerDto beerDto ) {

        return this.beerRepository.findById( beerId )
                .defaultIfEmpty( Beer.builder().build() )
                .map( beer -> {
                    beer.setBeerName( beerDto.getBeerName() );
                    beer.setBeerStyle( BeerStyleEnum.valueOf( beerDto.getBeerStyle() ) );
                    beer.setPrice( beerDto.getPrice() );
                    beer.setUpc( beerDto.getUpc() );

                    return beer;
                } ).flatMap( updateBeer -> {
                    if ( updateBeer.getId() != null ) {
                        return this.beerRepository.save( updateBeer );
                    }

                    return Mono.just( updateBeer );
                } ).map( this.beerMapper::beerToBeerDto );
    }

    @Cacheable( cacheNames = "beerUpcCache" )
    @Override
    public Mono<BeerDto> getByUpc( final String upc ) {

        return this.beerRepository.findByUpc( upc ).map( this.beerMapper::beerToBeerDto );
    }

    @Override
    public void deleteBeerById( final Integer beerId ) {

        this.beerRepository.deleteById( beerId ).subscribe();
    }

}
