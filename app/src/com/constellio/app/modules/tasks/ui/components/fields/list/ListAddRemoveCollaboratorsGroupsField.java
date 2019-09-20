package com.constellio.app.modules.tasks.ui.components.fields.list;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationListCollaboratorsGoupsField;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.components.fields.list.TaskCollaboratorsGroupItem;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.TransactionalPropertyWrapper;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("unchecked")
public class ListAddRemoveCollaboratorsGroupsField extends ListAddRemoveField<TaskCollaboratorsGroupItem, TaskAssignationListCollaboratorsGoupsField> {
	private static final String AUTHORIZATIONS_PROPERTY_ID = "authorisations";

	private RecordVO taskVO;

	private List<TaskCollaboratorsGroupItem> taskCollaboratorGroupItem;

	private boolean currentUserIsCollaborator;

	public ListAddRemoveCollaboratorsGroupsField(RecordVO taskVO, boolean currentUserIsCollaborator) {
		this.taskVO = taskVO;
		this.currentUserIsCollaborator = currentUserIsCollaborator;
		init();
	}

	private void init() {
		taskCollaboratorGroupItem = new ArrayList<>();
		List<Boolean> writeAuthorizations = taskVO.get(Task.TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS);
		List<String> collaboratorsGroupsIds = taskVO.get(Task.TASK_COLLABORATORS_GROUPS);
		for (int i = 0; i < collaboratorsGroupsIds.size(); i++) {
			Boolean writeAuthorization = writeAuthorizations.get(i);
			String collaboratorId = collaboratorsGroupsIds.get(i);
			taskCollaboratorGroupItem.add(new TaskCollaboratorsGroupItem(collaboratorId, writeAuthorization));
		}
		setValue(taskCollaboratorGroupItem);
	}

	@Override
	protected TaskAssignationListCollaboratorsGoupsField newAddEditField() {
		return new TaskAssignationListCollaboratorsGoupsField();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setPropertyDataSource(Property newDataSource) {
		super.setPropertyDataSource(new TransactionalPropertyWrapper<>(new AbstractProperty<Object>() {
			@Override
			public Object getValue() {
				return taskCollaboratorGroupItem;
			}

			@Override
			@SuppressWarnings("unchecked")
			public void setValue(Object newValue) throws ReadOnlyException {
				taskCollaboratorGroupItem = (List<TaskCollaboratorsGroupItem>) newValue;
				if (taskCollaboratorGroupItem != null) {
					List<Boolean> newWriteAuthorizations = new ArrayList<>();
					List<String> newCollaboratorsGroupsIds = new ArrayList<>();
					for (TaskCollaboratorsGroupItem newItem : taskCollaboratorGroupItem) {
						if (newCollaboratorsGroupsIds.contains(newItem.getTaskCollaboratorGroup())) {
							int index = newCollaboratorsGroupsIds.indexOf(newItem.getTaskCollaboratorGroup());
							if (newItem.isTaskCollaboratorGroupWriteAuthorization() && !newWriteAuthorizations.get(index)) {
								newWriteAuthorizations.set(index, newItem.isTaskCollaboratorGroupWriteAuthorization());
							} else {
								continue;
							}
						}
						newWriteAuthorizations.add(newItem.isTaskCollaboratorGroupWriteAuthorization());
						newCollaboratorsGroupsIds.add(newItem.getTaskCollaboratorGroup());
					}
					taskVO.set(Task.TASK_COLLABORATORS_GROUPS, newCollaboratorsGroupsIds);
					taskVO.set(Task.TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS, newWriteAuthorizations);
				} else {
					taskVO.set(Task.TASK_COLLABORATORS_GROUPS, Collections.EMPTY_LIST);
					taskVO.set(Task.TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS, Collections.EMPTY_LIST);
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

	private String getCaption(String collaboratorId) {
		RecordIdToCaptionConverter itemConverter = new RecordIdToCaptionConverter();
		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		return itemConverter.convertToPresentation(collaboratorId, String.class, locale);
	}

	private class CollaboratorValuesContainer extends ValuesContainer {

		public CollaboratorValuesContainer(List<TaskCollaboratorsGroupItem> values) {
			super(values);
			addContainerProperty(AUTHORIZATIONS_PROPERTY_ID, getCaptionComponentClass(), null);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Property<?> getContainerProperty(final Object itemId, Object propertyId) {
			if (itemId != null) {
				TaskCollaboratorsGroupItem taskCollaboratorsGroupItem = (TaskCollaboratorsGroupItem) itemId;
				if (CAPTION_PROPERTY_ID.equals(propertyId)) {
					return new ObjectProperty<Component>(new Label(getCaption(taskCollaboratorsGroupItem.getTaskCollaboratorGroup())), Component.class);
				} else if (AUTHORIZATIONS_PROPERTY_ID.equals(propertyId)) {
					List<String> authorisations = new ArrayList<>(2);
					authorisations.add($("AuthorizationsView.short.READ"));
					if (taskCollaboratorsGroupItem.isTaskCollaboratorGroupWriteAuthorization()) {
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

	@Override
	protected boolean isEditButtonVisible(TaskCollaboratorsGroupItem item) {
		if (currentUserIsCollaborator) {
			return false;
		} else {
			return super.isEditButtonVisible(item);
		}
	}

	@Override
	protected boolean isDeleteButtonVisible(TaskCollaboratorsGroupItem item) {
		if (currentUserIsCollaborator) {
			return false;
		} else {
			return super.isDeleteButtonVisible(item);
		}
	}

}
