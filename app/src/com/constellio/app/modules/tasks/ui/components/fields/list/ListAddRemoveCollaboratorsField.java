package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationListCollaboratorsField;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.components.fields.list.TaskCollaboratorItem;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.TransactionalPropertyWrapper;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("unchecked")
public class ListAddRemoveCollaboratorsField extends ListAddRemoveField<TaskCollaboratorItem, TaskAssignationListCollaboratorsField> {
	private static final String AUTHORIZATIONS_PROPERTY_ID = "authorisations";

	private RecordVO taskVO;

	private List<TaskCollaboratorItem> taskCollaboratorItem;

	private boolean currentUserIsCollaborator = false;

	private boolean writeButtonVisible = true;

	public ListAddRemoveCollaboratorsField(RecordVO taskVO) {
		this.taskVO = taskVO;
		init();
	}

	private void init() {
		taskCollaboratorItem = new ArrayList<>();
		List<Boolean> writeAuthorizations = taskVO.get(Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS);
		List<String> collaboratorsIds = taskVO.get(Task.TASK_COLLABORATORS);
		for (int i = 0; i < collaboratorsIds.size(); i++) {
			Boolean writeAuthorization = writeAuthorizations.get(i);
			String collaboratorId = collaboratorsIds.get(i);
			taskCollaboratorItem.add(new TaskCollaboratorItem(collaboratorId, writeAuthorization));
		}
		setValue(taskCollaboratorItem);
	}

	@Override
	protected TaskAssignationListCollaboratorsField newAddEditField() {
		return new TaskAssignationListCollaboratorsField(writeButtonVisible);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setPropertyDataSource(Property newDataSource) {
		super.setPropertyDataSource(new TransactionalPropertyWrapper<>(new AbstractProperty<Object>() {
			@Override
			public Object getValue() {
				return taskCollaboratorItem;
			}

			@Override
			@SuppressWarnings("unchecked")
			public void setValue(Object newValue) throws ReadOnlyException {
				taskCollaboratorItem = (List<TaskCollaboratorItem>) newValue;
				if (taskCollaboratorItem != null) {
					List<Boolean> newWriteAuthorizations = new ArrayList<>();
					List<String> newCollaboratorsIds = new ArrayList<>();
					for (TaskCollaboratorItem newItem : taskCollaboratorItem) {
						if (newCollaboratorsIds.contains(newItem.getTaskCollaborator())) {
							int index = newCollaboratorsIds.indexOf(newItem.getTaskCollaborator());
							if (newItem.isTaskCollaboratorsWriteAuthorization() && !newWriteAuthorizations.get(index)) {
								newWriteAuthorizations.set(index, newItem.isTaskCollaboratorsWriteAuthorization());
							} else {
								continue;
							}
						}
						newWriteAuthorizations.add(newItem.isTaskCollaboratorsWriteAuthorization());
						newCollaboratorsIds.add(newItem.getTaskCollaborator());
					}
					taskVO.set(Task.TASK_COLLABORATORS, newCollaboratorsIds);
					taskVO.set(Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS, newWriteAuthorizations);
				} else {
					taskVO.set(Task.TASK_COLLABORATORS, Collections.EMPTY_LIST);
					taskVO.set(Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS, Collections.EMPTY_LIST);
				}
			}

			@Override
			public Class<? extends Object> getType() {
				return List.class;
			}
		}));
	}

	@Override
	protected void setValuesContainer() {
		valuesContainer = new CollaboratorValuesContainer(new ArrayList<>());
	}

	@Override
	protected void addValue(TaskCollaboratorItem value) {
		if (value != null) {
			Iterator<TaskCollaboratorItem> iterator = (Iterator<TaskCollaboratorItem>) valuesAndButtonsContainer.getItemIds().iterator();
			while (iterator.hasNext()) {
				TaskCollaboratorItem taskCollaboratorItem = iterator.next();
				if (value.getTaskCollaborator().equals(taskCollaboratorItem.getTaskCollaborator())) {
					super.addValue(value);
					removeValue(taskCollaboratorItem);
					return;
				}
			}
			super.addValue(value);
			if (!writeButtonVisible) {
				TaskCollaboratorItem taskCollaboratorItem = new TaskCollaboratorItem();
				taskCollaboratorItem.setCollaboratorReadAuthorization(false);
				addEditField.setValue(taskCollaboratorItem);
				notifyValueChange();
			}
		}
	}

	private String getCaption(String collaboratorId) {
		RecordIdToCaptionConverter itemConverter = new RecordIdToCaptionConverter();
		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		return itemConverter.convertToPresentation(collaboratorId, String.class, locale);
	}

	private class CollaboratorValuesContainer extends ValuesContainer {

		public CollaboratorValuesContainer(List<TaskCollaboratorItem> values) {
			super(values, null);
			addContainerProperty(AUTHORIZATIONS_PROPERTY_ID, getCaptionComponentClass(), null);
		}

		@Override
		public Property<?> getContainerProperty(final Object itemId, Object propertyId) {
			if (itemId != null) {
				TaskCollaboratorItem taskCollaboratorItem = (TaskCollaboratorItem) itemId;
				if (CAPTION_PROPERTY_ID.equals(propertyId)) {
					return new ObjectProperty<Component>(new Label(getCaption(taskCollaboratorItem.getTaskCollaborator())), Component.class);
				} else if (AUTHORIZATIONS_PROPERTY_ID.equals(propertyId)) {
					List<String> authorisations = new ArrayList<>(2);
					authorisations.add($("AuthorizationsView.short.READ"));
					if (taskCollaboratorItem.isTaskCollaboratorsWriteAuthorization()) {
						authorisations.add($("AuthorizationsView.short.WRITE"));
					}
					return new ObjectProperty<Component>(new Label(StringUtils.join(authorisations, "/")), Component.class);
				} else {
					return getExtraColumnProperty(itemId, propertyId);
				}
			} else {
				return new ObjectProperty<>(null);
			}
		}
	}

	public void setCurrentUserIsCollaborator(boolean currentUserIsCollaborator) {
		this.currentUserIsCollaborator = currentUserIsCollaborator;
	}

	public void writeButtonIsVisible(boolean writeButtonIsVisible) {
		this.writeButtonVisible = writeButtonIsVisible;
	}

	@Override
	protected boolean isEditButtonVisible(TaskCollaboratorItem item) {
		if (currentUserIsCollaborator) {
			return false;
		} else {
			return super.isEditButtonVisible(item);
		}
	}

	@Override
	protected boolean isDeleteButtonVisible(TaskCollaboratorItem item) {
		if (currentUserIsCollaborator) {
			return false;
		} else {
			return super.isDeleteButtonVisible(item);
		}
	}
}
