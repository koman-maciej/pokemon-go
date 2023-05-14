package org.pokemon.go.pokemongo.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pokemon.go.pokemongo.client.PokemonClient;
import org.pokemon.go.pokemongo.domain.dto.AttributeType;
import org.pokemon.go.pokemongo.domain.dto.PokemonDto;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class PokemonHandlerTest {

    @Mock
    private PokemonClient pokemonClient;
    @Mock
    private ServerRequest serverRequest;

    private PokemonHandler pokemonHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pokemonHandler = new PokemonHandler(pokemonClient);
    }

    @Test
    void fetchPokemons_ValidRequest_ReturnsOkResponse() {
        // Given
        when(serverRequest.queryParam("attribute")).thenReturn(Optional.of("weight"));
        when(serverRequest.queryParam("limit")).thenReturn(Optional.of("1"));

        PokemonDto pokemonDto = PokemonDto.builder().id(3).name("Charizard").weight(400).height(60).baseExperience(50).build();
        when(pokemonClient.findPokemonsByAttributeAndLimit(AttributeType.WEIGHT, 1))
                .thenReturn(Mono.just(List.of(pokemonDto)));

        // When
        Mono<ServerResponse> response = pokemonHandler.fetchPokemons(serverRequest);

        // Then
        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> serverResponse.statusCode().is2xxSuccessful())
                .verifyComplete();

        verify(serverRequest, times(4)).queryParam(anyString());
        verify(pokemonClient).findPokemonsByAttributeAndLimit(AttributeType.WEIGHT, 1);
    }

    @Test
    void fetchPokemons_InvalidAttribute_ReturnsBadRequest() {
        // Given
        when(serverRequest.queryParam("attribute")).thenReturn(Optional.of("invalid"));
        when(serverRequest.queryParam("limit")).thenReturn(Optional.of("1"));

        // When
        Mono<ServerResponse> response = pokemonHandler.fetchPokemons(serverRequest);

        // Then
        StepVerifier.create(response)
                .expectNextMatches(serverResponse -> serverResponse.statusCode().is4xxClientError())
                .verifyComplete();

        verify(serverRequest, times(2)).queryParam(anyString());
        verifyNoInteractions(pokemonClient);
    }
}
