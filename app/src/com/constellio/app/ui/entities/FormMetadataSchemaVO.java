package com.constellio.app.ui.entities;

import java.io.Serializable;

import com.constellio.model.services.schemas.SchemaUtils;

@SuppressWarnings("serial")
public class FormMetadataSchemaVO implements Serializable {

	String code;

	String localCode;

	String collection;

	String label;

	public FormMetadataSchemaVO() {
		super();
		this.localCode = "";
		this.code = "";
		this.collection = "";
		this.label = "";
	}

	public FormMetadataSchemaVO(String code, String collection, String label) {
		super();

		String localCodeParsed = SchemaUtils.underscoreSplitWithCache(code)[1];
		if (localCodeParsed.contains("USR")) {
			localCodeParsed = localCodeParsed.split("USR")[1];
		}

		this.localCode = localCodeParsed;
		this.code = code;
		this.collection = collection;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLocalCode() {
		return localCode;
	}

	public String getCollection() {
		return collection;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setLocalCode(String code) {
		this.localCode = code;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((collection == null) ? 0 : collection.hashCode());
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
		FormMetadataSchemaVO other = (FormMetadataSchemaVO) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		return true;
	}

	/**
	 * Used by Vaadin to populate the header of the column in a table (since we use MetadataVO objects as property ids).
	 *
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		String toString;
		try {
			toString = getLabel();
		} catch (RuntimeException e) {
			toString = code;
		}
		return toString;
	}

}
