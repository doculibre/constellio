package com.constellio.app.ui.framework.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.SimpleStringFilter;

@SuppressWarnings("serial")
public class FacetValuesLazyContainer extends LazyQueryContainer {

	public FacetValuesLazyContainer(GlobalGroupVODataProvider dataProvider, int batchSize) {
		super(new GlobalGroupVOLazyQueryDefinition(dataProvider, batchSize), new GlobalGroupVOLazyQueryFactory(dataProvider));
	}

	public static class GlobalGroupVOLazyQueryDefinition extends LazyQueryDefinition {

		GlobalGroupVODataProvider dataProvider;

		public GlobalGroupVOLazyQueryDefinition(GlobalGroupVODataProvider dataProvider, int batchSize) {
			super(true, batchSize, null);
			this.dataProvider = dataProvider;

			super.addProperty("code", String.class, null, true, true);
			super.addProperty("name", String.class, null, true, true);
		}
	}

	public static class GlobalGroupVOLazyQueryFactory implements QueryFactory, Serializable {

		GlobalGroupVODataProvider dataProvider;

		public GlobalGroupVOLazyQueryFactory(GlobalGroupVODataProvider dataProvider) {
			this.dataProvider = dataProvider;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
			if (queryDefinition.getFilters().isEmpty()) {
				dataProvider.setFilter(null);
			} else {
				SimpleStringFilter defaultFilter = (SimpleStringFilter) queryDefinition.getFilters().get(0);
				dataProvider.setFilter(defaultFilter.getFilterString());
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
					List<GlobalGroupVO> globalGroupVOs = dataProvider.listGlobalGroupVOs(startIndex, count);
					for (GlobalGroupVO globalGroupVO : globalGroupVOs) {
						Item item = new BeanItem<>(globalGroupVO);
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
