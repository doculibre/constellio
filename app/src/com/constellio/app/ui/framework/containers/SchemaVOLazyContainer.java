package com.constellio.app.ui.framework.containers;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.data.SchemaVODataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;

@SuppressWarnings("serial")
public class SchemaVOLazyContainer extends LazyQueryContainer {
	public static final String LABEL = "caption";

	public SchemaVOLazyContainer(SchemaVODataProvider dataProvider) {
		super(new SchemaVOLazyQueryDefinition(dataProvider), new SchemaVOLazyQueryFactory(dataProvider));
	}

	public static class SchemaVOLazyQueryDefinition extends LazyQueryDefinition {
		SchemaVODataProvider dataProvider;

		public SchemaVOLazyQueryDefinition(SchemaVODataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			super.addProperty(LABEL, String.class, null, true, true);
		}
	}

	public static class SchemaVOLazyQueryFactory implements SerializableQueryFactory {
		SchemaVODataProvider dataProvider;

		public SchemaVOLazyQueryFactory(SchemaVODataProvider dataProvider) {
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
					if (queryDefinition.getSortPropertyIds().length > 0) {
						dataProvider.sort(queryDefinition.getSortPropertyIds(), queryDefinition.getSortPropertyAscendingStates());
					}
					List<Item> items = new ArrayList<>();
					List<MetadataSchemaVO> schemaVOs = dataProvider.listSchemaVO(startIndex, count);
					for (MetadataSchemaVO schemaVO : schemaVOs) {
						Item item = new BeanItem<>(schemaVO);
						item.addItemProperty("caption", new ObjectProperty<>(schemaVO.getLabel()));
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
	}

}
