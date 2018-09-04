package com.constellio.app.modules.tasks.ui.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Table;

public class ColumnReorderListenerAdapter implements CustomTable.ColumnReorderListener {
	private final Table.ColumnReorderListener adaptee;

	public ColumnReorderListenerAdapter(Table.ColumnReorderListener adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void columnReorder(CustomTable.ColumnReorderEvent event) {
		if (adaptee != null) {
			adaptee.columnReorder(new Table.ColumnReorderEvent((Component) event.getSource()));
		}
	}
}
