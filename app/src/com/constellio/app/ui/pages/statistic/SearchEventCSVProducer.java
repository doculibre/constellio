package com.constellio.app.ui.pages.statistic;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.containers.SearchEventVOLazyContainer.SearchEventVOLazyQueryFactory.SearchEventVOItem;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SearchEventVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchEventCSVProducer extends AbstractCSVProducer {
    private final SearchEventVODataProvider dataProvider;
    private final Map<String, MetadataVO> definedMetadatas;

    public SearchEventCSVProducer(Table table, SearchEventVODataProvider dataProvider) {
        super(table);

        this.dataProvider = dataProvider;
        this.definedMetadatas = new HashMap<>();

        initDefinedMetadatas();
    }

    private void initDefinedMetadatas() {
        MetadataSchemaVO schema = dataProvider.getSchema();
        List<MetadataVO> dataProviderDisplayMetadataVOs = schema.getDisplayMetadatas();
        for (MetadataVO metadataVO : dataProviderDisplayMetadataVOs) {
            definedMetadatas.put(metadataVO.getCode(), metadataVO);
        }
    }

    @Override
    protected long getRowCount() {
        return dataProvider.size();
    }

    @Override
    protected List<Item> loadItems(int startIndex, int numberOfItems) {
        List<Item> items = new ArrayList<>();
        List<RecordVO> recordVOsFromFirstDataProvider = dataProvider.listRecordVOs(startIndex, numberOfItems);
        for (RecordVO recordVO : recordVOsFromFirstDataProvider) {
            Item item = new SearchEventVOItem(recordVO, definedMetadatas);
            items.add(item);
        }
        return items;
    }
}
