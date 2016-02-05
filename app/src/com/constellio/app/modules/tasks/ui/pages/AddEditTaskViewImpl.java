package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskForm;
import com.constellio.app.modules.tasks.ui.components.fields.TaskFormImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

public class AddEditTaskViewImpl extends BaseViewImpl implements AddEditTaskView {
	
	private final AddEditTaskPresenter presenter;

	private TaskFormImpl recordForm;
	
	private TaskVO taskVO;

	public AddEditTaskViewImpl() {
		presenter = new AddEditTaskPresenter(this);
	}

	@Override
	public void setRecord(TaskVO taskVO) {
		this.taskVO = taskVO;
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
		return newForm();
	}

	private TaskFormImpl newForm() {
		recordForm = new TaskFormImpl(taskVO) {
			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked(viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}

			@Override
			public void reload() {
				replaceComponent(this, newForm());
			}

			@Override
			public void commit() {
				for (Field<?> field : fieldGroup.getFields()) {
					try {
						field.commit();
					} catch (SourceException | InvalidValueException e) {
					}
				}
			}
		};

		for (final Field<?> field : recordForm.getFields()) {
			if (field instanceof CustomTaskField) {
				field.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						presenter.customFieldValueChanged((CustomTaskField<?>) field);
					}
				});
			}
		}
		
		return recordForm;
	}

	@Override
	public TaskForm getForm() {
		return recordForm;
	}
}
