package com.constellio.app.ui.pages.statistic;

import com.constellio.app.ui.framework.components.AbstractCSVProducer;
import com.constellio.app.ui.framework.containers.FacetsLazyContainer.FacetsLazyQueryFactory.FacetsItem;
import com.constellio.app.ui.framework.data.FacetsDataProvider;
import com.constellio.app.ui.framework.data.FacetsDataProvider.Facets;
import com.vaadin.data.Item;
import com.vaadin.ui.Table;

import java.util.ArrayList;
import java.util.List;

public class FacetsCSVProducer extends AbstractCSVProducer {
    private FacetsDataProvider dataProvider;
    private List<String> properties;

    public FacetsCSVProducer(Table table, Long maxRow, FacetsDataProvider dataProvider, List<String> properties) {
        super(table, maxRow);

        this.dataProvider = dataProvider;
        this.properties = properties;
    }

    @Override
    protected long getRowCount() {
        return dataProvider.size();
    }

    @Override
    protected List<Item> loadItems(int startIndex, int numberOfItems) {
        List<Item> items = new ArrayList<>();

        List<Facets> facetsList = dataProvider.facetsList(startIndex, numberOfItems);
        for (Facets facets: facetsList) {
            items.add(new FacetsItem(facets, properties));
        }

        return items;
    }
}
