package com.ivanov.configDtos;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Probability {
    private int column;
    private int row;
    private Map<String, Integer> symbols;

}