package com.ivanov.configDtos;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusSymbols {
    private Map<String, Integer> symbols;

}