package com.constellio.app.modules.restapi.apis.v2.record.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonRootName("Record")
public class RecordDtoV2 {
	private String id;
	private String schemaType;
	@JsonIgnore
	private String version;
	private Map<String, List<String>> metadatas;
}
