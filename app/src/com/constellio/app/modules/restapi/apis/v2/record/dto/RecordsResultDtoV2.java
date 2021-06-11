package com.constellio.app.modules.restapi.apis.v2.record.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonRootName("RecordsResult")
public class RecordsResultDtoV2 {
	private List<RecordDtoV2> records;
	private List<FacetDtoV2> facets;
	private List<ReferenceDtoV2> references;
}
