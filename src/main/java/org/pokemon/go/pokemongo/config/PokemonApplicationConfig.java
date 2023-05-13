package org.pokemon.go.pokemongo.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.TimeUnit;

@Configuration
@AllArgsConstructor
public class PokemonApplicationConfig {
    private static final int CACHE_SIZE = 9999;

    private final PokemonApiConfig pokemonApiConfig;
    private final PokemonApiUrlConfig pokemonApiUrlConfig;

    @Bean
    public Cache<?,?> caffeineCache() {
        return Caffeine.newBuilder().maximumSize(CACHE_SIZE)
                .expireAfterWrite(pokemonApiConfig.getCacheTtl(), TimeUnit.SECONDS).build();
    }

    @Bean
    public WebClient pokemonWebClient() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(pokemonApiConfig.getBufferSize()))
                        .build())
                .baseUrl(pokemonApiUrlConfig.getBasePath())
                .build();
    }
}
