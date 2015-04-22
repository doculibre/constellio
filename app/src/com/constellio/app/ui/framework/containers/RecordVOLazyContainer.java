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

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.DataProvider.DataRefreshListener;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.vaadin.data.Item;

@SuppressWarnings("serial")
public class RecordVOLazyContainer extends LazyQueryContainer {

	private RecordVODataProvider dataProvider;

	public RecordVOLazyContainer(RecordVODataProvider dataProvider) {
		super(new RecordVOLazyQueryDefinition(dataProvider), new RecordVOLazyQueryFactory(dataProvider));
		this.dataProvider = dataProvider;
		dataProvider.addDataRefreshListener(new DataRefreshListener() {
			@Override
			public void dataRefresh() {
				RecordVOLazyContainer.this.refresh();
			}
		});
	}
	
	public MetadataSchemaVO getSchema() {
		return dataProvider.getSchema();
	}

	public RecordVO getRecordVO(int index) {
		return dataProvider.getRecordVO(index);
	}

	public static class RecordVOLazyQueryDefinition extends LazyQueryDefinition {
		
		RecordVODataProvider dataProvider;

		/**
		 * final boolean compositeItems, final int batchSize, final Object idPropertyId
		 *
		 * //@param dataProvider
		 * //@param compositeItems
		 * //@param batchSize
		 * //@param idPropertyId
		 */
		public RecordVOLazyQueryDefinition(RecordVODataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			MetadataSchemaVO schema = dataProvider.getSchema();
			if (schema != null) {
				for (MetadataVO metadata : schema.getMetadatas()) {
					super.addProperty(metadata, metadata.getJavaType(), null, true, true);
				}
			}
		}

	}

	public static class RecordVOLazyQueryFactory implements QueryFactory, Serializable {

		RecordVODataProvider dataProvider;

		public RecordVOLazyQueryFactory(RecordVODataProvider dataProvider) {
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
					List<Item> items = new ArrayList<Item>();
					List<RecordVO> recordVOs = dataProvider.listRecordVOs(startIndex, count);
					for (RecordVO recordVO : recordVOs) {
						Item item = new RecordVOItem(recordVO);
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
