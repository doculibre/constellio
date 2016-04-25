package com.constellio.app.modules.robots.model.actions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;

public class RunExtractorsActionExecutor implements ActionExecutor {

	public final static String ID = "runExtractorsAction";
	private final static Logger LOGGER = LoggerFactory.getLogger(RunExtractorsActionExecutor.class);

	public static void registerIn(RobotsManager robotsManager) {
		// ParamsSchema is null because this action requires no parameters
		// The types list would be null or empty because it applies to everything
		robotsManager.registerAction(ID, null, null, new RunExtractorsActionExecutor());
	}

	@Override
	public Transaction execute(String robotId, ActionParameters actionParameters, AppLayerFactory appLayerFactory,
			List<Record> records, List<Record> processedRecords) {
		// Parameters will always be null, not use them
		Transaction transaction = new Transaction();
		transaction.setSkippingRequiredValuesValidation(true);
		transaction.getRecordUpdateOptions().forceReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		transaction.addUpdate(records);
		return transaction;
	}
}
