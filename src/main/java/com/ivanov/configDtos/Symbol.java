package com.ivanov.configDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ivanov.configDtos.enums.Impact;
import com.ivanov.configDtos.enums.Type;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Symbol {
    @JsonProperty("reward_multiplier")
    private BigDecimal rewardMultiplier;
    private Type type;
    private Impact impact;
    private BigDecimal extra;

}