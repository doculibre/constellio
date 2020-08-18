package com.constellio.app.ui.framework.components.fields;

import com.vaadin.ui.Field;

public interface ExtraTabAdditionalRecordField<T> extends Field<T> {
	public String getMetadataLocalCode();
	public Object getCommittableValue();

	public String getTab();
}
