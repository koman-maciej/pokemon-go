package org.pokemon.go.pokemongo.domain.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PokemonDefinitionResource implements Serializable {

    private int id;
    private String name;
    private int weight;
    private int height;

    @JsonProperty("base_experience")
    private int baseExperience;
}
