package com.constellio.model.services.search.memoryConditions;

import com.constellio.model.entities.records.Record;

public interface InMemoryCondition {

	boolean isReturnable(Record record);

}
