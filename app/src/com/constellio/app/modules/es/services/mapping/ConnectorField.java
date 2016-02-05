package com.constellio.app.modules.es.services.mapping;

import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;

public class ConnectorField implements ModifiableStructure, Comparable<ConnectorField> {

	boolean dirty;
	private String id;
	private String label;
	private MetadataValueType type;

	public ConnectorField(String id, String label, MetadataValueType type) {
		this.id = id;
		this.label = label;
		this.type = type;
	}

	public ConnectorField() {
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public MetadataValueType getType() {
		return type;
	}

	public void setId(String id) {
		dirty = true;
		this.id = id;
	}

	public void setLabel(String label) {
		dirty = true;
		this.label = label;
	}

	public void setType(MetadataValueType type) {
		dirty = true;
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ConnectorField that = (ConnectorField) o;

		if (id != null ? !id.equals(that.id) : that.id != null)
			return false;
		if (label != null ? !label.equals(that.label) : that.label != null)
			return false;
		return type == that.type;

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (label != null ? label.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "ConnectorField{" +
				", id='" + id + '\'' +
				", label='" + label + '\'' +
				", type=" + type +
				'}';
	}

	@Override
	public int compareTo(ConnectorField o) {
		return getLabel().compareTo(o.getLabel());
	}
}
