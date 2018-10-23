package com.constellio.app.ui.framework.components;

import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.StringUtils;

public abstract class TabWithTable {
	private Object id;
	private VerticalLayout tabLayout;
	private Table table;

	public TabWithTable(Object id) {
		this(id, null);
	}


	public TabWithTable(Object id, String caption) {
		this.id = id;
		this.tabLayout = new VerticalLayout();
		this.table = buildTable();
		this.tabLayout.addComponent(table);
		if(StringUtils.isNotBlank(caption)) {
			tabLayout.setCaption(caption);
		}
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

	public TabWithTable setCaption(String caption) {
		tabLayout.setCaption(caption);
		return this;
	}

	public Object getId() {
		return id;
	}
}