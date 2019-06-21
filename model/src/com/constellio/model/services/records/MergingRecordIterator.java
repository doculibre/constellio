package com.constellio.model.services.records;

import com.constellio.data.utils.LazyMergingIterator;
import com.constellio.model.entities.records.Record;

import java.util.Iterator;
import java.util.List;

public class MergingRecordIterator extends LazyMergingIterator<Record> {

	public MergingRecordIterator(List<Iterator<Record>> iterators) {
		super(iterators);
	}

	public MergingRecordIterator(Iterator<Record>... iterators) {
		super(iterators);
	}

	@Override
	protected String toUniqueKey(Record record) {
		if(record != null) {
			return record.getId();
		}
		return null;
	}

	public static Iterator<Record> merge(List<Iterator<Record>> iterators) {
		return new MergingRecordIterator(iterators);
	}
}
