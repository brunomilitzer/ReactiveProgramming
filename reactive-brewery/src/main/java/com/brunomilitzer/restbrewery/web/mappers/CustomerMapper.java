package com.brunomilitzer.restbrewery.web.mappers;

import com.brunomilitzer.restbrewery.domain.Customer;
import com.brunomilitzer.restbrewery.web.model.CustomerDto;
import org.mapstruct.Mapper;

/**
 * Created by jt on 2019-05-25.
 */
@Mapper
public interface CustomerMapper {

    Customer customerDtoToCustomer( CustomerDto dto );

    CustomerDto customerToCustomerDto( Customer customer );

}
