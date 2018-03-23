package com.constellio.app.ui.framework.components;

import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public abstract class TabWithTable {
	private String id;
	private VerticalLayout tabLayout;
	private Table table;

	public TabWithTable(String id) {
		this.id = id;
		this.tabLayout = new VerticalLayout();
		this.table = buildTable();
		this.tabLayout.addComponent(table);
	}

	public abstract Table buildTable();

	public void refreshTable() {
		Table newTable = buildTable();
		tabLayout.replaceComponent(table, newTable);
		table = newTable;
	}

	public VerticalLayout getTabLayout() {
		return tabLayout;
	}

	public String getId() {
		return id;
	}
}