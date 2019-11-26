package com.constellio.app.ui.framework.containers;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import org.apache.commons.lang.StringUtils;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public class MetadataVOLazyContainer extends LazyQueryContainer {

	private static final String LABEL = "caption";
	private static final String ENABLED = "enabledCaption";
	private static final String TYPE = "valueCaption";
	private static final String DISPLAY_TYPE = "inputCaption";
	private static final String REQUIRED = "requiredCaption";
	private static final String CODE = "localCode";

	public MetadataVOLazyContainer(MetadataVODataProvider dataProvider, int batchSize) {
		super(new SchemaVOLazyQueryDefinition(dataProvider, batchSize), new SchemaVOLazyQueryFactory(dataProvider));
	}

	public static class SchemaVOLazyQueryDefinition extends LazyQueryDefinition {

		MetadataVODataProvider dataProvider;

		public SchemaVOLazyQueryDefinition(MetadataVODataProvider dataProvider, int batchSize) {
			super(true, batchSize, null);
			this.dataProvider = dataProvider;

			super.addProperty(CODE, String.class, null, true, true);
			super.addProperty(LABEL, String.class, null, true, true);
			super.addProperty(TYPE, String.class, null, true, true);
			super.addProperty(DISPLAY_TYPE, String.class, null, true, true);
			super.addProperty(REQUIRED, String.class, null, true, true);
			super.addProperty(ENABLED, String.class, null, true, true);
		}
	}


	public static class SchemaVOLazyQueryFactory implements SerializableQueryFactory {

		MetadataVODataProvider dataProvider;

		public SchemaVOLazyQueryFactory(MetadataVODataProvider dataProvider) {
			this.dataProvider = dataProvider;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
			if (queryDefinition.getSortPropertyIds().length > 0) {
				dataProvider.sort(queryDefinition.getSortPropertyIds(), queryDefinition.getSortPropertyAscendingStates());
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
					List<MetadataVO> schemaVOs = dataProvider.listMetadataVO(startIndex, count);
					for (MetadataVO schemaVO : schemaVOs) {
						Item item = new BeanItem<>(schemaVO);
						item.addItemProperty("localCode", new ObjectProperty<>(
								StringUtils.defaultIfBlank(schemaVO.getCode(), "")));
						item.addItemProperty("caption", new ObjectProperty<>(
								StringUtils.defaultIfBlank(schemaVO.getLabel(), "")));
						item.addItemProperty("valueCaption", new ObjectProperty<>(StringUtils.defaultIfBlank(
								$(MetadataValueType.getCaptionFor(schemaVO.getType())), "")));
						item.addItemProperty("inputCaption", new ObjectProperty<>(
								$(MetadataInputType.getCaptionFor(schemaVO.getMetadataInputType()))));
						item.addItemProperty("requiredCaption", new ObjectProperty<>(
								$("AddEditSchemaMetadataView." + schemaVO.isRequired())));
						item.addItemProperty("enabledCaption", new ObjectProperty<>(
								$("AddEditSchemaMetadataView." + schemaVO.isEnabled())));
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
