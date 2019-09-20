package com.constellio.app.services.sip.record;

import com.constellio.model.entities.records.Record;

public interface RecordPathProvider {

	String getPath(Record record);
}
