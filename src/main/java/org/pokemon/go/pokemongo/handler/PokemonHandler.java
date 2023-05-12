package org.pokemon.go.pokemongo.handler;

import lombok.AllArgsConstructor;
import org.pokemon.go.pokemongo.client.PokemonClient;
import org.pokemon.go.pokemongo.domain.dto.AttributeType;
import org.pokemon.go.pokemongo.domain.dto.PokemonDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class PokemonHandler {

    private static final String ATTRIBUTE_QUERY_PARAM = "attribute";
    private static final String LIMIT_QUERY_PARAM = "limit";

    private final PokemonClient pokemonClientV2;

    public Mono<ServerResponse> fetchPokemons(ServerRequest request) {
        try {
            final AttributeType attribute = request.queryParam(ATTRIBUTE_QUERY_PARAM).isPresent() ?
                    AttributeType.valueOf(request.queryParam(ATTRIBUTE_QUERY_PARAM).get().toUpperCase()) : AttributeType.NONE;
            final int limit = request.queryParam(LIMIT_QUERY_PARAM).isPresent() ?
                    Integer.parseInt(request.queryParam(LIMIT_QUERY_PARAM).get()) : Integer.MAX_VALUE;
            return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(pokemonClientV2.fetchAllPokemons(attribute, limit), PokemonDto.class);
        } catch (IllegalArgumentException e) {
            return ServerResponse.badRequest().build();
        }
    }
}
