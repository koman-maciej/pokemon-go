package org.pokemon.go.pokemongo.resource;

import org.pokemon.go.pokemongo.handler.PokemonHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class PokemonRouter {

    @Bean
    public RouterFunction<ServerResponse> route(PokemonHandler pokemonHandler) {

        return RouterFunctions.route()
                .GET("/pokemons", pokemonHandler::fetchPokemons)
                .build();
    }
}
