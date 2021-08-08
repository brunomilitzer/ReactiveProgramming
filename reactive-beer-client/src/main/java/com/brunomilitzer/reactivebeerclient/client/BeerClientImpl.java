package com.brunomilitzer.reactivebeerclient.client;

import com.brunomilitzer.reactivebeerclient.config.WebClientProperties;
import com.brunomilitzer.reactivebeerclient.model.BeerDto;
import com.brunomilitzer.reactivebeerclient.model.BeerPagedList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BeerClientImpl implements BeerClient {

    private final WebClient webClient;

    @Override
    public Mono<BeerDto> getBeerById( final UUID id, final Boolean showInventoryOnHand ) {

        return this.webClient.get().uri( uriBuilder -> uriBuilder.path( WebClientProperties.BEER_V1_PATH + "/" + id.toString() )
                .queryParamIfPresent( "showInventoryOnHand", Optional.ofNullable( showInventoryOnHand ) )
                .build() ).retrieve().bodyToMono( BeerDto.class );
    }

    @Override
    public Mono<BeerPagedList> listBeers( final Integer pageNumber, final Integer pageSize, final String beerName, final String beerStyle, final Boolean showInventoryOnHand ) {

        return this.webClient.get().uri( uriBuilder -> uriBuilder.path( WebClientProperties.BEER_V1_PATH )
                .queryParamIfPresent( "pageNumber", Optional.ofNullable( pageNumber ) )
                .queryParamIfPresent( "pageSize", Optional.ofNullable( pageSize ) )
                .queryParamIfPresent( "beerName", Optional.ofNullable( beerName ) )
                .queryParamIfPresent( "beerStyle", Optional.ofNullable( beerStyle ) )
                .queryParamIfPresent( "showInventoryOnHand", Optional.ofNullable( showInventoryOnHand ) )
                .build() ).retrieve().bodyToMono( BeerPagedList.class );
    }

    @Override
    public Mono<ResponseEntity> createBeer( final BeerDto beerDto ) {

        return null;
    }

    @Override
    public Mono<ResponseEntity> updateBeer( final BeerDto beerDto ) {

        return null;
    }

    @Override
    public Mono<ResponseEntity> deleteBeerById( final UUID id ) {

        return null;
    }

    @Override
    public Mono<BeerDto> getBeerByUPC( final String upc ) {

        return this.webClient.get().uri( uriBuilder -> uriBuilder.path( WebClientProperties.BEER_V1_UPC_PATH + "/" + upc )
                .build() ).retrieve().bodyToMono( BeerDto.class );
    }

}
