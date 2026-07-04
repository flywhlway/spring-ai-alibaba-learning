package com.flywhl.saa.jsonschema.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 演员与其代表作列表（嵌套集合字段）。
 *
 * @author flywhl
 */
public record ActorFilms(
        @JsonPropertyDescription("演员姓名") String actor,
        @JsonPropertyDescription("代表电影片名列表") List<String> movies) {
}
