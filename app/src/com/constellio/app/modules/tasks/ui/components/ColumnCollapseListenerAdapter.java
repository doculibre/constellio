package com.constellio.app.modules.tasks.ui.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Table;

public class ColumnCollapseListenerAdapter implements CustomTable.ColumnCollapseListener {
	private final Table.ColumnCollapseListener adaptee;

	public ColumnCollapseListenerAdapter(Table.ColumnCollapseListener adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void columnCollapseStateChange(CustomTable.ColumnCollapseEvent event) {
		if (adaptee != null) {
			adaptee.columnCollapseStateChange(new Table.ColumnCollapseEvent((Component) event.getSource(), event.getPropertyId()));
		}
	}
}
