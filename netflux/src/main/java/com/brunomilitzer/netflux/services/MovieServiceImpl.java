package com.brunomilitzer.netflux.services;

import com.brunomilitzer.netflux.domain.Movie;
import com.brunomilitzer.netflux.domain.MovieEvent;
import com.brunomilitzer.netflux.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    public Mono<Movie> getMovieById( final String id ) {

        return this.movieRepository.findById( id );
    }

    @Override
    public Flux<Movie> getAllMovies() {

        return this.movieRepository.findAll();
    }

    @Override
    public Flux<MovieEvent> streamMovieEvents( final String id ) {

        return Flux.<MovieEvent>generate( movieEventSynchronousSink -> {
            movieEventSynchronousSink.next( new MovieEvent( id, new Date() ) );
        } ).delayElements( Duration.ofSeconds( 1 ) );
    }

}
