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
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
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
			List<Record> records, List<Record> processedRecords) {
		String collection = actionParameters.getCollection();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		RobotSchemaRecordServices robots = new RobotSchemaRecordServices(collection, appLayerFactory);

		logguerMessage(robotId, recordServices, robots, "Démarrage du robot dans la collection " + collection);

		ClassifyConnectorFolderActionParameters params = wrap(actionParameters);

		User user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(User.ADMIN, collection);

		for (Record record : records) {

			try {
				new ClassifyConnectorRecordInTaxonomyExecutor(record, params, appLayerFactory, user, robotId, processedRecords)
						.execute();
				processedRecords.add(record);
			} catch (Throwable e) {
				LOGGER.warn("Cannot classify record", e);
				
				logguerMessage(robotId, recordServices, robots, ExceptionUtils.getStackTrace(e));
			}
		}

		return new Transaction();
	}

	private void logguerMessage(String robotId, RecordServices recordServices, RobotSchemaRecordServices robots,
			String message) {
		try {
			
			recordServices
					.add(robots.newRobotLog().setTitle(message).setRobot(robotId));
		} catch (RecordServicesException e1) {
			throw new RuntimeException("Failed to create the robot error log");
		}
	}

	protected abstract ClassifyConnectorFolderActionParameters wrap(ActionParameters actionParameters);

}
