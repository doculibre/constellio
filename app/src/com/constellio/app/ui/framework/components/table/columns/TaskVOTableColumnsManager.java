package com.constellio.app.ui.framework.components.table.columns;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.vaadin.ui.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Constellio on 2016-12-13.
 */
public class TaskVOTableColumnsManager extends RecordVOTableColumnsManager {

	public TaskVOTableColumnsManager() {
		super();
	}

	public TaskVOTableColumnsManager(RecordVOTable table, String tableId) {
		super(table, tableId);
	}

	@Override
	protected List<String> getDefaultVisibleColumnIds(Table table) {
		ArrayList<String> defaultVisibleColumns = new ArrayList<>();

		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Task.TITLE);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Task.ASSIGNEE);
		defaultVisibleColumns = addColumnIfNotAlreadyIn(defaultVisibleColumns, Task.END_DATE);

		return defaultVisibleColumns;
	}

	protected ArrayList<String> addColumnIfNotAlreadyIn(ArrayList<String> defaultVisibleColumnIds,
														String metadataLocalCode) {

		ArrayList<String> allVisibleColumns = new ArrayList<>(defaultVisibleColumnIds);
		if (!defaultVisibleColumnIds.contains(Task.DEFAULT_SCHEMA + "_" +
											  metadataLocalCode)) {

			allVisibleColumns.add(Task.DEFAULT_SCHEMA
								  + "_" + metadataLocalCode);
		}
		return allVisibleColumns;
	}

}
