package org.pokemon.go.pokemongo.client;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pokemon.go.pokemongo.config.PokemonApiConfig;
import org.pokemon.go.pokemongo.config.PokemonApiUrlConfig;
import org.pokemon.go.pokemongo.domain.dto.AttributeType;
import org.pokemon.go.pokemongo.domain.dto.PokemonDto;
import org.pokemon.go.pokemongo.domain.pokemon.PageParameters;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonDefinitionResource;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonPageResource;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonResource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PokemonClientTest {

    @Mock
    private PokemonApiConfig pokemonApiConfig;
    @Mock
    private PokemonApiUrlConfig pokemonApiUrlConfig;
    @Mock
    private WebClient webClient;
    @Mock
    private Cache<String, PokemonDto> pokemonUrlCache;
    @Mock
    private Cache<PageParameters, PokemonPageResource> pokemonPageCache;

    private PokemonClient pokemonClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pokemonClient = new PokemonClient(webClient, pokemonApiConfig, pokemonApiUrlConfig, pokemonUrlCache, pokemonPageCache);
    }

    @ParameterizedTest
    @EnumSource(AttributeType.class)
    void findPokemonsByAttributeAndLimit_ReturnsSortedByAttribute(AttributeType attribute) {
        // Given
        int limit = 10;
        final List<PokemonDefinitionResource> pokemonResponses = List.of(
                new PokemonDefinitionResource(1, "Charizard", 90, 8, 240),
                new PokemonDefinitionResource(3, "Bulbasaur", 10, 7, 112),
                new PokemonDefinitionResource(2, "Pikachu", 6, 6, 64)
        );

        final List<PokemonDto> expectedPokemonListSortedByWeight = List.of(
                PokemonDto.builder().id(1).name("Charizard").weight(90).height(8).baseExperience(240).build(),
                PokemonDto.builder().id(3).name("Bulbasaur").weight(10).height(7).baseExperience(112).build(),
                PokemonDto.builder().id(2).name("Pikachu").weight(6).height(6).baseExperience(64).build()
        );

        final PokemonPageResource pokemonPageResource = new PokemonPageResource();
        pokemonPageResource.setResults(List.of(
                new PokemonResource("Pikachu", "https://pokeapi.co/api/v2/pokemon/25"),
                new PokemonResource("Charizard", "https://pokeapi.co/api/v2/pokemon/6"),
                new PokemonResource("Bulbasaur", "https://pokeapi.co/api/v2/pokemon/1")
        ));

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        WebClient.RequestHeadersUriSpec requestSpec = mock(WebClient.RequestHeadersUriSpec.class);

        when(pokemonApiConfig.getOffset()).thenReturn(0);
        when(pokemonApiConfig.getLimit()).thenReturn(9999);
        when(pokemonApiUrlConfig.getPokemonPath()).thenReturn("/pokemon?offset=%s&limit=%s");
        when(webClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(Mockito.anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(PokemonPageResource.class)).thenReturn(Flux.just(pokemonPageResource));
        when(responseSpec.bodyToMono(PokemonDefinitionResource.class))
                .thenReturn(Mono.just(pokemonResponses.get(0)))
                .thenReturn(Mono.just(pokemonResponses.get(1)))
                .thenReturn(Mono.just(pokemonResponses.get(2)));

        // When
        Mono<List<PokemonDto>> result = pokemonClient.findPokemonsByAttributeAndLimit(attribute, limit);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedPokemonListSortedByWeight)
                .verifyComplete();
    }
}
