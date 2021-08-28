package com.brunomilitzer.restbrewery.web.functional;

import com.brunomilitzer.restbrewery.services.BeerService;
import com.brunomilitzer.restbrewery.web.controller.NotFoundException;
import com.brunomilitzer.restbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandlerV2 {

    private final BeerService beerService;
    private final Validator validator;

    public Mono<ServerResponse> getBeerById( final ServerRequest request ) {

        final Integer beerId = Integer.valueOf( request.pathVariable( "beerId" ) );
        final Boolean showInventory = Boolean.valueOf( request.queryParam( "showInventory" ).orElse( "false" ) );

        return this.beerService.getById( beerId, showInventory ).flatMap( beerDto -> {
            return ServerResponse.ok().bodyValue( beerDto );
        } ).switchIfEmpty( ServerResponse.notFound().build() );
    }

    public Mono<ServerResponse> getBeerByUpc( final ServerRequest request ) {

        final String upc = request.pathVariable( "upc" );

        return this.beerService.getByUpc( upc ).flatMap( beerDto -> {
            return ServerResponse.ok().bodyValue( beerDto );
        } ).switchIfEmpty( ServerResponse.notFound().build() );
    }

    public Mono<ServerResponse> saveNewBeer( final ServerRequest request ) {

        final Mono<BeerDto> beerDtoMono = request.bodyToMono( BeerDto.class ).doOnNext( this::validate );

        return this.beerService.saveNewBeerMono( beerDtoMono ).flatMap( beerDto -> {
            return ServerResponse.ok()
                    .header( "location", BeerRouterConfig.BEER_V2_URL + "/" + beerDto.getId() )
                    .build();
        } );
    }

    public Mono<ServerResponse> updateBeer( final ServerRequest request ) {

        return request.bodyToMono( BeerDto.class ).doOnNext( this::validate )
                .flatMap( beerDto -> {
                    return this.beerService.updateBeer( Integer.valueOf( request.pathVariable( "beerId" ) ), beerDto );
                } ).flatMap( savedBerDto -> {

                    if ( savedBerDto.getId() != null ) {
                        log.debug( "Saved Beer Id: {}", savedBerDto.getId() );
                        return ServerResponse.noContent().build();
                    }

                    log.debug( "Beer Id {} Not Found", request.pathVariable( "beerId" ) );
                    return ServerResponse.notFound().build();
                } );
    }

    public Mono<ServerResponse> deleteBeer( final ServerRequest request ) {

        return this.beerService.reactiveDeleteById( Integer.valueOf( request.pathVariable( "beerId" ) ) )
                .flatMap( voidMono -> {
                    return ServerResponse.ok().build();
                } ).onErrorResume( e -> e instanceof NotFoundException,
                        e -> ServerResponse.notFound().build() );
    }

    private void validate( final BeerDto beerDto ) {

        final Errors errors = new BeanPropertyBindingResult( beerDto, "beerDto" );
        this.validator.validate( beerDto, errors );

        if ( errors.hasErrors() ) {
            throw new ServerWebInputException( errors.toString() );
        }
    }

}
