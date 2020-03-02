package com.constellio.app.ui.framework.components.table.columns;

import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.model.entities.records.wrappers.Authorization;
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

		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Authorization.PRINCIPALS);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Authorization.SHARED_BY);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Authorization.ROLES);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Authorization.START_DATE);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Authorization.END_DATE);

		return defaultVisibleColumns;
	}

	protected ArrayList<String> addColumnIfNotAlreadyIn(ArrayList<String> defaultVisibleColumnIds,
														String metadataLocalCode) {

		ArrayList<String> allVisibleColumns = new ArrayList<>(defaultVisibleColumnIds);
		if (!defaultVisibleColumnIds.contains(com.constellio.model.entities.records.wrappers.Authorization.DEFAULT_SCHEMA + "_" +
											  metadataLocalCode)) {

			allVisibleColumns.add(com.constellio.model.entities.records.wrappers.Authorization.DEFAULT_SCHEMA
								  + "_" + metadataLocalCode);
		}
		return allVisibleColumns;
	}
}
