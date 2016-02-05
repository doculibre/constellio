package com.constellio.app.modules.tasks.ui.components.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;

public class TaskFollowerFieldImpl extends CustomField<TaskFollowerVO> implements TaskFollowerField {
	
	private TaskFollowerVO taskFollowerVO;
	
	private BeanItem<TaskFollowerVO> taskFollowerItem;
	
	private FieldGroup fieldGroup;
	
	@PropertyId("dirty")
	private CheckBox dirtyField;
	@PropertyId("followerId")
	private LookupRecordField followerIdField;
	@PropertyId("followTaskStatusModified")
	private CheckBox followTaskStatusModifiedField;
	@PropertyId("followTaskAssigneeModified")
	private CheckBox followTaskAssigneeModifiedField;
	@PropertyId("followSubTasksModified")
	private CheckBox followSubTasksModifiedField;
	@PropertyId("followTaskCompleted")
	private CheckBox followTaskCompletedField;
	@PropertyId("followTaskDeleted")
	private CheckBox followTaskDeletedField;

	@Override
	protected Component initContent() {
		if (taskFollowerVO == null) {
			taskFollowerVO = new TaskFollowerVO();
		}
		taskFollowerItem = new BeanItem<>(taskFollowerVO);
		fieldGroup = new FieldGroup(taskFollowerItem);
		
		setPropertyDataSource(new AbstractProperty<TaskFollowerVO>() {
			@Override
			public TaskFollowerVO getValue() {
				boolean submittedValueValid = taskFollowerVO.getFollowerId() != null; 
				return  submittedValueValid ? taskFollowerVO : null;
			}

			@Override
			public void setValue(TaskFollowerVO newValue)
					throws com.vaadin.data.Property.ReadOnlyException {
				setInternalValue(newValue);
				taskFollowerVO = newValue != null ? newValue : new TaskFollowerVO();
				if (fieldGroup != null) {
					taskFollowerItem = new BeanItem<>(taskFollowerVO);
					fieldGroup.setItemDataSource(taskFollowerItem);
				}
			}

			@Override
			public Class<? extends TaskFollowerVO> getType() {
				return TaskFollowerVO.class;
			}
		});
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("99%");
		mainLayout.setHeightUndefined();
		mainLayout.setSpacing(true);

		dirtyField = new CheckBox();
		dirtyField.setCaption($("TaskFollowerField.dirty"));
		dirtyField.setVisible(false);

		followerIdField = new LookupRecordField(User.SCHEMA_TYPE);
		followerIdField.setCaption($("TaskFollowerField.followerId"));
		
		followTaskStatusModifiedField = new CheckBox();
		followTaskStatusModifiedField.setCaption($("TaskFollowerField.followTaskStatusModified"));
		
		followTaskAssigneeModifiedField = new CheckBox();
		followTaskAssigneeModifiedField.setCaption($("TaskFollowerField.followTaskAssigneeModified"));
		
		followSubTasksModifiedField = new CheckBox();
		followSubTasksModifiedField.setCaption($("TaskFollowerField.followSubTasksModified"));
		
		followTaskCompletedField = new CheckBox();
		followTaskCompletedField.setCaption($("TaskFollowerField.followTaskCompleted"));
		
		followTaskDeletedField = new CheckBox();
		followTaskDeletedField.setCaption($("TaskFollowerField.followTaskDeleted"));

		mainLayout.addComponent(dirtyField);
		mainLayout.addComponent(followerIdField);
		mainLayout.addComponent(followTaskStatusModifiedField);
		mainLayout.addComponent(followTaskAssigneeModifiedField);
		mainLayout.addComponent(followSubTasksModifiedField);
		mainLayout.addComponent(followTaskCompletedField);
		mainLayout.addComponent(followTaskDeletedField);
		
		fieldGroup.bindMemberFields(this);

		return mainLayout;
	}

	@Override
	public Class<? extends TaskFollowerVO> getType() {
		return TaskFollowerVO.class;
	}
	
	private boolean isInvalidFieldValue() {
		boolean invalidFieldValue;
		String followerIdValue = followerIdField.getValue();
		Boolean followTaskStatusModifiedValue = (Boolean) followTaskStatusModifiedField.getValue();
		Boolean followTaskAssigneeModifiedValue = followTaskAssigneeModifiedField.getValue();
		Boolean followSubTasksModifiedValue = followSubTasksModifiedField.getValue();
		Boolean followTaskCompletedValue = followTaskCompletedField.getValue();
		Boolean followTaskDeletedValue = followTaskDeletedField.getValue();
		if (followerIdValue == null && 
				(Boolean.TRUE.equals(followTaskStatusModifiedValue) || 
						Boolean.TRUE.equals(followTaskAssigneeModifiedValue) || 
						Boolean.TRUE.equals(followSubTasksModifiedValue) ||  
						Boolean.TRUE.equals(followTaskCompletedValue) ||  
						Boolean.TRUE.equals(followTaskDeletedValue))) {
			invalidFieldValue = true;
		} else if (followerIdValue != null && 
				Boolean.FALSE.equals(followTaskStatusModifiedValue) && 
				Boolean.FALSE.equals(followTaskAssigneeModifiedValue) && 
				Boolean.FALSE.equals(followSubTasksModifiedValue) && 
				Boolean.FALSE.equals(followTaskCompletedValue) && 
				Boolean.FALSE.equals(followTaskDeletedValue)) {
			invalidFieldValue = true;
		} else {
			invalidFieldValue = false;
		}
		return invalidFieldValue;
	}

	@Override
	public void commit()
			throws SourceException, InvalidValueException {
		if (!isInvalidFieldValue()) {
			try {
				fieldGroup.commit();
			} catch (CommitException e) {
				throw new InvalidValueException(e.getMessage());
			}
			super.commit();
		} 
	}
	
}
