package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SchemaTypeVOLazyContainer extends LazyQueryContainer {
	public static final String LABEL = "caption";

	public SchemaTypeVOLazyContainer(SchemaTypeVODataProvider dataProvider) {
		super(new SchemaTypeVOLazyQueryDefinition(dataProvider), new SchemaTypeVOLazyQueryFactory(dataProvider));
	}

	public static class SchemaTypeVOLazyQueryDefinition extends LazyQueryDefinition {
		SchemaTypeVODataProvider dataProvider;

		public SchemaTypeVOLazyQueryDefinition(SchemaTypeVODataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			super.addProperty(LABEL, String.class, null, true, true);
		}
	}

	public static class SchemaTypeVOLazyQueryFactory implements SerializableQueryFactory {
		SchemaTypeVODataProvider dataProvider;

		public SchemaTypeVOLazyQueryFactory(SchemaTypeVODataProvider dataProvider) {
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
					List<MetadataSchemaTypeVO> schemaVOs = dataProvider.listSchemaTypeVO();
					for (MetadataSchemaTypeVO schemaVO : schemaVOs) {
						Item item = new BeanItem<>(schemaVO);
						item.addItemProperty("caption", new ObjectProperty<>(StringUtils.defaultIfBlank(schemaVO.getLabel(), schemaVO.getCode())));
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
