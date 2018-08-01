package com.constellio.app.modules.tasks.ui.components;

import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Table;

public class ColumnGeneratorAdapter implements CustomTable.ColumnGenerator {
	private final Table adaptedTable;
	private final Table.ColumnGenerator adaptee;

	public ColumnGeneratorAdapter(Table adaptedTable, Table.ColumnGenerator adaptee) {
		this.adaptedTable = adaptedTable;
		this.adaptee = adaptee;
	}

	@Override
	public Object generateCell(CustomTable customTable, Object o, Object o1) {
		if (adaptee != null) {
			return adaptee.generateCell(adaptedTable, o, o1);
		} else {
			return null;
		}

	}
}
