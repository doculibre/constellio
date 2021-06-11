package com.constellio.app.modules.restapi.apis.v2.record.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Data
@Builder
@JsonRootName("Query")
public class QueryDtoV2 {
	@NotNull private String collection;
	private Set<String> schemaTypes;
	@Valid private List<SortDtoV2> sorting;
	private FacetMode facetMode;
	private Set<String> facetValueIds;
	private String expression;
	private Integer rowsStart;
	private Integer rowsLimit;
	private boolean requireWriteAccess;
}
