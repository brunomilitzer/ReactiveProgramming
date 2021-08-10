package com.brunomilitzer.restbrewery.services;

import com.brunomilitzer.restbrewery.web.model.CustomerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by jt on 2019-04-21.
 */
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    @Override
    public CustomerDto getCustomerById( final UUID customerId ) {

        return CustomerDto.builder()
                .id( UUID.randomUUID() )
                .name( "Joe Buck" )
                .build();
    }

    @Override
    public CustomerDto saveNewCustomer( final CustomerDto customerDto ) {

        return CustomerDto.builder()
                .id( UUID.randomUUID() )
                .build();
    }

    @Override
    public void updateCustomer( final UUID customerId, final CustomerDto customerDto ) {
        //todo impl
        log.debug( "Updating...." );
    }

    @Override
    public void deleteById( final UUID customerId ) {

        log.debug( "Deleting.... " );
    }

}
