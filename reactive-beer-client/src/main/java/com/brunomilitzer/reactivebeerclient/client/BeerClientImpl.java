package com.brunomilitzer.reactivebeerclient.client;

import com.brunomilitzer.reactivebeerclient.config.WebClientProperties;
import com.brunomilitzer.reactivebeerclient.model.BeerDto;
import com.brunomilitzer.reactivebeerclient.model.BeerPagedList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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

        return this.webClient.get().uri( uriBuilder -> uriBuilder.path( WebClientProperties.BEER_V1_PATH_GET_BY_ID )
                .queryParamIfPresent( "showInventoryOnHand", Optional.ofNullable( showInventoryOnHand ) )
                .build( id ) ).retrieve().bodyToMono( BeerDto.class );
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
    public Mono<ResponseEntity<Void>> createBeer( final BeerDto beerDto ) {

        return this.webClient.post().uri( uriBuilder -> uriBuilder.path( WebClientProperties.BEER_V1_PATH ).build() )
                .body( BodyInserters.fromValue( beerDto ) ).retrieve().toBodilessEntity();
    }

    @Override
    public Mono<ResponseEntity<Void>> updateBeer( final UUID beerId, final BeerDto beerDto ) {

        return this.webClient.put().uri( uriBuilder -> uriBuilder.path( WebClientProperties.BEER_V1_PATH_GET_BY_ID ).build( beerId ) )
                .body( BodyInserters.fromValue( beerDto ) ).retrieve().toBodilessEntity();
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteBeerById( final UUID id ) {

        return this.webClient.delete().uri( uriBuilder -> uriBuilder.path( WebClientProperties.BEER_V1_PATH_GET_BY_ID ).build( id ) )
                .retrieve().toBodilessEntity();
    }

    @Override
    public Mono<BeerDto> getBeerByUPC( final String upc ) {

        return this.webClient.get().uri( uriBuilder -> uriBuilder.path( WebClientProperties.BEER_V1_UPC_PATH )
                .build( upc ) ).retrieve().bodyToMono( BeerDto.class );
    }

}
