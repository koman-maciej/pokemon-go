package org.pokemon.go.pokemongo.domain.pokemon;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class PageParameters implements Serializable {
    private final Integer offset;
    private final Integer limit;
}
