package com.constellio.app.modules.robots.services;

import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class RobotBatchProcessAction implements BatchProcessAction {
	private String robotId;
	private String action;

	private String actionParametersId;

	private boolean dryRun;
	private List<Record> processedRecords;

	public RobotBatchProcessAction(String robotId, String action, String actionParametersId) {
		this.robotId = robotId;
		this.action = action;
		this.actionParametersId = actionParametersId;
		this.processedRecords = new ArrayList<>();
	}

	@Override
	public Transaction execute(List<Record> batch, User user, MetadataSchemaTypes schemaTypes,
							   RecordProvider recordProvider, ModelLayerFactory modelLayerFactory) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		RobotSchemaRecordServices schemas = new RobotSchemaRecordServices(schemaTypes.getCollection(), appLayerFactory);
		RobotsManager robotsManager = schemas.getRobotsManager();

		ActionExecutor actionExecutor = robotsManager.getActionExecutorFor(action);
		ActionParameters actionParameters = null;
		if (actionParametersId != null) {
			actionParameters = schemas.getActionParameters(actionParametersId);
		}

		processedRecords.clear();
		Transaction transaction = actionExecutor != null
								  ? actionExecutor.execute(robotId, actionParameters, appLayerFactory, batch, processedRecords, isDryRun()) : new Transaction();

		int documents = 0;
		int folders = 0;
		for (Record record : processedRecords) {
			if (StringUtils.containsIgnoreCase(record.getSchemaCode(), "document")) {
				documents++;
			} else if (StringUtils.containsIgnoreCase(record.getSchemaCode(), "folder")) {
				folders++;
			}
		}

		String message = $("RobotBatchProcessAction.completed", documents, folders);
		transaction.add(schemas.newRobotLog().setRobot(robotId).setTitle(message).setProcessRecordsCount(processedRecords.size()));
		return transaction;
	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[]{robotId, action, actionParametersId};
	}

	public boolean isDryRun() {
		return dryRun;
	}

	public void setDryRun(boolean pDryRun) {
		dryRun = pDryRun;
	}

	public List<Record> getProcessedRecords() {
		return Collections.unmodifiableList(processedRecords);
	}
}
