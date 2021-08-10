package com.brunomilitzer.restbrewery.web.controller;

import com.brunomilitzer.restbrewery.services.BeerService;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
import com.brunomilitzer.restbrewery.web.model.BeerPagedList;
import com.brunomilitzer.restbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

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
    public ResponseEntity<BeerPagedList> listBeers( @RequestParam( value = "pageNumber", required = false ) Integer pageNumber,
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

        final BeerPagedList beerList = this.beerService.listBeers( beerName, beerStyle, PageRequest.of( pageNumber, pageSize ), showInventoryOnHand );

        return new ResponseEntity<>( beerList, HttpStatus.OK );
    }

    @GetMapping( "beer/{beerId}" )
    public ResponseEntity<BeerDto> getBeerById( @PathVariable( "beerId" ) final UUID beerId,
                                                @RequestParam( value = "showInventoryOnHand", required = false ) Boolean showInventoryOnHand ) {

        if ( showInventoryOnHand == null ) {
            showInventoryOnHand = false;
        }

        return new ResponseEntity<>( this.beerService.getById( beerId, showInventoryOnHand ), HttpStatus.OK );
    }

    @GetMapping( "beerUpc/{upc}" )
    public ResponseEntity<BeerDto> getBeerByUpc( @PathVariable( "upc" ) final String upc ) {

        return new ResponseEntity<>( this.beerService.getByUpc( upc ), HttpStatus.OK );
    }

    @PostMapping( path = "beer" )
    public ResponseEntity saveNewBeer( @RequestBody @Validated final BeerDto beerDto ) {

        final BeerDto savedBeer = this.beerService.saveNewBeer( beerDto );

        return ResponseEntity
                .created( UriComponentsBuilder
                        .fromHttpUrl( "http://api.springframework.guru/api/v1/beer/" + savedBeer.getId().toString() )
                        .build().toUri() )
                .build();
    }

    @PutMapping( "beer/{beerId}" )
    public ResponseEntity updateBeerById( @PathVariable( "beerId" ) final UUID beerId, @RequestBody @Validated final BeerDto beerDto ) {

        return new ResponseEntity<>( this.beerService.updateBeer( beerId, beerDto ), HttpStatus.NO_CONTENT );
    }

    @DeleteMapping( "beer/{beerId}" )
    public ResponseEntity<Void> deleteBeerById( @PathVariable( "beerId" ) final UUID beerId ) {

        this.beerService.deleteBeerById( beerId );

        return new ResponseEntity<>( HttpStatus.NO_CONTENT );
    }

}
