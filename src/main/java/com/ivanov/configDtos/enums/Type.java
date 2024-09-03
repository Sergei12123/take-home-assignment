package com.ivanov.configDtos.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Type {

    @JsonProperty("bonus")
    BONUS,
    @JsonProperty("standard")
    STANDARD
}
