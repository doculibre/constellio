package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.api.extensions.params.FieldBindingExtentionParam;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationListRecordLookupField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskForm;
import com.constellio.app.modules.tasks.ui.components.fields.TaskFormImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.BooleanOptionGroup;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.Record;
import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.UI;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

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
	public void adjustAcceptedField(boolean isVisible) {
		BooleanOptionGroup field = (BooleanOptionGroup) getForm().getCustomField(BorrowRequest.ACCEPTED);
		if (field != null) {
			if (isVisible) {
				field.setVisible(true);
				field.setRequired(true);
			} else {
				field.setVisible(false);
				field.setRequired(false);
			}
		}
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
		recordForm = new TaskFormImpl(taskVO, presenter.isEditMode(), presenter.getUnavailableTaskTypes(), this, getConstellioFactories()) {
			@Override
			protected void saveButtonClick(final RecordVO viewObject) {
				if (presenter.isCompletedOrClosedStatus(viewObject) && presenter.isSubTaskPresentAndHaveCertainStatus(viewObject)) {
					ConfirmDialog.show(UI.getCurrent(), $("DisplayTaskView.subTaskSpecialCaseCompleteTask"),
							new ConfirmDialog.Listener() {
								@Override
								public void onClose(ConfirmDialog dialog) {
									if (dialog.isConfirmed()) {
										presenter.saveButtonClicked(viewObject);
									}
								}
							});
				} else {
					presenter.saveButtonClicked(viewObject);
				}
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

		getConstellioFactories().getAppLayerFactory().getExtensions()
				.forCollection(taskVO.getSchema().getCollection())
				.fieldBindingExtentions(new FieldBindingExtentionParam(recordForm.getFields()));

		for (final Field<?> field : recordForm.getFields()) {
			if (field instanceof CustomTaskField) {
				field.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						presenter.customFieldValueChanged((CustomTaskField<?>) field);
					}
				});
			}

			if(field instanceof TaskAssignationListRecordLookupField) {
				((TaskAssignationListRecordLookupField) field).addLookupValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						presenter.fieldValueChanged(field);
					}
				});
			}

			field.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.fieldValueChanged(field);
				}
			});
		}

		return recordForm;
	}

	@Override
	public TaskForm getForm() {
		return recordForm;
	}

	@Override
	public void navigateToWorkflow(String workflowId) {
		navigate().to(TaskViews.class).displayWorkflow(workflowId);
	}

	@Override
	public Record getWorkflow(String workflowId) {
		return presenter.getWorkflow(workflowId);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}
}
