package com.constellio.app.modules.tasks.ui.pages.tasks;

import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.FORM;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.ui.components.TaskFieldFactory;
import com.constellio.app.modules.tasks.ui.components.fields.*;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveWorkflowInclusiveDecisionFieldImpl;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.jgoodies.common.base.Strings;
import com.vaadin.ui.Field;
import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
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
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;

public class AddEditTaskPresenter extends SingleSchemaBasePresenter<AddEditTaskView> {
	TaskVO taskVO;
	transient TasksSearchServices tasksSearchServices;
	transient private TasksSchemasRecordsServices tasksSchemas;
	transient private TaskPresenterServices taskPresenterServices;
	private boolean editMode;
	private boolean completeMode;
	transient private LoggingServices loggingServices;
	private String parentId;
	private String workflowId;
	private TaskToVOBuilder voBuilder = new TaskToVOBuilder();
	private ListAddRemoveWorkflowInclusiveDecisionFieldImpl listAddRemoveWorkflowInclusiveDecision;
	private TaskDecisionField field;
	private TasksSchemasRecordsServices tasksSchemasRecordsServices;
	List<String> finishedOrClosedStatuses;

	boolean inclusideDecision = false;
	boolean exclusiveDecision = false;

	public static final String IS_INCLUSIVE_DECISION = "isInclusiveDecision";

	public AddEditTaskPresenter(AddEditTaskView view) {
		super(view, Task.DEFAULT_SCHEMA);
		initTransientObjects();
		tasksSchemasRecordsServices = new TasksSchemasRecordsServices(collection, appLayerFactory);
		finishedOrClosedStatuses = getFinishedOrClosedStatuses();
		tasksSchemasRecordsServices = new TasksSchemasRecordsServices(collection,appLayerFactory);
	}

	private List<String> getFinishedOrClosedStatuses() {
		return new RecordUtils().toWrappedRecordIdsList(tasksSchemasRecordsServices.getFinishedOrClosedStatuses());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		boolean isTerminatedOrClosed = finishedOrClosedStatuses.contains(tasksSchemasRecordsServices.wrapTask(restrictedRecord).getStatus());
		return user.hasWriteAccess().on(restrictedRecord) && !isTerminatedOrClosed;
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
		if (StringUtils.isNotBlank(workflowId)) {
			view.navigateToWorkflow(workflowId);
		} else {
			view.navigate().to(TaskViews.class).taskManagement();
		}
	}



	public void saveButtonClicked(RecordVO recordVO) {
		if (recordVO == null) {
			return;
		}
		Task task = taskPresenterServices.toTask(new TaskVO(recordVO), toRecord(recordVO));

		try {

			if(isEditMode() && finishedOrClosedStatuses.contains(task.getStatus())) {
				if (inclusideDecision) {
					List<String> decisionList = listAddRemoveWorkflowInclusiveDecision.getValue();
					if(decisionList == null && decisionList.size() <= 0) {
						view.showErrorMessage($("AddEditTaskPresenter.error.decision"));
						return ;
					}
				}

				if(exclusiveDecision && finishedOrClosedStatuses.contains(task.getStatus())) {
					String decision = field.getFieldValue();

					if(decision == null && Strings.isBlank(decision)) {
						view.showErrorMessage($("AddEditTaskPresenter.error.decision"));
						return;
					}
				}
			}

			if (completeMode && tasksSchemas.isRequestTask(task)) {
				task.set(RequestTask.RESPONDANT, getCurrentUser().getId());
			}
			if (task.getAssignee() == null) {
				task.setAssignationDate(null);
				task.setAssigner(null);
			} else {
				if (task.getWrappedRecord().isModified(tasksSchemas.userTask.assignee())) {
					task.setAssignationDate(TimeProvider.getLocalDate());
					task.setAssigner(getCurrentUser().getId());
					Field<?> field = getAssignerField();
					if(field != null && field.getValue() != null) {
						task.setAssigner((String) field.getValue());
					}
				}
			}
			addOrUpdate(task.getWrappedRecord());
			if (StringUtils.isNotBlank(workflowId)) {
				view.navigateToWorkflow(workflowId);
			} else if (StringUtils.isNotBlank(parentId)) {
				view.navigate().to(TaskViews.class).displayTask(parentId);
			} else {
				view.navigate().to(TaskViews.class).taskManagement();
			}
		} catch (final IcapException e) {
			view.showErrorMessage(e.getMessage());
		}
	}

	private Field getAssignerField() {
		TaskForm form = view.getForm();
		if(form instanceof TaskFormImpl) {
			return ((TaskFormImpl) form).getField(Task.ASSIGNER);
		}
		return null;
	}

	public void initTaskVO(String parameters) {
		Map<String, String> paramsMap = ParamUtils.getParamsMap(parameters);
		String id = paramsMap.get("id");
		Task task;
		if (StringUtils.isNotBlank(id)) {
			editMode = true;
			task = tasksSchemas.getTask(id);
			setSchemaCode(task.getSchemaCode());
		} else {
			editMode = false;
			task = tasksSchemas.newTask();
			task.setAssignee(getCurrentUser().getId());
			task.setDueDate(TimeProvider.getLocalDate());
			parentId = paramsMap.get("parentId");
			task.setParentTask(parentId);

			String folderId = paramsMap.get("folderId");
			if (folderId != null) {
				new RMTask(task).setLinkedFolders(asList(folderId));
			}
			String documentId = paramsMap.get("documentId");
			if (documentId != null) {
				new RMTask(task).setLinkedDocuments(asList(documentId));
			}
		}
		completeMode = "true".equals(paramsMap.get("completeTask"));
		if (completeMode) {
			TaskStatus finishedStatus = tasksSearchServices
					.getFirstFinishedStatus();
			if (finishedStatus != null) {
				task.setStatus(finishedStatus.getId());
			}
		}
		workflowId = paramsMap.get("workflowId");
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
		adjustTypeField();
		adjustProgressPercentageField();
		adjustDecisionField();
		adjustQuestionField();
		adjustInclusiveDecisionField();
		adjustRelativeDueDate();
		adjustAcceptedField();
		adjustReasonField();
		adjustAssignerField();
	}

	private void adjustQuestionField() {
		TaskQuestionFieldImpl questionField = (TaskQuestionFieldImpl) view.getForm().getCustomField(Task.QUESTION);
		if(questionField != null) {
			if(field != null) {
				questionField.setVisible(field.isVisible());
			}
			questionField.setReadOnly(true);
		}
	}

	private void adjustAssignerField() {
		Field assignerField = getAssignerField();
		if(assignerField != null) {
			assignerField.setValue(getCurrentUser().getId());
		}
	}

	private void adjustProgressPercentageField() {
		TaskProgressPercentageField progressPercentageField = (TaskProgressPercentageField) view.getForm()
				.getCustomField(Task.PROGRESS_PERCENTAGE);
		progressPercentageField.setVisible(editMode);
	}

	private void adjustDecisionField() {
		field = (TaskDecisionField) view.getForm().getCustomField(Task.DECISION);

		if (field != null) {
			try {
				BetaWorkflowTask task = loadTask();
				boolean isInclusiveDecision;

				try
				{
					isInclusiveDecision = Boolean.TRUE.equals(task.get(IS_INCLUSIVE_DECISION));
				} catch (Exception exception) {
					isInclusiveDecision = false;
				}


				if (!task.hasDecisions() || task.getModelTask() == null || isInclusiveDecision) {
					field.setVisible(false);
					return;
				} else {
					exclusiveDecision = true;
					field.setVisible(true);

					if(completeMode) {
						field.setRequired(true);
					}
				}

				for (String code : task.getNextTasksDecisionsCodes()) {
					field.addItem(code);
				}

			} catch (NoSuchRecordWithId e) {
				field.setVisible(false);
			}
		}
	}

	private void adjustInclusiveDecisionField() {
		listAddRemoveWorkflowInclusiveDecision = (ListAddRemoveWorkflowInclusiveDecisionFieldImpl) ((RecordForm)view.getForm()).getField(TaskFieldFactory.INCLUSIVE_DECISION);

		if(listAddRemoveWorkflowInclusiveDecision != null) {

			try {
				BetaWorkflowTask task = loadTask();
				boolean isInclusiveDecision;

				try
				{
					isInclusiveDecision = Boolean.TRUE.equals(task.get(IS_INCLUSIVE_DECISION));
				} catch (Exception exception) {
					isInclusiveDecision = true;
				}

			if (!task.hasDecisions() || task.getModelTask() == null || !isInclusiveDecision) {
					listAddRemoveWorkflowInclusiveDecision.setVisible(false);
					return;
			} else {
					listAddRemoveWorkflowInclusiveDecision.setVisible(true);
					inclusideDecision = true;
					if(completeMode) {
						listAddRemoveWorkflowInclusiveDecision.setRequired(true);
					}
			}

			for (String code : task.getNextTasksDecisionsCodes()) {
				listAddRemoveWorkflowInclusiveDecision.addItem(code);
			}
			} catch (NoSuchRecordWithId e) {
				listAddRemoveWorkflowInclusiveDecision.setVisible(false);
			}
		}
	}

	private void adjustAcceptedField() {
		List<String> acceptedSchemas = new ArrayList<>(asList(BorrowRequest.FULL_SCHEMA_NAME, ReturnRequest.FULL_SCHEMA_NAME,
				ReactivationRequest.FULL_SCHEMA_NAME, ExtensionRequest.FULL_SCHEMA_NAME));
		String schemaCode = getTask().getSchema().getCode();
		if (acceptedSchemas.contains(schemaCode)) {
			try {
				if (!completeMode) {
					view.adjustAcceptedField(false);
					return;
				}
				view.adjustAcceptedField(true);

			} catch (NoSuchRecordWithId e) {
				view.adjustAcceptedField(false);
			}
		}
	}

	private void adjustReasonField() {
		CustomTaskField field = view.getForm().getCustomField(Task.REASON);
		if (field != null) {
			try {
				Task task = loadTask();

				if (!completeMode) {
					field.setVisible(false);
					return;
				}
				field.setVisible(true);

			} catch (NoSuchRecordWithId e) {
				field.setVisible(false);
			}
		}
	}

	private void adjustRelativeDueDate() {
		TaskRelativeDueDateField field = (TaskRelativeDueDateField) view.getForm().getCustomField(Task.RELATIVE_DUE_DATE);
		try {
			Task task = loadTask();

			field.setVisible(task.isModel());
		} catch (NoSuchRecordWithId e) {
			field.setVisible(false);
		}
	}

	void reloadForm() {
		view.getForm().reload();
		adjustQuestionField();
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
		Field typeField = getTypeField();
		if(typeField != null) {
			if(tasksSchemas.isRequestTask(getTask())) {
				typeField.setReadOnly(true);
			}
		}
	}

	private Field getTypeField() {
		TaskForm form = view.getForm();
		if(form instanceof TaskFormImpl) {
			return ((TaskFormImpl) form).getField(Task.TYPE);
		}
		return null;
	}

	boolean isReloadRequiredAfterTaskTypeChange() {
		boolean reload = false;
		String currentSchemaCode = getSchemaCode();
		if(isTaskTypeFieldVisible()) {
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
				if (matchingMetadata.getDataEntry().getType() == DataEntryType.MANUAL && !matchingMetadata.isSystemReserved()) {
					Object voMetadataValue = taskVO.get(metadataVO);
					Object defaultValue = matchingMetadata.getDefaultValue();
					Object voDefaultValue = metadataVO.getDefaultValue();
					if (voMetadataValue == null && defaultValue == null) {
						task.getWrappedRecord().set(matchingMetadata, voMetadataValue);
					} else if (voMetadataValue != null && !voMetadataValue.equals(voDefaultValue)) {
						task.getWrappedRecord().set(matchingMetadata, voMetadataValue);
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

	private BetaWorkflowTask loadTask() {
		TaskProgressPercentageField progressPercentageField = (TaskProgressPercentageField) view.getForm()
				.getCustomField(Task.PROGRESS_PERCENTAGE);
		progressPercentageField.setVisible(editMode);
		return tasksSchemas.getBetaWorkflowTask(taskVO.getId());
	}

	public boolean isEditMode() {
		return editMode;
	}

	public Record getWorkflow(String workflowId) {
		return recordServices().getDocumentById(workflowId);
	}

	public boolean isTaskTypeFieldVisible() {
		return view.getForm().getCustomField(Task.TYPE) != null;
	}

	public List<String> getUnavailableTaskTypes() {
		return !tasksSchemas.isRequestTask(getTask())? searchServices().searchRecordIds(
				LogicalSearchQueryOperators.from(tasksSchemas.taskTypeSchemaType()).where(Schemas.CODE)
						.isIn(asList(BorrowRequest.SCHEMA_NAME, ReturnRequest.SCHEMA_NAME, ExtensionRequest.SCHEMA_NAME, ReactivationRequest.SCHEMA_NAME))): null;
	}
}
