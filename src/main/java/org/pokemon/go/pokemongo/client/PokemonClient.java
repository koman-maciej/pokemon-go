package org.pokemon.go.pokemongo.client;

import org.pokemon.go.pokemongo.config.PokemonApiConfig;
import org.pokemon.go.pokemongo.config.PokemonApiUrlConfig;
import org.pokemon.go.pokemongo.domain.dto.AttributeType;
import org.pokemon.go.pokemongo.domain.dto.PokemonDto;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonDefinitionResource;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonPageResource;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
public class PokemonClient {

    private final Comparator<PokemonDto> baseComparator = (p1, p2) -> 0;
    private final Comparator<PokemonDto> weightComparator = (p1, p2) -> p2.getWeight().compareTo(p1.getWeight());
    private final Comparator<PokemonDto> heightComparator = (p1, p2) -> p2.getHeight().compareTo(p1.getHeight());
    private final Comparator<PokemonDto> baseExperienceComparator = (p1, p2) -> p2.getBaseExperience().compareTo(p1.getBaseExperience());

    private final WebClient webClient;
    private final PokemonApiConfig pokemonApiConfig;
    private final PokemonApiUrlConfig pokemonApiUrlConfig;

    public PokemonClient(WebClient.Builder builder, PokemonApiConfig pokemonApiConfig, PokemonApiUrlConfig pokemonApiUrlConfig) {
        this.pokemonApiConfig = pokemonApiConfig;
        this.pokemonApiUrlConfig = pokemonApiUrlConfig;

        this.webClient = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(this.pokemonApiConfig.getBufferSize()))
                        .build())
                .baseUrl(this.pokemonApiUrlConfig.getBasePath())
                .build();
    }

    public Mono<List<PokemonDto>> fetchAllPokemons(AttributeType attribute, int limit) {
        final Comparator<PokemonDto> comparator =
                switch (attribute) {
                    case WEIGHT -> weightComparator;
                    case HEIGHT -> heightComparator;
                    case BASE_EXPERIENCE -> baseExperienceComparator;
                    default -> baseComparator;
                };

        return webClient.get().uri(String.format(pokemonApiUrlConfig.getPokemonPath(),
                        pokemonApiConfig.getOffset(), pokemonApiConfig.getLimit()))
                .retrieve()
                .bodyToFlux(PokemonPageResource.class)
                .flatMap(a -> fetchPokemonsByUrls(a.getResults().parallelStream().map(PokemonResource::getUrl)))
                .sort(comparator)
                .take(limit)
                .collectList();
    }

    private Mono<PokemonDto> fetchPokemon(final String pokemonUrl) {
        return webClient.get()
                .uri(pokemonUrl)
                .retrieve()
                .bodyToMono(PokemonDefinitionResource.class)
                .map(pokemon -> PokemonDto.builder()
                        .id(pokemon.getId())
                        .name(pokemon.getName())
                        .weight(pokemon.getWeight())
                        .height(pokemon.getHeight())
                        .baseExperience(pokemon.getBaseExperience())
                        .build());
    }

    private Flux<PokemonDto> fetchPokemonsByUrls(Stream<String> pokemonUrls) {
        return Flux.fromStream(pokemonUrls)
                .flatMap(this::fetchPokemon);
    }
}
