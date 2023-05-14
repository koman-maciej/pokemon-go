package org.pokemon.go.pokemongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pokemon.go.pokemongo.client.PokemonClient;
import org.pokemon.go.pokemongo.config.PokemonApiConfig;
import org.pokemon.go.pokemongo.config.PokemonApiUrlConfig;
import org.pokemon.go.pokemongo.domain.dto.AttributeType;
import org.pokemon.go.pokemongo.domain.dto.PokemonDto;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonDefinitionResource;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonPageResource;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonResource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class PokemonIT {

    public static final PokemonDefinitionResource BULBASAUR = PokemonDefinitionResource.builder()
            .id(1).name("bulbasaur").weight(69).height(7).baseExperience(64).build();
    public static final PokemonDefinitionResource IVYSAUR = PokemonDefinitionResource.builder()
            .id(2).name("ivysaur").weight(130).height(10).baseExperience(142).build();
    public static final PokemonDefinitionResource VENUSAUR = PokemonDefinitionResource.builder()
            .id(3).name("venusaur").weight(1000).height(20).baseExperience(263).build();
    public static final PokemonDefinitionResource CHARMANDER = PokemonDefinitionResource.builder()
            .id(4).name("charmander").weight(85).height(6).baseExperience(62).build();

    private MockWebServer mockWebServer;
    private ObjectMapper objectMapper;
    private WebClient webClient;
    private PokemonClient pokemonClient;

    @BeforeEach
    void setup() throws IOException {
        objectMapper = new ObjectMapper();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        webClient = WebClient.builder()
                .baseUrl(getMockPokeApiServerBaseUrl())
                .build();

        PokemonApiConfig pokemonApiConfig = new PokemonApiConfig();
        PokemonApiUrlConfig pokemonApiUrlConfig = new PokemonApiUrlConfig();
        pokemonApiUrlConfig.setPokemonPath("/v2/pokemon");

        pokemonClient = new PokemonClient(webClient, pokemonApiConfig,
                pokemonApiUrlConfig,
                Caffeine.newBuilder().build(), Caffeine.newBuilder().build());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetThreeHaviestPokemons() throws JsonProcessingException, InterruptedException {
        // Given
        final AttributeType attributeType = AttributeType.WEIGHT;
        final int limit = 3;

        mockPageResponse();
        mockPokemonResponses();

        final List<PokemonDto> expectedResult = Stream.of(VENUSAUR, IVYSAUR, CHARMANDER)
                .map(p -> PokemonDto.builder().id(p.getId()).name(p.getName())
                        .height(p.getHeight()).weight(p.getWeight())
                        .baseExperience(p.getBaseExperience()).build())
                .collect(Collectors.toList());

        // When
        StepVerifier.create(pokemonClient.findPokemonsByAttributeAndLimit(attributeType, limit))
                .expectNextMatches(expectedResult::equals)
                .expectComplete()
                .verify();

        // Then
        assertPokemonServerInteractions();
    }

    @Test
    void testGetThreeHighestPokemons() throws JsonProcessingException, InterruptedException {
        // Given
        final AttributeType attributeType = AttributeType.HEIGHT;
        final int limit = 3;

        mockPageResponse();
        mockPokemonResponses();

        final List<PokemonDto> expectedResult = Stream.of(VENUSAUR, IVYSAUR, BULBASAUR)
                .map(p -> PokemonDto.builder().id(p.getId()).name(p.getName())
                        .height(p.getHeight()).weight(p.getWeight())
                        .baseExperience(p.getBaseExperience()).build())
                .collect(Collectors.toList());

        // When
        StepVerifier.create(pokemonClient.findPokemonsByAttributeAndLimit(attributeType, limit))
                .expectNextMatches(expectedResult::equals)
                .expectComplete()
                .verify();

        // Then
        assertPokemonServerInteractions();
    }

    @Test
    void testGetThreePokemonsWithMoreBaseExperience() throws JsonProcessingException, InterruptedException {
        // Given
        final AttributeType attributeType = AttributeType.BASE_EXPERIENCE;
        final int limit = 3;

        mockPageResponse();
        mockPokemonResponses();

        final List<PokemonDto> expectedResult = Stream.of(VENUSAUR, IVYSAUR, BULBASAUR)
                .map(p -> PokemonDto.builder().id(p.getId()).name(p.getName())
                        .height(p.getHeight()).weight(p.getWeight())
                        .baseExperience(p.getBaseExperience()).build())
                .collect(Collectors.toList());

        // When
        StepVerifier.create(pokemonClient.findPokemonsByAttributeAndLimit(attributeType, limit))
                .expectNextMatches(expectedResult::equals)
                .expectComplete()
                .verify();

        // Then
        assertPokemonServerInteractions();
    }

    private void assertPokemonServerInteractions() throws InterruptedException {
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        RecordedRequest recordedPokemonRequest1 = mockWebServer.takeRequest();
        RecordedRequest recordedPokemonRequest2 = mockWebServer.takeRequest();
        RecordedRequest recordedPokemonRequest3 = mockWebServer.takeRequest();
        RecordedRequest recordedPokemonRequest4 = mockWebServer.takeRequest();

        assertEquals(HttpMethod.GET.toString(), recordedRequest.getMethod());
        assertEquals("/v2/pokemon", recordedRequest.getPath());
        assertEquals(HttpMethod.GET.toString(), recordedRequest.getMethod());
        assertEquals("/v2/pokemon/1/", recordedPokemonRequest1.getPath());
        assertEquals(HttpMethod.GET.toString(), recordedRequest.getMethod());
        assertEquals("/v2/pokemon/2/", recordedPokemonRequest2.getPath());
        assertEquals(HttpMethod.GET.toString(), recordedRequest.getMethod());
        assertEquals("/v2/pokemon/3/", recordedPokemonRequest3.getPath());
        assertEquals(HttpMethod.GET.toString(), recordedRequest.getMethod());
        assertEquals("/v2/pokemon/4/", recordedPokemonRequest4.getPath());
    }

    private String getMockPokeApiServerBaseUrl() {
        return String.format("http://localhost:%s", mockWebServer.getPort());
    }

    private void mockPageResponse() throws JsonProcessingException {
        final PokemonPageResource pokemonPageResource = new PokemonPageResource();
        pokemonPageResource.setCount(6);
        pokemonPageResource.setNext(null);
        pokemonPageResource.setPrevious(null);
        pokemonPageResource.setResults(List.of(
                new PokemonResource("bulbasaur", getMockPokeApiServerBaseUrl() + "/v2/pokemon/1/"),
                new PokemonResource("ivysaur", getMockPokeApiServerBaseUrl() + "/v2/pokemon/2/"),
                new PokemonResource("venusaur", getMockPokeApiServerBaseUrl() + "/v2/pokemon/3/"),
                new PokemonResource("charmander", getMockPokeApiServerBaseUrl() + "/v2/pokemon/4/")
        ));

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(pokemonPageResource))
                .addHeader("Content-Type", "application/json"));
    }

    private void mockPokemonResponses() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(BULBASAUR))
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(IVYSAUR))
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(VENUSAUR))
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(CHARMANDER))
                .addHeader("Content-Type", "application/json"));
    }
}
