package com.constellio.app.modules.restapi.apis.v2.record.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonRootName("Reference")
public class ReferenceDtoV2 {
	private String id;
	private String schemaType;
	private String code;
	private String title;
	private String description;
}
