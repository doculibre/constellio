package com.constellio.app.modules.robots.services;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordProvider;

public class RobotBatchProcessAction implements BatchProcessAction {
	private String robotId;
	private String action;

	private String actionParametersId;

	public RobotBatchProcessAction(String robotId, String action, String actionParametersId) {
		this.robotId = robotId;
		this.action = action;
		this.actionParametersId = actionParametersId;
	}

	@Override
	public Transaction execute(List<Record> batch, MetadataSchemaTypes schemaTypes, RecordProvider recordProvider) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		RobotSchemaRecordServices schemas = new RobotSchemaRecordServices(schemaTypes.getCollection(), appLayerFactory);
		RobotsManager robotsManager = schemas.getRobotsManager();

		ActionExecutor actionExecutor = robotsManager.getActionExecutorFor(action);
		ActionParameters actionParameters = null;
		if (actionParametersId != null) {
			actionParameters = schemas.getActionParameters(actionParametersId);
		}

		List<Record> processedRecords = new ArrayList<>();
		Transaction transaction = actionExecutor != null
				? actionExecutor.execute(robotId, actionParameters, appLayerFactory, batch, processedRecords) : new Transaction();

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
		return new Object[] { robotId, action, actionParametersId };
	}
}
