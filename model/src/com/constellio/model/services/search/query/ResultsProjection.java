package com.constellio.model.services.search.query;

import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public interface ResultsProjection {

	SPEQueryResponse project(LogicalSearchQuery query, SPEQueryResponse originalResponse);

}
