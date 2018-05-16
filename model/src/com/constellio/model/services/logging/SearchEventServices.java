package com.constellio.model.services.logging;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDate;

import java.util.*;

import static java.util.Arrays.asList;

public class SearchEventServices {

	ModelLayerFactory modelLayerFactory;

	String collection;

	SchemasRecordsServices schemas;

	static Map<String, Object> incrementCounterMap;

	boolean limit = true;

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

	public void updateDwellTime(String searchEventId, long dwellTime) {

		SolrInputDocument doc = new SolrInputDocument();

		doc.setField("id", searchEventId);
		doc.setField(schemas.searchEvent.dwellTime().getDataStoreCode(), dwellTime);

		BigVaultServerTransaction tx = new BigVaultServerTransaction(RecordsFlushing.ADD_LATER());
		tx.setUpdatedDocuments(asList(doc));
		try {
			modelLayerFactory.getDataLayerFactory().newEventsDao().getBigVaultServer().addAll(tx);
		} catch (BigVaultException e) {
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

	public QueryResponse getFamousRequests(String collection, LocalDate from, LocalDate to, String excludedRequest, Integer offset, Integer limit, String paramsfilter) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.set("rows", "0");
		params.add("fq", "schema_s:" + SearchEvent.SCHEMA_TYPE + "*");
		params.add("fq", "collection_s:" + collection);
		if (StringUtils.isNotBlank(paramsfilter)) {
			params.add("fq", "params_ss:*" + ClientUtils.escapeQueryChars(paramsfilter) + "*");
		}

		computeDateParams(from, to, params);

		computeExcludedRequest(excludedRequest, params);

		String limits = "";
		if (limit != null && limit > 0) {
			limits = ", 'limit': "+limit;
		}

		String offsets = "";
		if (offset != null && offset > 0) {
			offsets = ", 'offset': "+ offset;
		}

		params.add("json.facet", "{'query_s': {'type':'terms', 'field':'query_s'" + offsets + limits + ", 'numBuckets':true, 'facet': {'clickCount_d': 'sum(clickCount_d)'}}}");

		return modelLayerFactory.getDataLayerFactory().newEventsDao().nativeQuery(params);
	}

	public QueryResponse getFamousRequestsWithResults(String collection, LocalDate from, LocalDate to, String excludedRequest, Integer offset, Integer limit, String paramsfilter) {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", "*:*");
			params.set("rows", "0");
			params.add("fq", "numFound_d:[1 TO *]");
			params.add("fq", "schema_s:" + SearchEvent.SCHEMA_TYPE + "*");
			params.add("fq", "collection_s:" + collection);
			if (StringUtils.isNotBlank(paramsfilter)) {
				params.add("fq", "params_ss:*" + ClientUtils.escapeQueryChars(paramsfilter) + "*");
			}

		computeDateParams(from, to, params);

		computeExcludedRequest(excludedRequest, params);

		String limits = "";
		if (limit != null && limit > 0) {
			limits = ", 'limit': "+ limit;
		}

		String offsets = "";
		if (offset != null && offset > 0) {
			offsets = ", 'offset': "+ offset;
		}

		params.add("json.facet", "{'query_s': {'type':'terms', 'field':'query_s'" + offsets + limits + ", 'numBuckets':true, 'facet': {'clickCount_d': 'sum(clickCount_d)'}}}");

		return modelLayerFactory.getDataLayerFactory().newEventsDao().nativeQuery(params);
	}

	public List<String> getMostPopularQueriesAutocomplete(String input, int maxResults, String[] excludedRequests) {
		String escapedInput = ClientUtils.escapeQueryChars(input).toLowerCase();
		String query = "query_s:" + escapedInput + "*";
		if(excludedRequests.length>0){
			query+= " AND NOT query_s:(" + StringUtils.join(excludedRequests, " OR ") + ")";
		}

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", query);
		params.set("rows", 0);
		params.add("fq", "numFound_d:[1 TO *]");
		params.add("fq", "collection_s:" + collection);
		params.add("facet", "true");
		params.add("facet.field", "query_s");
		params.add("facet.limit", String.valueOf(maxResults));

		List<String> queries = new ArrayList<>();
		QueryResponse queryResponse = modelLayerFactory.getDataLayerFactory().newEventsDao().nativeQuery(params);
		for(FacetField.Count count : queryResponse.getFacetField("query_s").getValues()) {
			if(count.getCount()>0){
				queries.add(count.getName());
			}
		}
		return queries;
	}

	public QueryResponse getFamousRequestsWithoutResults(String collection, LocalDate from, LocalDate to, String excludedRequest, Integer offset, Integer limit, String paramsfilter) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.set("rows", "0");
		params.add("fq", "numFound_d:0");
		params.add("fq", "schema_s:" + SearchEvent.SCHEMA_TYPE + "*");
		params.add("fq", "collection_s:" + collection);
		if (StringUtils.isNotBlank(paramsfilter)) {
			params.add("fq", "params_ss:*" + ClientUtils.escapeQueryChars(paramsfilter) + "*");
		}

		computeDateParams(from, to, params);

		computeExcludedRequest(excludedRequest, params);

		String limits = "";
		if (limit != null && limit > 0) {
			limits = ", 'limit': "+ limit;
		}

		String offsets = "";
		if (offset != null && offset > 0) {
			offsets = ", 'offset': "+ offset;
		}

		params.add("json.facet", "{'query_s': {'type':'terms', 'field':'query_s'" + offsets + limits + ", 'numBuckets':true, 'facet': {'clickCount_d': 'sum(clickCount_d)'}}}");

		return modelLayerFactory.getDataLayerFactory().newEventsDao().nativeQuery(params);
	}

	public QueryResponse getFamousRequestsWithClicks(String collection, LocalDate from, LocalDate to, String excludedRequest, Integer offset, Integer limit, String paramsfilter) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.set("rows", "0");
		params.add("fq", "clickCount_d:[1 TO *]");
		params.add("fq", "schema_s:" + SearchEvent.SCHEMA_TYPE + "*");
		params.add("fq", "collection_s:" + collection);
		if (StringUtils.isNotBlank(paramsfilter)) {
			params.add("fq", "params_ss:*" + ClientUtils.escapeQueryChars(paramsfilter) + "*");
		}

		computeDateParams(from, to, params);

		computeExcludedRequest(excludedRequest, params);

		String limits = "";
		if (limit != null && limit > 0) {
			limits = ", 'limit': "+ limit;
		}

		String offsets = "";
		if (offset != null && offset > 0) {
			offsets = ", 'offset': "+ offset;
		}

		params.add("json.facet", "{'query_s': {'type':'terms', 'field':'query_s'" + offsets + limits + ", 'numBuckets':true, 'facet': {'clickCount_d': 'sum(clickCount_d)'}}}");

		return modelLayerFactory.getDataLayerFactory().newEventsDao().nativeQuery(params);
	}

	public QueryResponse getFamousRequestsWithoutClicks(String collection, LocalDate from, LocalDate to, String excludedRequest, Integer offset, Integer limit, String paramsfilter) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.set("rows", "0");
		params.add("fq", "clickCount_d:0");
		params.add("fq", "schema_s:" + SearchEvent.SCHEMA_TYPE + "*");
		params.add("fq", "collection_s:" + collection);
		if (StringUtils.isNotBlank(paramsfilter)) {
			params.add("fq", "params_ss:*" + ClientUtils.escapeQueryChars(paramsfilter) + "*");
		}

		computeDateParams(from, to, params);

		computeExcludedRequest(excludedRequest, params);

		String limits = "";
		if (limit != null && limit > 0) {
			limits = ", 'limit': "+ limit;
		}

		String offsets = "";
		if (offset != null && offset > 0) {
			offsets = ", 'offset': "+ offset;
		}

		params.add("json.facet", "{'query_s': {'type':'terms', 'field':'query_s'" + offsets + limits + ", 'numBuckets':true, 'facet': {'clickCount_d': 'sum(clickCount_d)'}}}");

		return modelLayerFactory.getDataLayerFactory().newEventsDao().nativeQuery(params);
	}

	private void computeDateParams(LocalDate from, LocalDate to, ModifiableSolrParams params) {
		String sFrom = "*";
		if (from != null) {
			sFrom = SolrUtils.convertLocalDateToSolrDate(from);
		}

		String sTo = "*";
		if (to != null) {
			sTo = SolrUtils.convertLocalDateToSolrDate(to);
		}

		params.add("fq", Schemas.CREATED_ON.getDataStoreCode() + ":[" + sFrom + " TO " + sTo + "]");
	}

	private void computeExcludedRequest(String excludedRequest, ModifiableSolrParams params) {
		if (StringUtils.isNotBlank(excludedRequest)) {
			Scanner scanner = new Scanner(excludedRequest);
			while(scanner.hasNextLine()) {
				params.add("fq", "-query_s:" + scanner.nextLine());
			}
		}
	}
}