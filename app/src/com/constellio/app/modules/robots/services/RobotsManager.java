package com.constellio.app.modules.robots.services;

import static com.constellio.app.modules.robots.model.DryRunRobotAction.dryRunRobotAction;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.impossibleCondition;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.not;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.DryRunRobotAction;
import com.constellio.app.modules.robots.model.RegisteredAction;
import com.constellio.app.modules.robots.model.services.RobotsService;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.ui.pages.search.criteria.ConditionBuilder;
import com.constellio.app.ui.pages.search.criteria.ConditionException;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class RobotsManager implements StatefulService {

	public static final String ID = "robotsManager";

	private Map<String, RegisteredAction> actions = new HashMap<>();

	private RobotSchemaRecordServices robotSchemas;

	private SearchServices searchServices;

	private BatchProcessesManager batchProcessesManager;

	private String collection;

	private RobotsService robotsService;

	public RobotsManager(RobotSchemaRecordServices robotSchemas) {
		this.collection = robotSchemas.getCollection();
		this.robotSchemas = robotSchemas;
		this.searchServices = robotSchemas.getModelLayerFactory().newSearchServices();
		this.batchProcessesManager = robotSchemas.getModelLayerFactory().getBatchProcessesManager();
		this.robotsService = new RobotsService(robotSchemas.getCollection(), robotSchemas.getAppLayerFactory());
	}

	public RegisteredAction registerAction(String code, String parametersSchemaLocalCode, List<String> types,
			ActionExecutor executor) {
		RegisteredAction registeredAction = new RegisteredAction(code, parametersSchemaLocalCode, executor, types);
		actions.put(code, registeredAction);
		return registeredAction;
	}

	public List<RegisteredAction> getRegisteredActionsFor(String schemaTypeCode) {
		List<RegisteredAction> returnedActions = new ArrayList<>();
		for (RegisteredAction action : actions.values()) {
			if (action.getSupportedSchemaTypes() == null || action.getSupportedSchemaTypes().contains(schemaTypeCode)) {
				returnedActions.add(action);
			}
		}
		return returnedActions;
	}

	public List<String> getSupportedSchemaTypes() {
		Set<String> types = new HashSet<>();
		for (RegisteredAction action : actions.values()) {
			if (action.getSupportedSchemaTypes() != null) {
				types.addAll(action.getSupportedSchemaTypes());
			}
		}
		return new ArrayList<>(types);
	}

	public void startAllRobotsExecution() {
		for (Robot robot : robotsService.getRootRobots()) {
			Stack<LogicalSearchCondition> conditions = new Stack<>();
			startRobotExecution(robot, conditions, null);
		}
	}

	public void startRobotExecution(String id) {
		startRobotExecution(robotSchemas.getRobot(id));
	}

	private void startRobotExecution(Robot robot) {
		Stack<LogicalSearchCondition> conditions = new Stack<>();
		startRobotExecution(robot, conditions, null);
	}

	private RobotCondition startRobotExecution(Robot robot, Stack<LogicalSearchCondition> conditions,
			List<DryRunRobotAction> dryRunRobotActions) {
		LogicalSearchCondition localCondition = getResolveCondition(robot);
		conditions.push(localCondition);

		RobotCondition condition = newRobotCondition(conditions, robot);
		if (searchServices.hasResults(condition.conditionIncludingParentCondition)) {
			for (Robot childRobot : robotsService.getChildRobots(robot.getId())) {
				RobotCondition childsCondition = startRobotExecution(childRobot, conditions, dryRunRobotActions);
				condition.childRobotConditions.add(childsCondition);
			}
		}
		conditions.pop();
		if (robot.getAction() != null) {
			LogicalSearchCondition builtCondition = condition.buildCondition();
			if (dryRunRobotActions != null) {
				Iterator<Record> recordsIterator = searchServices.recordsIterator(new LogicalSearchQuery(builtCondition), 5000);
				while (recordsIterator.hasNext()) {
					Record record = recordsIterator.next();
					dryRunRobotActions.add(dryRunRobotAction(record, robot, robotSchemas));
				}
			} else {
				createBatchProcess(robot.getId(), builtCondition, robot.getAction(), robot.getActionParameters());
			}
		}
		return condition;
	}

	private void createBatchProcess(String robotId, LogicalSearchCondition condition, String action, String actionParametersId) {
		if (searchServices.hasResults(condition)) {
			RobotBatchProcessAction batchProcessAction = new RobotBatchProcessAction(robotId, action, actionParametersId);
			BatchProcess batchProcess = batchProcessesManager.addBatchProcessInStandby(condition, batchProcessAction);
			batchProcessesManager.markAsPending(batchProcess);
		}
	}

	public LogicalSearchCondition getResolveCondition(Robot robot) {

		MetadataSchemaType type = robotSchemas.schemaType(robot.getSchemaFilter());
		try {
			List<LogicalSearchCondition> conditions = buildConditionsForRobot(type, robot);
			return allConditions(conditions);

		} catch (ConditionException e) {
			e.printStackTrace();
			return impossibleCondition(robot.getCollection());
		}

	}

	private List<LogicalSearchCondition> buildConditionsForRobot(MetadataSchemaType type, Robot robot)
			throws ConditionException {

		List<LogicalSearchCondition> conditions = new ArrayList<>();
		LogicalSearchCondition condition = new ConditionBuilder(type).build(robot.getSearchCriteria());
		conditions.add(condition);
		String parentRobotId = robot.getParent();
		if (parentRobotId != null) {
			Robot parentRobot = robotSchemas.getRobot(parentRobotId);
			conditions.addAll(buildConditionsForRobot(type, parentRobot));
		}

		return conditions;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}

	public RegisteredAction getActionFor(String actionCode) {
		return actions.get(actionCode);
	}

	public ActionExecutor getActionExecutorFor(String actionCode) {
		RegisteredAction action = getActionFor(actionCode);
		return action == null ? null : action.getExecutor();
	}

	public boolean canExecute(String id) {
		return canExecute(robotSchemas.getRobot(id));
	}

	private boolean canExecute(Robot robot) {
		return robot.isRoot();
	}

	private RobotCondition newRobotCondition(Stack<LogicalSearchCondition> conditions, Robot robot) {
		LogicalSearchCondition condition = allConditions(new ArrayList<>(conditions));
		return new RobotCondition(conditions.peek(), condition, robot);
	}

	public List<DryRunRobotAction> dryRun(Robot robot) {
		List<DryRunRobotAction> actions = new ArrayList<>();
		Stack<LogicalSearchCondition> conditions = new Stack<>();
		startRobotExecution(robot, conditions, actions);
		return actions;
	}

	private static class RobotCondition {

		LogicalSearchCondition localCondition;

		LogicalSearchCondition conditionIncludingParentCondition;

		Robot robot;

		List<RobotCondition> childRobotConditions = new ArrayList<>();

		public RobotCondition(LogicalSearchCondition localCondition, LogicalSearchCondition conditionIncludingParentCondition,
				Robot robot) {
			this.localCondition = localCondition;
			this.conditionIncludingParentCondition = conditionIncludingParentCondition;
			this.robot = robot;
		}

		private LogicalSearchCondition newConditionIncludingParentsAndExcludingChildren() {
			List<LogicalSearchCondition> conditions = new ArrayList<>();
			for (RobotCondition robotCondition : childRobotConditions) {
				conditions.add(robotCondition.localCondition);
			}

			if (conditions.isEmpty()) {
				return conditionIncludingParentCondition;
			} else {
				return allConditions(
						conditionIncludingParentCondition,
						not(anyConditions(conditions))
				);
			}

		}

		public LogicalSearchCondition buildCondition() {
			if (robot.getExcludeProcessedByChildren()) {
				return newConditionIncludingParentsAndExcludingChildren();
			} else {
				return conditionIncludingParentCondition;
			}
		}
	}
}
