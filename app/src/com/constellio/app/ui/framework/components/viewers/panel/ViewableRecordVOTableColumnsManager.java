package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnHeaderMode;

import java.util.ArrayList;
import java.util.List;

public class ViewableRecordVOTableColumnsManager extends RecordVOTableColumnsManager {

	@Override
	public void manage(Table table, String tableId) {
		ViewableRecordVOTable viewableRecordVOTable = (ViewableRecordVOTable) table;
		List<Object> visibleColumns = new ArrayList<>(table.getContainerPropertyIds());
		if (viewableRecordVOTable.isCompressed()) {
			visibleColumns.remove(RecordVOTable.MENUBAR_PROPERTY_ID);
			if (visibleColumns.contains(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID)) {
				visibleColumns.remove(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID);
			}
			table.setVisibleColumns(visibleColumns.toArray(new Object[0]));
			table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
			viewableRecordVOTable.setTitleColumnWidth(366);
		} else {
			visibleColumns.add(RecordVOTable.MENUBAR_PROPERTY_ID);
			super.manage(table, tableId);
//			if (visibleColumns.contains(SearchResultContainer.THUMBNAIL_PROPERTY)) {
//				table.setColumnCollapsed(SearchResultContainer.THUMBNAIL_PROPERTY, false);
//			}	
//			if (visibleColumns.contains(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID)) {
//				table.setColumnCollapsed(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, false);
//			}
			table.setVisibleColumns(visibleColumns.toArray(new Object[0]));
			table.setColumnHeaderMode(ColumnHeaderMode.EXPLICIT_DEFAULTS_ID);
			viewableRecordVOTable.setTitleColumnWidth(-1);
			viewableRecordVOTable.setExpandTitleColumn(true);
		}
	}

}
