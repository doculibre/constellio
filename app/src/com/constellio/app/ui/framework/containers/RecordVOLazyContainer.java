package com.constellio.app.ui.framework.containers;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.containers.exception.ContainerException.ContainerException_ItemListChanged;
import com.constellio.app.ui.framework.data.DataProvider.DataRefreshListener;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.RecordVOFilter;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.Item;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class RecordVOLazyContainer extends LazyQueryContainer implements RecordVOContainer {

	private List<RecordVODataProvider> dataProviders;

	public RecordVOLazyContainer(RecordVODataProvider dataProvider, boolean isOnlyTableMetadatasShown) {
		this(Arrays.asList(dataProvider), 100, isOnlyTableMetadatasShown);
	}

	public RecordVOLazyContainer(RecordVODataProvider dataProvider) {
		this(Arrays.asList(dataProvider));
	}

	public RecordVOLazyContainer(List<RecordVODataProvider> dataProviders) {
		this(dataProviders, 100);
	}

	public RecordVOLazyContainer(List<RecordVODataProvider> dataProviders, int batchSize) {
		this(dataProviders, batchSize, isOnlyTableMetadatasShown());
	}

	public RecordVOLazyContainer(List<RecordVODataProvider> dataProviders, int batchSize,
								 boolean isOnlyTableMetadatasShown) {
		super(new RecordVOLazyQueryDefinition(dataProviders, isOnlyTableMetadatasShown, batchSize),
				new RecordVOLazyQueryFactory(dataProviders));
		this.dataProviders = dataProviders;
		for (RecordVODataProvider dataProvider : dataProviders) {
			dataProvider.setBatchSize(batchSize);
		}
		for (RecordVODataProvider dataProvider : dataProviders) {
			dataProvider.addDataRefreshListener(new DataRefreshListener() {
				@Override
				public void dataRefresh() {
					RecordVOLazyContainer.this.refresh();
				}
			});
		}
	}

	@Override
	public void forceRefresh() {
		for (RecordVODataProvider dataProvider : dataProviders) {
			dataProvider.fireDataRefreshEvent();
		}
	}

	public List<RecordVODataProvider> getDataProviders() {
		return dataProviders;
	}

	public static boolean isOnlyTableMetadatasShown() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ConstellioEIMConfigs configs = new ConstellioEIMConfigs(
				constellioFactories.getModelLayerFactory().getSystemConfigurationsManager());
		return !configs.isTableDynamicConfiguration();
	}

	public List<MetadataSchemaVO> getSchemas() {
		List<MetadataSchemaVO> schemas = new ArrayList<>();
		for (RecordVODataProvider dataProvider : dataProviders) {
			schemas.add(dataProvider.getSchema());
			schemas.addAll(dataProvider.getExtraSchemas());
		}
		return schemas;
	}

	@Override
	public Map<String, List<String>> getHighlights(Object itemId) {
		Integer index = (Integer) itemId;
		RecordVODataProviderAndRecordIndex dataProviderAndRecordIndex = forRecordIndex(dataProviders, index);
		return dataProviderAndRecordIndex.dataProvider.getHighlighting(index);
	}

	private static RecordVODataProviderAndRecordIndex forRecordIndex(List<RecordVODataProvider> dataProviders,
																	 int index) {
		RecordVODataProviderAndRecordIndex result = null;
		int lastSize = 0;
		for (RecordVODataProvider dataProvider : dataProviders) {
			int dataProviderSize = dataProvider.size();
			if ((lastSize + dataProviderSize) > index) {
				int actualIndex = index - lastSize;
				result = new RecordVODataProviderAndRecordIndex(dataProvider, actualIndex);
				break;
			}
			lastSize += dataProviderSize;
		}
		return result;
	}

	@Override
	public RecordVO getRecordVO(Object itemId) {
		Integer index = (Integer) itemId;
		RecordVODataProviderAndRecordIndex dataProviderAndRecordIndex = forRecordIndex(dataProviders, index);
		int recordIndexForDataProvider = dataProviderAndRecordIndex.recordIndex;
		return dataProviderAndRecordIndex.dataProvider.getRecordVO(recordIndexForDataProvider);
	}

	public double getLastCallQTime() {
		int total = 0;
		for (RecordVODataProvider dataProvider : dataProviders) {
			total += dataProvider.getQTime();
		}

		double totalInSeconds;
		if (total < 10) {
			totalInSeconds = total / 1000.0;
		} else {
			totalInSeconds = Math.round(total / 10.0) / 100.0;
		}

		return totalInSeconds;
	}

	private static class RecordVODataProviderAndRecordIndex implements Serializable {

		private RecordVODataProvider dataProvider;

		private int recordIndex;

		public RecordVODataProviderAndRecordIndex(RecordVODataProvider dataProvider, int recordIndex) {
			this.dataProvider = dataProvider;
			this.recordIndex = recordIndex;
		}

	}

	public static class RecordVOLazyQueryDefinition extends LazyQueryDefinition {

		List<RecordVODataProvider> dataProviders;

		/**
		 * final boolean compositeItems, final int batchSize, final Object idPropertyId
		 * <p>
		 * //@param dataProviders
		 * //@param compositeItems
		 * //@param batchSize
		 * //@param idPropertyId
		 */
		public RecordVOLazyQueryDefinition(List<RecordVODataProvider> dataProviders, boolean tableMetadatasOnly,
										   int batchSize) {
			super(true, batchSize, null);
			this.dataProviders = dataProviders;

			List<MetadataVO> propertyMetadataVOs = new ArrayList<>();
			List<MetadataVO> tablePropertyMetadataVOs = new ArrayList<>();
			List<MetadataVO> extraPropertyMetadataVOs = new ArrayList<>();
			List<MetadataVO> queryMetadataVOs = new ArrayList<>();
			ModelLayerFactory modelLayerFactory = null;
			SessionContext sessionContext;
			UserServices userServices;
			MetadataSchemasManager metadataSchemasManager;
			MetadataSchemaTypes metadataTypes = null;
			User user = null;

			for (RecordVODataProvider dataProvider : dataProviders) {
				if (modelLayerFactory == null) {
					modelLayerFactory = dataProviders.get(0).getModelLayerFactory();
					sessionContext = dataProviders.get(0).getSessionContext();
					userServices = modelLayerFactory.newUserServices();
					metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
					metadataTypes = metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection());
					user = userServices.getUserInCollection(sessionContext.getCurrentUser().getUsername(),
							sessionContext.getCurrentCollection());
				}

				List<MetadataSchemaVO> dataProviderSchemaVOs = new ArrayList<>();
				MetadataSchemaVO dataProviderDefaultSchema = dataProvider.getSchema();
				dataProviderSchemaVOs.add(dataProviderDefaultSchema);
				dataProviderSchemaVOs.addAll(dataProvider.getExtraSchemas());

				for (MetadataSchemaVO schemaVO : dataProviderSchemaVOs) {
					List<MetadataVO> schemaVOTableMetadataVOs = schemaVO.getTableMetadatas();

					for (MetadataVO metadataVO : schemaVOTableMetadataVOs) {
						tablePropertyMetadataVOs.add(metadataVO);
					}

					List<MetadataVO> dataProviderQueryMetadataVOs = new ArrayList<>(schemaVOTableMetadataVOs);
					if (!tableMetadatasOnly) {

						List<MetadataVO> dataProviderDisplayMetadataVOs = schemaVO.getDisplayMetadatas();
						for (MetadataVO metadataVO : dataProviderDisplayMetadataVOs) {
							if (!dataProviderQueryMetadataVOs.contains(metadataVO)) {
								dataProviderQueryMetadataVOs.add(metadataVO);
							}
						}
					}
					for (MetadataVO metadataVO : dataProviderQueryMetadataVOs) {
						if (!queryMetadataVOs.contains(metadataVO)) {
							if (schemaVOTableMetadataVOs.contains(metadataVO)) {
								tablePropertyMetadataVOs.add(metadataVO);
							} else {
								extraPropertyMetadataVOs.add(metadataVO);
							}
						}
					}
				}
			}

			Collections.sort(extraPropertyMetadataVOs, new Comparator<MetadataVO>() {
				@Override
				public int compare(MetadataVO o1, MetadataVO o2) {
					if (o1.getLabel() == null || o2.getLabel() == null) {
						return -1;
					}
					return o1.getLabel().compareTo(o2.getLabel());
				}
			});
			propertyMetadataVOs.addAll(tablePropertyMetadataVOs);
			propertyMetadataVOs.addAll(extraPropertyMetadataVOs);

			for (MetadataVO metadataVO : propertyMetadataVOs) {
				if (user.hasGlobalAccessToMetadata(metadataTypes.getMetadata(metadataVO.getCode()))) {
					super.addProperty(metadataVO, metadataVO.getJavaType(), null, true, true);
				}
			}
		}

	}

	public static class RecordVOLazyQueryFactory implements QueryFactory, Serializable {

		List<RecordVODataProvider> dataProviders;

		public RecordVOLazyQueryFactory(List<RecordVODataProvider> dataProviders) {
			this.dataProviders = dataProviders;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
			List<RecordVOFilter> filters = new ArrayList<>();
			List<Filter> queryDefinitionFilters = queryDefinition.getFilters();
			if (queryDefinitionFilters != null) {
				for (Filter filter : queryDefinitionFilters) {
					if (filter instanceof RecordVOFilter) {
						filters.add((RecordVOFilter) filter);
					}
				}
			}

			if (!filters.isEmpty()) {
				for (RecordVODataProvider dataProvider : dataProviders) {
					dataProvider.setFilters(filters);
				}
			}

			Object[] sortPropertyIds = queryDefinition.getSortPropertyIds();
			if (sortPropertyIds != null && sortPropertyIds.length > 0) {
				List<MetadataVO> sortMetadatas = new ArrayList<MetadataVO>();
				for (int i = 0; i < sortPropertyIds.length; i++) {
					if (sortPropertyIds[i] instanceof MetadataVO) {
						MetadataVO sortMetadata = (MetadataVO) sortPropertyIds[i];
						sortMetadatas.add(sortMetadata);
					}
				}
				for (RecordVODataProvider dataProvider : dataProviders) {
					dataProvider.sort(sortMetadatas.toArray(new MetadataVO[0]), queryDefinition.getSortPropertyAscendingStates());
				}
			}
			return new SerializableQuery() {
				@Override
				public int size() {
					int totalSizes = 0;
					for (RecordVODataProvider dataProvider : dataProviders) {
						totalSizes += dataProvider.size();
					}
					return totalSizes;
				}

				@Override
				public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
					throw new UnsupportedOperationException("Query is read-only");
				}

				@Override
				public List<Item> loadItems(int startIndex, int count) {
					List<Item> items = new ArrayList<Item>();

					RecordVODataProviderAndRecordIndex dataProviderAndRecordIndex = forRecordIndex(dataProviders, startIndex);
					RecordVODataProvider firstDataProvider = dataProviderAndRecordIndex.dataProvider;
					int startIndexForFirstDataProvider = dataProviderAndRecordIndex.recordIndex;

					List<RecordVO> recordVOsFromFirstDataProvider = firstDataProvider
							.listRecordVOs(startIndexForFirstDataProvider, count);
					for (RecordVO recordVO : recordVOsFromFirstDataProvider) {
						Item item = new RecordVOItem(recordVO);
						items.add(item);
					}

					if (items.size() < count) {
						// We need to add results from extra dataProviders
						boolean firstDataProviderFound = false;
						for (RecordVODataProvider dataProvider : dataProviders) {
							if (dataProvider.equals(firstDataProvider)) {
								firstDataProviderFound = true;
							} else if (firstDataProviderFound) {
								// Only records belonging to dataProviders after the first are relevant
								int startIndexForDataProvider = 0;
								int countForDataProvider = count - items.size();
								List<RecordVO> recordVOsFromDataProvider = dataProvider
										.listRecordVOs(startIndexForDataProvider, countForDataProvider);
								for (RecordVO recordVO : recordVOsFromDataProvider) {
									Item item = new RecordVOItem(recordVO);
									items.add(item);
								}
								if (items.size() >= count) {
									break;
								}
							}
						}
					}

					return items;
				}

				@Override
				public boolean deleteAllItems() {
					throw new UnsupportedOperationException("Query is read-only");
				}

				@Override
				public Item constructItem() {
					throw new ContainerException_ItemListChanged();
				}
			};
		}

		private interface SerializableQuery extends Query, Serializable {

		}
	}
}
