package com.constellio.app.ui.pages.management.schemas.metadata;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.ui.entities.FormMetadataVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.vaadin.ui.Field;

public abstract class MetadataForm extends BaseForm<FormMetadataVO> {

	public MetadataForm(FormMetadataVO viewObject, Serializable objectWithMemberFields, Field... fields) {
		super(viewObject, objectWithMemberFields, fields);
	}

	public MetadataForm(FormMetadataVO viewObject, List fieldsAndPropertyIds) {
		super(viewObject, fieldsAndPropertyIds);
	}

	public void reload() {
	}

	public void commit() {
	}
}
