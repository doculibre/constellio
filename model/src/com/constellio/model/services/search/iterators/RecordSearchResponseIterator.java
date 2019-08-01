package com.constellio.model.services.search.iterators;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.LazyResultsIterator;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import org.apache.solr.common.params.SolrParams;

public class RecordSearchResponseIterator extends LazyResultsIterator<Record> implements SearchResponseIterator<Record> {

	private boolean fullyLoaded;
	private RecordServices recordServices;

	public RecordSearchResponseIterator(ModelLayerFactory modelLayerFactory, SolrParams solrParams, int intervalsLength,
										boolean fullyLoaded, String queryName) {
		super(modelLayerFactory.getDataLayerFactory().newRecordDao(), solrParams, intervalsLength, true, queryName);
		this.fullyLoaded = fullyLoaded;
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	public RecordSearchResponseIterator beginAfterId(String id) {
		this.lastId = id;
		return this;
	}

	@Override
	public Record convert(RecordDTO recordDTO) {
		return recordServices.toRecord(recordDTO, fullyLoaded);
	}
}
