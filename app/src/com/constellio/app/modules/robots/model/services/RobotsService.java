package com.constellio.app.modules.robots.model.services;

import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RobotsService {
	private final RecordServices recordServices;
	private final SearchServices searchServices;
	private final RobotSchemaRecordServices robots;

	public RobotsService(String collection, AppLayerFactory appLayerFactory) {
		ModelLayerFactory factory = appLayerFactory.getModelLayerFactory();
		recordServices = factory.newRecordServices();
		searchServices = factory.newSearchServices();
		robots = new RobotSchemaRecordServices(collection, appLayerFactory);
	}

	public Robot newRobot(Robot parent) {
		Robot robot = robots.newRobot();
		if (parent != null) {
			robot.setParent(parent);
			robot.setSchemaFilter(parent.getSchemaFilter());
		}
		return robot;
	}

	public Robot newRobot(String parentId) {
		Robot parent = parentId != null ? robots.getRobot(parentId) : null;
		return newRobot(parent);
	}

	public List<Robot> loadAncestors(Robot robot) {
		String path = robot.getPaths().get(0);
		List<String> lineage = Arrays.asList(path.substring(1).split("/"));
		// TODO: The sort is just a quick workaround
		LogicalSearchQuery query = new LogicalSearchQuery(from(robots.robot.schemaType()).where(Schemas.IDENTIFIER).isIn(lineage))
				.sortAsc(Schemas.CREATED_ON).sortAsc(Schemas.IDENTIFIER);
		return robots.searchRobots(query);
	}

	public List<Robot> loadAncestors(String robotId) {
		return loadAncestors(robots.getRobot(robotId));
	}

	public List<String> loadIdTreeOf(String robotId) {
		List<String> result = new ArrayList<>();
		result.add(robotId);
		List<String> generation = loadGenerationIds(Arrays.asList(robotId));
		while (!generation.isEmpty()) {
			result.addAll(generation);
			generation = loadGenerationIds(generation);
		}
		return result;
	}

	private List<String> loadGenerationIds(List<String> robotIds) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(robots.robot.schemaType()).where(robots.robot.parent()).isIn(robotIds));
		return searchServices.searchRecordIds(query);
	}

	public void deleteRobotHierarchy(Robot robot) {
		for (String robotLogId : getRobotLogIds(robot.getId())) {
			RobotLog robotLog = robots.getRobotLog(robotLogId);
			recordServices.logicallyDelete(robotLog.getWrappedRecord(), User.GOD);
			recordServices.physicallyDelete(robotLog.getWrappedRecord(), User.GOD);
		}

		for (Robot child : getChildRobots(robot.getId())) {
			deleteRobotHierarchy(child);
		}

		deleteRobotsActionParameters(robot);

		recordServices.logicallyDelete(robot.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(robot.getWrappedRecord(), User.GOD);
	}

	private void deleteRobotsActionParameters(Robot robot) {
		String actionParamId = robot.getActionParameters();
		if (actionParamId != null) {
			ActionParameters actionParameters = robots.getActionParameters(actionParamId);

			recordServices.logicallyDelete(actionParameters.getWrappedRecord(), User.GOD);
			recordServices.physicallyDeleteNoMatterTheStatus(actionParameters.getWrappedRecord(), User.GOD, new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
		}
	}

	public void deleteRobotHierarchy(String robotId) {
		deleteRobotHierarchy(robots.getRobot(robotId));
	}

	public List<Robot> getRootRobots() {
		LogicalSearchQuery searchQuery = new LogicalSearchQuery(from(robots.robot.schemaType())
				.where(robots.robot.parent()).isNull());
		searchQuery.sortAsc(Schemas.CODE);
		return robots.searchRobots(searchQuery);
	}

	public List<Robot> getAutoExecutingRootRobots() {

		LogicalSearchQuery searchQuery = new LogicalSearchQuery(from(robots.robot.schemaType())
				.where(robots.robot.parent()).isNull().andWhere(robots.robot.autoExecute()).isTrue());
		searchQuery.sortAsc(Schemas.CODE);
		return robots.searchRobots(searchQuery);

	}

	public List<Robot> getChildRobots(String robotId) {
		LogicalSearchQuery searchQuery = new LogicalSearchQuery(from(robots.robot.schemaType())
				.where(robots.robot.parent()).isEqualTo(robotId));
		searchQuery.sortAsc(Schemas.CODE);
		return robots.searchRobots(searchQuery);
	}

	private List<String> getRobotLogIds(String robotId) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(robots.robotLog.schemaType()).where(robots.robotLog.robot()).isEqualTo(robotId));
		return searchServices.searchRecordIds(query);
	}
}
