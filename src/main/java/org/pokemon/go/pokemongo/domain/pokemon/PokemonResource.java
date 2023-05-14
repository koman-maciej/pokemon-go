package org.pokemon.go.pokemongo.domain.pokemon;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PokemonResource implements Serializable {

    private String name;
    private String url;
}
