/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
