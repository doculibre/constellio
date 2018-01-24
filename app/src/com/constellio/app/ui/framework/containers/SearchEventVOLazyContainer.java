package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.addons.lazyquerycontainer.*;

import java.io.Serializable;
import java.util.*;

public class SearchEventVOLazyContainer extends LazyQueryContainer implements RefreshableContainer {
    public static final String CLICK_COUNT = "searchEvent_default_clickCount";
    public static final String ORIGINAL_QUERY = "searchEvent_default_originalQuery";
    public static final String PAGE_NAVIGATION_COUNT = "searchEvent_default_pageNavigationCount";
    public static final String PARAMS = "searchEvent_default_params";
    public static final String QUERY = "searchEvent_default_query";
    public static final String USER = "searchEvent_default_username";
    public static final String CREATION_DATE = "searchEvent_default_createdOn";

    public static final List<String> PROPERTIES = Collections.unmodifiableList(Arrays.asList(CREATION_DATE, QUERY, CLICK_COUNT, PAGE_NAVIGATION_COUNT, PARAMS));

    public SearchEventVOLazyContainer(QueryDefinition queryDefinition, QueryFactory queryFactory) {
        super(queryDefinition, queryFactory);
    }

    public static class SearchEventVOLazyQueryDefinition extends LazyQueryDefinition {
        private Map<String, MetadataVO> definedMetadatas  = new HashMap<>();

        public SearchEventVOLazyQueryDefinition(RecordVODataProvider dataProvider, List<String> properties) {
            super(true, 100, null);

            if(properties == null || properties.isEmpty()) {
                properties = PROPERTIES;
            }

            MetadataSchemaVO schema = dataProvider.getSchema();
            List<MetadataVO> dataProviderDisplayMetadataVOs = schema.getDisplayMetadatas();
            for (MetadataVO metadataVO : dataProviderDisplayMetadataVOs) {
                if(properties.contains(metadataVO.getCode())) {
                    definedMetadatas.put(metadataVO.getCode(), metadataVO);
                }
            }

            for(String code: properties) {
                super.addProperty(StringUtils.trimToEmpty(code), definedMetadatas.get(code).getJavaType(), null, true, true);
            }
        }

        public Map<String, MetadataVO> getDefinedMetadatas() {
            return (Map<String, MetadataVO>) Collections.unmodifiableMap(definedMetadatas);
        }
    }

    public static class SearchEventVOLazyQueryFactory implements QueryFactory, Serializable {
        private final Map<String, MetadataVO> definedMetadatas;
        private final RecordVODataProvider dataProvider;

        public SearchEventVOLazyQueryFactory(RecordVODataProvider dataProvider, Map<String, MetadataVO> definedMetadatas) {
            this.dataProvider = dataProvider;
            this.definedMetadatas = definedMetadatas;
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
                    List<Item> items = new ArrayList<Item>();
                    List<RecordVO> recordVOsFromFirstDataProvider = dataProvider.listRecordVOs(startIndex, count);
                    for (RecordVO recordVO : recordVOsFromFirstDataProvider) {
                        Item item = new SearchEventVOItem(recordVO);
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

        private class SearchEventVOItem extends RecordVOItem {
            public SearchEventVOItem(RecordVO recordVO) {
                super(recordVO);
            }

            @Override
            public Property<?> getItemProperty(Object id) {
                return super.getItemProperty(definedMetadatas.get(id));
            }

            @Override
            public Collection<?> getItemPropertyIds() {
                return definedMetadatas.keySet();
            }
        }
    }

    public static SearchEventVOLazyContainer defaultInstance(RecordVODataProvider dataProvider, List<String> properties) {
        SearchEventVOLazyQueryDefinition qDef = new SearchEventVOLazyQueryDefinition(dataProvider, properties);
        SearchEventVOLazyQueryFactory qFact = new SearchEventVOLazyQueryFactory(dataProvider, qDef.getDefinedMetadatas());

        final SearchEventVOLazyContainer searchEventVOLazyContainer = new SearchEventVOLazyContainer(qDef, qFact);

        dataProvider.addDataRefreshListener(new DataProvider.DataRefreshListener() {
            @Override
            public void dataRefresh() {
                searchEventVOLazyContainer.refresh();
            }
        });

        return searchEventVOLazyContainer;
    }
}
