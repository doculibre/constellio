package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TablePanelProperties implements ModifiableStructure {

	protected String tablePanelId;
	protected String tableMode;
	protected boolean dirty;

	public TablePanelProperties() {
	}

	public TablePanelProperties(String tableId) {
		setTablePanelId(tableId);
	}

	public String getTablePanelId() {
		return tablePanelId;
	}

	public void setTablePanelId(String value) {
		dirty = true;
		tablePanelId = value;
	}

	public String getTableMode() {
		return tableMode;
	}

	public void setTableMode(String tableMode) {
		dirty = true;
		this.tableMode = tableMode;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "TablePanelProperties{" +
			   "tablePanelId='" + tablePanelId + '\'' +
			   ", tableMode='" + tableMode + '\'' +
			   ", dirty=" + dirty +
			   '}';
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}
}
