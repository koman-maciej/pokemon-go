package org.pokemon.go.pokemongo.client;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.AllArgsConstructor;
import org.pokemon.go.pokemongo.config.PokemonApiConfig;
import org.pokemon.go.pokemongo.config.PokemonApiUrlConfig;
import org.pokemon.go.pokemongo.domain.dto.AttributeType;
import org.pokemon.go.pokemongo.domain.dto.PokemonDto;
import org.pokemon.go.pokemongo.domain.pokemon.PageParameters;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonDefinitionResource;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonPageResource;
import org.pokemon.go.pokemongo.domain.pokemon.PokemonResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class PokemonClient {

    private final Comparator<PokemonDto> baseComparator = (p1, p2) -> 0;
    private final Comparator<PokemonDto> weightComparator = (p1, p2) -> p2.getWeight().compareTo(p1.getWeight());
    private final Comparator<PokemonDto> heightComparator = (p1, p2) -> p2.getHeight().compareTo(p1.getHeight());
    private final Comparator<PokemonDto> baseExperienceComparator = (p1, p2) -> p2.getBaseExperience().compareTo(p1.getBaseExperience());

    private final WebClient webClient;
    private final PokemonApiConfig pokemonApiConfig;
    private final PokemonApiUrlConfig pokemonApiUrlConfig;
    private final Cache<String, PokemonDto> pokemonUrlCache;
    private final Cache<PageParameters, PokemonPageResource> pokemonPageCache;

    public Mono<List<PokemonDto>> findPokemonsByAttributeAndLimit(final AttributeType attribute, final int limit) {
        final Comparator<PokemonDto> comparator =
                switch (attribute) {
                    case WEIGHT -> weightComparator;
                    case HEIGHT -> heightComparator;
                    case BASE_EXPERIENCE -> baseExperienceComparator;
                    default -> baseComparator;
                };
        final PageParameters pageParams = new PageParameters(pokemonApiConfig.getOffset(), pokemonApiConfig.getLimit());

        return fetchPokemonPage(pageParams)
                .flatMap(a -> fetchPokemonsByUrls(a.getResults().parallelStream().map(PokemonResource::getUrl)))
                .sort(comparator)
                .take(limit)
                .collectList();
    }

    private Mono<PokemonDto> fetchPokemon(final String pokemonUrl) {
        final PokemonDto cachedPokemonDto = pokemonUrlCache.getIfPresent(pokemonUrl);
        if (cachedPokemonDto != null) {
            return Mono.just(cachedPokemonDto);
        }

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
                        .build())
                .doOnNext(dto -> pokemonUrlCache.put(pokemonUrl, dto));
    }

    private Flux<PokemonPageResource> fetchPokemonPage(final PageParameters pageParameters) {
        final PokemonPageResource pokemonPage = pokemonPageCache.getIfPresent(pageParameters);
        if (pokemonPage != null) {
            return Flux.just(pokemonPage);
        }

        return webClient.get()
                .uri(String.format(pokemonApiUrlConfig.getPokemonPath(), pageParameters.getOffset(), pageParameters.getLimit()))
                .retrieve()
                .bodyToFlux(PokemonPageResource.class)
                .doOnNext(pokemonPageResponse -> pokemonPageCache.put(pageParameters, pokemonPageResponse));
    }

    private Flux<PokemonDto> fetchPokemonsByUrls(Stream<String> pokemonUrls) {
        return Flux.fromStream(pokemonUrls)
                .flatMap(this::fetchPokemon);
    }
}
