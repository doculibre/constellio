package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SolrDataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.vaadin.addons.lazyquerycontainer.*;

import java.io.Serializable;
import java.util.*;

public class StatisticsLazyContainer extends LazyQueryContainer implements RefreshableContainer {
    public static final String CLICK_COUNT = "statistic_default_clickCount";
    public static final String FREQUENCY = "statistic_default_frequency";
    public static final String QUERY = "statistic_default_query";

    public static final List<String> PROPERTIES = Collections.unmodifiableList(Arrays.asList(QUERY, CLICK_COUNT, FREQUENCY));

    public StatisticsLazyContainer(QueryDefinition queryDefinition, QueryFactory queryFactory) {
        super(queryDefinition, queryFactory);
    }

    public static class StatisticsLazyQueryDefinition extends LazyQueryDefinition {
        public StatisticsLazyQueryDefinition(List<String> properties) {
            super(true, 100, null);

            if(properties == null || properties.isEmpty()) {
                properties = PROPERTIES;
            }

            for(String code: properties) {
                super.addProperty(code, String.class, null, true, true);
            }
        }
    }

    public static class StatisticsLazyQueryFactory implements QueryFactory, Serializable {
        private final List<String> properties;
        private final SolrDataProvider dataProvider;

        public StatisticsLazyQueryFactory(SolrDataProvider dataProvider, List<String> properties) {
            this.dataProvider = dataProvider;

            if(properties == null || properties.isEmpty()) {
                properties = PROPERTIES;
            }
            this.properties = properties;
        }

        @Override
        public Query constructQuery(final QueryDefinition queryDefinition) {
            final ArrayList<SimpleOrderedMap> buckets = new ArrayList<>();

            QueryResponse queryResponse = dataProvider.getQueryResponse();
            NamedList<Object> namedList = queryResponse.getResponse();

            try {
                SimpleOrderedMap facets = (SimpleOrderedMap) namedList.get("facets");
                SimpleOrderedMap queryS = (SimpleOrderedMap) facets.get("query_s");
                buckets.addAll((ArrayList<SimpleOrderedMap>) queryS.get("buckets"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new Query() {

                @Override
                public int size() {
                    return buckets.size();
                }

                @Override
                public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
                    throw new UnsupportedOperationException("Query is read-only");
                }

                @Override
                public List<Item> loadItems(int startIndex, int count) {
                    List<Item> items = new ArrayList<Item>();

                    for (int i = startIndex; i < count; i++) {
                        SimpleOrderedMap bucket = buckets.get(i);

                        String query = (String) bucket.get("val");
                        String clickCount = String.valueOf(((Number)bucket.get("clickCount_d")).intValue());
                        String frequency = String.valueOf(((Number)bucket.get("count")).intValue());

                        Item item = new StatisticsItem(query, clickCount, frequency);
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

        private class StatisticsItem implements Item {
            private final String query;
            private final String clickCount;
            private final String frequency;

            public StatisticsItem(String query, String clickCount, String frequency) {
                this.query = query;
                this.clickCount = clickCount;
                this.frequency = frequency;
            }

            @Override
            public Property<?> getItemProperty(Object id) {
                if(id == null) {
                    return null;
                }

                switch (id.toString()) {
                    case QUERY:
                        return new ItemProperty(query);
                    case CLICK_COUNT:
                        return new ItemProperty(clickCount);
                    case FREQUENCY:
                        return new ItemProperty(frequency);
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

    public static StatisticsLazyContainer defaultInstance(SolrDataProvider dataProvider, List<String> properties) {
        StatisticsLazyQueryDefinition qDef = new StatisticsLazyQueryDefinition(properties);
        StatisticsLazyQueryFactory qFact = new StatisticsLazyQueryFactory(dataProvider, properties);

        final StatisticsLazyContainer statisticsLazyContainer = new StatisticsLazyContainer(qDef, qFact);

        dataProvider.addDataRefreshListener(new DataProvider.DataRefreshListener() {
            @Override
            public void dataRefresh() {
                statisticsLazyContainer.refresh();
            }
        });

        return statisticsLazyContainer;
    }
}
