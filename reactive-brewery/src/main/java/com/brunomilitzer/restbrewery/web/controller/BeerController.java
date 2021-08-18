package com.brunomilitzer.restbrewery.web.controller;

import com.brunomilitzer.restbrewery.services.BeerService;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import com.brunomilitzer.restbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jt on 2019-04-20.
 */
@RequiredArgsConstructor
@RequestMapping( "/api/v1/" )
@RestController
public class BeerController {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private final BeerService beerService;

    @GetMapping( produces = { "application/json" }, path = "beer" )
    public ResponseEntity<Mono<BeerPagedList>> listBeers( @RequestParam( value = "pageNumber", required = false ) Integer pageNumber,
                                                          @RequestParam( value = "pageSize", required = false ) Integer pageSize,
                                                          @RequestParam( value = "beerName", required = false ) final String beerName,
                                                          @RequestParam( value = "beerStyle", required = false ) final BeerStyleEnum beerStyle,
                                                          @RequestParam( value = "showInventoryOnHand", required = false ) Boolean showInventoryOnHand ) {

        if ( showInventoryOnHand == null ) {
            showInventoryOnHand = false;
        }

        if ( pageNumber == null || pageNumber < 0 ) {
            pageNumber = DEFAULT_PAGE_NUMBER;
        }

        if ( pageSize == null || pageSize < 1 ) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        return ResponseEntity.ok( this.beerService.listBeers( beerName, beerStyle, PageRequest.of( pageNumber, pageSize ), showInventoryOnHand ) );
    }

    @GetMapping( "beer/{beerId}" )

    public ResponseEntity<Mono<BeerDto>> getBeerById( @PathVariable( "beerId" ) final Integer beerId,
                                                      @RequestParam( value = "showInventoryOnHand", required = false ) Boolean showInventoryOnHand ) {

        if ( showInventoryOnHand == null ) {
            showInventoryOnHand = false;
        }

        return ResponseEntity.ok( this.beerService.getById( beerId, showInventoryOnHand ).defaultIfEmpty( BeerDto.builder().build() )
                .doOnNext( beerDto -> {
                    if ( beerDto.getId() == null ) {
                        throw new NotFoundException();
                    }
                } ) );
    }

    @GetMapping( "beerUpc/{upc}" )
    public ResponseEntity<Mono<BeerDto>> getBeerByUpc( @PathVariable( "upc" ) final String upc ) {

        return ResponseEntity.ok( this.beerService.getByUpc( upc ) );
    }

    @PostMapping( path = "beer" )
    public ResponseEntity<Void> saveNewBeer( @RequestBody @Validated final BeerDto beerDto ) {

        final AtomicInteger beerId = new AtomicInteger();

        this.beerService.saveNewBeer( beerDto ).subscribe( savedBeerDto -> {
            beerId.set( savedBeerDto.getId() );
        } );

        return ResponseEntity
                .created( UriComponentsBuilder
                        .fromHttpUrl( "http://api.springframework.guru/api/v1/beer/" + beerId.get() )
                        .build().toUri() )
                .build();
    }

    @PutMapping( "beer/{beerId}" )
    public ResponseEntity<Void> updateBeerById( @PathVariable( "beerId" ) final Integer beerId, @RequestBody @Validated final BeerDto beerDto ) {

        final AtomicBoolean atomicBoolean = new AtomicBoolean( false );

        this.beerService.updateBeer( beerId, beerDto ).subscribe( savedDto -> {
            if ( savedDto.getId() != null ) {
                atomicBoolean.set( true );
            }
        } );

        if ( atomicBoolean.get() ) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping( "beer/{beerId}" )
    public ResponseEntity<Void> deleteBeerById( @PathVariable( "beerId" ) final Integer beerId ) {

        this.beerService.deleteBeerById( beerId );

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler
    ResponseEntity<Void> handleNotFound( final NotFoundException exception ) {

        return ResponseEntity.notFound().build();
    }

}
