package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.vaadin.data.Container;
import com.vaadin.ui.Table;

public class BaseTable extends Table {
	
	private String tableId;
	
	private TableColumnsManager columnsManager;
	
	public BaseTable(String tableId) {
		super();
		this.tableId = tableId;
		init();
	}

	public BaseTable(String tableId, String caption) {
		super(caption);
		this.tableId = tableId;
		init();
	}

	public BaseTable(String tableId, String caption, Container dataSource) {
		super(caption, dataSource);
		this.tableId = tableId;
		init();
	}
	
	private void init() {
		addAttachListener(new AttachListener() {
			@Override
			public void attach(AttachEvent event) {
				String tableId = getTableId();
				if (tableId != null && columnsManager == null) {
					columnsManager = newColumnsManager();
					columnsManager.manage(BaseTable.this, tableId);
				}
			}
		});
	}
	
	protected TableColumnsManager newColumnsManager() {
		return new TableColumnsManager();
	}
	
	protected String getTableId() {
		return tableId;
	}

}
