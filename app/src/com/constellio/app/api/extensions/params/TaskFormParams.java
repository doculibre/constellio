package com.constellio.app.api.extensions.params;

import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;

public class TaskFormParams {
	private SingleSchemaBasePresenter addEditTaskPresenter;
	private Record record;

	public TaskFormParams(SingleSchemaBasePresenter addEditTaskPresenter, Record record) {
		this.addEditTaskPresenter = addEditTaskPresenter;
		this.record = record;
	}

	public SingleSchemaBasePresenter getPresenter() {
		return addEditTaskPresenter;
	}

	public Record getRecord() {
		return record;
	}
}
