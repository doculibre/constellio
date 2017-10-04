package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.cache.SerializableSearchCache;
import com.constellio.model.services.search.cache.SerializedCacheSearchService;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQuerySort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class RecordVODataProvider extends AbstractDataProvider {
	
	SerializableSearchCache queryCache = new SerializableSearchCache();
	transient LogicalSearchQuery query;
	transient Integer size = null;
	transient Map<Integer, Record> cache;
	transient MetadataSchemaVO schema;
	protected transient ModelLayerFactory modelLayerFactory;
	RecordToVOBuilder voBuilder;
	SessionContext sessionContext;
	private int batchSize = 20;

	@Deprecated
	public RecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory) {
		this.schema = schema;
		this.voBuilder = voBuilder;
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		init(modelLayerFactory);
	}

	public RecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder,
			SessionContextProvider sessionContextProvider) {
		this.schema = schema;
		this.voBuilder = voBuilder;
		this.sessionContext = sessionContextProvider.getSessionContext();
		init(sessionContextProvider.getConstellioFactories().getModelLayerFactory());
	}

	public RecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory,
			SessionContext sessionContext) {
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

		query = getQuery();
		cache = new HashMap<>();
	}

	@Override
	public void fireDataRefreshEvent() {
		query = getQuery();
		size = null;
		cache.clear();
		queryCache.clear();
		super.fireDataRefreshEvent();
	}

	public MetadataSchemaVO getSchema() {
		return schema;
	}

	public RecordVO getRecordVO(int index) {
		Record record = cache.get(index);
		if (record == null) {
			SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, false);
			List<Record> recordList = searchServices.search(query, batchSize);
			if (!recordList.isEmpty()) {
				record = recordList.get(index);
				cache.put(index, record);
			} else {
				record = null;
			}
		}
		return record != null ? voBuilder.build(record, VIEW_MODE.TABLE, schema, sessionContext) : null;
	}

	public int size() {
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, false);
		if (size == null) {
			size = searchServices.search(query, batchSize).size();
		}
		return size;
	}

	public List<RecordVO> listRecordVOs(int startIndex, int numberOfItems) {
		List<RecordVO> recordVOs = new ArrayList<>();
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, false);
		List<Record> recordList = searchServices.search(query, batchSize);
		for (int i = startIndex; i < startIndex + numberOfItems && i < recordList.size(); i++) {
			Record record = recordList.get(i);
			RecordVO recordVO = voBuilder.build(record, VIEW_MODE.TABLE, schema, sessionContext);
			recordVOs.add(recordVO);
		}
		return recordVOs;
	}

	public void sort(MetadataVO[] propertyId, boolean[] ascending) {
		clearSort(query);
		cache.clear();

		for (int i = 0; i < propertyId.length; i++) {
			Metadata metadata;
			MetadataSchema schema = query.getSchemaCondition();
			MetadataVO metadataVO = propertyId[i];
			if(schema.hasMetadataWithCode(new SchemaUtils().getLocalCodeFromMetadataCode(metadataVO.getCode()))) {
				metadata = schema.getMetadata(new SchemaUtils().getLocalCodeFromMetadataCode(metadataVO.getCode()));
				if(metadata.getType() == MetadataValueType.REFERENCE && metadata.isSortable() && !metadata.isMultivalue()) {
					LogicalSearchQuerySort sortField = new LogicalSearchQuerySort(metadata.getLocalCode() + ".caption_s", ascending[i]);
					query.sortOn(sortField);
				} else {
					if (ascending[i]) {
						query = query.sortAsc(metadata);
					} else {
						query = query.sortDesc(metadata);
					}
				}
			}
		}
	}

	protected void clearSort(LogicalSearchQuery query) {
		query.clearSort();
	}

	protected abstract LogicalSearchQuery getQuery();

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
