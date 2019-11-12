package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.records.Record;

public interface TaxonomiesSearchServicesCache {

	void initialize(String collection);

	void insert(String username, String recordId, String mode, Boolean value);

	void invalidateAll();

	/*pour tous les users, invalide quand c’est true pour ce id*/
	void invalidateWithChildren(String recordId);

	/*pour tous les users, invalide quand c’est false pour ce id*/
	void invalidateWithoutChildren(String recordId);

	/*pour tous les users, invalide  pour ce id*/
	void invalidateRecord(String recordId);

	/*pour tous les users, invalide  pour ce id*/
	void invalidateUser(String username);

	Boolean getCachedValue(String username, Record record, String mode);

}
