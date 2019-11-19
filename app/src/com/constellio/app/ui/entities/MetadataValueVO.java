package com.constellio.app.ui.entities;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MetadataValueVO implements Serializable {

	final MetadataVO metadata;

	Object value;

	public MetadataValueVO(MetadataVO metadata) {
		this(metadata, null);
	}

	public MetadataValueVO(MetadataVO metadata, Object value) {
		super();
		setMetadataValue(metadata, value);
		this.metadata = metadata;
	}

	private void setMetadataValue(MetadataVO metadata, Object value) {
		//		if (metadata.getCode().toLowerCase().endsWith("_id")) {
		//			value = removeLeftZeros((String) value);
		//		}
		this.value = value;
	}

	private String removeLeftZeros(String value) {
		try {
			long idL = Long.parseLong(value);
			return Long.toString(idL);
		} catch (NumberFormatException e) {
			return value;
		}
	}

	public MetadataVO getMetadata() {
		return metadata;
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getValue() {
		return (T) value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return metadata.toString() + ":" + value;
	}

}
