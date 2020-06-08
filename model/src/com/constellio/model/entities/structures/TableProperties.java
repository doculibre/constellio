package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableProperties implements ModifiableStructure {

	protected String tableId;
	protected List<String> visibleColumnIds;
	protected Map<String, Integer> columnWidths;
	protected String sortedColumnId;
	protected Boolean isSortedAscending;
	protected boolean dirty;

	public TableProperties() {
	}

	public TableProperties(String tableId) {
		setTableId(tableId);
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String value) {
		dirty = true;
		tableId = value;
	}

	public List<String> getVisibleColumnIds() {
		return visibleColumnIds;
	}

	public void setVisibleColumnIds(List<String> value) {
		dirty = true;

		if (value == null || value.isEmpty()) {
			visibleColumnIds = null;
		} else {
			visibleColumnIds = value;
		}
	}

	public Map<String, Integer> getColumnWidths() {
		return columnWidths;
	}

	public void setColumnWidths(Map<String, Integer> value) {
		dirty = true;

		if (value == null || value.isEmpty()) {
			columnWidths = null;
		} else {
			columnWidths = value;
		}
	}

	public Integer getColumnWidth(String columnId) {
		if (columnWidths != null && columnWidths.containsKey(columnId)) {
			return columnWidths.get(columnId);
		}

		return null;
	}

	public void setColumnWidth(String columnId, Integer value) {
		dirty = true;

		if (columnWidths == null) {
			columnWidths = new HashMap<>();
		}

		if (value == null && columnWidths.containsKey(columnId)) {
			columnWidths.remove(columnId);
		} else {
			columnWidths.put(columnId, value);
		}

		if (columnWidths.isEmpty()) {
			columnWidths = null;
		}
	}

	public String getSortedColumnId() {
		return sortedColumnId;
	}

	public void setSortedColumnId(String value) {
		dirty = true;
		sortedColumnId = value;
	}

	public Boolean getSortedAscending() {
		return isSortedAscending;
	}

	public void setSortedAscending(Boolean value) {
		dirty = true;
		isSortedAscending = value;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "TableProperties{" +
			   "tableId='" + tableId + '\'' +
			   ", visibleColumnIds='" + visibleColumnIds + '\'' +
			   ", columnWidths='" + columnWidths + '\'' +
			   ", sortedColumnId='" + sortedColumnId + '\'' +
			   ", isSoredtAscending='" + isSortedAscending + '\'' +
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
