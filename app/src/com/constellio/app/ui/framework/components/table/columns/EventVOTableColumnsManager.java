package com.constellio.app.ui.framework.components.table.columns;

import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.model.entities.records.wrappers.Event;
import com.vaadin.ui.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Constellio on 2016-12-13.
 */
public class EventVOTableColumnsManager extends RecordVOTableColumnsManager {

	public EventVOTableColumnsManager() {
		super();
	}

	public EventVOTableColumnsManager(RecordVOTable table, String tableId) {
		super(table, tableId);
	}

	@Override
	protected List<String> getDefaultVisibleColumnIds(Table table) {
		ArrayList<String> defaultVisibleColumns = new ArrayList<>();

		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Event.TYPE);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Event.USERNAME);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, "createdOn");

		return defaultVisibleColumns;
	}

	protected ArrayList<String> addColumnIfNotAlreadyIn(ArrayList<String> defaultVisibleColumnIds,
														String metadataLocalCode) {

		ArrayList<String> allVisibleColumns = new ArrayList<>(defaultVisibleColumnIds);
		if (!defaultVisibleColumnIds.contains(com.constellio.model.entities.records.wrappers.Event.DEFAULT_SCHEMA + "_" +
											  metadataLocalCode)) {

			allVisibleColumns.add(com.constellio.model.entities.records.wrappers.Event.DEFAULT_SCHEMA
								  + "_" + metadataLocalCode);
		}
		return allVisibleColumns;
	}
}
