package org.pokemon.go.pokemongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@SpringBootApplication
public class PokemonGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokemonGoApplication.class, args);
	}

}
