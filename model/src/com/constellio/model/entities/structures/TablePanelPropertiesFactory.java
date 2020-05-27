package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringTokenizer;

public class TablePanelPropertiesFactory extends TablePropertiesFactory {

	@Override
	public ModifiableStructure build(String value) {
		if (value == null) {
			return null;
		}

		StringTokenizer stringTokenizer = new StringTokenizer(value, DELIMITER);

		TablePanelProperties properties = new TablePanelProperties();
		properties.tablePanelId = readString(stringTokenizer);
		properties.tableMode = readString(stringTokenizer);
		properties.dirty = false;
		return properties;
	}

	@Override
	public String toString(ModifiableStructure structure) {
		if (structure == null) {
			return null;
		}

		TablePanelProperties properties = (TablePanelProperties) structure;
		StringBuilder stringBuilder = new StringBuilder();
		writeString(stringBuilder, properties.getTablePanelId());
		writeString(stringBuilder, properties.getTableMode());
		return stringBuilder.toString();
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
