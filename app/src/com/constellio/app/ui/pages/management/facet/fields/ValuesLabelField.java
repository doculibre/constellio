package com.constellio.app.ui.pages.management.facet.fields;

import com.vaadin.ui.CustomField;
import com.vaadin.ui.Table;

public abstract class ValuesLabelField<T> extends CustomField<T> {

	public abstract void saveValues();

	public abstract Table getValueListTable();

}