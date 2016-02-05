package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

@SuppressWarnings("unchecked")
public class ListAddRemoveTextField extends ListAddRemoveField<String, TextField> {

	@Override
	protected Component initContent() {
		Component content = super.initContent();
		HorizontalLayout addEditFieldLayout = getAddEditFieldLayout();
		addEditFieldLayout.setWidth("100%");
		addEditFieldLayout.setExpandRatio(getAddEditField(), 1);
		return content;
	}

	@Override
	protected TextField newAddEditField() {
		return new BaseTextField();
	}

}
