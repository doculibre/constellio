package com.constellio.app.ui.framework.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.framework.data.AuthorizationVODataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class AuthorizationVOLazyContainer extends LazyQueryContainer {

	public AuthorizationVOLazyContainer(AuthorizationVODataProvider dataProvider, String recordId) {
		super(new AuthorizationVOLazyQueryDefinition(dataProvider), new AuthorizationVOLazyQueryFactory(dataProvider, recordId));
	}

	public static class AuthorizationVOLazyQueryDefinition extends LazyQueryDefinition {

		AuthorizationVODataProvider dataProvider;

		public AuthorizationVOLazyQueryDefinition(AuthorizationVODataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			super.addProperty("users", String.class, null, true, true);
			super.addProperty("groups", String.class, null, true, true);
			super.addProperty("records", String.class, null, true, true);
			super.addProperty("roles", String.class, null, true, true);
		}
	}

	public static class AuthorizationVOLazyQueryFactory implements QueryFactory, Serializable {

		AuthorizationVODataProvider dataProvider;
		final String recordId;

		public AuthorizationVOLazyQueryFactory(AuthorizationVODataProvider dataProvider, String recordId) {
			this.dataProvider = dataProvider;
			this.recordId = recordId;
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
					List<AuthorizationVO> AuthorizationVOs = dataProvider.listAuthorizationVOs(recordId);
					for (AuthorizationVO AuthorizationVO : AuthorizationVOs) {
						Item item = new BeanItem<>(AuthorizationVO);
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
