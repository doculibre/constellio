package com.constellio.app.ui.framework.components.table.columns;

import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.vaadin.ui.Table;

import java.util.ArrayList;
import java.util.List;

public class SharesVOTableColumnsManager extends RecordVOTableColumnsManager {

	public SharesVOTableColumnsManager() {
		super();
	}

	public SharesVOTableColumnsManager(RecordVOTable table, String tableId) {
		super(table, tableId);
	}

	@Override
	protected List<String> getDefaultVisibleColumnIds(Table table) {
		ArrayList<String> defaultVisibleColumns = new ArrayList<>();

		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, RecordAuthorization.PRINCIPALS);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, RecordAuthorization.SHARED_BY);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, RecordAuthorization.ROLES);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, RecordAuthorization.START_DATE);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, RecordAuthorization.END_DATE);

		return defaultVisibleColumns;
	}

	protected ArrayList<String> addColumnIfNotAlreadyIn(ArrayList<String> defaultVisibleColumnIds,
														String metadataLocalCode) {

		ArrayList<String> allVisibleColumns = new ArrayList<>(defaultVisibleColumnIds);
		if (!defaultVisibleColumnIds.contains(com.constellio.model.entities.records.wrappers.RecordAuthorization.DEFAULT_SCHEMA + "_" +
											  metadataLocalCode)) {

			allVisibleColumns.add(com.constellio.model.entities.records.wrappers.RecordAuthorization.DEFAULT_SCHEMA
								  + "_" + metadataLocalCode);
		}
		return allVisibleColumns;
	}
}
