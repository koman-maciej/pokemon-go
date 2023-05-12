package org.pokemon.go.pokemongo.domain.pokemon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PokemonPageResource implements Serializable {

    private Integer count;
    private String next;
    private Boolean previous;
    private List<PokemonResource> results;
}
