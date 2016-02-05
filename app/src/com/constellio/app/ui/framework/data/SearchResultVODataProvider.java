package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.cache.SerializableSearchCache;
import com.constellio.model.services.search.cache.SerializedCacheSearchService;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public abstract class SearchResultVODataProvider implements DataProvider {

	private static int DEFAULT_PAGE_SIZE = 10;

	transient LogicalSearchQuery query;
	transient boolean serializeRecords;
	transient Integer size = null;
	//transient Map<Integer, SearchResultVO> cache;
	protected transient ModelLayerFactory modelLayerFactory;
	SerializableSearchCache queryCache = new SerializableSearchCache();
	private transient SessionContext sessionContext;

	RecordToVOBuilder voBuilder;

	private List<DataRefreshListener> dataRefreshListeners = new ArrayList<>();

	public SearchResultVODataProvider(RecordToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory,
			SessionContext sessionContext) {
		this.voBuilder = voBuilder;
		init(modelLayerFactory, sessionContext);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getModelLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}

	void init(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		this.sessionContext = sessionContext;
		this.modelLayerFactory = modelLayerFactory;
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

	public int size() {
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, true);
		if (size == null) {
			size = searchServices.search(query, DEFAULT_PAGE_SIZE).size();
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
		SPEQueryResponse response = searchServices.query(query, DEFAULT_PAGE_SIZE);
		List<Record> records = response.getRecords();
		for (int i = 0; i < Math.min(numberOfItems, records.size()); i++) {
			RecordVO recordVO = voBuilder.build(records.get(startIndex + i), VIEW_MODE.SEARCH, sessionContext);
			SearchResultVO searchResultVO = new SearchResultVO(recordVO, response.getHighlighting(recordVO.getId()));
			results.add(searchResultVO);
		}
		return results;
	}

	public void sort(MetadataVO[] propertyId, boolean[] ascending) {
		query.clearSort();

		for (int i = 0; i < propertyId.length; i++) {
			Metadata metadata;
			MetadataSchema schema = query.getSchemaCondition();
			MetadataVO metadataVO = propertyId[i];
			metadata = schema.getMetadata(metadataVO.getCode());

			if (ascending[i]) {
				query = query.sortAsc(metadata);
			} else {
				query = query.sortDesc(metadata);
			}
		}

	}

	protected abstract LogicalSearchQuery getQuery();
}
