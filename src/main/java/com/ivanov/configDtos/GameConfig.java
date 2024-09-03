package com.ivanov.configDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameConfig {
    private int columns;
    private int rows;
    private Map<String, Symbol> symbols;
    private Probabilities probabilities;
    @JsonProperty("win_combinations")
    private Map<String, Combination> winCombinations;

}