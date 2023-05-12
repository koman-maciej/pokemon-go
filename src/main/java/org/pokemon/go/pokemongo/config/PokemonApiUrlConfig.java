package org.pokemon.go.pokemongo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("pokemon.api.url")
public class PokemonApiUrlConfig {

    private String basePath;
    private String pokemonPath;
}
