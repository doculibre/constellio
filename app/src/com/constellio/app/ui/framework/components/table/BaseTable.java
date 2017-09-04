package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

public class BaseTable extends Table {
	
	private String tableId;
	
	private TableColumnsManager columnsManager;
	
	private TablePropertyCache cellProperties = new TablePropertyCache();
	
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
	
	protected CellKey getCellKey(Object itemId, Object propertyId) {
		return null;
	}
	
	@Override
	public void containerItemSetChange(ItemSetChangeEvent event) {
		super.containerItemSetChange(event);
		cellProperties.clear();
	}

	@Override
	public final Property<?> getContainerProperty(Object itemId, Object propertyId) {
		Property<?> containerProperty;
		CellKey cellKey = getCellKey(itemId, propertyId);
		if (cellKey != null) {
			containerProperty = cellProperties.get(cellKey);
			if (containerProperty == null) {
				containerProperty = loadContainerProperty(itemId, propertyId);
				cellProperties.put(cellKey, containerProperty);
			}
		} else {
			containerProperty = loadContainerProperty(itemId, propertyId);
		}
		return containerProperty;
	}

	protected Property<?> loadContainerProperty(final Object itemId, final Object propertyId) {
		return super.getContainerProperty(itemId, propertyId);
	}
	
}
