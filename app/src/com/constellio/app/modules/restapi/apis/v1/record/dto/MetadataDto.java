package com.constellio.app.modules.restapi.apis.v1.record.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MetadataDto {
	private String code;
	private List<String> values;
}
