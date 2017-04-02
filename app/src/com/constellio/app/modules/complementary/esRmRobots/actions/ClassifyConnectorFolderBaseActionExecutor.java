package com.constellio.app.modules.complementary.esRmRobots.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.services.ClassifyConnectorRecordInTaxonomyExecutor;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public abstract class ClassifyConnectorFolderBaseActionExecutor implements ActionExecutor {

	public final static Set<String> SUPPORTED_TYPES = new HashSet<>();

	static {
		SUPPORTED_TYPES.add(ConnectorSmbFolder.SCHEMA_TYPE);
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(ClassifyConnectorFolderBaseActionExecutor.class);

	@Override
	public Transaction execute(String robotId, ActionParameters actionParameters, AppLayerFactory appLayerFactory,
			List<Record> records, List<Record> processedRecords, boolean dryRun) {
		Transaction transaction = new Transaction();
		String collection = actionParameters.getCollection();
		RobotSchemaRecordServices robots = new RobotSchemaRecordServices(collection, appLayerFactory);

		transaction.add(logguerMessage(robotId, robots, "Démarrage du robot dans la collection " + collection));

		ClassifyConnectorFolderActionParameters params = wrap(actionParameters);

		User user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(User.ADMIN, collection);

		for (Record record : records) {

			try {
				new ClassifyConnectorRecordInTaxonomyExecutor(record, params, appLayerFactory, user, robotId, processedRecords, dryRun)
						.execute();
				processedRecords.add(record);
			} catch (Throwable e) {
				LOGGER.warn("Cannot classify record", e);
				
				transaction.add(logguerMessage(robotId, robots, ExceptionUtils.getStackTrace(e)));
			}
		}

		return transaction;
	}

	private RecordWrapper logguerMessage(String robotId, RobotSchemaRecordServices robots, String message) {
		return robots.newRobotLog().setTitle(message).setRobot(robotId);
	}

	protected abstract ClassifyConnectorFolderActionParameters wrap(ActionParameters actionParameters);

}
