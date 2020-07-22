package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.cache.SerializableSearchCache;
import com.constellio.model.services.search.cache.SerializedCacheSearchService;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.condition.SchemaTypesFilters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class SearchResultVODataProvider implements DataProvider {

	protected transient LogicalSearchQuery query;
	transient boolean serializeRecords;
	protected transient Integer size = null;
	//transient Map<Integer, SearchResultVO> cache;
	protected transient ModelLayerFactory modelLayerFactory;
	protected transient AppLayerFactory appLayerFactory;
	protected SerializableSearchCache queryCache = new SerializableSearchCache();
	private transient SessionContext sessionContext;
	protected Supplier<Integer> resultsPerPageSupplier;

	RecordToVOBuilder voBuilder;

	private List<DataRefreshListener> dataRefreshListeners = new ArrayList<>();

	public SearchResultVODataProvider(RecordToVOBuilder voBuilder, AppLayerFactory appLayerFactory,
									  SessionContext sessionContext, int resultsPerPage) {
		this.voBuilder = voBuilder;
		//		String username = sessionContext.getCurrentUser().getUsername();
		//
		//		SolrUserCredential userCredential = (SolrUserCredential) appLayerFactory.getModelLayerFactory().getUserCredentialsManager()
		//				.getUserCredential(username);

		this.resultsPerPageSupplier = () -> resultsPerPage;
		init(appLayerFactory, sessionContext);
	}

	public SearchResultVODataProvider(RecordToVOBuilder voBuilder, AppLayerFactory appLayerFactory,
									  SessionContext sessionContext, Supplier<Integer> resultsPerPageSupplier) {
		this.voBuilder = voBuilder;
		//		String username = sessionContext.getCurrentUser().getUsername();
		//
		//		SolrUserCredential userCredential = (SolrUserCredential) appLayerFactory.getModelLayerFactory().getUserCredentialsManager()
		//				.getUserCredential(username);

		this.resultsPerPageSupplier = resultsPerPageSupplier;
		init(appLayerFactory, sessionContext);
	}

	public int getResultsPerPage() {
		return resultsPerPageSupplier.get();
	}

	public Map<String, List<FacetValue>> getFieldFacetValues() {
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, true);
		return searchServices.getFieldFacetValues(query);
	}

	public Map<String, Integer> getQueryFacetsValues() {
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, true);
		return searchServices.getQueryFacetsValues(query);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getAppLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}

	void init(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		this.sessionContext = sessionContext;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		query = getQuery();
		serializeRecords = !query.getReturnedMetadatas().isFullyLoaded();
	}

	public void addDataRefreshListener(DataRefreshListener dataRefreshListener) {
		this.dataRefreshListeners.add(dataRefreshListener);
	}

	public List<DataRefreshListener> getDataRefreshListeners() {
		return dataRefreshListeners;
	}

	public void removeDataRefreshListener(DataRefreshListener dataRefreshListener) {
		dataRefreshListeners.remove(dataRefreshListener);
	}

	public void fireDataRefreshEvent() {
		size = null;
		for (DataRefreshListener dataRefreshListener : dataRefreshListeners) {
			dataRefreshListener.dataRefresh();
		}
	}

	public SearchResultVO getSearchResultVO(int index) {
		List<SearchResultVO> found = listSearchResultVOs(index, 1);
		SearchResultVO result = found.size() > 0 ? found.get(0) : null;

		return result;
	}

	public RecordVO getRecordVO(int index) {
		SearchResultVO searchResultVO = getSearchResultVO(index);
		return searchResultVO != null ? searchResultVO.getRecordVO() : null;
	}

	public List<RecordVO> getRecordsVO(List<Integer> indexes) {

		int currentStart = -1;
		int currentEnd = -1;
		List<RecordVO> recordVOS = new ArrayList<>();

		for (int i = 0; i < indexes.size(); i++) {
			if (currentStart == -1) {
				currentStart = indexes.get(i);
				currentEnd = indexes.get(i);

			} else if (currentEnd + 1 == indexes.get(i)) {
				currentEnd++;

			} else {
				for (SearchResultVO searchResultVO : listSearchResultVOs(currentStart, currentEnd - currentStart + 1)) {
					recordVOS.add(searchResultVO.getRecordVO());
				}
				currentStart = indexes.get(i);
				currentEnd = indexes.get(i);
			}
		}

		if (currentStart != -1) {
			for (SearchResultVO searchResultVO : listSearchResultVOs(currentStart, currentEnd - currentStart + 1)) {
				recordVOS.add(searchResultVO.getRecordVO());
			}
		}

		return recordVOS;
	}

	public int size() {
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, true);
		if (size == null) {
			SPEQueryResponse response = searchServices.query(query, resultsPerPageSupplier.get());

			size = response.getRecords().size();
		}
		return size;
	}

	public List<Integer> list(int startIndex, int numberOfItems) {
		List<Integer> indexes = new ArrayList<>();
		List<SearchResultVO> results = listSearchResultVOs(startIndex, numberOfItems);
		for (int i = startIndex; (startIndex + i) < results.size(); i++) {
			indexes.add(i);
		}
		return indexes;
	}

	public List<SearchResultVO> listSearchResultVOs(int startIndex, int numberOfItems) {
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, true);
		List<SearchResultVO> results = new ArrayList<>(numberOfItems);

		SPEQueryResponse response = searchServices.query(query, Math.max(resultsPerPageSupplier.get(), numberOfItems));
		onQuery(query, response);
		List<Record> records = response.getRecords();
		List<Record> subListOfRecords = records.subList(startIndex, Math.min(startIndex + numberOfItems, records.size()));

		int searchResultIndex = startIndex;
		for (Record recordId : subListOfRecords) {
			Record recordSummary = null;
			boolean recordNotFound = false;
			try {
				recordSummary = modelLayerFactory.newRecordServices().realtimeGetRecordSummaryById(recordId.getId());
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				recordNotFound = true;
			}
			if (!recordNotFound) {
				RecordVO recordVO = voBuilder.build(recordSummary, VIEW_MODE.SEARCH, sessionContext);
				SearchResultVO searchResultVO = new SearchResultVO(searchResultIndex, recordVO, response.getHighlighting(recordVO.getId()));
				results.add(searchResultVO);
			} else {
				results.add(new SearchResultVO(searchResultIndex, true));
			}

			searchResultIndex++;
		}

		return results;
	}

	protected void onQuery(LogicalSearchQuery query, SPEQueryResponse response) {
	}

	public void sort(MetadataVO[] propertyId, boolean[] ascending) {
		query.clearSort();

		List<MetadataSchema> schemas = getSchemas();
		MetadataSchema schema = !schemas.isEmpty() ? schemas.get(0) : null;
		if (schema != null) {
			for (int i = 0; i < propertyId.length; i++) {
				Metadata metadata;
				MetadataVO metadataVO = propertyId[i];
				metadata = schema.getMetadata(metadataVO.getCode());

				if (ascending[i]) {
					query = query.sortAsc(metadata);
				} else {
					query = query.sortDesc(metadata);
				}
			}
		}

	}

	public int getQTime() {
		int qtime = queryCache.getTotalQTime();
		queryCache.resetTotalQTime();
		return qtime;
	}

	public int getResultsCount() {
		int resultsCount = queryCache.getSize();
		return resultsCount;
	}

	public List<MetadataSchema> getSchemas() {
		List<MetadataSchema> schemas;
		DataStoreFilters filters = query.getCondition().getFilters();
		if (filters instanceof SchemaFilters) {
			MetadataSchema schema = ((SchemaFilters) query.getCondition().getFilters()).getSchema();
			schemas = Arrays.asList(schema);
		} else {
			schemas = new ArrayList<>();
			List<MetadataSchemaType> schemaTypes = ((SchemaTypesFilters) query.getCondition().getFilters()).getSchemaTypes();
			for (MetadataSchemaType schemaType : schemaTypes) {
				MetadataSchema schema = schemaType.getDefaultSchema();
				schemas.add(schema);
			}
		}
		return schemas;
	}

	public abstract LogicalSearchQuery getQuery();
}
