package com.constellio.model.services.records.preparation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data @AllArgsConstructor
public class ResolvedRecordLinks {
	private Set<String> idsToReindex;
	private List<AggregatedMetadataIncrementation> aggregatedMetadatasToIncrement;
}
