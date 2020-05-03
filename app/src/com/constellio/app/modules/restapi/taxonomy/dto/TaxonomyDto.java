package com.constellio.app.modules.restapi.taxonomy.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonRootName("Taxonomy")
public class TaxonomyDto {
	private String code;
	private Map<String, String> titles;
	private List<String> schemaTypes;
}
