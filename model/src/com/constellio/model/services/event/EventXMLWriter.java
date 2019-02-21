package com.constellio.model.services.event;

import com.constellio.model.entities.records.Record;

public interface EventXMLWriter {
	void write(Record event);
	void close();
}
