package com.constellio.app.modules.tasks.ui.components;

import com.vaadin.ui.CustomTable;
import com.vaadin.ui.Table;

public class CellStyleGeneratorAdapter implements CustomTable.CellStyleGenerator {
	private final Table adaptedTable;
	private final Table.CellStyleGenerator adaptee;

	public CellStyleGeneratorAdapter(Table adaptedTable, Table.CellStyleGenerator adaptee) {
		this.adaptedTable = adaptedTable;
		this.adaptee = adaptee;
	}


	@Override
	public String getStyle(CustomTable customTable, Object o, Object o1) {
		if (adaptee != null) {
			return adaptee.getStyle(adaptedTable, o, o1);
		} else {
			return null;
		}
	}
}
