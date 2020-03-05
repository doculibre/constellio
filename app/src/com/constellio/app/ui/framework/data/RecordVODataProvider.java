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
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.services.Stats;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.cache.SerializableSearchCache;
import com.constellio.model.services.search.cache.SerializedCacheSearchService;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.condition.SchemaTypesFilters;
import org.apache.commons.collections4.CollectionUtils;

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
	transient MetadataSchemaVO defaultSchema;
	protected transient ModelLayerFactory modelLayerFactory;
	protected SessionContext sessionContext;
	protected int batchSize = 20;
	private Map<String, RecordToVOBuilder> voBuilders = new HashMap<>();

	private List<MetadataSchemaVO> extraSchemas = new ArrayList<>();

	private String statName;

	@Deprecated
	public RecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder,
								ModelLayerFactory modelLayerFactory) {
		this.defaultSchema = schema;
		this.voBuilders.put(schema.getCode(), voBuilder);
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		this.statName = Stats.getCurrentName();
		init(modelLayerFactory);
	}

	public RecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder,
								SessionContextProvider sessionContextProvider) {
		this.defaultSchema = schema;
		this.voBuilders.put(schema.getCode(), voBuilder);
		this.sessionContext = sessionContextProvider.getSessionContext();
		this.statName = Stats.getCurrentName();
		init(sessionContextProvider.getConstellioFactories().getModelLayerFactory());
	}

	public RecordVODataProvider(MetadataSchemaVO schema, RecordToVOBuilder voBuilder,
								ModelLayerFactory modelLayerFactory,
								SessionContext sessionContext) {
		this.defaultSchema = schema;
		this.voBuilders.put(schema.getCode(), voBuilder);
		this.sessionContext = sessionContext;
		this.statName = Stats.getCurrentName();
		init(modelLayerFactory);
	}

	public RecordVODataProvider(List<MetadataSchemaVO> schemas, Map<String, RecordToVOBuilder> voBuilders,
								ModelLayerFactory modelLayerFactory,
								SessionContext sessionContext) {
		this.defaultSchema = schemas.get(0);
		this.voBuilders = voBuilders;
		this.sessionContext = sessionContext;
		this.statName = Stats.getCurrentName();

		for (int i = 0; i < schemas.size(); i++) {
			MetadataSchemaVO schema = schemas.get(i);
			if (i == 0) {
				this.defaultSchema = schema;
			} else {
				extraSchemas.add(schema);
			}
		}

		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getModelLayerFactory());
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	void init(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;

		query = getQuery();

		query.setLanguage(sessionContext.getCurrentLocale());
		cache = new HashMap<>();
	}

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	private List<RecordVOFilter> filters = new ArrayList<>();

	public void setFilters(List<RecordVOFilter> filters) {
		this.filters = filters;
		initializeQuery();
	}

	private LogicalSearchQuery getFilteredQuery() {
		LogicalSearchQuery query = getQuery();
		if (query != null) {
			for (RecordVOFilter filter : CollectionUtils.emptyIfNull(filters)) {
				filter.addCondition(query);
			}
		}
		return query;
	}

	@Override
	public void fireDataRefreshEvent() {
		initializeQuery();
		super.fireDataRefreshEvent();
	}

	protected void initializeQuery() {
		query = getFilteredQuery();
		query.setLanguage(sessionContext.getCurrentLocale());
		size = null;
		cache.clear();
		queryCache.clear();
	}

	public MetadataSchemaVO getSchema() {
		return defaultSchema;
	}

	public List<MetadataSchemaVO> getExtraSchemas() {
		return extraSchemas;
	}

	private MetadataSchemaVO getSchema(String code) {
		MetadataSchemaVO match = null;
		if (defaultSchema.getCode().equals(code)) {
			match = defaultSchema;
		} else {
			for (MetadataSchemaVO extraSchema : extraSchemas) {
				if (extraSchema.getCode().equals(code)) {
					match = extraSchema;
					break;
				}
			}
		}
		return match;
	}

	public RecordVO getRecordVO(int index) {
		return Stats.compilerFor(statName).log(() -> {
			RecordVO recordVO;
			Record record = cache.get(index);
			if (record == null) {
				List<Record> recordList = doSearch();
				if (!recordList.isEmpty()) {
					record = recordList.get(index);
					cache.put(index, record);
				} else {
					record = null;
				}
			}
			if (record != null) {
				String schemaCode = record.getSchemaCode();
				RecordToVOBuilder voBuilder = getVOBuilder(record);
				recordVO = voBuilder.build(record, VIEW_MODE.TABLE, getSchema(schemaCode), sessionContext);
			} else {
				recordVO = null;
			}
			return recordVO;
		});
	}

	public int size() {
		return Stats.compilerFor(statName).log(() -> {
			if (size == null) {
				size = doSearch().size();
			}
			return size;
		});
	}

	protected List<Record> doSearch() {
		List<Record> recordList;
		if (query == null) {
			recordList = new ArrayList<>();
		} else if (isSearchCache()) {
			query.setNumberOfRows(LogicalSearchQuery.DEFAULT_NUMBER_OF_ROWS);
			query.setLanguage(sessionContext.getCurrentLocale());
			SearchServices searchServices = modelLayerFactory.newSearchServices();
			recordList = searchServices.search(query);
		} else {
			query.setLanguage(sessionContext.getCurrentLocale());
			SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, false);
			recordList = searchServices.search(query, batchSize);
		}
		return recordList;
	}

	public SearchResponseIterator<Record> getIterator() {
		query.setLanguage(sessionContext.getCurrentLocale());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		SearchResponseIterator<Record> searchResponseIterator = searchServices.recordsIterator(query, batchSize);
		return searchResponseIterator;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public List<RecordVO> listRecordVOs(int startIndex, int numberOfItems) {
		List<RecordVO> recordVOs = new ArrayList<>();
		List<Record> recordList = doSearch();
		for (int i = startIndex; i < startIndex + numberOfItems && i < recordList.size(); i++) {
			Record record = recordList.get(i);
			MetadataSchemaVO schema = getSchema(record.getSchemaCode());
			RecordToVOBuilder voBuilder = getVOBuilder(record);
			RecordVO recordVO = voBuilder.build(record, VIEW_MODE.TABLE, schema, sessionContext);
			recordVOs.add(recordVO);
		}
		return recordVOs;
	}

	private RecordToVOBuilder getVOBuilder(Record record) {
		String schemaCode = record.getSchemaCode();
		String typeCode = record.getTypeCode();
		RecordToVOBuilder voBuilder = voBuilders.get(schemaCode);
		if (voBuilder == null) {
			String defaultSchemaCode = typeCode + "_default";
			voBuilder = voBuilders.get(defaultSchemaCode);
		}
		return voBuilder;
	}

	public void sort(MetadataVO[] propertyId, boolean[] ascending) {
		if (query != null) {
			clearSort(query);
			cache.clear();

			for (int i = 0; i < propertyId.length; i++) {
				Metadata metadata;
				MetadataSchema schema;
				DataStoreFilters filters = query.getCondition().getFilters();
				if (filters instanceof SchemaFilters) {
					schema = ((SchemaFilters) filters).getSchema();
				} else {
					schema = ((SchemaTypesFilters) filters).getSchemaTypes().get(0).getDefaultSchema();
				}
				MetadataVO metadataVO = propertyId[i];
				if (schema.hasMetadataWithCode(new SchemaUtils().getLocalCodeFromMetadataCode(metadataVO.getCode()))) {
					metadata = schema.getMetadata(new SchemaUtils().getLocalCodeFromMetadataCode(metadataVO.getCode()));
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

	public abstract LogicalSearchQuery getQuery();

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getQTime() {
		int qtime = queryCache.getTotalQTime();
		queryCache.resetTotalQTime();
		return qtime;
	}

	protected boolean isSearchCache() {
		return false;
	}

	public Map<String, List<FacetValue>> getFieldFacetValues() {
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, true);
		return searchServices.getFieldFacetValues(getQuery());
	}

	public Map<String, Integer> getQueryFacetsValues() {
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, true);
		return searchServices.getQueryFacetsValues(getQuery());
	}

}
