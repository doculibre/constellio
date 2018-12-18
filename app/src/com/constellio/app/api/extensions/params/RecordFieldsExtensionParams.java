package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.Record;
import com.vaadin.ui.Component;

public class RecordFieldsExtensionParams {

	BaseView mainComponent;

	Record record;

	public RecordFieldsExtensionParams(BaseView mainComponent, Record record) {
		this.mainComponent = mainComponent;
		this.record = record;
	}

	public BaseView getMainComponent() {
		return mainComponent;
	}

	public Record getRecord() {
		return record;
	}
}
