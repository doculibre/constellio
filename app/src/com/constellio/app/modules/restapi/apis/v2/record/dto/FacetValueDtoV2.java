package com.constellio.app.modules.restapi.apis.v2.record.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonRootName("FacetValue")
public class FacetValueDtoV2 {
	private String id;
	private String name;
	private long count;
}
