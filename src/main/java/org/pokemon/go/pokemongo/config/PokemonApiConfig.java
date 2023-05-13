package org.pokemon.go.pokemongo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("pokemon.api")
public class PokemonApiConfig {

    private int bufferSize;
    private int cacheTtl;
    private int offset;
    private int limit;
}
