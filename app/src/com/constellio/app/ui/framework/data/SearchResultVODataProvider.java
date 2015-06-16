/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public abstract class SearchResultVODataProvider implements DataProvider {
	transient LogicalSearchQuery query;
	transient SearchServices searchServices;
	transient Integer size = null;
	transient Map<Integer, SearchResultVO> cache;
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
		searchServices = modelLayerFactory.newSearchServices();
		query = getQuery();
		cache = new HashMap<>();
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
		cache.clear();
		for (DataRefreshListener dataRefreshListener : dataRefreshListeners) {
			dataRefreshListener.dataRefresh();
		}
	}

	public SearchResultVO getSearchResultVO(int index) {
		SearchResultVO searchResultVO = cache.get(index);
		if (searchResultVO != null) {
			return searchResultVO;
		}
		List<SearchResultVO> found = listSearchResultVOs(index, 1);
		return found.size() > 0 ? found.get(0) : null;
	}

	public RecordVO getRecordVO(int index) {
		SearchResultVO searchResultVO = getSearchResultVO(index);
		return searchResultVO != null ? searchResultVO.getRecordVO() : null;
	}

	public int size() {
		if (size == null) {
			size = new Long(searchServices.getResultsCount(query)).intValue();
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
		List<SearchResultVO> results = new ArrayList<>(numberOfItems);
		SPEQueryResponse response = searchServices.query(query.setStartRow(startIndex).setNumberOfRows(numberOfItems));
		List<Record> records = response.getRecords();
		Map<String, Map<String, List<String>>> highlights = response.getHighlights();
		for (int i = 0; i < records.size(); i++) {
			RecordVO recordVO = voBuilder.build(records.get(i), VIEW_MODE.TABLE, sessionContext);
			SearchResultVO searchResultVO = new SearchResultVO(recordVO, highlights.get(recordVO.getId()));
			results.add(searchResultVO);
			cache.put(startIndex + i, searchResultVO);
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
