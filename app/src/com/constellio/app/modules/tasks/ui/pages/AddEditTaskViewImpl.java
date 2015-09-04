/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.modules.tasks.ui.components.fields.TaskForm;
import com.constellio.app.modules.tasks.ui.components.fields.TaskFormImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

public class AddEditTaskViewImpl extends BaseViewImpl implements AddEditTaskView {
	private final AddEditTaskPresenter presenter;

	private TaskFormImpl form;

	public AddEditTaskViewImpl() {
		presenter = new AddEditTaskPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.initTaskVO(event.getParameters());
	}

	protected String getTitle() {
		return presenter.getViewTitle();
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		TaskVO task = presenter.getTask();

		form = new TaskFormImpl(task) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked(viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancel();
			}
		};

		return form;
	}

	@Override
	public TaskForm getForm() {
		return form;
	}
}
