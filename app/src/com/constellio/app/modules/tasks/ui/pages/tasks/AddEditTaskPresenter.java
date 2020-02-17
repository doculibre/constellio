package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.extensions.TaskAddEditTaskPresenterExtension;
import com.constellio.app.modules.tasks.extensions.action.Action;
import com.constellio.app.modules.tasks.extensions.api.TaskModuleExtensions;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormParams;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormRetValue;
import com.constellio.app.modules.tasks.extensions.param.PromptUserParam;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.TaskUser;
import com.constellio.app.modules.tasks.model.wrappers.request.*;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.components.TaskFieldFactory;
import com.constellio.app.modules.tasks.ui.components.fields.*;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveCollaboratorsField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveCollaboratorsGroupsField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveTaskFollowerField;
import com.constellio.app.modules.tasks.ui.components.fields.list.ListAddRemoveWorkflowInclusiveDecisionFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.ErrorDisplayUtil;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.TimedCache;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.jgoodies.common.base.Strings;
import com.vaadin.ui.Field;
import com.vaadin.ui.OptionGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static com.constellio.app.modules.tasks.model.wrappers.Task.*;
import static com.constellio.app.modules.tasks.ui.components.fields.AuthorizationFieldItem.READ;
import static com.constellio.app.modules.tasks.ui.components.fields.AuthorizationFieldItem.WRITE;
import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.FORM;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class AddEditTaskPresenter extends SingleSchemaBasePresenter<AddEditTaskView> {
	public static final String ASSIGNATION_MODES = "assignationModes";
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
	private static Logger LOGGER = LoggerFactory.getLogger(AddEditTaskPresenter.class);
	List<String> finishedOrClosedStatuses;
	private RMModuleExtensions rmModuleExtensions;
	private String previousPage;

	boolean inclusideDecision = false;
	boolean exclusiveDecision = false;
	boolean isCompletedOrClosedOnInitialization = false;

	String originalAssigner;
	String originalAssignedTo;

	public static final String IS_INCLUSIVE_DECISION = "isInclusiveDecision";
	public static final String INCLUSIVE_DECISION = "inclusiveDecision";

	public AddEditTaskPresenter(AddEditTaskView view) {
		super(view, Task.DEFAULT_SCHEMA);
		initTransientObjects();
		tasksSchemasRecordsServices = new TasksSchemasRecordsServices(collection, appLayerFactory);
		finishedOrClosedStatuses = getFinishedOrClosedStatuses();
		tasksSchemasRecordsServices = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
	}

	public AddEditTaskView getView() {
		return view;
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
		TasksSchemasRecordsServices tasksSchemasRecordsServices = new TasksSchemasRecordsServices(restrictedRecord.getCollection(), appLayerFactory);
		TaskStatusType statusType = tasksSchemasRecordsServices.wrapTask(restrictedRecord).getStatusType();

		boolean isModelTaskAndUserIsWorkflowManager = false;

		if (restrictedRecord.getSchemaCode().startsWith(Task.SCHEMA_TYPE + "_")) {
			Task task = tasksSchemasRecordsServices.wrapTask(restrictedRecord);
			isModelTaskAndUserIsWorkflowManager = getCurrentUser().has(TasksPermissionsTo.MANAGE_WORKFLOWS).globally()
												  && task.isModel();
		}
		tasksSchemasRecordsServices.wrapTask(restrictedRecord);
		return user.hasWriteAccess().on(restrictedRecord) && !(statusType != null && statusType.isFinishedOrClosed()) || isModelTaskAndUserIsWorkflowManager;
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
		final Task task = taskPresenterServices.toTask(new TaskVO(recordVO), toRecord(recordVO));

		try {

			if (isEditMode() && finishedOrClosedStatuses.contains(task.getStatus())) {
				if (inclusideDecision) {
					List<String> decisionList = listAddRemoveWorkflowInclusiveDecision.getValue();
					if (decisionList == null && decisionList.size() <= 0) {
						view.showErrorMessage($("AddEditTaskPresenter.error.decision"));
						return;
					}
				}

				if (exclusiveDecision && finishedOrClosedStatuses.contains(task.getStatus())) {
					String decision = field.getFieldValue();

					if (decision == null && Strings.isBlank(decision)) {
						view.showErrorMessage($("AddEditTaskPresenter.error.decision"));
						return;
					}
				}
			}

			if (!isEditMode() && task.getCreatedBy() == null) {
				task.setCreatedBy(getCurrentUser());
				task.setCreatedOn(new LocalDateTime());
			}

			if (completeMode && tasksSchemas.isRequestTask(task)) {
				task.set(RequestTask.RESPONDANT, getCurrentUser().getId());
			}

			if (task.getAssigner() == null) {

				if ((task.getAssigneeUsersCandidates() != null && task.getAssigneeUsersCandidates().size() != 0 && task.getWrappedRecord().isModified(tasksSchemas.userTask.assigneeUsersCandidates()))
					|| task.getAssigneeGroupsCandidates() != null && task.getAssigneeGroupsCandidates().size() != 0 && task.getWrappedRecord().isModified(tasksSchemas.userTask.assigneeGroupsCandidates())
					|| task.getWrappedRecord().isModified(tasksSchemas.userTask.assignee())) {
					task.setAssignationDate(TimeProvider.getLocalDate());
					task.setAssigner(getCurrentUser().getId());

					Field<?> field = getAssignerField();
					if (originalAssigner == null && field != null && field.getValue() != null ||
						field != null && field.getValue() != null && originalAssigner != null && !originalAssigner.equals(field.getValue())) {
						task.setAssigner((String) field.getValue());
					}
				}
			} else {
				if (task.getAssignee() == null
					&& (task.getAssigneeGroupsCandidates() == null || task.getAssigneeGroupsCandidates().size() == 0)
					&& (task.getAssigneeUsersCandidates() == null || task.getAssigneeUsersCandidates().size() == 0)) {
					task.setAssignationDate(null);
					task.setAssigner(null);
				} else {
					if (task.getWrappedRecord().isModified(tasksSchemas.userTask.assignee())) {
						task.setAssignationDate(TimeProvider.getLocalDate());
						task.setAssigner(getCurrentUser().getId());
						Field<?> field = getAssignerField();
						if (originalAssigner == null && field != null && field.getValue() != null
							|| field != null && field.getValue() != null && originalAssigner != null && !originalAssigner.equals(field.getValue())) {
							task.setAssigner((String) field.getValue());
						}
					} else if (task.getWrappedRecord().isModified(tasksSchemas.userTask.assigner())) {
						Field<?> field = getAssignerField();
						task.setAssigner((String) field.getValue());
						if (task.getAssignedOn() == null) {
							task.setAssignationDate(TimeProvider.getLocalDate());
						}
					}
				}
			}

			if (!task.isModel() && task.getDueDate() == null && task.getRelativeDueDate() != null && task.getAssignedOn() != null) {
				task.setDueDate(task.getAssignedOn().plusDays(task.getRelativeDueDate()));
			}

			TaskModuleExtensions taskModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
					.forModule(TaskModule.ID);

			// No transaction because the extention return record to be save
			if (taskModuleExtensions != null) {
				TaskFormRetValue taskFormRetValue = taskModuleExtensions.taskFormExtentions(new TaskFormParams(this, task));
				for (Record currentRecord : taskFormRetValue.getRecords()) {
					saveRecord(task, currentRecord, taskFormRetValue.isSaveWithValidation(currentRecord));
				}
			}
			boolean isPromptUser = false;

			// this is in case of special validation that would only occur when saving the task.
			Transaction transaction = new Transaction();
			transaction.addUpdate(task.getWrappedRecord());
			recordServices().prepareRecords(transaction);

			if (!isCompletedOrClosedOnInitialization && isCompleted(recordVO)) {
				isPromptUser = rmModuleExtensions.isPromptUser(new PromptUserParam(task, new Action() {
					@Override
					public void doAction() {
						saveAndNavigate(task);
					}
				}));
			}
			if (!isPromptUser) {
				saveAndNavigate(task);
			}

		} catch (final IcapException e) {
			view.showErrorMessage(e.getMessage());
		} catch (ValidationException e) {
			ErrorDisplayUtil.showBackendValidationException(e.getValidationErrors());
		} catch (RecordServicesException.ValidationException e) {
			ErrorDisplayUtil.showBackendValidationException(e.getErrors());
		}
	}

	private void saveAndNavigate(Task task) {
		saveRecord(task, task.getWrappedRecord(), true);

		if (previousPage != null) {
			URI location = ConstellioUI.getCurrent().getPage().getLocation();
			view.navigate().to(CoreViews.class).navigateTo(location.getPath(), previousPage, false);
			//view.showMessage("tache créée avec succes pour les documents x blabla");
		} else if (StringUtils.isNotBlank(workflowId)) {
			view.navigateToWorkflow(workflowId);
		} else if (StringUtils.isNotBlank(parentId)) {
			view.navigate().to(TaskViews.class).displayTask(parentId);
		} else {
			view.navigate().to(TaskViews.class).taskManagement();
		}
	}

	private void saveRecord(Task task, Record record, boolean withRequiredValidation) {
		RecordUpdateOptions recordUpdateOptions = null;

		if (rmModuleExtensions != null) {
			for (TaskAddEditTaskPresenterExtension extension : rmModuleExtensions.getTaskAddEditTaskPresenterExtension()) {
				recordUpdateOptions = extension.getRecordUpdateOption();
			}
		}

		if (task.isModel()) {
			recordUpdateOptions = RecordUpdateOptions.userModificationsSafeOptions();
		}

		if (withRequiredValidation) {
			addOrUpdate(record, recordUpdateOptions);
		} else {
			if (recordUpdateOptions == null) {
				recordUpdateOptions = new RecordUpdateOptions();
			}
			addOrUpdate(record, recordUpdateOptions.setSkippingRequiredValuesValidation(true));
		}
	}


	private Field getAssignerField() {
		TaskForm form = view.getForm();
		if (form instanceof TaskFormImpl) {
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
			TaskUser taskUser = new TaskUser(getCurrentUser().getWrappedRecord(), types(),
					modelLayerFactory.getRolesManager().getCollectionRoles(collection, modelLayerFactory));
			if (!Boolean.FALSE.equals(taskUser.getAssignTaskAutomatically())) {
				task.setAssignee(getCurrentUser().getId());
			}

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

			previousPage = paramsMap.get("previousPage");


			String tempParamKey = paramsMap.get("tempParams");
			if (tempParamKey != null) {
				setLinkedRecordsFromUserCache(task, tempParamKey);
			}

		}

		completeMode = "true".

				equals(paramsMap.get("completeTask"));
		if (completeMode) {
			TaskStatus finishedStatus = tasksSearchServices
					.getFirstFinishedStatus();
			if (finishedStatus != null) {
				task.setStatus(finishedStatus.getId());
			}
		}

		workflowId = paramsMap.get("workflowId");

		taskVO = new

				TaskVO(new TaskToVOBuilder().

				build(task.getWrappedRecord(), FORM, view.

						getSessionContext()));
		isCompletedOrClosedOnInitialization =

				isCompletedOrClosedStatus(taskVO);
		view.setRecord(taskVO);
		if (taskVO.getMetadataCodes().

				contains(taskVO.getSchema().

						getCode() + "_" + ASSIGNEE)) {
			originalAssignedTo = taskVO.getAssignee();
		}
		if (taskVO.getMetadataCodes().

				contains(taskVO.getSchema().

						getCode() + "_" + Task.ASSIGNER)) {
			originalAssigner = taskVO.get(Task.ASSIGNER);
		}

	}

	private void setLinkedRecordsFromUserCache(Task task, String key) {
		Object cachedParam = this.getView().getSessionContext().getAttribute(key);
		if (cachedParam instanceof TimedCache) {
			TimedCache timedCache = (TimedCache) cachedParam;
			Map params = (Map) timedCache.get(key);
			if (params != null) {
				Object folders = params.get("folderId");
				Object documents = params.get("documentId");

				if (folders != null && folders instanceof List) {
					new RMTask(task).setLinkedFolders((List<String>) folders);
				}

				if (documents != null && documents instanceof List) {
					new RMTask(task).setLinkedDocuments((List<String>) documents);
				}
			} else {
				this.getView().showMessage($("AddEditTaskPresenter.error.expiredCache"));

			}
		}
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
		adjustFollowersField();
		adjustRequiredUSRMetadatasFields();
		adjustFieldsForCollaborators();
	}

	private void adjustRequiredUSRMetadatasFields() {
		try {
			TaskVO taskVO = getTask();
			Task task = taskPresenterServices.toTask(new TaskVO(taskVO), toRecord(taskVO));
			boolean adjustRequiredUSRMetadatasFields = false;
			if (rmModuleExtensions != null) {
				for (TaskAddEditTaskPresenterExtension extension : rmModuleExtensions.getTaskAddEditTaskPresenterExtension()) {
					adjustRequiredUSRMetadatasFields = extension.adjustRequiredUSRMetadatasFields();
				}
			}
			if (task.isModel() || adjustRequiredUSRMetadatasFields) {
				TaskForm form = view.getForm();
				if (form != null && form instanceof RecordForm) {
					MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
					MetadataSchema schema = schemasManager.getSchemaTypes(collection).getSchema(taskVO.getSchema().getCode());
					for (Metadata metadata : schema.getMetadatas().onlyUSR().onlyAlwaysRequired()) {
						Field<?> field = ((RecordForm) form).getField(metadata.getLocalCode());
						if (field != null) {
							field.setRequired(false);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isCompletedOrClosedStatus(RecordVO recordVO) {
		Task task = tasksSchemas.wrapTask(toRecord(recordVO));
		TaskStatus statusType = tasksSchemasRecordsServices.getTaskStatus(task.getStatus());
		if (statusType == null || statusType.getStatusType() == null) {
			return false;
		}

		return statusType.getStatusType().isFinishedOrClosed();
	}

	public boolean isCompleted(RecordVO recordVO) {
		Task task = tasksSchemas.wrapTask(toRecord(recordVO));
		TaskStatus statusType = tasksSchemasRecordsServices.getTaskStatus(task.getStatus());
		if (statusType == null || statusType.getStatusType() == null) {
			return false;
		}

		return statusType.getStatusType().isFinished();
	}

	public boolean isSubTaskPresentAndHaveCertainStatus(RecordVO recordVO) {
		return taskPresenterServices.isSubTaskPresentAndHaveCertainStatus(recordVO.getId());
	}

	private void adjustQuestionField() {
		TaskQuestionFieldImpl questionField = (TaskQuestionFieldImpl) view.getForm().getCustomField(Task.QUESTION);
		if (questionField != null) {
			if (field != null) {
				questionField.setVisible(field.isVisible());
			}
			questionField.setReadOnly(true);
		}
	}

	private void adjustAssignerField() {
		Field assignerField = getAssignerField();
		if (assignerField != null && taskVO != null && taskVO.getMetadataCodes().contains(taskVO.getSchema().getCode() + "_" + Task.ASSIGNEE)
			&& !Objects.equals(originalAssignedTo, taskVO.getAssignee())) {
			assignerField.setValue(getCurrentUser().getId());
		}
	}

	private void adjustProgressPercentageField() {
		TaskProgressPercentageField progressPercentageField = (TaskProgressPercentageField) view.getForm()
				.getCustomField(Task.PROGRESS_PERCENTAGE);
		if (progressPercentageField != null) {
			progressPercentageField.setVisible(editMode);
		}
	}

	private void adjustDecisionField() {
		field = (TaskDecisionField) view.getForm().getCustomField(Task.DECISION);

		if (field != null) {
			try {
				BetaWorkflowTask task = loadTask();
				boolean isInclusiveDecision;

				try {
					isInclusiveDecision = Boolean.TRUE.equals(task.get(IS_INCLUSIVE_DECISION));
				} catch (Exception exception) {
					isInclusiveDecision = false;
				}

				Object decisions = task.get(Task.BETA_NEXT_TASKS_DECISIONS);
				if (!task.hasDecisions() || task.getModelTask() == null || isInclusiveDecision || DisplayTaskPresenter.containsExpressionLanguage(decisions)) {
					field.setVisible(false);
					return;
				} else {
					exclusiveDecision = true;
					field.setVisible(true);

					if (completeMode) {
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
		listAddRemoveWorkflowInclusiveDecision = (ListAddRemoveWorkflowInclusiveDecisionFieldImpl) ((RecordForm) view.getForm()).getField(TaskFieldFactory.INCLUSIVE_DECISION);

		if (listAddRemoveWorkflowInclusiveDecision != null) {

			try {
				BetaWorkflowTask task = loadTask();
				boolean isInclusiveDecision;

				try {
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
					if (completeMode) {
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
		if (field != null) {
			try {
				Task task = loadTask();

				field.setVisible(task.isModel());
			} catch (NoSuchRecordWithId e) {
				field.setVisible(false);
			}
		}
	}

	private void adjustFollowersField() {
		ListAddRemoveTaskFollowerField field = (ListAddRemoveTaskFollowerField) ((TaskFormImpl) view.getForm()).getField(Task.TASK_FOLLOWERS);
		TaskUser taskUser = new TaskUser(getCurrentUser().getWrappedRecord(), types(),
				modelLayerFactory.getRolesManager().getCollectionRoles(collection, modelLayerFactory));
		TaskFollower defaultFollower = taskUser.getDefaultFollowerWhenCreatingTask();
		if (!editMode && field != null && defaultFollower != null) {
			field.addTaskFollower(defaultFollower);
		}
	}

	void reloadForm() {
		view.getForm().reload();
		adjustProgressPercentageField();
		adjustDecisionField();
		adjustQuestionField();
		adjustInclusiveDecisionField();
		adjustRelativeDueDate();
		adjustReasonField();
		adjustRequiredUSRMetadatasFields();
		adjustFieldsForCollaborators();
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

	public void fieldValueChanged(Field<?> customField) {

		Field<String> assignee = (Field<String>) view.getForm().getField(Task.ASSIGNEE);
		boolean assigneeValue = assignee != null && StringUtils.isNotBlank(assignee.getValue());

		ListAddRemoveField priorite = (ListAddRemoveField) view.getForm().getField(ASSIGNATION_MODES);
		boolean prioriteValue = priorite != null && CollectionUtils.isNotEmpty(priorite.getValue());

		if (assignee != null) {
			assignee.setReadOnly(prioriteValue);
		}

		if (priorite != null) {
			priorite.setReadOnly(assigneeValue);
		}

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
		if (typeField != null) {
			if (tasksSchemas.isRequestTask(getTask())) {
				typeField.setReadOnly(true);
			}
		}
	}

	private Field getTypeField() {
		TaskForm form = view.getForm();
		if (form instanceof TaskFormImpl) {
			return ((TaskFormImpl) form).getField(Task.TYPE);
		}
		return null;
	}

	boolean isReloadRequiredAfterTaskTypeChange() {
		boolean reload = false;
		String currentSchemaCode = getSchemaCode();
		if (isTaskTypeFieldVisible()) {
			String taskTypeRecordId = getTypeFieldValue();
			if (StringUtils.isNotBlank(taskTypeRecordId)) {
				String schemaCodeForTaskTypeRecordId = tasksSchemas.getSchemaCodeForTaskTypeRecordId(taskTypeRecordId);
				if (schemaCodeForTaskTypeRecordId != null) {
					reload = !currentSchemaCode.equals(schemaCodeForTaskTypeRecordId);
				} else {
					reload = !currentSchemaCode.equals(Task.DEFAULT_SCHEMA);
				}
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
		if (progressPercentageField != null) {
			progressPercentageField.setVisible(editMode);
		}

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
		return !tasksSchemas.isRequestTask(getTask()) ? searchServices().searchRecordIds(
				LogicalSearchQueryOperators.from(tasksSchemas.taskTypeSchemaType()).where(Schemas.CODE)
						.isIn(asList(BorrowRequest.SCHEMA_NAME, ReturnRequest.SCHEMA_NAME, ExtensionRequest.SCHEMA_NAME, ReactivationRequest.SCHEMA_NAME))) : null;
	}

	private boolean currentUserIsCollaborator() {
		if (((List) taskVO.get(TASK_COLLABORATORS)).contains(getCurrentUser().getId())) {
			return true;
		} else {
			return !Collections.disjoint(getCurrentUser().getUserGroups(), taskVO.get(TASK_COLLABORATORS_GROUPS));
		}
	}

	private boolean currentUserHasWriteAuthorisation() {
		return getCurrentUser().hasWriteAccess().on(toRecord(taskVO));
	}

	private void adjustFieldsForCollaborators() {
		boolean currentUserIsCollaborator = currentUserIsCollaborator();

		TaskAssignationListRecordLookupField assigneeGroupCandidatesField = (TaskAssignationListRecordLookupField) view.getForm().getField(Task.ASSIGNEE_GROUPS_CANDIDATES);
		if (assigneeGroupCandidatesField != null) {
			assigneeGroupCandidatesField.setVisible(!currentUserIsCollaborator);
		}

		TaskAssignationListRecordLookupField assigneeUserCandidatesField = (TaskAssignationListRecordLookupField) view.getForm().getField(Task.ASSIGNEE_USERS_CANDIDATES);
		if (assigneeUserCandidatesField != null) {
			assigneeUserCandidatesField.setVisible(!currentUserIsCollaborator);
		}

		LookupRecordField assignerField = (LookupRecordField) view.getForm().getField(Task.ASSIGNER);
		if (assignerField != null) {
			assignerField.setVisible(!currentUserIsCollaborator);
		}

		LookupRecordField assigneeField = (LookupRecordField) view.getForm().getField(Task.ASSIGNEE);
		if (assigneeField != null) {
			assigneeField.setVisible(!currentUserIsCollaborator);
		}

		TaskAssignationEnumField assignationModesField = (TaskAssignationEnumField) view.getForm().getField(ASSIGNATION_MODES);
		if (assignationModesField != null) {
			assignationModesField.setVisible(!currentUserIsCollaborator);
		}

		Field<?> statusField = view.getForm().getField(STATUS);
		if (statusField != null) {
			statusField.setVisible(!currentUserIsCollaborator);
		}

		boolean currentUserHasWriteAuthorisation = currentUserHasWriteAuthorisation();
		ListAddRemoveCollaboratorsField taskCollaboratorsField = (ListAddRemoveCollaboratorsField) view.getForm().getField(Task.TASK_COLLABORATORS);
		if (taskCollaboratorsField != null) {
			taskCollaboratorsField.writeButtonIsVisible(currentUserHasWriteAuthorisation);
			taskCollaboratorsField.setCurrentUserIsCollaborator(currentUserIsCollaborator());
			OptionGroup authorizationField = taskCollaboratorsField.getAddEditField().getAuthorizationField();
			if (authorizationField != null && !currentUserHasWriteAuthorisation) {
				authorizationField.removeItem(WRITE);
				authorizationField.setValue(READ);
			}
		}

		ListAddRemoveCollaboratorsGroupsField taskCollaboratorGroupsField = (ListAddRemoveCollaboratorsGroupsField) view.getForm().getField(Task.TASK_COLLABORATORS_GROUPS);
		if (taskCollaboratorGroupsField != null) {
			taskCollaboratorGroupsField.writeButtonIsVisible(currentUserHasWriteAuthorisation);
			taskCollaboratorGroupsField.setCurrentUserIsCollaborator(currentUserIsCollaborator);
			OptionGroup authorizationField = taskCollaboratorGroupsField.getAddEditField().getAuthorizationField();
			if (authorizationField != null && !currentUserHasWriteAuthorisation) {
				authorizationField.removeItem(WRITE);
				authorizationField.setValue(READ);
			}
		}
	}
}
