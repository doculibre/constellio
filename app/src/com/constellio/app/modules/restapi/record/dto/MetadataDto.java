package com.constellio.app.modules.restapi.record.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MetadataDto {
	private String code;
	private List<String> values;
}
