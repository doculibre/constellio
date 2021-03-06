package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.ui.pages.folder.MergeRecordsWindow;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.TaskConfigs;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.extensions.TaskAddEditTaskPresenterExtension;
import com.constellio.app.modules.tasks.extensions.action.Action;
import com.constellio.app.modules.tasks.extensions.api.TaskModuleExtensions;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormParams;
import com.constellio.app.modules.tasks.extensions.api.params.TaskFormRetValue;
import com.constellio.app.modules.tasks.extensions.param.PromptUserParam;
import com.constellio.app.modules.tasks.model.utils.DateUtils;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.TaskUser;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.RequestTask;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.TaskPresenterServices;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.builders.TaskToVOBuilder;
import com.constellio.app.modules.tasks.ui.components.TaskFieldFactory;
import com.constellio.app.modules.tasks.ui.components.fields.CustomTaskField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationEnumField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationListCollaboratorsField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationListCollaboratorsGoupsField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskAssignationListRecordLookupField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskDecisionField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskForm;
import com.constellio.app.modules.tasks.ui.components.fields.TaskFormImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskProgressPercentageField;
import com.constellio.app.modules.tasks.ui.components.fields.TaskQuestionFieldImpl;
import com.constellio.app.modules.tasks.ui.components.fields.TaskRelativeDueDateField;
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
import com.constellio.app.ui.pages.home.HomeView;
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
import com.constellio.model.frameworks.validation.OptimisticLockException;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.jgoodies.common.base.Strings;
import com.vaadin.ui.Field;
import com.vaadin.ui.OptionGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.constellio.app.modules.tasks.model.wrappers.Task.ASSIGNEE;
import static com.constellio.app.modules.tasks.model.wrappers.Task.STATUS;
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
	private TaskConfigs taskConfigs;
	private static Logger LOGGER = LoggerFactory.getLogger(AddEditTaskPresenter.class);
	List<String> finishedOrClosedStatuses;
	private RMModuleExtensions rmModuleExtensions;
	private String previousPage;

	public static final String LINKED_RECORDS_PARAM = "linkedRecords";
	public static final String PREVIOUS_PAGE_PARAM = "previousPage";
	public static final String TEMP_PARAMS_ID = "tempParams";

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
		taskConfigs = new TaskConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
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
		if (previousPage != null) {
			navigateToPreviousPage();
		} else if (StringUtils.isNotBlank(workflowId)) {
			view.navigateToWorkflow(workflowId);
		} else {
			view.navigate().to(TaskViews.class).taskManagement();
		}
	}

	public void saveButtonClicked(RecordVO recordVO) {
		Record record;
		try {
			record = toRecord(recordVO);
		} catch (OptimisticLockException e) {
			Record recordFromVo = recordVO.getRecord();
			new MergeRecordsWindow() {
				@Override
				public void mergeButtonClick() {
					try {
						save(recordFromVo, recordVO);
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
						view.showErrorMessage(e.getMessage());
					}
				}

				@Override
				public void cancelButtonClick() {
					cancelButtonClicked();
				}
			};
			return;
		}
		save(record, recordVO);
	}

	public void save(Record record, RecordVO recordVO) {
		if (recordVO == null) {
			return;
		}
		final Task task = taskPresenterServices.toTask(new TaskVO(recordVO), record);

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
				ConstellioEIMConfigs constellioEIMConfigs = appLayerFactory.getModelLayerFactory().getSystemConfigs();
				LocalDate dueDate = DateUtils.addWorkingDays(task.getAssignedOn(), task.getRelativeDueDate(), constellioEIMConfigs.getCalendarCountry());
				task.setDueDate(dueDate);
			}

			TaskModuleExtensions taskModuleExtensions = appLayerFactory.getExtensions().forCollection(collection)
					.forModule(TaskModule.ID);

			// No transaction because the extention return record to be save
			if (taskModuleExtensions != null) {
				TaskFormRetValue taskFormRetValue = taskModuleExtensions.taskFormExtensions(new TaskFormParams(this, task));
				for (Record currentRecord : taskFormRetValue.getRecords()) {
					RecordUpdateOptions taskUpdateOptions = getTaskUpdateOptions(task, taskFormRetValue.isSaveWithValidation(currentRecord));
					saveRecord(currentRecord, taskUpdateOptions);
				}
			}
			boolean isPromptUser = false;

			Transaction transaction = new Transaction();
			RecordUpdateOptions taskUpdateOptions = getTaskUpdateOptions(task, true);
			transaction.setOptions(taskUpdateOptions);
			transaction.addUpdate(task.getWrappedRecord());

			if (!isCompletedOrClosedOnInitialization && isCompleted(recordVO)) {
				isPromptUser = rmModuleExtensions.isPromptUser(new PromptUserParam(task, new Action() {
					@Override
					public void doAction() {
						saveAndNavigate(task, taskUpdateOptions);
					}
				}));
			}
			if (!isPromptUser) {
				saveAndNavigate(task, taskUpdateOptions);
			}

		} catch (final IcapException e) {
			view.showErrorMessage(e.getMessage());
		} catch (ValidationException e) {
			ErrorDisplayUtil.showBackendValidationException(e.getValidationErrors());
		}
	}

	private void saveAndNavigate(Task task, RecordUpdateOptions taskUpdateOptions) {
		saveRecord(task.getWrappedRecord(), taskUpdateOptions);

		if (StringUtils.isNotBlank(workflowId)) {
			view.navigateToWorkflow(workflowId);
		} else if (StringUtils.isNotBlank(parentId)) {
			view.navigate().to(TaskViews.class).displayTask(parentId);
		} else if (previousPage != null) {
			navigateToPreviousPage();
		} else {
			view.navigate().to(TaskViews.class).taskManagement();
		}
	}

	private void saveRecord(Record record, RecordUpdateOptions recordUpdateOptions) {
		addOrUpdate(record, recordUpdateOptions);
	}

	private RecordUpdateOptions getTaskUpdateOptions(Task task, boolean withRequiredValidation) {
		RecordUpdateOptions recordUpdateOptions = new RecordUpdateOptions();

		if (rmModuleExtensions != null) {
			for (TaskAddEditTaskPresenterExtension extension : rmModuleExtensions.getTaskAddEditTaskPresenterExtension()) {
				recordUpdateOptions = extension.getRecordUpdateOption();
			}
		}

		if (task.isModel()) {
			recordUpdateOptions = RecordUpdateOptions.userModificationsSafeOptions().setSkipUserAccessValidation(true);
		}

		if (withRequiredValidation) {
			return recordUpdateOptions;
		} else {
			return recordUpdateOptions.setSkippingRequiredValuesValidation(true);
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

			int defaultDueDate = taskConfigs.getDefaultDueDate();
			if (defaultDueDate > -1) {
				task.setDueDate(TimeProvider.getLocalDate().plusDays(defaultDueDate));
			}
			parentId = paramsMap.get("parentId");
			task.setParentTask(parentId);

			String tempParamKey = paramsMap.get(TEMP_PARAMS_ID);
			if (tempParamKey != null) {
				TimedCache timedCache = getCachedAttributes(tempParamKey);
				if (timedCache != null) {
					setLinkedRecords(timedCache.get(LINKED_RECORDS_PARAM), task);
					setPreviousPage(timedCache.get(PREVIOUS_PAGE_PARAM));
				}
			}
		}

		completeMode = "true".equals(paramsMap.get("completeTask"));
		if (completeMode) {
			TaskStatus finishedStatus = tasksSearchServices.getFirstFinishedStatus();
			if (finishedStatus != null) {
				task.setStatus(finishedStatus.getId());
			}
		}

		workflowId = paramsMap.get("workflowId");
		taskVO = new TaskToVOBuilder()
				.build(task.getWrappedRecord(), FORM, view.getSessionContext());
		isCompletedOrClosedOnInitialization = isCompletedOrClosedStatus(taskVO);
		view.setRecord(taskVO);
		if (taskVO.getMetadataCodes().contains(taskVO.getSchema().getCode() + "_" + ASSIGNEE)) {
			originalAssignedTo = taskVO.getAssignee();
		}
		if (taskVO.getMetadataCodes().contains(taskVO.getSchema().getCode() + "_" + Task.ASSIGNER)) {
			originalAssigner = taskVO.get(Task.ASSIGNER);
		}

	}

	private void setPreviousPage(Object previousPage) {
		if (previousPage instanceof String) {
			this.previousPage = (String) previousPage;
		}
	}

	private TimedCache getCachedAttributes(String key) {
		Object cachedParam = this.getView().getSessionContext().getAttribute(key);
		if (cachedParam instanceof TimedCache) {
			return (TimedCache) cachedParam;
		}
		return null;
	}

	private void setLinkedRecords(Object linkedRecordIds, Task task) {
		if (linkedRecordIds instanceof List) {
			HashMap<String, List<String>> schemasMap = filterSchemaType((List<String>) linkedRecordIds);
			new RMTask(task).setLinkedFolders(schemasMap.get(Folder.SCHEMA_TYPE));
			new RMTask(task).setLinkedDocuments(schemasMap.get(Document.SCHEMA_TYPE));
		}
	}

	private HashMap<String, List<String>> filterSchemaType(List<String> recordIds) {
		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		HashMap<String, List<String>> recordsMap = new HashMap<>();
		List<Record> records = recordIds
				.stream()
				.map(i -> recordServices().realtimeGetRecordSummaryById(i))
				.collect(Collectors.toList());
		for (Record record : records) {
			String schemaType = schemasManager.getSchemaTypeOf(record).getCode();
			List<String> schemaTypeList = recordsMap.get(schemaType);
			if (schemaTypeList == null) {
				schemaTypeList = new ArrayList<>();
			}
			schemaTypeList.add(record.getId());
			recordsMap.put(schemaType, schemaTypeList);
		}
		return recordsMap;
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
		adjustDisabledFields();
	}

	private void adjustDisabledFields() {
		if (rmModuleExtensions != null) {
			for (TaskAddEditTaskPresenterExtension extension : rmModuleExtensions.getTaskAddEditTaskPresenterExtension()) {
				extension.adjustDisabledFields(view, taskVO);
			}
		}
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
		Task task = null;
		try {
			task = tasksSchemas.wrapTask(toRecord(recordVO));
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
		TaskStatus statusType = tasksSchemasRecordsServices.getTaskStatus(task.getStatus());
		if (statusType == null || statusType.getStatusType() == null) {
			return false;
		}

		return statusType.getStatusType().isFinishedOrClosed();
	}

	public boolean isCompleted(RecordVO recordVO) {
		Task task = null;
		try {
			task = tasksSchemas.wrapTask(toRecord(recordVO));
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
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
		adjustDisabledFields();
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
			boolean isAssignationNotModifiable = prioriteValue && originalAssignedTo != null &&
												 !(getCurrentUser().getId().equals(originalAssignedTo)
												   || getCurrentUser().getId().equals(originalAssigner)
												   || getCurrentUser().isSystemAdmin());
			assignee.setReadOnly(isAssignationNotModifiable);
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

		try {
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
		} catch (OptimisticLockException e) {
			LOGGER.error(e.getMessage());
			view.showErrorMessage(e.getMessage());
		}
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

	public boolean currentUserHasWriteAuthorizationWithoutBeingCollaborator(TaskVO taskVO) {
		boolean isModel = false;
		if (taskVO.get(RMTask.IS_MODEL) != null) {
			isModel = taskVO.get(RMTask.IS_MODEL);
		}
		String currentUserId = getCurrentUser().getId();
		List<String> assigneeGroupsCandidatesIds = taskVO.get(Task.ASSIGNEE_GROUPS_CANDIDATES);
		List<String> assigneeCandidates = taskVO.get(Task.ASSIGNEE_USERS_CANDIDATES);
		List<String> userGroups = getCurrentUser().getUserGroups();
		boolean userHasCollectionWriteAccess = getCurrentUser().hasCollectionWriteAccess();
		boolean userIsCandidate = !ListUtils.intersection(assigneeGroupsCandidatesIds, userGroups).isEmpty() || assigneeCandidates.contains(currentUserId);
		return userHasCollectionWriteAccess || userIsCandidate || isModel || !isEditMode() || currentUserId.equals(taskVO.get(Task.ASSIGNEE)) || currentUserId.equals(taskVO.get(Task.ASSIGNER)) || currentUserId.equals(taskVO.get(Schemas.CREATED_BY));
	}

	private boolean currentUserHasWriteAuthorisation() {
		return getCurrentUser().hasWriteAccess().on(taskVO.getRecord()) || !isEditMode();
	}

	private void adjustFieldsForCollaborators() {
		boolean currentUserHasWriteAuthorizationWithoutBeingCollaborator = currentUserHasWriteAuthorizationWithoutBeingCollaborator(taskVO);

		TaskAssignationListRecordLookupField assigneeGroupCandidatesField = (TaskAssignationListRecordLookupField) view.getForm().getField(Task.ASSIGNEE_GROUPS_CANDIDATES);
		if (assigneeGroupCandidatesField != null) {
			assigneeGroupCandidatesField.setVisible(currentUserHasWriteAuthorizationWithoutBeingCollaborator);
		}

		TaskAssignationListRecordLookupField assigneeUserCandidatesField = (TaskAssignationListRecordLookupField) view.getForm().getField(Task.ASSIGNEE_USERS_CANDIDATES);
		if (assigneeUserCandidatesField != null) {
			assigneeUserCandidatesField.setVisible(currentUserHasWriteAuthorizationWithoutBeingCollaborator);
		}

		LookupRecordField assignerField = (LookupRecordField) view.getForm().getField(Task.ASSIGNER);
		if (assignerField != null) {
			assignerField.setVisible(currentUserHasWriteAuthorizationWithoutBeingCollaborator);
		}

		LookupRecordField assigneeField = (LookupRecordField) view.getForm().getField(Task.ASSIGNEE);
		if (assigneeField != null) {
			assigneeField.setVisible(currentUserHasWriteAuthorizationWithoutBeingCollaborator);
		}

		TaskAssignationEnumField assignationModesField = (TaskAssignationEnumField) view.getForm().getField(ASSIGNATION_MODES);
		if (assignationModesField != null) {
			assignationModesField.setVisible(currentUserHasWriteAuthorizationWithoutBeingCollaborator);
		}

		Field<?> statusField = view.getForm().getField(STATUS);
		if (statusField != null) {
			statusField.setVisible(currentUserHasWriteAuthorizationWithoutBeingCollaborator);
		}

		boolean currentUserHasWriteAuthorisation = currentUserHasWriteAuthorisation();
		ListAddRemoveCollaboratorsField taskCollaboratorsField = (ListAddRemoveCollaboratorsField) view.getForm().getField(Task.TASK_COLLABORATORS);
		if (taskCollaboratorsField != null) {
			taskCollaboratorsField.writeButtonIsVisible(currentUserHasWriteAuthorisation);
			taskCollaboratorsField.setCurrentUserCanModifyDelete(currentUserHasWriteAuthorizationWithoutBeingCollaborator);
			TaskAssignationListCollaboratorsField addEditField = taskCollaboratorsField.getAddEditField();
			if (addEditField != null) {
				OptionGroup authorizationField = addEditField.getAuthorizationField();
				if (authorizationField != null && !currentUserHasWriteAuthorisation) {
					authorizationField.removeItem(WRITE);
					authorizationField.setValue(READ);
				}
			}
		}

		ListAddRemoveCollaboratorsGroupsField taskCollaboratorGroupsField = (ListAddRemoveCollaboratorsGroupsField) view.getForm().getField(Task.TASK_COLLABORATORS_GROUPS);
		if (taskCollaboratorGroupsField != null) {
			taskCollaboratorGroupsField.writeButtonIsVisible(currentUserHasWriteAuthorisation);
			taskCollaboratorGroupsField.setCurrentUserCanModifyDelete(currentUserHasWriteAuthorizationWithoutBeingCollaborator);
			TaskAssignationListCollaboratorsGoupsField addEditField = taskCollaboratorGroupsField.getAddEditField();
			if (addEditField != null) {
				OptionGroup authorizationField = addEditField.getAuthorizationField();
				if (authorizationField != null && !currentUserHasWriteAuthorisation) {
					authorizationField.removeItem(WRITE);
					authorizationField.setValue(READ);
				}
			}
		}
	}

	private void navigateToPreviousPage() {
		URI location = ConstellioUI.getCurrent().getPage().getLocation();
		view.navigate().to(CoreViews.class).navigateTo(location.getPath(), previousPage, false);
		if (ConstellioUI.getCurrent().getCurrentView() instanceof HomeView) {
			String previousPageName = previousPage.replace("/", "");
			view.navigate().to(CoreViews.class).home(previousPageName);
		}
	}
}
