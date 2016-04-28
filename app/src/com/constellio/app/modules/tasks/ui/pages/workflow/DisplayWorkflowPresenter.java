package com.constellio.app.modules.tasks.ui.pages.workflow;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.WorkflowServices;
import com.constellio.app.modules.tasks.services.WorkflowServicesRuntimeException.WorkflowServicesRuntimeException_UnsupportedAddAtPosition;
import com.constellio.app.modules.tasks.ui.builders.WorkflowToVoBuilder;
import com.constellio.app.modules.tasks.ui.entities.WorkflowTaskVO;
import com.constellio.app.modules.tasks.ui.entities.WorkflowVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class DisplayWorkflowPresenter extends SingleSchemaBasePresenter<DisplayWorkflowView> {
	private WorkflowVO workflowVO;
	private List<WorkflowTaskVO> workflowTaskVOs;
	private transient WorkflowServices workflowServices;
	private WorkflowToVoBuilder workflowToVoBuilder;

	public DisplayWorkflowPresenter(DisplayWorkflowView view) {
		super(view, Workflow.DEFAULT_SCHEMA);
		initTransientObjects();
		workflowToVoBuilder = new WorkflowToVoBuilder();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		workflowServices = new WorkflowServices(collection, appLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(TasksPermissionsTo.MANAGE_WORKFLOWS).globally();
	}

	void forParams(String params) {
		String id = params;
		Record workflowRecord = getRecord(id);
		workflowVO = workflowToVoBuilder.build(workflowRecord, VIEW_MODE.DISPLAY, view.getSessionContext());
		view.setWorkflowVO(workflowVO);

		SessionContext sessionContext = view.getSessionContext();
		Workflow workflow = new Workflow(workflowRecord, types());
		workflowTaskVOs = workflowServices.getRootModelTaskVOs(workflow, sessionContext);
		view.setWorkflowTaskVOs(workflowTaskVOs);

		MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
		MetadataSchema taskTypeSchema = schema(TaskType.DEFAULT_SCHEMA);
		MetadataSchemaVO taskTypeSchemaVO = schemaVOBuilder.build(taskTypeSchema, VIEW_MODE.TABLE, sessionContext);
		view.setTaskTypeDataProvider(new RecordVODataProvider(taskTypeSchemaVO, new RecordToVOBuilder(), view) {
			@Override
			protected LogicalSearchQuery getQuery() {
				LogicalSearchQuery query = new LogicalSearchQuery();
				query.setCondition(from(schemaType(TaskType.SCHEMA_TYPE)).returnAll());
				query.sortDesc(Schemas.TITLE);
				return query;
			}
		});
	}

	List<WorkflowTaskVO> getChildren(WorkflowTaskVO parent) {
		SessionContext sessionContext = view.getSessionContext();
		return workflowServices.getChildModelTasks(parent, sessionContext);
	}

	void backButtonClicked() {
		view.navigate().to(TaskViews.class).listWorkflows();
	}

	void editButtonClicked() {
		view.navigate().to(TaskViews.class).editWorkflow(workflowVO.getId());
	}

	void deleteButtonClicked() {
		try {
			delete(toRecord(workflowVO), false);
			view.navigate().to(TaskViews.class).listWorkflows();
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
		}
	}

	public void addTaskButtonClicked() {
		String workflowId = workflowVO.getId();
		SessionContext sessionContext = view.getSessionContext();
		List<WorkflowTaskVO> availableTaskVOs = workflowServices.getAvailableWorkflowTaskVOForNewTask(workflowId, sessionContext);
		view.openAddTaskWindow(null, availableTaskVOs);
	}

	void createTaskSelected(WorkflowTaskVO workflowTaskVOBefore) {
		String workflowId = workflowVO.getId();
		SessionContext sessionContext = view.getSessionContext();
		if (workflowServices.canAddTaskIn(workflowTaskVOBefore, sessionContext)) {
			List<WorkflowTaskVO> availableTaskVOs = workflowServices
					.getAvailableWorkflowTaskVOForNewTask(workflowId, sessionContext);
			if (!availableTaskVOs.isEmpty()) {
				view.openAddTaskWindow(workflowTaskVOBefore, availableTaskVOs);
			}
		}
	}

	//TODO Thiago
	void createExistingTaskSelected(WorkflowTaskVO workflowTaskVOBefore) {
		String workflowId = workflowVO.getId();
		SessionContext sessionContext = view.getSessionContext();
		if (workflowServices.canAddTaskIn(workflowTaskVOBefore, sessionContext)) {
			List<WorkflowTaskVO> availableTaskVOs = workflowServices
					.getAvailableWorkflowTaskVOForNewTask(workflowId, sessionContext);
			if (!availableTaskVOs.isEmpty()) {
				//				if (availableTaskVOs.contains(workflowTaskVOBefore)) {
				availableTaskVOs.remove(workflowTaskVOBefore);
				List<WorkflowTaskVO> toDelete = new ArrayList<>();
				for (WorkflowTaskVO availableTaskVO : availableTaskVOs) {
					if (availableTaskVO.getDecision() != null) {
						toDelete.add(availableTaskVO);
					}
				}
				for (WorkflowTaskVO workflowTaskVO : toDelete) {
					availableTaskVOs.remove(workflowTaskVO);
				}
				//				}
				view.openExistingTasksWindow(workflowTaskVOBefore, availableTaskVOs);
			}
		}
	}

	boolean isDecisionField(WorkflowTaskVO workflowTaskVO) {
		SessionContext sessionContext = view.getSessionContext();
		return workflowServices.canAddDecisionTaskIn(workflowTaskVO, sessionContext);
	}

	void editTaskSelected(WorkflowTaskVO workflowTaskVO) {
		String taskId = workflowTaskVO.getTaskVO().getId();
		String workflowId = workflowVO.getId();
		view.navigate().to().editTask(taskId, workflowId);
	}

	void deleteTaskSelected(WorkflowTaskVO workflowTaskVO) {
		if (workflowTaskVO.getDecision() != null) {
			view.showMessage($("DisplayWorkflowView.cannotDeleteDecisionTask"));
		} else {

			SessionContext sessionContext = view.getSessionContext();
			workflowServices.delete(workflowTaskVO, sessionContext);
			view.remove(workflowTaskVO);
		}
	}

	public void saveNewTaskButtonClicked(String taskType, String taskTitle, List<String> decisions,
			WorkflowTaskVO workflowTaskVO) {
		try {
			SessionContext sessionContext = view.getSessionContext();
			Record workflowRecord = toRecord(workflowVO);
			Workflow workflow = new Workflow(workflowRecord, types());
			if (!decisions.isEmpty()) {
				workflowServices.createDecisionModelTaskAfter(workflow, workflowTaskVO, taskType, taskTitle, decisions,
						sessionContext);
			} else {
				workflowServices.createModelTaskAfter(workflow, workflowTaskVO, taskType, taskTitle, sessionContext);
			}
			view.closeAddTaskWindow();
			view.navigate().to(TaskViews.class).displayWorkflow(workflow.getId());
		} catch (WorkflowServicesRuntimeException_UnsupportedAddAtPosition e) {
			e.printStackTrace();
			view.showMessage($("DisplayWorkflowView.addTaskWindow.cannotAdd"));
		}
	}

	public void cancelNewTaskButtonClicked() {
		view.closeAddTaskWindow();
	}

	public boolean moveAfter(WorkflowTaskVO droppedItemId, WorkflowTaskVO targetItemId) {
		boolean allow;
		SessionContext sessionContext = view.getSessionContext();
		if (StringUtils.isBlank(droppedItemId.getDecision())) {
			allow = workflowServices.canAddTaskIn(targetItemId, sessionContext);
		} else {
			allow = workflowServices.canAddDecisionTaskIn(targetItemId, sessionContext);
		}
		if (allow) {
			try {
				workflowServices.moveAfter(droppedItemId, targetItemId, sessionContext);
			} catch (Exception e) {
				e.printStackTrace();
				allow = false;
			}
		}
		return allow;
	}

	//TODO Thiago
	public boolean addExistingTaskAfter(WorkflowTaskVO existingItemId, WorkflowTaskVO targetItemId) {
		boolean allow;
		SessionContext sessionContext = view.getSessionContext();
		if (StringUtils.isBlank(existingItemId.getDecision())) {
			allow = workflowServices.canAddTaskIn(targetItemId, sessionContext);
		} else {
			allow = workflowServices.canAddDecisionTaskIn(targetItemId, sessionContext);
		}
		if (allow) {
			try {
				workflowServices.addAfter(existingItemId, targetItemId, sessionContext);
				view.closeAddTaskWindow();
				view.navigate().to(TaskViews.class).displayWorkflow(workflowVO.getId());
			} catch (Exception e) {
				e.printStackTrace();
				allow = false;
			}
		}
		return allow;
	}

}
