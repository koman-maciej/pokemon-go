package org.pokemon.go.pokemongo.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pokemon.go.pokemongo.handler.PokemonHandler;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class PokemonRouterTest {

    @Mock
    private PokemonHandler pokemonHandler;

    private PokemonRouter pokemonRouter;
    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pokemonRouter = new PokemonRouter();
        webClient = WebTestClient.bindToRouterFunction(pokemonRouter.route(pokemonHandler)).build();
    }

    @Test
    public void testRoute() {
        // Given
        ServerResponse okResponse = ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).build().block();
        Mockito.when(pokemonHandler.fetchPokemons(Mockito.any())).thenReturn(Mono.just(okResponse));

        // When
        webClient.get()
                .uri("/pokemons")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class); // Expect an empty response body

        // Then
        Mockito.verify(pokemonHandler, Mockito.times(1)).fetchPokemons(Mockito.any());
    }
}