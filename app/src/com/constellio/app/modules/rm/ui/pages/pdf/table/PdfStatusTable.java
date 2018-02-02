package com.constellio.app.modules.rm.ui.pages.pdf.table;

import com.constellio.app.ui.framework.components.table.BaseTable;
import com.vaadin.data.Container;

import java.util.Collection;

public class PdfStatusTable extends BaseTable {
    public PdfStatusTable(String tableId, PdfStatusDataProvider<?> dataProvider) {
        super(tableId);

        PdfStatusDataContainer dataSource = new PdfStatusDataContainer(dataProvider);

        super.setContainerDataSource(dataSource);
        super.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);

        Collection<?> containerPropertyIds = dataSource.getContainerPropertyIds();
        for (Object containerPropertyId : containerPropertyIds) {
            setColumnHeader(containerPropertyId, "");
        }
    }
}
