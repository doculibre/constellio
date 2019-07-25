package com.constellio.app.modules.tasks.ui.pages.workflow;

import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.navigation.TaskViews;
import com.constellio.app.modules.tasks.services.BetaWorkflowServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class BetaListWorkflowsPresenter extends SingleSchemaBasePresenter<BetaListWorkflowsView> {
	private transient BetaWorkflowServices workflowServices;

	public BetaListWorkflowsPresenter(BetaListWorkflowsView view) {
		super(view, BetaWorkflow.DEFAULT_SCHEMA);
	}

	public void addButtonClicked() {
		view.navigate().to(TaskViews.class).addWorkflow();
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	public RecordVODataProvider getWorkflows() {
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder()
				.build(schema(BetaWorkflow.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return workflowServices().getWorkflowsQuery();
			}
		};
	}

	public void displayButtonClicked(RecordVO record) {
		view.navigate().to(TaskViews.class).displayWorkflow(record.getId());
	}

	public void editButtonClicked(RecordVO record) {
		view.navigate().to(TaskViews.class).editWorkflow(record.getId());
	}

	public void deleteButtonClicked(RecordVO record) {
		delete(toRecord(record), false);
		view.navigate().to(TaskViews.class).listWorkflows();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(TasksPermissionsTo.MANAGE_WORKFLOWS).globally();
	}

	private BetaWorkflowServices workflowServices() {
		if (workflowServices == null) {
			workflowServices = new BetaWorkflowServices(view.getCollection(), appLayerFactory);
		}
		return workflowServices;
	}
}
