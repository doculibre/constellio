package com.constellio.app.modules.restapi.collection.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonRootName("Collection")
public class CollectionDto {
	private String name;
	private String code;
	private List<String> languages;
}
