package org.pokemon.go.pokemongo.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PokemonDto {
    private int id;
    private String name;
    private Integer weight;
    private Integer height;

    @JsonProperty("base_experience")
    private Integer baseExperience;
}
