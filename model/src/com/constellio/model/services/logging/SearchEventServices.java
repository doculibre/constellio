package com.constellio.model.services.logging;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;

public class SearchEventServices {

	ModelLayerFactory modelLayerFactory;

	String collection;

	SchemasRecordsServices schemas;

	static Map<String, Object> incrementCounterMap;

	static {
		incrementCounterMap = new HashMap<>();
		incrementCounterMap.put("inc", 1.0);
		incrementCounterMap = Collections.unmodifiableMap(incrementCounterMap);
	}

	public SearchEventServices(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.schemas = new SchemasRecordsServices(collection, modelLayerFactory);
	}

	public void save(SearchEvent searchEvent) {
		save(asList(searchEvent));
	}

	public void save(List<SearchEvent> searchEvents) {
		Transaction tx = new Transaction();
		tx.addAll(searchEvents);
		tx.setRecordFlushing(RecordsFlushing.ADD_LATER());
		try {
			modelLayerFactory.newRecordServices().execute(tx);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	public void incrementClickCounter(String searchEventId) {

		SolrInputDocument doc = new SolrInputDocument();

		doc.setField("id", searchEventId);
		doc.setField(schemas.searchEvent.clickCount().getDataStoreCode(), incrementCounterMap);

		BigVaultServerTransaction tx = new BigVaultServerTransaction(RecordsFlushing.ADD_LATER());
		tx.setUpdatedDocuments(asList(doc));
		try {
			modelLayerFactory.getDataLayerFactory().newEventsDao().getBigVaultServer().addAll(tx);
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}

	}

	public void incrementPageNavigationCounter(String searchEventId) {
		SolrInputDocument doc = new SolrInputDocument();

		doc.setField("id", searchEventId);
		doc.setField(schemas.searchEvent.pageNavigationCount().getDataStoreCode(), incrementCounterMap);

		BigVaultServerTransaction tx = new BigVaultServerTransaction(RecordsFlushing.ADD_LATER());
		tx.setUpdatedDocuments(asList(doc));
		try {
			modelLayerFactory.getDataLayerFactory().newEventsDao().getBigVaultServer().addAll(tx);
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}
	}
}
