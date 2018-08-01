package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.SimpleStringFilter;
import org.vaadin.addons.lazyquerycontainer.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class UserCredentialVOLazyContainer extends LazyQueryContainer {

	public UserCredentialVOLazyContainer(UserCredentialVODataProvider dataProvider, int batchSize) {
		super(new UserCredentialVOLazyQueryDefinition(dataProvider, batchSize),
				new UserCredentialVOLazyQueryFactory(dataProvider));
	}

	public static class UserCredentialVOLazyQueryDefinition extends LazyQueryDefinition {

		UserCredentialVODataProvider dataProvider;

		public UserCredentialVOLazyQueryDefinition(UserCredentialVODataProvider dataProvider, int batchSize) {
			super(true, batchSize, null);
			this.dataProvider = dataProvider;

			super.addProperty("username", String.class, null, true, true);
			super.addProperty("firstName", String.class, null, true, true);
			super.addProperty("lastName", String.class, null, true, true);
			super.addProperty("email", String.class, null, true, true);
		}
	}

	public static class UserCredentialVOLazyQueryFactory implements QueryFactory, Serializable {

		UserCredentialVODataProvider dataProvider;

		public UserCredentialVOLazyQueryFactory(UserCredentialVODataProvider dataProvider) {
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
					List<UserCredentialVO> userCredentialVOs = dataProvider.listUserCredentialVOs(startIndex, count);
					for (UserCredentialVO userCredentialVO : userCredentialVOs) {
						Item item = new BeanItem<>(userCredentialVO);
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
