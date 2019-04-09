package com.constellio.app.ui.framework.components.table;

import java.util.LinkedHashMap;

import com.vaadin.ui.Table;

public class DefaultSelectionTableAdapter extends SelectionTableAdapter {
	
	private LinkedHashMap<Object, Boolean> selectionMap = new LinkedHashMap<>();

	public DefaultSelectionTableAdapter() {
	}

	public DefaultSelectionTableAdapter(Table table) {
		super(table);
	}

	@Override
	public void selectAll() {
		Table table = getTable();
		if (table != null) {
			for (Object itemId : table.getItemIds()) {
				selectionMap.put(itemId, true);
			}
		}
	}

	@Override
	public void deselectAll() {
		Table table = getTable();
		if (table != null) {
			for (Object itemId : table.getItemIds()) {
				selectionMap.put(itemId, false);
			}
		}
	}

	@Override
	public boolean isAllItemsSelected() {
		boolean allSelected = true;
		Table table = getTable();
		if (table != null) {
			for (Object itemId : table.getItemIds()) {
				Boolean selected = selectionMap.get(itemId);
				if (!Boolean.TRUE.equals(selected)) {
					allSelected = false;
					break;
				}
			}
		}
		return allSelected;
	}

	@Override
	public boolean isAllItemsDeselected() {
		boolean allDeselected = true;
		Table table = getTable();
		if (table != null) {
			for (Object itemId : table.getItemIds()) {
				Boolean selected = selectionMap.get(itemId);
				if (Boolean.TRUE.equals(selected)) {
					allDeselected = false;
					break;
				}
			}
		}
		return allDeselected;
	}

	@Override
	public boolean isSelected(Object itemId) {
		return Boolean.TRUE.equals(selectionMap.get(itemId));
	}

	@Override
	public void setSelected(Object itemId, boolean selected) {
		selectionMap.put(itemId, selected);
	}

}
