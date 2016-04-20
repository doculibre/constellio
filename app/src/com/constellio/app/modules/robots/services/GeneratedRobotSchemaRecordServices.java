package com.constellio.app.modules.robots.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class GeneratedRobotSchemaRecordServices extends SchemasRecordsServices {
	protected final AppLayerFactory appLayerFactory;

	public GeneratedRobotSchemaRecordServices(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory.getModelLayerFactory());
		this.appLayerFactory = appLayerFactory;
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

	public ActionParameters wrapActionParameters(Record record) {
		return record == null ? null : new ActionParameters(record, getTypes());
	}

	public List<ActionParameters> wrapActionParameterss(List<Record> records) {
		List<ActionParameters> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new ActionParameters(record, getTypes()));
		}

		return wrapped;
	}

	public List<ActionParameters> searchActionParameterss(LogicalSearchQuery query) {
		return wrapActionParameterss(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<ActionParameters> searchActionParameterss(LogicalSearchCondition condition) {
		MetadataSchemaType type = actionParameters.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapActionParameterss(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public ActionParameters getActionParameters(String id) {
		return wrapActionParameters(get(id));
	}

	public List<ActionParameters> getActionParameterss(List<String> ids) {
		return wrapActionParameterss(get(ids));
	}

	public ActionParameters getActionParametersWithLegacyId(String legacyId) {
		return wrapActionParameters(getByLegacyId(actionParameters.schemaType(), legacyId));
	}

	public ActionParameters newActionParameters() {
		return wrapActionParameters(create(actionParameters.schema()));
	}

	public ActionParameters newActionParametersWithId(String id) {
		return wrapActionParameters(create(actionParameters.schema(), id));
	}

	public final SchemaTypeShortcuts_actionParameters_default actionParameters
			= new SchemaTypeShortcuts_actionParameters_default("actionParameters_default");

	public class SchemaTypeShortcuts_actionParameters_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_actionParameters_default(String schemaCode) {
			super(schemaCode);
		}
	}

	public Robot wrapRobot(Record record) {
		return record == null ? null : new Robot(record, getTypes());
	}

	public List<Robot> wrapRobots(List<Record> records) {
		List<Robot> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Robot(record, getTypes()));
		}

		return wrapped;
	}

	public List<Robot> searchRobots(LogicalSearchQuery query) {
		return wrapRobots(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<Robot> searchRobots(LogicalSearchCondition condition) {
		MetadataSchemaType type = robot.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapRobots(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public Robot getRobot(String id) {
		return wrapRobot(get(id));
	}

	public List<Robot> getRobots(List<String> ids) {
		return wrapRobots(get(ids));
	}

	public Robot getRobotWithCode(String code) {
		return wrapRobot(getByCode(robot.schemaType(), code));
	}

	public Robot getRobotWithLegacyId(String legacyId) {
		return wrapRobot(getByLegacyId(robot.schemaType(), legacyId));
	}

	public Robot newRobot() {
		return wrapRobot(create(robot.schema()));
	}

	public Robot newRobotWithId(String id) {
		return wrapRobot(create(robot.schema(), id));
	}

	public final SchemaTypeShortcuts_robot_default robot
			= new SchemaTypeShortcuts_robot_default("robot_default");

	public class SchemaTypeShortcuts_robot_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_robot_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata action() {
			return metadata("action");
		}

		public Metadata actionParameters() {
			return metadata("actionParameters");
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata excludeProcessedByChildren() {
			return metadata("excludeProcessedByChildren");
		}

		public Metadata parent() {
			return metadata("parent");
		}

		public Metadata schemaFilter() {
			return metadata("schemaFilter");
		}

		public Metadata searchCriteria() {
			return metadata("searchCriteria");
		}

		public Metadata autoExecute() {
			return metadata("autoExecute");
		}
	}

	public RobotLog wrapRobotLog(Record record) {
		return record == null ? null : new RobotLog(record, getTypes());
	}

	public List<RobotLog> wrapRobotLogs(List<Record> records) {
		List<RobotLog> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new RobotLog(record, getTypes()));
		}

		return wrapped;
	}

	public List<RobotLog> searchRobotLogs(LogicalSearchQuery query) {
		return wrapRobotLogs(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public List<RobotLog> searchRobotLogs(LogicalSearchCondition condition) {
		MetadataSchemaType type = robotLog.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapRobotLogs(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}

	public RobotLog getRobotLog(String id) {
		return wrapRobotLog(get(id));
	}

	public List<RobotLog> getRobotLogs(List<String> ids) {
		return wrapRobotLogs(get(ids));
	}

	public RobotLog getRobotLogWithLegacyId(String legacyId) {
		return wrapRobotLog(getByLegacyId(robotLog.schemaType(), legacyId));
	}

	public RobotLog newRobotLog() {
		return wrapRobotLog(create(robotLog.schema()));
	}

	public RobotLog newRobotLogWithId(String id) {
		return wrapRobotLog(create(robotLog.schema(), id));
	}

	public final SchemaTypeShortcuts_robotLog_default robotLog
			= new SchemaTypeShortcuts_robotLog_default("robotLog_default");

	public class SchemaTypeShortcuts_robotLog_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_robotLog_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata robot() {
			return metadata("robot");
		}
	}
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
}
