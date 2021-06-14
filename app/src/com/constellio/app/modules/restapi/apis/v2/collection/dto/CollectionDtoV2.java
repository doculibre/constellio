package com.constellio.app.modules.restapi.apis.v2.collection.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonRootName("Collection")
public class CollectionDtoV2 {
	private String name;
	private String code;
	private List<String> languages;
}