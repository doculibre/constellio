package com.constellio.model.services.search.services;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.Elevations;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;

public interface ElevationService {
	void elevate(Record record, String query);

	void removeElevation(Record record, String query);

	void removeCollectionElevation(String collection, String query);

	List<DocElevation> getCollectionElevation(String collection, String query);

	Elevations getCollectionElevations(String collection);

	void removeCollectionElevations(String collection);
}
