package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.Record;

import java.util.Collection;


interface FeedsExtractor<T>{
	Collection<T> getFeeds(Record record);
}
