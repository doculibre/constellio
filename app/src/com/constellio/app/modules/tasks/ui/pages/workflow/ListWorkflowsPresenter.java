package com.constellio.app.modules.tasks.ui.pages.workflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.services.WorkflowServices;
import com.constellio.app.modules.tasks.ui.builders.WorkflowToVoBuilder;
import com.constellio.app.modules.tasks.ui.entities.WorkflowVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;

public class ListWorkflowsPresenter extends SingleSchemaBasePresenter<ListWorkflowsView> {
	
	private transient WorkflowServices workflowServices;
	
	private WorkflowToVoBuilder voBuilder;
	
	private List<WorkflowVO> workflowVOs = new ArrayList<WorkflowVO>();

	public ListWorkflowsPresenter(ListWorkflowsView view) {
		super(view, Workflow.DEFAULT_SCHEMA);
		initTransientObjects();
		voBuilder = new WorkflowToVoBuilder();
		SessionContext sessionContext = view.getSessionContext();
		List<Workflow> workflows = workflowServices.getWorkflows();
		for (Workflow workflow : workflows) {
			WorkflowVO workflowVO = voBuilder.build(workflow.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
			workflowVOs.add(workflowVO);
		}
		view.setWorkflowVOs(workflowVOs);
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
		return true;
	}
	
	void backButtonClicked() {
		view.navigateTo().tasksManagement();
	}
	
	void addButtonClicked() {
		view.navigateTo().addWorkflow();
	}
	
	void displayButtonClicked(WorkflowVO workflowVO) {
		view.navigateTo().displayWorkflow(workflowVO.getId());
	}
	
	void editButtonClicked(WorkflowVO workflowVO) {
		view.navigateTo().editWorkflow(workflowVO.getId());
	}
	
	void deleteButtonClicked(WorkflowVO workflowVO) {
		try {
			Record workflowRecord = toRecord(workflowVO);
			User god = User.GOD;
			recordServices().logicallyDelete(workflowRecord, god);
			modelLayerFactory.newLoggingServices().logDeleteRecordWithJustification(workflowRecord, getCurrentUser(), null);
			recordServices().physicallyDelete(workflowRecord, god);
			view.remove(workflowVO);
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
		}
	}	

}
