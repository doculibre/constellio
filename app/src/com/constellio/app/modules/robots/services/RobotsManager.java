package com.constellio.app.modules.robots.services;

import com.constellio.app.modules.robots.RobotsConfigs;
import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.DryRunRobotAction;
import com.constellio.app.modules.robots.model.RegisteredAction;
import com.constellio.app.modules.robots.model.services.RobotsService;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.ui.pages.search.criteria.ConditionBuilder;
import com.constellio.app.ui.pages.search.criteria.ConditionException;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static com.constellio.app.modules.robots.model.DryRunRobotAction.dryRunRobotAction;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.impossibleCondition;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.not;

public class RobotsManager implements StatefulService {
	public static final String ID = "robotsManager";
	private Map<String, RegisteredAction> actions = new HashMap<>();
	private RobotSchemaRecordServices robotSchemas;
	private SearchServices searchServices;
	private BatchProcessesManager batchProcessesManager;
	private String collection;
	private RobotsService robotsService;
	private RobotsConfigs robotsConfigs;

	public RobotsManager(RobotSchemaRecordServices robotSchemas) {
		this.collection = robotSchemas.getCollection();
		this.robotSchemas = robotSchemas;
		this.searchServices = robotSchemas.getModelLayerFactory().newSearchServices();
		this.batchProcessesManager = robotSchemas.getModelLayerFactory().getBatchProcessesManager();
		this.robotsService = new RobotsService(robotSchemas.getCollection(), robotSchemas.getAppLayerFactory());
		this.robotsConfigs = new RobotsConfigs(robotSchemas.getModelLayerFactory());
		startAutoExecutorThread();
	}

	private void startAutoExecutorThread() {
		BackgroundThreadsManager manager = robotSchemas.getModelLayerFactory().getDataLayerFactory()
				.getBackgroundThreadsManager();

		manager.configure(BackgroundThreadConfiguration.repeatingAction("startAutoExecutingRobots", new Runnable() {
			@Override
			public void run() {
				startAutoExecutingRobots();
			}
		}).handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE)
				.executedEvery(Duration.standardMinutes(robotsConfigs.getRobotsAutomaticExecutionDelay())));
	}

	public RegisteredAction registerAction(String code, String parametersSchemaLocalCode, Collection<String> types,
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

	public void startAutoExecutingRobots() {
		for (Robot robot : robotsService.getAutoExecutingRootRobots()) {
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
		if (searchServices.hasResults(query(condition.conditionIncludingParentCondition))) {
			for (Robot childRobot : robotsService.getChildRobots(robot.getId())) {
				RobotCondition childsCondition = startRobotExecution(childRobot, conditions, dryRunRobotActions);
				condition.childRobotConditions.add(childsCondition);
			}
		}
		conditions.pop();
		if (robot.getAction() != null) {
			LogicalSearchQuery query = query(condition.buildCondition());
			if (dryRunRobotActions != null) {
				ModelLayerFactory modelLayerFactory = robotSchemas.getModelLayerFactory();
				MetadataSchemasManager msm = modelLayerFactory.getMetadataSchemasManager();
				MetadataSchemaTypes schemaTypes = msm.getSchemaTypes(robot.getCollection());
				RecordServices recordServices = modelLayerFactory.newRecordServices();

				RobotBatchProcessAction batchProcessAction = new RobotBatchProcessAction(robot.getId(), robot.getAction(),
						robot.getActionParameters());
				batchProcessAction.setDryRun(true);
				batchProcessAction.execute(searchServices.search(query), null, schemaTypes, new RecordProvider(recordServices), modelLayerFactory);

				Iterator<Record> recordsIterator = batchProcessAction.getProcessedRecords().iterator();
				while (recordsIterator.hasNext()) {
					Record record = recordsIterator.next();
					dryRunRobotActions.add(dryRunRobotAction(record, robot, robotSchemas));
				}
			} else {
				createBatchProcess(robot.getId(), query, robot.getAction(), robot.getActionParameters());
			}
		}
		return condition;
	}

	private void createBatchProcess(String robotId, LogicalSearchQuery query, String action,
									String actionParametersId) {
		if (searchServices.hasResults(query)) {
			RobotBatchProcessAction batchProcessAction = new RobotBatchProcessAction(robotId, action, actionParametersId);
			BatchProcess batchProcess = batchProcessesManager
					.addBatchProcessInStandby(query, batchProcessAction, "robot " + robotId);
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
		String languageCode = searchServices.getLanguageCode(robot.getCollection());
		LogicalSearchCondition condition = new ConditionBuilder(type, languageCode).build(robot.getSearchCriteria());
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

	private LogicalSearchQuery query(LogicalSearchCondition condition) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.setPreferAnalyzedFields(true);
		return query;
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

		public RobotCondition(LogicalSearchCondition localCondition,
							  LogicalSearchCondition conditionIncludingParentCondition,
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
