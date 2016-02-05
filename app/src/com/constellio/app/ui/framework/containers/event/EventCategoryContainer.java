package com.constellio.app.ui.framework.containers.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.pages.events.EventsCategoryDataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class EventCategoryContainer extends LazyQueryContainer {
	public static final String LABEL = "label";
	public static final String VALUE = "value";

	public EventCategoryContainer(EventsCategoryDataProvider dataProvider) {
		super(new EventStatisticsVOLazyQueryDefinition(dataProvider), new EventStatisticsVOLazyQueryFactory(dataProvider));
	}

	public static class EventStatisticsVOLazyQueryDefinition extends LazyQueryDefinition {

		EventsCategoryDataProvider dataProvider;

		public EventStatisticsVOLazyQueryDefinition(EventsCategoryDataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			super.addProperty(LABEL, String.class, null, true, true);
			super.addProperty(VALUE, Float.class, null, true, true);
		}
	}

	public static class EventStatisticsVOLazyQueryFactory implements QueryFactory, Serializable {

		EventsCategoryDataProvider dataProvider;

		public EventStatisticsVOLazyQueryFactory(EventsCategoryDataProvider dataProvider) {
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
					List<EventStatistics> eventStatisticsVOs = dataProvider.getEvents();
					for (EventStatistics eventStaticsVO : eventStatisticsVOs) {
						Item item = new BeanItem<>(eventStaticsVO);
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
