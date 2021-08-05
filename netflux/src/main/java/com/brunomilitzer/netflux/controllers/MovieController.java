package com.brunomilitzer.netflux.controllers;

import com.brunomilitzer.netflux.domain.Movie;
import com.brunomilitzer.netflux.domain.MovieEvent;
import com.brunomilitzer.netflux.services.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping( "/movies" )
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping( "/{id}" )
    Mono<Movie> getMovieById( @PathVariable final String id ) {

        return this.movieService.getMovieById( id );
    }

    @GetMapping
    Flux<Movie> getAllMovies() {

        return this.movieService.getAllMovies();
    }

    @GetMapping( value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE )
    Flux<MovieEvent> streamMovieEvents( @PathVariable final String id ) {

        return this.movieService.streamMovieEvents( id );
    }

}
