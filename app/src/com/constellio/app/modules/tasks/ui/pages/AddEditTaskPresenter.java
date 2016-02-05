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
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskProgressPercentageField;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.logging.LoggingServices;

public class AddEditTaskPresenter extends SingleSchemaBasePresenter<AddEditTaskView> {
	
	TaskVO taskVO;
	transient TasksSearchServices tasksSearchServices;
	transient private TasksSchemasRecordsServices tasksSchemas;
	transient private TaskPresenterServices taskPresenterServices;
	private boolean editMode;
	transient private LoggingServices loggingServices;
	private String parentId;
	private TaskToVOBuilder voBuilder = new TaskToVOBuilder();

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

	public void cancelButtonClicked() {
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
		String completeTask = paramsMap.get("completeTask");
		if (StringUtils.isNotBlank(completeTask) && completeTask.equals("" + true)) {
			TaskStatus finishedStatus = tasksSearchServices
					.getFirstFinishedStatus();
			if (finishedStatus != null) {
				task.setStatus(finishedStatus.getId());
			}
		}
		taskVO = new TaskVO(new TaskToVOBuilder().build(task.getWrappedRecord(), FORM, view.getSessionContext()));
		view.setRecord(taskVO);
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
	
	void reloadFormAfterFieldChanged() {
		commitForm();
		reloadForm();
	}

	void reloadForm() {
		view.getForm().reload();
	}

	void commitForm() {
		view.getForm().commit();
	}

	String getTypeFieldValue() {
		return (String) view.getForm().getCustomField(Task.TYPE).getFieldValue();
	}

	public void customFieldValueChanged(CustomTaskField<?> customField) {
		adjustCustomFields(customField);
	}

	void adjustCustomFields(CustomTaskField<?> customField) {
		adjustTypeField();
		boolean reload = isReloadRequiredAfterTaskTypeChange();
		if (reload) {
			reloadFormAfterTaskTypeChange();
		}
	}

	void adjustTypeField() {
		// Nothing to adjust
	}

	boolean isReloadRequiredAfterTaskTypeChange() {
		boolean reload;
		String currentSchemaCode = getSchemaCode();
		String taskTypeRecordId = getTypeFieldValue();
		if (StringUtils.isNotBlank(taskTypeRecordId)) {
			String schemaCodeForTaskTypeRecordId = tasksSchemas.getSchemaCodeForTaskTypeRecordId(taskTypeRecordId);
			if (schemaCodeForTaskTypeRecordId != null) {
				reload = !currentSchemaCode.equals(schemaCodeForTaskTypeRecordId);
			} else
				reload = !currentSchemaCode.equals(Task.DEFAULT_SCHEMA);
		} else {
			reload = !currentSchemaCode.equals(Task.DEFAULT_SCHEMA);
		}
		return reload;
	}

	void reloadFormAfterTaskTypeChange() {
		String taskTypeId = getTypeFieldValue();
		
		String newSchemaCode;
		if (taskTypeId != null) {
			newSchemaCode = tasksSearchServices.getSchemaCodeForTaskTypeRecordId(taskTypeId);
		} else {
			newSchemaCode = Task.DEFAULT_SCHEMA;
		}
		if (newSchemaCode == null) {
			newSchemaCode = Task.DEFAULT_SCHEMA;
		}
 
		Record taskRecord = toRecord(taskVO);
		Task task = new Task(taskRecord, types());

		setSchemaCode(newSchemaCode);
		task.changeSchemaTo(newSchemaCode);
		MetadataSchema newSchema = task.getSchema();

		commitForm();
		for (MetadataVO metadataVO : taskVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);

			try {
				Metadata matchingMetadata = newSchema.getMetadata(metadataCodeWithoutPrefix);
				if (matchingMetadata.getDataEntry().getType() == DataEntryType.MANUAL) {
					Object metadataValue = taskVO.get(metadataVO);
					Object defaultValue = metadataVO.getDefaultValue();
					if (metadataValue == null || !metadataValue.equals(defaultValue)) {
						task.getWrappedRecord().set(matchingMetadata, metadataValue);
					}
				}
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				// Ignore
			}
		}

		taskVO = voBuilder.build(task.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());

		view.setRecord(taskVO);
		reloadForm();
	}
}
