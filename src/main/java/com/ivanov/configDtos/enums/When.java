package com.ivanov.configDtos.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum When {
    @JsonProperty("same_symbols")
    SAME_SYMBOLS,
    @JsonProperty("linear_symbols")
    LINEAR_SYMBOLS


}
