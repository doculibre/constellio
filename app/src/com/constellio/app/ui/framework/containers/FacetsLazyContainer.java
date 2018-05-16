package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.FacetsDataProvider;
import com.constellio.app.ui.framework.data.FacetsDataProvider.Facets;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import org.vaadin.addons.lazyquerycontainer.*;

import java.io.Serializable;
import java.util.*;

public class FacetsLazyContainer extends LazyQueryContainer implements RefreshableContainer {
    public static final String CLICK_COUNT = "statistic_default_clickCount";
    public static final String FREQUENCY = "statistic_default_frequency";
    public static final String QUERY = "statistic_default_query";

    public static final List<String> PROPERTIES = Collections.unmodifiableList(Arrays.asList(QUERY, CLICK_COUNT, FREQUENCY));

    public FacetsLazyContainer(QueryDefinition queryDefinition, QueryFactory queryFactory) {
        super(queryDefinition, queryFactory);
    }

    public static class FacetsLazyQueryDefinition extends LazyQueryDefinition {
        public FacetsLazyQueryDefinition(List<String> properties) {
            super(true, 100, null);

            if(properties == null || properties.isEmpty()) {
                properties = PROPERTIES;
            }

            for(String code: properties) {
                super.addProperty(code, String.class, null, true, true);
            }
        }
    }

    public static class FacetsLazyQueryFactory implements QueryFactory, Serializable {
        private final List<String> properties;
        private final FacetsDataProvider dataProvider;

        public FacetsLazyQueryFactory(FacetsDataProvider dataProvider, List<String> properties) {
            this.dataProvider = dataProvider;

            if(properties == null || properties.isEmpty()) {
                properties = PROPERTIES;
            }
            this.properties = properties;
        }

        @Override
        public Query constructQuery(final QueryDefinition queryDefinition) {
            return new Query() {

                @Override
                public int size() {
                    return (int) dataProvider.size();
                }

                @Override
                public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
                }

                @Override
                public List<Item> loadItems(int startIndex, int count) {
                    List<Item> items = new ArrayList<Item>();
                    List<Facets> facetsList = dataProvider.facetsList(startIndex, count);

                    for (int i = 0; i < facetsList.size(); i++) {
                        items.add(new FacetsItem(facetsList.get(i), properties));
                    }

                    return items;
                }

                @Override
                public boolean deleteAllItems() {
                    return false;
                }

                @Override
                public Item constructItem() {
                    return null;
                }
            };
        }

        public static class FacetsItem implements Item {
            private final List<String> properties;
            private final Facets facets;

            public FacetsItem(Facets facets, List<String> properties) {
                this.facets = facets;

                if(properties == null || properties.isEmpty()) {
                    properties = PROPERTIES;
                }
                this.properties = properties;
            }

            @Override
            public Property<?> getItemProperty(Object id) {
                if(id == null) {
                    return null;
                }

                switch (id.toString()) {
                    case QUERY:
                        return new ItemProperty(facets.getQuery());
                    case CLICK_COUNT:
                        return new ItemProperty(facets.getClickCount());
                    case FREQUENCY:
                        return new ItemProperty(facets.getFrequency());
                    default:
                        return new ItemProperty(id.toString());
                }
            }

            @Override
            public Collection<?> getItemPropertyIds() {
                return properties;
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

        private static class ItemProperty extends AbstractProperty<Object> {
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

    public static FacetsLazyContainer defaultInstance(FacetsDataProvider dataProvider, List<String> properties) {
        FacetsLazyQueryDefinition qDef = new FacetsLazyQueryDefinition(properties);
        FacetsLazyQueryFactory qFact = new FacetsLazyQueryFactory(dataProvider, properties);

        final FacetsLazyContainer facetsLazyContainer = new FacetsLazyContainer(qDef, qFact);

        dataProvider.addDataRefreshListener(new DataProvider.DataRefreshListener() {
            @Override
            public void dataRefresh() {
                facetsLazyContainer.refresh();
            }
        });

        return facetsLazyContainer;
    }
}
