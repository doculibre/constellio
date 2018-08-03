package com.constellio.app.ui.framework.components.table;

import com.vaadin.data.Property;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TablePropertyCache implements Serializable {

	private Map<CellKey, Property<?>> cellProperties = new HashMap<>();

	public void clear() {
		cellProperties.clear();
	}

	public Property<?> get(CellKey cellKey) {
		return cellKey != null ? cellProperties.get(cellKey) : null;
	}

	public void put(CellKey cellKey, Property<?> property) {
		cellProperties.put(cellKey, property);
	}

	public static class CellKey implements Serializable {

		private Object objectId;

		private Object propertyId;

		public CellKey(Object objectId, Object propertyId) {
			this.objectId = objectId;
			this.propertyId = propertyId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((propertyId == null) ? 0 : propertyId.hashCode());
			result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CellKey other = (CellKey) obj;
			if (propertyId == null) {
				if (other.propertyId != null) {
					return false;
				}
			} else if (!propertyId.equals(other.propertyId)) {
				return false;
			}
			if (objectId == null) {
				if (other.objectId != null) {
					return false;
				}
			} else if (!objectId.equals(other.objectId)) {
				return false;
			}
			return true;
		}

	}

}
