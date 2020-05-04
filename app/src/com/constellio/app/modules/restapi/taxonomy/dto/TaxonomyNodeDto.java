package com.constellio.app.modules.restapi.taxonomy.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonRootName("TaxonomyNode")
public class TaxonomyNodeDto {
	private String id;
	private String schemaType;
	private boolean linkable;
	private boolean hasChildren;
	private Map<String, String> metadatas;
}
