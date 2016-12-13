package com.constellio.app.ui.framework.components.table.columns;

import com.constellio.app.ui.framework.components.table.RecordVOTable;
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
        List<String> defaultVisibleColumnIds = new ArrayList<>(super.getDefaultVisibleColumnIds(table));
        defaultVisibleColumnIds = addForcedVisibleColumnsToDefaults(defaultVisibleColumnIds);
        return defaultVisibleColumnIds;
    }

    protected List<String> addForcedVisibleColumnsToDefaults(List<String> defaultVisibleColumnIds) {

        ArrayList<String> allVisibleColumns = new ArrayList<>(defaultVisibleColumnIds);

        allVisibleColumns = addColumnIfNotAlreadyIn(allVisibleColumns, com.constellio.model.entities.records.wrappers.Event.TYPE);
        allVisibleColumns = addColumnIfNotAlreadyIn(allVisibleColumns, com.constellio.model.entities.records.wrappers.Event.USERNAME);

        return allVisibleColumns;
    }

    protected ArrayList<String> addColumnIfNotAlreadyIn(ArrayList<String> defaultVisibleColumnIds, String metadataLocalCode) {

        ArrayList<String> allVisibleColumns = new ArrayList<>(defaultVisibleColumnIds);
        if(!defaultVisibleColumnIds.contains(com.constellio.model.entities.records.wrappers.Event.DEFAULT_SCHEMA + "_" +
                metadataLocalCode)) {

            allVisibleColumns.add(com.constellio.model.entities.records.wrappers.Event.DEFAULT_SCHEMA
                    + "_" + metadataLocalCode);
        }
        return allVisibleColumns;
    }
}
