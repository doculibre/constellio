package com.constellio.app.modules.robots.extensions;

import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.constellio.app.api.extensions.SystemCheckExtension;
import com.constellio.app.api.extensions.params.CollectionSystemCheckParams;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;

public class RobotSystemCheckExtension extends SystemCheckExtension {

	public static final String UNUSED_ROBOT_ACTIONS = "robots.unusedRobotActions";

	AppLayerFactory appLayerFactory;

	RecordServices recordServices;
	SearchServices searchServices;
	RobotSchemaRecordServices robotSchemas;

	public RobotSystemCheckExtension(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.robotSchemas = new RobotSchemaRecordServices(collection, appLayerFactory);
	}

	@Override
	public void checkCollection(CollectionSystemCheckParams collectionCheckParams) {

		Set<String> currentlyUsedParameterIds = new HashSet<>();
		Iterator<Record> robotsIterator = searchServices.recordsIterator(query(from(
				robotSchemas.robot.schemaType()).returnAll()), 1000);
		while (robotsIterator.hasNext()) {
			Robot robot = robotSchemas.wrapRobot(robotsIterator.next());
			currentlyUsedParameterIds.add(robot.getActionParameters());
		}

		List<String> actionParametersIds = searchServices.searchRecordIds(query(from(
				robotSchemas.actionParameters.schemaType()).returnAll()));

		for (String actionParameterId : actionParametersIds) {
			if (!currentlyUsedParameterIds.contains(actionParameterId)) {
				collectionCheckParams.getResultsBuilder().incrementMetric(UNUSED_ROBOT_ACTIONS);

				if (collectionCheckParams.isRepair()) {
					ActionParameters actionParameters = robotSchemas.getActionParameters(actionParameterId);
					recordServices.physicallyDeleteNoMatterTheStatus(actionParameters.getWrappedRecord(), User.GOD,
							new RecordPhysicalDeleteOptions());
				}
			}
		}

	}
}
