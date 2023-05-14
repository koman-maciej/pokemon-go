package org.pokemon.go.pokemongo.domain.pokemon;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PokemonPageResource implements Serializable {

    private Integer count;
    private String next;
    private Boolean previous;
    private List<PokemonResource> results;
}
