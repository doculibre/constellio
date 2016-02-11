package com.constellio.app.modules.complementary.esRmRobots.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.services.ClassifyConnectorRecordInTaxonomyExecutor;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.modules.robots.services.RobotsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesException;

public class ClassifyConnectorTaxonomyActionExecutor implements ActionExecutor {

	public final static String ID = ClassifyConnectorTaxonomyActionParameters.SCHEMA_LOCAL_CODE;

	public final static String PARAMETER_SCHEMA = ClassifyConnectorTaxonomyActionParameters.SCHEMA_LOCAL_CODE;

	public final static Set<String> SUPPORTED_TYPES = new HashSet<>();

	static {
		SUPPORTED_TYPES.add(ConnectorSmbFolder.SCHEMA_TYPE);
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(ClassifyConnectorTaxonomyActionExecutor.class);

	@Override
	public Transaction execute(String robotId, ActionParameters actionParameters, AppLayerFactory appLayerFactory,
			List<Record> records) {

		String collection = actionParameters.getCollection();
		RobotSchemaRecordServices robots = new RobotSchemaRecordServices(collection, appLayerFactory);

		ClassifyConnectorTaxonomyActionParameters params = ClassifyConnectorTaxonomyActionParameters.wrap(actionParameters);

		User user = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(User.ADMIN, collection);

		for (Record record : records) {

			try {
				new ClassifyConnectorRecordInTaxonomyExecutor(
						record, params, appLayerFactory, user, robotId).execute();

			} catch (Throwable e) {
				LOGGER.warn("Cannot classify record", e);
				try {
					appLayerFactory.getModelLayerFactory().newRecordServices()
							.add(robots.newRobotLog().setTitle(e.getMessage()).setRobot(robotId));
				} catch (RecordServicesException e1) {
					throw new RuntimeException("Failed to create the robot error log");
				}
			}
		}

		return new Transaction();
	}

	public static void registerIn(RobotsManager robotsManager) {
		robotsManager.registerAction(ID, PARAMETER_SCHEMA, SUPPORTED_TYPES, new ClassifyConnectorTaxonomyActionExecutor());
	}

}
