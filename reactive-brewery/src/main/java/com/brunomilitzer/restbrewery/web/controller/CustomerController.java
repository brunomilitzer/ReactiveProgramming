package com.brunomilitzer.restbrewery.web.controller;

import com.brunomilitzer.restbrewery.services.CustomerService;
import com.brunomilitzer.restbrewery.web.model.CustomerDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Created by jt on 2019-04-21.
 */

@RequestMapping( "api/v1/customer" )
@RestController
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController( final CustomerService customerService ) {

        this.customerService = customerService;
    }

    @GetMapping( "/{customerId}" )
    public ResponseEntity<CustomerDto> getCustomer( @PathVariable( "customerId" ) final UUID customerId ) {

        return new ResponseEntity<>( this.customerService.getCustomerById( customerId ), HttpStatus.OK );
    }

    @PostMapping
    public ResponseEntity handlePost( @RequestBody @Validated final CustomerDto customerDto ) {

        final CustomerDto savedDto = this.customerService.saveNewCustomer( customerDto );

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add( "Location", "/api/v1/customer/" + savedDto.getId().toString() );

        return new ResponseEntity( httpHeaders, HttpStatus.CREATED );
    }

    @PutMapping( "/{customerId}" )
    @ResponseStatus( HttpStatus.NO_CONTENT )
    public void handleUpdate( @PathVariable( "customerId" ) final UUID customerId, @Validated @RequestBody final CustomerDto customerDto ) {

        this.customerService.updateCustomer( customerId, customerDto );
    }

    @DeleteMapping( "/{customerId}" )
    public void deleteById( @PathVariable( "customerId" ) final UUID customerId ) {

        this.customerService.deleteById( customerId );
    }

}
