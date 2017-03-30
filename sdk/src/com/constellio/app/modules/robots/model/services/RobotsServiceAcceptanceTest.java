package com.constellio.app.modules.robots.model.services;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static org.assertj.core.api.Assertions.assertThat;

import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;

import java.util.List;

public class RobotsServiceAcceptanceTest extends ConstellioTest {

	private final String rootRobotId = "rootRobotId";
	private final String child1Id = "child1";
	private final String child2Id = "child2";
	private final String anothertRobotId = "anothertRobotId";

	private RobotSchemaRecordServices robots;
	private RobotsService robotsService;
	private Metadata parentMetadata;
	private MetadataSchema robotSchema;
	private MetadataSchema actionParameterSchema;
	private Robot rootRobot;
	private Robot child1;
	private Robot child2;
	private Robot child11;
	private Robot child12;
	private Robot child21;

	private Robot anotherRobot;


	private String dummyActionParametersId = "dummyActionParametersId";
	private String anotherDummyActionParametersId = "anotherDummyActionParametersId";

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withRobotsModule().withAllTestUsers());

		robots = new RobotSchemaRecordServices(zeCollection, getAppLayerFactory());
		robotsService = new RobotsService(zeCollection, getAppLayerFactory());

		robotSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchema(Robot.DEFAULT_SCHEMA);
		parentMetadata = robotSchema.getMetadata(Robot.PARENT);

		actionParameterSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchema(ActionParameters.DEFAULT_SCHEMA);

		getModelLayerFactory().newRecordServices().add(robots.newActionParametersWithId(dummyActionParametersId));
		getModelLayerFactory().newRecordServices().add(robots.newActionParametersWithId(anotherDummyActionParametersId));
		createRobotsHierarchy();

	}

	@Test
	public void whenDeletingRootRobotThenAllHierarchyDeleted() {
		robotsService.deleteRobotHierarchy(rootRobotId);

		assertThat(robots.searchRobots(allRobotsQuery())).isEmpty();
	}

	@Test
	public void whenDeletingRobotThenActionParameterDeleted() {
		robotsService.deleteRobotHierarchy(anothertRobotId);

		List<ActionParameters> actionParametersList = robots.searchActionParameterss(new LogicalSearchQuery(LogicalSearchQueryOperators.from(actionParameterSchema).returnAll()));
		assertThat(actionParametersList).extracting("id").containsExactly(dummyActionParametersId);
	}

	@Test
	public void whenDeleting2ndLevelRobotThenItsHierarchyDeleted() {
		robotsService.deleteRobotHierarchy(child1Id);

		assertThat(robots.searchRobots(allRobotsQuery())).doesNotContain(child1, child11, child12);
		assertThat(robots.searchRobots(allRobotsQuery())).containsOnly(rootRobot, child2, child21);
	}

	@Test
	public void whenLoadingTheLineageThenReturnParentsFromRootDownAndItself() {
		assertThat(robotsService.loadAncestors("child11")).extracting("id").containsExactly(rootRobotId, child1Id, "child11");
	}

	private void createRobotsHierarchy()
			throws RecordServicesException {
		Transaction transaction = new Transaction();

		rootRobot = newRobot(rootRobotId).setTitle("Root robot");
		child1 = newRobot(child1Id).setTitle("Child 1").setParent(rootRobot);
		child2 = newRobot(child2Id).setTitle("Child 2").setParent(rootRobot);
		child11 = newRobot("child11").setTitle("Child 1.1").setParent(child1);
		child12 = newRobot("child12").setTitle("Child 1.2").setParent(child1);
		child21 = newRobot("child21").setTitle("Child 2.1").setParent(child2);
		anotherRobot = newRobot(anothertRobotId).setTitle("Child 2.1").setParent(child2).setActionParameters(anotherDummyActionParametersId);

		transaction.add(rootRobot);
		transaction.add(child1);
		transaction.add(child2);
		transaction.add(child11);
		transaction.add(child12);
		transaction.add(child21);
		transaction.add(anotherRobot);

		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	private Robot newRobot(String code) {
		Robot robot = robots.newRobotWithId(code).setCode(code);
		robot.setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE);
		robot.setSearchCriterion(new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE).where(TITLE).isEqualTo("Chuck Norris"));
		robot.setAction("zeAction");
		robot.setActionParameters(dummyActionParametersId);
		robot.setExcludeProcessedByChildren(true);
		return robot;
	}

	public LogicalSearchQuery allRobotsQuery() {
		return new LogicalSearchQuery(LogicalSearchQueryOperators.from(robotSchema).returnAll());
	}
}
