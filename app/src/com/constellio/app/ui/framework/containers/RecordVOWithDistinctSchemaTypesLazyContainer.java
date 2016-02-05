package com.constellio.app.ui.framework.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.DataProvider.DataRefreshListener;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;

// AFTER: Rename to MultitypeRecordVOContainer
@SuppressWarnings("serial")
public class RecordVOWithDistinctSchemaTypesLazyContainer extends LazyQueryContainer {

	private RecordVOWithDistinctSchemasDataProvider dataProvider;

	public RecordVOWithDistinctSchemaTypesLazyContainer(RecordVOWithDistinctSchemasDataProvider dataProvider,
			List<String> reportMetadata) {
		super(new RecordVOLazyQueryDefinition(dataProvider, reportMetadata),
				new RecordVOLazyQueryFactory(dataProvider, reportMetadata));
		this.dataProvider = dataProvider;
		dataProvider.addDataRefreshListener(new DataRefreshListener() {
			@Override
			public void dataRefresh() {
				RecordVOWithDistinctSchemaTypesLazyContainer.this.refresh();
			}
		});
	}

	public RecordVO getRecordVO(int index) {
		return dataProvider.getRecordVO(index);
	}

	public static class RecordVOLazyQueryDefinition extends LazyQueryDefinition {

		RecordVOWithDistinctSchemasDataProvider dataProvider;

		/**
		 * final boolean compositeItems, final int batchSize, final Object idPropertyId
		 *
		 * //@param dataProvider
		 * //@param compositeItems
		 * //@param batchSize
		 * //@param idPropertyId
		 */
		public RecordVOLazyQueryDefinition(RecordVOWithDistinctSchemasDataProvider dataProvider,
				List<String> reportMetadataList) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			List<String> propertiesIds = new ArrayList<>();
			for (String reportMetadata : reportMetadataList) {
				if (!propertiesIds.contains(reportMetadata)) {
					for (MetadataSchemaVO metadataSchemaVO : dataProvider.getSchemas()) {
						MetadataVO metadata = metadataSchemaVO.getMetadata(reportMetadata);
						propertiesIds.add(metadata.getLocalCode());
						super.addProperty(metadata.getLocalCode(), metadata.getJavaType(), null, true, true);
					}
				}
			}
		}
	}

	public static class RecordVOLazyQueryFactory implements QueryFactory, Serializable {

		RecordVOWithDistinctSchemasDataProvider dataProvider;
		List<String> reportMetadataList;

		public RecordVOLazyQueryFactory(RecordVOWithDistinctSchemasDataProvider dataProvider, List<String> reportMetadataList) {
			this.dataProvider = dataProvider;
			this.reportMetadataList = reportMetadataList;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
			MetadataSchemaVO schemaVO = dataProvider.getSchemas().get(0);
			Object[] sortPropertyIds = queryDefinition.getSortPropertyIds();
			if (sortPropertyIds != null && sortPropertyIds.length > 0) {
				List<MetadataVO> sortMetadatas = new ArrayList<MetadataVO>();
				for (int i = 0; i < sortPropertyIds.length; i++) {
					String sortMetadataCode = (String) sortPropertyIds[i];
					sortMetadatas.add(schemaVO.getMetadata(sortMetadataCode));
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
					List<Item> items = new ArrayList<Item>();
					List<RecordVO> recordVOs = dataProvider.listRecordVOs(startIndex, count);
					for (RecordVO recordVO : recordVOs) {
						Item item = new BeanItem<>(recordVO);
						List<String> propertiesIds = new ArrayList<>();
						for (String reportMetadata : reportMetadataList) {
							if (!propertiesIds.contains(reportMetadata)) {
								String schemaCode = recordVO.getSchema().getCode();
								Object value = recordVO.get(schemaCode + "_" + reportMetadata);
								if (value == null) {
									value = "";
								}
								item.addItemProperty(reportMetadata,
										new ObjectProperty<>(value));
							}
						}
						items.add(item);
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

		private interface SerializableQuery extends Query, Serializable {

		}
	}
}
