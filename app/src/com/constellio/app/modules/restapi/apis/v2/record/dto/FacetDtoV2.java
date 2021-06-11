package com.constellio.app.modules.restapi.apis.v2.record.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonRootName("Facet")
public class FacetDtoV2 {
	private String facetId;
	private String facetName;
	private List<FacetValueDtoV2> values;

}