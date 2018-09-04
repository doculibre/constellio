package com.constellio.app.modules.rm.ui.pages.pdf.table;

import com.constellio.app.ui.framework.components.table.BaseTable;
import com.vaadin.ui.UI;

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

	@Override
	public void containerItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {
		if (!UI.getCurrent().getConnectorTracker().isWritingResponse()) {
			super.containerItemSetChange(event);
		}
	}

}
