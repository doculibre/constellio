package com.constellio.app.api.extensions.params;

import java.util.List;

import com.vaadin.ui.Field;

public class FieldBindingExtentionParam {
	List<Field<?>> fields;

	public FieldBindingExtentionParam(List<Field<?>> fields) {
		this.fields = fields;
	}

	public List<Field<?>> getFields() {
		return fields;
	}
}
