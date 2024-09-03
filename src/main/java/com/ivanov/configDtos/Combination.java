package com.ivanov.configDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ivanov.configDtos.enums.When;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Combination {
    @JsonProperty("reward_multiplier")
    private BigDecimal rewardMultiplier;
    private When when;
    private int count;
    private String group;
    @JsonProperty("covered_areas")
    private List<List<String>> coveredAreas;

}