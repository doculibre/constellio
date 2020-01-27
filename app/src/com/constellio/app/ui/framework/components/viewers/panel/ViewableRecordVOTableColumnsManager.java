package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.ColumnHeaderMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class ViewableRecordVOTableColumnsManager extends RecordVOTableColumnsManager {

	@Override
	public void manage(Table table, String tableId) {
		ViewableRecordVOTable viewableRecordVOTable = (ViewableRecordVOTable) table;
		List<Object> visibleColumnsList = new ArrayList<>(table.getContainerPropertyIds());
		if (viewableRecordVOTable.isCompressed()) {
			visibleColumnsList.remove(RecordVOTable.MENUBAR_PROPERTY_ID);
			if (visibleColumnsList.contains(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID)) {
				visibleColumnsList.remove(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID);
			}
			if (viewableRecordVOTable.isSelectColumn()) {
				visibleColumnsList.remove(BaseTable.SELECT_PROPERTY_ID);
				visibleColumnsList.add(0, BaseTable.SELECT_PROPERTY_ID);
			}
			if (isRightToLeft()) {
				if (visibleColumnsList.contains(BaseTable.SELECT_PROPERTY_ID)) {
					visibleColumnsList.remove(ViewableRecordVOContainer.THUMBNAIL_PROPERTY);
					visibleColumnsList.add(1, ViewableRecordVOContainer.THUMBNAIL_PROPERTY);
				} else {
					visibleColumnsList.remove(ViewableRecordVOContainer.THUMBNAIL_PROPERTY);
					visibleColumnsList.add(0, ViewableRecordVOContainer.THUMBNAIL_PROPERTY);
				}
				Collections.reverse(visibleColumnsList);
			}
			table.setVisibleColumns(visibleColumnsList.toArray(new Object[0]));
			table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
			viewableRecordVOTable.setTitleColumnWidth(366);
		} else {
			if (viewableRecordVOTable.isSelectColumn()) {
				visibleColumnsList.remove(BaseTable.SELECT_PROPERTY_ID);
				visibleColumnsList.add(0, BaseTable.SELECT_PROPERTY_ID);
			}
			visibleColumnsList.add(RecordVOTable.MENUBAR_PROPERTY_ID);
			if (isRightToLeft()) {
				if (visibleColumnsList.contains(BaseTable.SELECT_PROPERTY_ID)) {
					visibleColumnsList.remove(ViewableRecordVOContainer.THUMBNAIL_PROPERTY);
					visibleColumnsList.add(1, ViewableRecordVOContainer.THUMBNAIL_PROPERTY);
				} else {
					visibleColumnsList.remove(ViewableRecordVOContainer.THUMBNAIL_PROPERTY);
					visibleColumnsList.add(0, ViewableRecordVOContainer.THUMBNAIL_PROPERTY);
				}
				Collections.reverse(visibleColumnsList);
			}
			
			super.manage(table, tableId);
//			if (visibleColumns.contains(SearchResultContainer.THUMBNAIL_PROPERTY)) {
//				table.setColumnCollapsed(SearchResultContainer.THUMBNAIL_PROPERTY, false);
//			}	
//			if (visibleColumns.contains(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID)) {
//				table.setColumnCollapsed(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, false);
//			}
			table.setVisibleColumns(visibleColumnsList.toArray(new Object[0]));
			table.setColumnHeaderMode(ColumnHeaderMode.EXPLICIT_DEFAULTS_ID);
			viewableRecordVOTable.setTitleColumnWidth(-1);
			viewableRecordVOTable.setExpandTitleColumn(true);
		}
		if (isRightToLeft()) {
			for (Object propertyId : table.getContainerPropertyIds()) {
				Align alignment = adjustAlignment(table.getColumnAlignment(propertyId));
				table.setColumnAlignment(propertyId, alignment);
			}
		}
	}

}
