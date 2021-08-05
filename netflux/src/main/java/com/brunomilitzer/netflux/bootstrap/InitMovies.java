package com.brunomilitzer.netflux.bootstrap;

import com.brunomilitzer.netflux.domain.Movie;
import com.brunomilitzer.netflux.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Component
public class InitMovies implements CommandLineRunner {

    private final MovieRepository movieRepository;

    @Override
    public void run( final String... args ) throws Exception {

        this.movieRepository.deleteAll().thenMany( Flux.just( "Memento", "Batman Begins", "The Dark Knight", "The Dark Knight Rises", "2001 Space Odyssey",
                "The Terminator", "Terminator II", "Interstellar", "Star Trek II", "ONe Flew over the Cuckoos Nest" ) )
                .map( title -> Movie.builder().title( title ).build() ).flatMap( this.movieRepository::save ).subscribe( null, null, () ->
                this.movieRepository.findAll().subscribe( System.out::println ) );
    }

}
