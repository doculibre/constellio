package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.AbstractCSVProducer;
import com.constellio.app.ui.framework.components.table.BasePagedTable;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer.RecordVOLazyQueryFactory.RecordVOWithDistinctSchemaItem;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConnectorReportCSVProducer extends AbstractCSVProducer {
    private RecordVOWithDistinctSchemasDataProvider provider;
    private List<String> reportMetadataList;

    public ConnectorReportCSVProducer(Table table) {
        super(table);

        RecordVOWithDistinctSchemaTypesLazyContainer container = getContainer((BasePagedTable) table);

        this.provider = container.getDataProvider();

        List<String> reportMetadataList = new ArrayList<>(container.getReportMetadataList());
        reportMetadataList.add(ConnectorHttpDocument.INLINKS);
        reportMetadataList.add(ConnectorHttpDocument.OUTLINKS);

        this.reportMetadataList = reportMetadataList;
    }

    private RecordVOWithDistinctSchemaTypesLazyContainer getContainer(BasePagedTable table) {
        return (RecordVOWithDistinctSchemaTypesLazyContainer) table.getContainer();
    }

    @Override
    protected long getRowCount() {
        return provider.size();
    }

    @Override
    protected List<Item> loadItems(int startIndex, int numberOfItems) {
        List<Item> items = new ArrayList<>();
        List<RecordVO> recordVOS = provider.listRecordVOs(startIndex, numberOfItems);
        for (RecordVO recordVO : recordVOS) {
            items.add(new RecordVOWithDistinctSchemaItem(recordVO, reportMetadataList));
        }
        return items;
    }

    @Override
    protected String getValue(Property prop) {
        Object value = prop.getValue();

        if(Collection.class.isAssignableFrom(value.getClass())) {
            return StringUtils.join((Collection)value, System.lineSeparator());
        }

        return super.getValue(prop);
    }

    @Override
    public Table getTable() {
        final Table table = super.getTable();

        return new Table() {
            @Override
            public Object[] getVisibleColumns() {
                List<Object> objects = new ArrayList<>(Arrays.asList(table.getVisibleColumns()));
                objects.add(ConnectorHttpDocument.INLINKS);
                objects.add(ConnectorHttpDocument.OUTLINKS);

                return objects.toArray(new Object[0]);
            }

            @Override
            public String getColumnHeader(Object propertyId) {
                if(ConnectorHttpDocument.INLINKS.equals(propertyId)) {
                    return $("ConnectorReportView.inlinks");
                } else if (ConnectorHttpDocument.OUTLINKS.equals(propertyId)) {
                    return $("ConnectorReportView.outlinks");
                } else {
                    return table.getColumnHeader(propertyId);
                }
            }
        };
    }
}
