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

import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.FORM;
import static com.constellio.app.ui.i18n.i18n.$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.builders.TaskToVoBuilder;
import com.constellio.app.modules.tasks.ui.components.fields.TaskProgressPercentageField;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.logging.LoggingServices;

public class AddEditTaskPresenter extends SingleSchemaBasePresenter<AddEditTaskView> {
	TaskVO taskVO;
	transient TasksSearchServices tasksSearchServices;
	transient private TasksSchemasRecordsServices tasksSchemas;
	transient private TaskPresenterServices taskPresenterServices;
	private boolean editMode;
	transient private LoggingServices loggingServices;
	private String parentId;

	public AddEditTaskPresenter(AddEditTaskView view) {
		super(view, Task.DEFAULT_SCHEMA);
		initTransientObjects();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasWriteAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String parameters) {
		List<String> returnList = new ArrayList<>();
		Map<String, String> paramsMap = ParamUtils.getParamsMap(parameters);
		String id = paramsMap.get("id");
		if (StringUtils.isNotBlank(id)) {
			returnList.add(id);
		} else {
			String parentId = paramsMap.get("parentId");
			if (StringUtils.isNotBlank(parentId)) {
				returnList.add(parentId);
			}
		}
		return returnList;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		tasksSearchServices = new TasksSearchServices(tasksSchemas);
		loggingServices = modelLayerFactory.newLoggingServices();
		taskPresenterServices = new TaskPresenterServices(tasksSchemas, recordServices(), tasksSearchServices, loggingServices);
	}

	public TaskVO getTask() {
		return taskVO;
	}

	public void cancel() {
		view.navigateTo().tasksManagement();
	}

	public void saveButtonClicked(RecordVO recordVO) {
		if (recordVO == null) {
			return;
		}
		Task task = taskPresenterServices.toTask(new TaskVO(recordVO), toRecord(recordVO));
		if (task.getAssignee() == null) {
			task.setAssignationDate(null);
			task.setAssigner(null);
		} else {
			if (task.getWrappedRecord().isModified(tasksSchemas.userTask.assignee())) {
				task.setAssignationDate(TimeProvider.getLocalDate());
				task.setAssigner(getCurrentUser().getId());
			}
		}
		addOrUpdate(task.getWrappedRecord());
		if (StringUtils.isNotBlank(parentId)) {
			view.navigateTo().displayTask(parentId);
		} else {
			view.navigateTo().tasksManagement();
		}
	}

	public void initTaskVO(String parameters) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(parameters);
		String id = paramsMap.get("id");
		Task task;
		if (StringUtils.isNotBlank(id)) {
			editMode = true;
			task = tasksSchemas.getTask(id);
		} else {
			editMode = false;
			task = tasksSchemas.newTask();
			task.setAssignee(getCurrentUser().getId());
			task.setDueDate(TimeProvider.getLocalDate());
			parentId = paramsMap.get("parentId");
			task.setParentTask(parentId);
		}
		String completeTask = paramsMap.get("competeTask");
		if (StringUtils.isNotBlank(completeTask) && completeTask.equals("" + true)) {
			TaskStatus finishedStatus = tasksSearchServices
					.getFirstFinishedStatus();
			if (finishedStatus != null) {
				task.setStatus(finishedStatus.getId());
			}
		}
		taskVO = new TaskVO(new TaskToVoBuilder().build(task.getWrappedRecord(), FORM, view.getSessionContext()));
	}

	public String getViewTitle() {
		if (editMode) {
			return $("AddEditTaskView.editViewTitle");
		} else {
			return $("AddEditTaskView.addViewTitle");
		}
	}

	public void viewAssembled() {
		adjustProgressPercentageField();
	}

	private void adjustProgressPercentageField() {
		TaskProgressPercentageField progressPercentageField = (TaskProgressPercentageField) view.getForm()
				.getCustomField(Task.PROGRESS_PERCENTAGE);
		progressPercentageField.setVisible(editMode);
	}
}
