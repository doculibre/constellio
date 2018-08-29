package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.data.DataProvider.DataRefreshListener;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchResultVOLazyContainer extends LazyQueryContainer {
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

	public SearchResultVODataProvider getDataProvider() {
		return dataProvider;
	}

	public RecordVO getRecordVO(int index) {
		return dataProvider.getRecordVO(index);
	}

	public SearchResultVO getSearchResultVO(int index) {
		return dataProvider.getSearchResultVO(index);
	}

	public static class SearchResultVOLazyQueryDefinition extends LazyQueryDefinition {
		SearchResultVODataProvider dataProvider;

		public SearchResultVOLazyQueryDefinition(SearchResultVODataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;
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
						items.add(new BeanItem<>(searchResultVO));
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

	public String getLastCallQTime() {
		return "" + dataProvider.getQTime();
	}
}
