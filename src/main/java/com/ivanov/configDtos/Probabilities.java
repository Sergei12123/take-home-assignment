package com.ivanov.configDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Probabilities {

    @JsonProperty("standard_symbols")
    private List<Probability> standardSymbols;

    @JsonProperty("bonus_symbols")
    private BonusSymbols bonusSymbols;

}