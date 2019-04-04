package com.constellio.data.dao.dto.records;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class FacetPivotValue implements Serializable {
	private String field;
	private Object value;
	private int count;
	private List<FacetPivotValue> facetPivotValues;
}
