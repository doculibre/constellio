package com.constellio.app.ui.framework.components.table;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
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
	
	protected Object getCellKey(Object itemId, Object propertyId) {
		return null;
	}
	
	private Map<CellKey, Property<?>> cellProperties = new HashMap<>();
	
	@Override
	public void containerItemSetChange(ItemSetChangeEvent event) {
		super.containerItemSetChange(event);
		cellProperties.clear();
	}

	@Override
	public final Property<?> getContainerProperty(Object itemId, Object propertyId) {
		Property<?> containerProperty;
		Item item = getItem(itemId);
		RecordVO recordVO;
		if (item instanceof RecordVOItem) {
			RecordVOItem recordVOItem = (RecordVOItem) item;
			recordVO = recordVOItem.getRecord();
		} else {
			recordVO = null;
		}
		
		CellKey cellKey;
		if (recordVO != null) {
			String recordId = recordVO.getId();
			if (propertyId instanceof MetadataVO) {
				MetadataVO metadataVO = (MetadataVO) propertyId;
				cellKey = new CellKey(recordId, metadataVO.getCode());
			} else {
				cellKey = new CellKey(recordId, propertyId);
			}
		} else {
			cellKey = null;
		}
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
	
	
	public static class CellKey implements Serializable {
		
		private String recordId;
		
		private Object propertyId;
		
		public CellKey(String recordId, Object propertyId) {
			this.recordId = recordId;
			this.propertyId = propertyId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((propertyId == null) ? 0 : propertyId.hashCode());
			result = prime * result + ((recordId == null) ? 0 : recordId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CellKey other = (CellKey) obj;
			if (propertyId == null) {
				if (other.propertyId != null)
					return false;
			} else if (!propertyId.equals(other.propertyId))
				return false;
			if (recordId == null) {
				if (other.recordId != null)
					return false;
			} else if (!recordId.equals(other.recordId))
				return false;
			return true;
		}
		
	}

}
