package com.brunomilitzer.restbrewery;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@SpringBootApplication
public class ReactiveBreweryApplication {

    public static void main( final String[] args ) {

        SpringApplication.run( ReactiveBreweryApplication.class, args );
    }

    @Value( "classpath:/schema.sql" )
    Resource resource;

    @Bean
    ConnectionFactoryInitializer initializer( final ConnectionFactory connectionFactory ) {

        final ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory( connectionFactory );
        initializer.setDatabasePopulator( new ResourceDatabasePopulator( this.resource ) );

        return initializer;
    }

}
