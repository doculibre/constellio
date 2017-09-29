package com.constellio.model.services.search.services;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.Elevations;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;

public interface ElevationService {
	void elevate(Record record, String query);

	void removeElevation(String recordId, String query);

}
