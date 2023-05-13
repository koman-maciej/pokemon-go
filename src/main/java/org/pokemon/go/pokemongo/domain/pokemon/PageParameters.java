package org.pokemon.go.pokemongo.domain.pokemon;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PageParameters implements Serializable {
    private final Integer offset;
    private final Integer limit;
}
