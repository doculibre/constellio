package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.ui.components.converters.TaskFollowerVOToStringConverter;
import com.constellio.app.modules.tasks.ui.components.fields.TaskFollowerFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

@SuppressWarnings("unchecked")
public class ListAddRemoveTaskFollowerField extends ListAddRemoveField<TaskFollowerVO, TaskFollowerFieldImpl> {

	private TaskFollowerVOToStringConverter converter = new TaskFollowerVOToStringConverter();

	@Override
	protected TaskFollowerFieldImpl newAddEditField() {
		return new TaskFollowerFieldImpl();
	}

	//FIXME should be always Vo or not
	@Override
	protected String getItemCaption(Object itemId) {
		if (itemId instanceof TaskFollowerVO) {
			return converter.convertToPresentation((TaskFollowerVO) itemId, String.class, getLocale());
		} else {
			return converter.convertToPresentation(toTaskFollowerVO((TaskFollower) itemId), String.class, getLocale());
		}
	}

	@Override
	protected ContainerButton addEditButton() {
		return new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				final TaskFollowerVO followerVO = convertObjectAsTaskFollowerVO(itemId);
				EditButton editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						removeValue(itemId);
						((Field<TaskFollowerVO>) addEditField).setValue(followerVO);
						addEditField.focus();
					}
				};
				if (!isEditButtonVisible(followerVO)) {
					editButton.setVisible(false);
				}
				editButton.setEnabled(!ListAddRemoveTaskFollowerField.this.isReadOnly() && ListAddRemoveTaskFollowerField.this.isEnabled());
				editButton.addStyleName(EDIT_BUTTON_STYLE_NAME);
				return editButton;
			}
		};
	}

	private TaskFollowerVO convertObjectAsTaskFollowerVO(Object itemId) {
		if(itemId instanceof TaskFollower) {
			return toTaskFollowerVO((TaskFollower) itemId);
		} else {
			return (TaskFollowerVO) itemId;
		}
	}

	private TaskFollowerVO toTaskFollowerVO(TaskFollower taskFollower) {
		return new TaskFollowerVO(taskFollower.getFollowerId(), taskFollower.getFollowTaskAssigneeModified(),
				taskFollower.getFollowSubTasksModified(), taskFollower.getFollowTaskStatusModified(),
				taskFollower.getFollowTaskCompleted(), taskFollower.getFollowTaskDeleted());
	}

}
