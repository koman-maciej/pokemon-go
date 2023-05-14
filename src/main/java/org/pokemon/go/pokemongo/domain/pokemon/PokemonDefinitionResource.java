package org.pokemon.go.pokemongo.domain.pokemon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Builder
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
