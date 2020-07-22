package com.constellio.app.ui.framework.components.fields;

import com.vaadin.ui.Field;

public interface SignatureRecordField<T> extends Field<T> {
	public String getMetadataLocalCode();

	public Object getCommittableValue();
}
