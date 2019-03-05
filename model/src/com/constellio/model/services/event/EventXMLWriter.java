package com.constellio.model.services.event;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;

import java.io.File;

public interface EventXMLWriter {
	void write(Record event);
	File getXMLFile();
	void close();
	boolean isClose();
	void deleteFile();
	KeySetMap<String, String> getAllEventWrittenEventsBySchema();
}
