package com.constellio.model.services.records.cache2;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.StreamAdaptor;

import java.util.stream.Stream;

public class ReferencingRecordsCacheStream extends StreamAdaptor<Record> {
	public ReferencingRecordsCacheStream(Stream<Record> adapted) {
		super(adapted);
	}
}
