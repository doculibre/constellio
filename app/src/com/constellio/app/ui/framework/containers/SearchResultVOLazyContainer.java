package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.DataProvider.DataRefreshListener;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Item;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchResultVOLazyContainer extends LazyQueryContainer implements RecordVOContainer {
	
	private SearchResultVODataProvider dataProvider;

	public SearchResultVOLazyContainer(SearchResultVODataProvider dataProvider) {
		super(new SearchResultVOLazyQueryDefinition(dataProvider), new SearchResultVOLazyQueryFactory(dataProvider));
		this.dataProvider = dataProvider;
		dataProvider.addDataRefreshListener(new DataRefreshListener() {
			@Override
			public void dataRefresh() {
				refresh();
			}
		});
	}

	@Override
	public void forceRefresh() {
		dataProvider.fireDataRefreshEvent();
	}

	public SearchResultVODataProvider getDataProvider() {
		return dataProvider;
	}

	@Override
	public RecordVO getRecordVO(Object itemId) {
		Integer index = (Integer) itemId;
		return dataProvider.getRecordVO(index);
	}

	public SearchResultVO getSearchResultVO(int index) {
		return dataProvider.getSearchResultVO(index);
	}

	public List<MetadataSchemaVO> getSchemas() {
		return getSchemaVOs(dataProvider);
	}

	private static List<MetadataSchemaVO> getSchemaVOs(SearchResultVODataProvider dataProvider) {
		List<MetadataSchemaVO> schemaVOs = new ArrayList<>();
		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		List<MetadataSchema> schemas = dataProvider.getSchemas();
		for (MetadataSchema schema : schemas) {
			MetadataSchemaVO schemaVO = schemaVOBuilder.build(schema, VIEW_MODE.TABLE, sessionContext);
			if (schemas.size() > 1) {
				MetadataVO titleMetadataVO = schemaVO.getMetadata(Schemas.TITLE_CODE);
				MetadataVO modifiedOnMetadataVO = schemaVO.getMetadata(Schemas.MODIFIED_ON.getCode());

				List<MetadataVO> tableMetadataVOs = schemaVO.getTableMetadatas();
				schemaVO.getMetadatas().removeAll(tableMetadataVOs);
				// schemaVO.getMetadatas().clear();
				schemaVO.getMetadatas().add(0, titleMetadataVO);
				schemaVO.getMetadatas().add(1, modifiedOnMetadataVO);
			}
			schemaVOs.add(schemaVO);
			// break;
		}
		return schemaVOs;
	}

	public static class SearchResultVOLazyQueryDefinition extends LazyQueryDefinition {
		SearchResultVODataProvider dataProvider;

		public SearchResultVOLazyQueryDefinition(SearchResultVODataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			List<RecordVODataProvider> recordVODataProviders = new ArrayList<>();

			RecordToVOBuilder voBuilder = new RecordToVOBuilder();
			boolean onlyTableMetadatas;
			List<MetadataSchema> schemas = dataProvider.getSchemas();
			if (schemas.size() == 1) {
				onlyTableMetadatas = RecordVOLazyContainer.isOnlyTableMetadatasShown();
			} else {
				//				onlyTableMetadatas = true;
				onlyTableMetadatas = RecordVOLazyContainer.isOnlyTableMetadatasShown();
			}

			List<MetadataSchemaVO> schemaVOs = getSchemaVOs(dataProvider);
			for (MetadataSchemaVO schemaVO : schemaVOs) {
				RecordVODataProvider recordVODataProvider = new RecordVODataProvider(schemaVO, voBuilder, ConstellioUI.getCurrent()) {
					@Override
					public LogicalSearchQuery getQuery() {
						return dataProvider.getQuery();
					}
				};
				recordVODataProviders.add(recordVODataProvider);
			}

			RecordVOLazyContainer.RecordVOLazyQueryDefinition nestedQueryDefinition = new RecordVOLazyContainer.RecordVOLazyQueryDefinition(recordVODataProviders, onlyTableMetadatas, 100);
			for (Object propertyId : nestedQueryDefinition.getPropertyIds()) {
				Class<?> type = nestedQueryDefinition.getPropertyType(propertyId);
				Object defaultValue = nestedQueryDefinition.getPropertyDefaultValue(propertyId);
				boolean readOnly = nestedQueryDefinition.isPropertyReadOnly(propertyId);
				boolean sortable = nestedQueryDefinition.isPropertySortable(propertyId);
				addProperty(propertyId, type, defaultValue, readOnly, sortable);
			}
		}
	}

	public static class SearchResultVOLazyQueryFactory implements QueryFactory, Serializable {
		SearchResultVODataProvider dataProvider;

		public SearchResultVOLazyQueryFactory(SearchResultVODataProvider dataProvider) {
			this.dataProvider = dataProvider;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
			Object[] sortPropertyIds = queryDefinition.getSortPropertyIds();
			if (sortPropertyIds != null && sortPropertyIds.length > 0) {
				List<MetadataVO> sortMetadatas = new ArrayList<MetadataVO>();
				for (int i = 0; i < sortPropertyIds.length; i++) {
					MetadataVO sortMetadata = (MetadataVO) sortPropertyIds[i];
					sortMetadatas.add(sortMetadata);
				}
				dataProvider.sort(sortMetadatas.toArray(new MetadataVO[0]), queryDefinition.getSortPropertyAscendingStates());
			}
			return new SerializableQuery() {
				@Override
				public int size() {
					return dataProvider.size();
				}

				@Override
				public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
					throw new UnsupportedOperationException("Query is read-only");
				}

				@Override
				public List<Item> loadItems(int startIndex, int count) {
					List<Item> items = new ArrayList<>();
					List<SearchResultVO> result = dataProvider.listSearchResultVOs(startIndex, count);
					for (SearchResultVO searchResultVO : result) {
						items.add(new RecordVOItem(searchResultVO));
					}
					return items;
				}

				@Override
				public boolean deleteAllItems() {
					throw new UnsupportedOperationException("Query is read-only");
				}

				@Override
				public Item constructItem() {
					throw new UnsupportedOperationException("Query is read-only");
				}
			};
		}

		private static interface SerializableQuery extends Query, Serializable {
		}
	}

	public double getLastCallQTime() {
		int total = dataProvider.getQTime();

		double totalInSeconds;
		if (total < 10) {
			totalInSeconds = total / 1000.0;
		} else {
			totalInSeconds = Math.round(total / 10.0) / 100.0;
		}

		return totalInSeconds;
	}
}
