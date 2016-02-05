package com.constellio.app.ui.framework.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

@SuppressWarnings("serial")
public class SearchBoostLazyContainer extends LazyQueryContainer {

	public SearchBoostLazyContainer(SearchBoostDataProvider dataProvider) {
		super(new SearchBoostLazyQueryDefinition(dataProvider),
				new SearchBoostLazyQueryFactory(dataProvider));
	}

	public static class SearchBoostLazyQueryDefinition extends LazyQueryDefinition {

		SearchBoostDataProvider dataProvider;

		public SearchBoostLazyQueryDefinition(SearchBoostDataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			super.addProperty("label", String.class, null, true, true);
			super.addProperty("value", String.class, null, true, true);
		}
	}

	public static class SearchBoostLazyQueryFactory implements QueryFactory, Serializable {

		SearchBoostDataProvider dataProvider;

		public SearchBoostLazyQueryFactory(SearchBoostDataProvider dataProvider) {
			this.dataProvider = dataProvider;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
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
					List<SearchBoostVO> searchBoostVOs = dataProvider.listBoostFields(startIndex, count);
					for (SearchBoostVO searchBoostVO : searchBoostVOs) {
						Item item = new BeanItem<>(searchBoostVO);
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

		private static interface SerializableQuery extends Query, Serializable {
		}

	}

}
