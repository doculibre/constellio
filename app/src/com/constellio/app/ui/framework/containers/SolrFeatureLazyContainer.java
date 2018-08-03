package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.framework.data.SolrFeatureDataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.ltr.feature.SolrFeature;
import org.vaadin.addons.lazyquerycontainer.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("serial")
public class SolrFeatureLazyContainer extends LazyQueryContainer {

	public SolrFeatureLazyContainer(SolrFeatureDataProvider dataProvider) {
		super(new SolrFeatureLazyQueryDefinition(dataProvider),
				new SolrFeatureLazyQueryFactory(dataProvider));
	}

	public static class SolrFeatureLazyQueryDefinition extends LazyQueryDefinition {

		SolrFeatureDataProvider dataProvider;

		public SolrFeatureLazyQueryDefinition(SolrFeatureDataProvider dataProvider) {
			super(true, 100, null);
			this.dataProvider = dataProvider;

			super.addProperty("name", String.class, null, true, true);
			super.addProperty("query", String.class, null, true, true);
		}
	}

	public static class SolrFeatureLazyQueryFactory implements QueryFactory, Serializable {

		SolrFeatureDataProvider dataProvider;

		public SolrFeatureLazyQueryFactory(SolrFeatureDataProvider dataProvider) {
			this.dataProvider = dataProvider;
		}

		@Override
		public Query constructQuery(final QueryDefinition queryDefinition) {
			return new Query() {
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
					List<SolrFeature> features = dataProvider.listFeatures(startIndex, count);
					for (SolrFeature feature : features) {
						Item item = new SolrFeatureItem(feature);
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

		private class SolrFeatureItem implements Item {
			private final SolrFeature feature;

			public SolrFeatureItem(SolrFeature feature) {
				this.feature = feature;
			}

			@Override
			public Property<?> getItemProperty(Object id) {
				if (id == null) {
					return null;
				}

				switch (id.toString()) {
					case "name":
						return new ItemProperty(feature.getName());
					case "query":
						String query = null;
						if (!CollectionUtils.isEmpty(feature.getFq())) {
							query = StringUtils.join(feature.getFq(), "; ");
						} else if (StringUtils.isNotBlank(feature.getQ())) {
							query = feature.getQ();
						}
						return new ItemProperty(query);
					default:
						return new ItemProperty(id.toString());
				}
			}

			@Override
			public Collection<?> getItemPropertyIds() {
				return Arrays.asList("name", "query");
			}

			@Override
			public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
				throw new UnsupportedOperationException("Unsupported operation");
			}

			@Override
			public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
				throw new UnsupportedOperationException("Unsupported operation");
			}
		}

		private class ItemProperty extends AbstractProperty<Object> {
			private final String value;

			public ItemProperty(String value) {
				this.value = value;
			}

			@Override
			public String getValue() {
				return value;
			}

			@Override
			public void setValue(Object newValue) throws ReadOnlyException {
			}

			@Override
			public Class<? extends String> getType() {
				return String.class;
			}
		}
	}

}
