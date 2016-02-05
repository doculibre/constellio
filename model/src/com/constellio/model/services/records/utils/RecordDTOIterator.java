package com.constellio.model.services.records.utils;

import java.util.Iterator;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordImpl;

public class RecordDTOIterator implements Iterator<RecordDTO> {

	Iterator<Record> nestedIterator;

	public RecordDTOIterator(Iterator<Record> nestedIterator) {
		this.nestedIterator = nestedIterator;
	}

	@Override
	public boolean hasNext() {
		return nestedIterator.hasNext();
	}

	@Override
	public RecordDTO next() {
		return ((RecordImpl) nestedIterator.next()).getRecordDTO();
	}

	@Override
	public void remove() {
		nestedIterator.remove();
	}
}
