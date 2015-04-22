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
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

@SuppressWarnings("serial")
public abstract class RecordVODataProvider implements DataProvider {

	transient LogicalSearchQuery query;

	transient SearchServices searchServices;

	transient Integer size = null;

	transient Map<Integer, Record> cache;

	transient MetadataSchemaVO schema;

	protected transient ModelLayerFactory modelLayerFactory;

	RecordToVOBuilder voBuilder;

	private List<DataRefreshListener> dataRefreshListeners = new ArrayList<DataRefreshListener>();

	SessionContext sessionContext;

	@Deprecated
	public RecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory) {
		this.schema = schema;
		this.voBuilder = voBuilder;
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		init(modelLayerFactory);
	}

	public RecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		this.schema = schema;
		this.voBuilder = voBuilder;
		this.sessionContext = sessionContext;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
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

	public MetadataSchemaVO getSchema() {
		return schema;
	}

	public RecordVO getRecordVO(int index) {
		Record record = cache.get(index);
		if (record == null) {
			List<Record> recordList = searchServices.search(query.setStartRow(index).setNumberOfRows(1));
			if (!recordList.isEmpty()) {
				record = recordList.get(0);
				cache.put(index, record);
			} else {
				record = null;
			}
		}
		return record != null ? voBuilder.build(record, VIEW_MODE.TABLE, schema, sessionContext) : null;
	}

	public int size() {
		if (size == null) {
			size = new Long(searchServices.query(query).getNumFound()).intValue();
		}
		return size;
	}

	public List<Integer> list(int startIndex, int numberOfItems) {
		List<Integer> indexes = new ArrayList<>();
		List<Record> recordList = searchServices.search(query.setStartRow(startIndex).setNumberOfRows(numberOfItems));
		for (int i = startIndex; i < numberOfItems && (startIndex + i) < size(); i++) {
			indexes.add(i);
			Record record = recordList.get(i);
			cache.put(i, record);
		}
		return indexes;
	}

	public List<RecordVO> listRecordVOs(int startIndex, int numberOfItems) {
		List<RecordVO> recordVOs = new ArrayList<>();
		List<Record> recordList = searchServices.search(query.setStartRow(startIndex).setNumberOfRows(numberOfItems));
		for (int i = 0; i < numberOfItems && i < recordList.size(); i++) {
			Record record = recordList.get(i);
			RecordVO recordVO = voBuilder.build(record, VIEW_MODE.TABLE, schema, sessionContext);
			recordVOs.add(recordVO);
		}
		return recordVOs;
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
