package com.ivanov.dtos;

import lombok.*;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WinCombinations {
    Map<String, Integer> countOfEachSymbol = new HashMap<>();
    Map<String, List<Map<String, String>>> combinations = new HashMap<>();
    Map<String, List<String>> appliedWinningCombinations = new HashMap<>();

    public WinCombinations(Map<String, List<List<String>>> mapCombinations, List<String> standardSymbols) {
        this.countOfEachSymbol = standardSymbols.stream().collect(Collectors.toMap(symbol -> symbol, symbol -> 0));
        this.combinations = mapCombinations.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(innerList -> innerList.stream()
                                        .collect(Collectors.toMap(key -> key, key -> StringUtils.EMPTY))
                                )
                                .collect(Collectors.toList())
                ));

    }

}
