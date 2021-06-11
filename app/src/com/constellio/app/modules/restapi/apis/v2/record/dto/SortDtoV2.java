package com.constellio.app.modules.restapi.apis.v2.record.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonRootName("Sort")
public class SortDtoV2 {
	@NotNull private String metadata;
	private boolean ascending;
}
