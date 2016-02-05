package com.constellio.model.services.records;

import com.constellio.model.entities.schemas.ModificationImpact;

public interface RecordModificationImpactHandler {

	void prepareToHandle(ModificationImpact modificationImpact);

	void handle();

	void cancel();

}
