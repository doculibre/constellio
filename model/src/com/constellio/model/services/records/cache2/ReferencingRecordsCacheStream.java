package com.constellio.model.services.records.cache2;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.StreamAdaptor;

import java.util.stream.Stream;

public class RecordCacheStream extends StreamAdaptor<Record> {
	public RecordCacheStream(Stream<Record> adapted) {
		super(adapted);
	}
}
