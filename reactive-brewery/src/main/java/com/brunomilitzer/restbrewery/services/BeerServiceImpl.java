package com.brunomilitzer.restbrewery.services;

import com.brunomilitzer.restbrewery.domain.Beer;
import com.brunomilitzer.restbrewery.repositories.BeerRepository;
import com.brunomilitzer.restbrewery.web.controller.NotFoundException;
import com.brunomilitzer.restbrewery.web.mappers.BeerMapper;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import com.brunomilitzer.restbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by jt on 2019-04-20.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    @Cacheable( cacheNames = "beerListCache", condition = "#showInventoryOnHand == false " )
    @Override
    public BeerPagedList listBeers( final String beerName, final BeerStyleEnum beerStyle, final PageRequest pageRequest, final Boolean showInventoryOnHand ) {

        final BeerPagedList beerPagedList;
        final Page<Beer> beerPage;

        if ( !StringUtils.isEmpty( beerName ) && !StringUtils.isEmpty( beerStyle ) ) {
            //search both
            beerPage = this.beerRepository.findAllByBeerNameAndBeerStyle( beerName, beerStyle, pageRequest );
        } else if ( !StringUtils.isEmpty( beerName ) && StringUtils.isEmpty( beerStyle ) ) {
            //search beer_service name
            beerPage = this.beerRepository.findAllByBeerName( beerName, pageRequest );
        } else if ( StringUtils.isEmpty( beerName ) && !StringUtils.isEmpty( beerStyle ) ) {
            //search beer_service style
            beerPage = this.beerRepository.findAllByBeerStyle( beerStyle, pageRequest );
        } else {
            beerPage = this.beerRepository.findAll( pageRequest );
        }

        if ( showInventoryOnHand ) {
            beerPagedList = new BeerPagedList( beerPage
                    .getContent()
                    .stream()
                    .map( this.beerMapper::beerToBeerDtoWithInventory )
                    .collect( Collectors.toList() ),
                    PageRequest
                            .of( beerPage.getPageable().getPageNumber(),
                                    beerPage.getPageable().getPageSize() ),
                    beerPage.getTotalElements() );
        } else {
            beerPagedList = new BeerPagedList( beerPage
                    .getContent()
                    .stream()
                    .map( this.beerMapper::beerToBeerDto )
                    .collect( Collectors.toList() ),
                    PageRequest
                            .of( beerPage.getPageable().getPageNumber(),
                                    beerPage.getPageable().getPageSize() ),
                    beerPage.getTotalElements() );
        }

        return beerPagedList;
    }

    @Cacheable( cacheNames = "beerCache", key = "#beerId", condition = "#showInventoryOnHand == false " )
    @Override
    public BeerDto getById( final UUID beerId, final Boolean showInventoryOnHand ) {

        if ( showInventoryOnHand ) {
            return this.beerMapper.beerToBeerDtoWithInventory(
                    this.beerRepository.findById( beerId ).orElseThrow( NotFoundException::new )
            );
        } else {
            return this.beerMapper.beerToBeerDto(
                    this.beerRepository.findById( beerId ).orElseThrow( NotFoundException::new )
            );
        }
    }

    @Override
    public BeerDto saveNewBeer( final BeerDto beerDto ) {

        return this.beerMapper.beerToBeerDto( this.beerRepository.save( this.beerMapper.beerDtoToBeer( beerDto ) ) );
    }

    @Override
    public BeerDto updateBeer( final UUID beerId, final BeerDto beerDto ) {

        final Beer beer = this.beerRepository.findById( beerId ).orElseThrow( NotFoundException::new );

        beer.setBeerName( beerDto.getBeerName() );
        beer.setBeerStyle( BeerStyleEnum.PILSNER.valueOf( beerDto.getBeerStyle() ) );
        beer.setPrice( beerDto.getPrice() );
        beer.setUpc( beerDto.getUpc() );

        return this.beerMapper.beerToBeerDto( this.beerRepository.save( beer ) );
    }

    @Cacheable( cacheNames = "beerUpcCache" )
    @Override
    public BeerDto getByUpc( final String upc ) {

        return this.beerMapper.beerToBeerDto( this.beerRepository.findByUpc( upc ) );
    }

    @Override
    public void deleteBeerById( final UUID beerId ) {

        this.beerRepository.deleteById( beerId );
    }

}
