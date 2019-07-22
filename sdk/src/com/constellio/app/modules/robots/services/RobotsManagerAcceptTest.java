package com.constellio.app.modules.robots.services;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.robots.model.ActionExecutor;
import com.constellio.app.modules.robots.model.actions.RunExtractorsActionExecutor;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.modules.robots.model.wrappers.Robot;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.robots.model.DryRunRobotAction.dryRunRobotAction;
import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.AND;
import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.OR;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class RobotsManagerAcceptTest extends ConstellioTest {

	private TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	private ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	private AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	private static final Metadata METADATA1 = Metadata.newGlobalMetadata((short)0,"metadata1_s", MetadataValueType.STRING, false, false);
	private static final Metadata METADATA2 = Metadata.newGlobalMetadata((short)0,"metadata2_s", MetadataValueType.STRING, false, false);
	private static final Metadata METADATA3 = Metadata.newGlobalMetadata((short)0,"metadata3_s", MetadataValueType.STRING, false, false);
	private static final Metadata METADATA4 = Metadata.newGlobalMetadata((short)0,"metadata4_s", MetadataValueType.STRING, false, false);
	private static final Metadata METADATA5 = Metadata.newGlobalMetadata((short)0,"metadata5_s", MetadataValueType.STRING, false, false);
	private static final Metadata METADATA6 = Metadata.newGlobalMetadata((short)0,"metadata6_s", MetadataValueType.STRING, false, false);

	private static final String SET_METADATA1 = "setMetadata1";
	private static final String SET_METADATA1_PARAMETERS_SCHEMA = "setMetadata1Parameters";

	private static final String SET_METADATA2 = "setMetadata2";
	private static final String SET_METADATA2_PARAMETERS_SCHEMA = "setMetadata2Parameters";

	private static final String SET_METADATA3 = "setMetadata3";
	private static final String SET_METADATA3_PARAMETERS_SCHEMA = "setMetadata3Parameters";

	private static final String SET_METADATA4 = "setMetadata4";
	private static final String SET_METADATA4_PARAMETERS_SCHEMA = "setMetadata4Parameters";

	private static final String SET_METADATA5 = "setMetadata5";
	private static final String SET_METADATA5_PARAMETERS_SCHEMA = "setMetadata5Parameters";

	private static final String SET_METADATA6 = "setMetadata6";
	private static final String SET_METADATA6_PARAMETERS_SCHEMA = "setMetadata6Parameters";

	MetadataSchemasManager schemasManager;
	RMTestRecords records;
	RecordServices recordServices;
	RobotSchemaRecordServices robotSchemas;
	RobotsManager manager;

	@Test
	public void givenOneRobotWithOneCriterionAndAnActionWhenExecutingThenExecuteActionOnRecords()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "record1").set(METADATA1, "A"));
		transaction.add(new TestRecord(zeSchema, "record2").set(METADATA1, "A"));
		transaction.add(new TestRecord(zeSchema, "record3").set(METADATA1, "B"));
		transaction.add(new TestRecord(anotherSchema, "record4").set(METADATA1, "A"));
		transaction.add(new TestRecord(anotherSchema, "record5").set(METADATA1, "B"));
		recordServices.execute(transaction);

		recordServices.add(newC3P0Robot()
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA1).isEqualTo("A"))
				.setAction(SET_METADATA1).setActionParameters(setMetadata1To("Z")));

		manager.startAllRobotsExecution();
		waitForBatchProcess();

		assertThatRecords(allRecords()).extractingMetadatas(IDENTIFIER, METADATA1).containsOnly(
				tuple("record1", "Z"),
				tuple("record2", "Z"),
				tuple("record3", "B"),
				tuple("record4", "A"),
				tuple("record5", "B")
		);

	}

	@Test
	public void givenARobotIsConfiguredWithAnUnregisteredActionWhenExecuteThenDoNothing()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "record1").set(METADATA1, "A"));
		transaction.add(new TestRecord(zeSchema, "record2").set(METADATA1, "A"));
		transaction.add(new TestRecord(zeSchema, "record3").set(METADATA1, "B"));
		recordServices.execute(transaction);

		recordServices.add(newC3P0Robot()
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA1).isEqualTo("A"))
				.setAction("anInvalidAction").setActionParameters(setMetadata1To("Z")));

		manager.startAllRobotsExecution();
		waitForBatchProcess();

		assertThatRecords(allRecords()).extractingMetadatas(IDENTIFIER, METADATA1).containsOnly(
				tuple("record1", "A"),
				tuple("record2", "A"),
				tuple("record3", "B")
		);

	}

	private TestRecord newRecord(SchemaShortcuts schema, String id) {
		TestRecord testRecord = new TestRecord(schema, id);
		testRecord.set(Schemas.TITLE, "title " + id);
		return testRecord;
	}

	@Test
	public void givenHierarchyOfRobotsWhenExecuteThenOnlyExecuteIfRecordIsResolvedByRobotsOfEveryLevels()
			throws Exception {

		Record record1, record2, record3, record4, record5, record6, record7, record8, record9, record10,
				record11, record12, record13, record14, record15, record16, record17, record18, record19, record20;

		Transaction transaction = new Transaction();
		record1 = transaction.add(newRecord(zeSchema, "record1").set(METADATA1, "1").set(METADATA2, "0").set(METADATA3, "0"));
		record2 = transaction.add(newRecord(zeSchema, "record2").set(METADATA1, "1").set(METADATA2, "0").set(METADATA3, "1"));
		record3 = transaction.add(newRecord(zeSchema, "record3").set(METADATA1, "1").set(METADATA2, "0").set(METADATA3, "2"));
		record4 = transaction.add(newRecord(zeSchema, "record4").set(METADATA1, "1").set(METADATA2, "1").set(METADATA3, "0"));
		record5 = transaction.add(newRecord(zeSchema, "record5").set(METADATA1, "1").set(METADATA2, "1").set(METADATA3, "1"));
		record6 = transaction.add(newRecord(zeSchema, "record6").set(METADATA1, "1").set(METADATA2, "1").set(METADATA3, "2"));
		record7 = transaction.add(newRecord(zeSchema, "record7").set(METADATA1, "1").set(METADATA2, "2").set(METADATA3, "0"));
		record8 = transaction.add(newRecord(zeSchema, "record8").set(METADATA1, "1").set(METADATA2, "2").set(METADATA3, "1"));
		record9 = transaction.add(newRecord(zeSchema, "record9").set(METADATA1, "1").set(METADATA2, "2").set(METADATA3, "2"));
		record10 = transaction
				.add(newRecord(anotherSchema, "record10").set(METADATA1, "1").set(METADATA2, "2").set(METADATA3, "2"));

		record11 = transaction.add(newRecord(zeSchema, "record11").set(METADATA1, "2").set(METADATA2, "0").set(METADATA3, "0"));
		record12 = transaction.add(newRecord(zeSchema, "record12").set(METADATA1, "2").set(METADATA2, "0").set(METADATA3, "1"));
		record13 = transaction.add(newRecord(zeSchema, "record13").set(METADATA1, "2").set(METADATA2, "0").set(METADATA3, "2"));
		record14 = transaction.add(newRecord(zeSchema, "record14").set(METADATA1, "2").set(METADATA2, "1").set(METADATA3, "0"));
		record15 = transaction.add(newRecord(zeSchema, "record15").set(METADATA1, "2").set(METADATA2, "1").set(METADATA3, "1"));
		record16 = transaction.add(newRecord(zeSchema, "record16").set(METADATA1, "2").set(METADATA2, "1").set(METADATA3, "2"));
		record17 = transaction.add(newRecord(zeSchema, "record17").set(METADATA1, "2").set(METADATA2, "2").set(METADATA3, "0"));
		record18 = transaction.add(newRecord(zeSchema, "record18").set(METADATA1, "2").set(METADATA2, "2").set(METADATA3, "1"));
		record19 = transaction.add(newRecord(zeSchema, "record19").set(METADATA1, "2").set(METADATA2, "2").set(METADATA3, "2"));
		record20 = transaction
				.add(newRecord(anotherSchema, "record20").set(METADATA1, "2").set(METADATA2, "2").set(METADATA3, "2"));

		String zeType = zeSchema.typeCode();
		Robot robot100 = transaction.add(newRobot("100"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA1).isEqualTo("1"))
				.setAction(SET_METADATA4).setActionParameters(setMetadata4To("100"));

		Robot robot110 = transaction.add(newRobot("110"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false).setParent(robot100)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA2).isEqualTo("1"))
				.setAction(SET_METADATA5).setActionParameters(setMetadata5To("110"));

		Robot robot111 = transaction.add(newRobot("111"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false).setParent(robot110)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA3).isEqualTo("1"))
				.setAction(SET_METADATA6).setActionParameters(setMetadata6To("111"));

		Robot robot112 = transaction.add(newRobot("112"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false).setParent(robot110)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA3).isEqualTo("2"))
				.setAction(SET_METADATA6).setActionParameters(setMetadata6To("112"));

		Robot robot120 = transaction.add(newRobot("120"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false).setParent(robot100)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA2).isEqualTo("2"))
				.setAction(SET_METADATA5).setActionParameters(setMetadata5To("120"));

		Robot robot121 = transaction.add(newRobot("121"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false).setParent(robot120)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA3).isEqualTo("1"))
				.setAction(SET_METADATA6).setActionParameters(setMetadata6To("121"));

		Robot robot122 = transaction.add(newRobot("122"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(false).setParent(robot120)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA3).isEqualTo("2"))
				.setAction(SET_METADATA6).setActionParameters(setMetadata6To("122"));

		Robot robot200 = transaction.add(newRobot("200"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(true)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA1).isEqualTo("2"))
				.setAction(SET_METADATA4).setActionParameters(setMetadata4To("200"));

		Robot robot210 = transaction.add(newRobot("210"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(true).setParent(robot200)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA2).isEqualTo("1"))
				.setAction(SET_METADATA5).setActionParameters(setMetadata5To("210"));

		Robot robot211 = transaction.add(newRobot("211"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(true).setParent(robot210)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA3).isEqualTo("1"))
				.setAction(SET_METADATA6).setActionParameters(setMetadata6To("211"));

		Robot robot212 = transaction.add(newRobot("212"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(true).setParent(robot210)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA3).isEqualTo("2"))
				.setAction(SET_METADATA6).setActionParameters(setMetadata6To("212"));

		Robot robot220 = transaction.add(newRobot("220"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(true).setParent(robot200)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA2).isEqualTo("2"))
				.setAction(SET_METADATA5).setActionParameters(setMetadata5To("220"));

		Robot robot221 = transaction.add(newRobot("221"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(true).setParent(robot220)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA3).isEqualTo("1"))
				.setAction(SET_METADATA6).setActionParameters(setMetadata6To("221"));

		Robot robot222 = transaction.add(newRobot("222"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(true).setParent(robot220)
				.setSearchCriterion(fromType(zeSchema.typeCode()).where(METADATA3).isEqualTo("2"))
				.setAction(SET_METADATA6).setActionParameters(setMetadata6To("222"));

		Robot robotTwoPieces = transaction.add(newRobot("twoPieces"))
				.setSchemaFilter(zeSchema.typeCode()).setExcludeProcessedByChildren(true).setParent(robot220)
				.setSearchCriteria(Arrays.asList(
						fromType(zeType).where(METADATA1).isEqualTo("2").withLeftParens().booleanOperator(AND).build(),
						fromType(zeType).where(METADATA2).isEqualTo("2").booleanOperator(AND).build(),
						fromType(zeType).where(METADATA3).isEqualTo("2").withRightParens().booleanOperator(OR).build(),
						fromType(zeType).where(METADATA3).isEqualTo("3").build()))
				.setAction(SET_METADATA4).setActionParameters(setMetadata4To("Deux morceaux de robots"));

		recordServices.execute(transaction);

		//Do A Dry run (no modifications)

		assertThat(manager.dryRun(robot111)).isEqualTo(asList(
				dryRunRobotAction(record5, robot111, robotSchemas)
		));

		assertThat(manager.dryRun(robot111).get(0).getRecordId()).isEqualTo(record5.getId());
		assertThat(manager.dryRun(robot111).get(0).getRecordUrl()).isNull();
		assertThat(manager.dryRun(robot111).get(0).getRecordTitle()).isEqualTo(record5.get(Schemas.TITLE));
		assertThat(manager.dryRun(robot111).get(0).getRobotId()).isEqualTo(robot111.getId());
		assertThat(manager.dryRun(robot111).get(0).getRobotCode()).isEqualTo(robot111.getCode());
		assertThat(manager.dryRun(robot111).get(0).getRobotTitle()).isEqualTo(robot111.getTitle());
		assertThat(manager.dryRun(robot111).get(0).getRobotHierarchy())
				.isEqualTo(robotSchemas.getRobotCodesPath(robot111, " > "));
		assertThat(manager.dryRun(robot111).get(0).getActionTitle()).isEqualTo("setMetadata6");
		Map<Metadata, Object> parameters = manager.dryRun(robot111).get(0).getActionParameters();
		assertThat(parameters.size()).isEqualTo(1);
		assertThat(parameters.values()).containsOnly("111");
		assertThat(parameters.keySet()).extracting("localCode").containsOnly("value");

		assertThat(manager.dryRun(robot110)).isEqualTo(asList(
				dryRunRobotAction(record5, robot111, robotSchemas),
				dryRunRobotAction(record6, robot112, robotSchemas),
				dryRunRobotAction(record4, robot110, robotSchemas),
				dryRunRobotAction(record5, robot110, robotSchemas),
				dryRunRobotAction(record6, robot110, robotSchemas)
		));

		assertThat(manager.dryRun(robot100)).extracting("recordId", "robotCode").isEqualTo(asList(
				tuple("record5", "111"),
				tuple("record6", "112"),
				tuple("record4", "110"),
				tuple("record5", "110"),
				tuple("record6", "110"),

				tuple("record8", "121"),
				tuple("record9", "122"),
				tuple("record7", "120"),
				tuple("record8", "120"),
				tuple("record9", "120"),

				tuple("record1", "100"),
				tuple("record2", "100"),
				tuple("record3", "100"),
				tuple("record4", "100"),
				tuple("record5", "100"),
				tuple("record6", "100"),
				tuple("record7", "100"),
				tuple("record8", "100"),
				tuple("record9", "100")

		));

		assertThat(manager.dryRun(robot100)).isEqualTo(asList(
				dryRunRobotAction(record5, robot111, robotSchemas),
				dryRunRobotAction(record6, robot112, robotSchemas),
				dryRunRobotAction(record4, robot110, robotSchemas),
				dryRunRobotAction(record5, robot110, robotSchemas),
				dryRunRobotAction(record6, robot110, robotSchemas),

				dryRunRobotAction(record8, robot121, robotSchemas),
				dryRunRobotAction(record9, robot122, robotSchemas),
				dryRunRobotAction(record7, robot120, robotSchemas),
				dryRunRobotAction(record8, robot120, robotSchemas),
				dryRunRobotAction(record9, robot120, robotSchemas),

				dryRunRobotAction(record1, robot100, robotSchemas),
				dryRunRobotAction(record2, robot100, robotSchemas),
				dryRunRobotAction(record3, robot100, robotSchemas),
				dryRunRobotAction(record4, robot100, robotSchemas),
				dryRunRobotAction(record5, robot100, robotSchemas),
				dryRunRobotAction(record6, robot100, robotSchemas),
				dryRunRobotAction(record7, robot100, robotSchemas),
				dryRunRobotAction(record8, robot100, robotSchemas),
				dryRunRobotAction(record9, robot100, robotSchemas)

		));

		assertThat(manager.dryRun(robot211)).isEqualTo(asList(
				dryRunRobotAction(record15, robot211, robotSchemas)
		));

		assertThat(manager.dryRun(robot210)).isEqualTo(asList(
				dryRunRobotAction(record15, robot211, robotSchemas),
				dryRunRobotAction(record16, robot212, robotSchemas),
				dryRunRobotAction(record14, robot210, robotSchemas)
		));

		assertThatRecords(allRecords())
				.extractingMetadatas(IDENTIFIER, METADATA1, METADATA2, METADATA3, METADATA4, METADATA5, METADATA6).containsOnly(
				tuple("record1", "1", "0", "0", null, null, null),
				tuple("record2", "1", "0", "1", null, null, null),
				tuple("record3", "1", "0", "2", null, null, null),
				tuple("record4", "1", "1", "0", null, null, null),
				tuple("record5", "1", "1", "1", null, null, null),
				tuple("record6", "1", "1", "2", null, null, null),
				tuple("record7", "1", "2", "0", null, null, null),
				tuple("record8", "1", "2", "1", null, null, null),
				tuple("record9", "1", "2", "2", null, null, null),
				tuple("record10", "1", "2", "2", null, null, null),

				tuple("record11", "2", "0", "0", null, null, null),
				tuple("record12", "2", "0", "1", null, null, null),
				tuple("record13", "2", "0", "2", null, null, null),
				tuple("record14", "2", "1", "0", null, null, null),
				tuple("record15", "2", "1", "1", null, null, null),
				tuple("record16", "2", "1", "2", null, null, null),
				tuple("record17", "2", "2", "0", null, null, null),
				tuple("record18", "2", "2", "1", null, null, null),
				tuple("record19", "2", "2", "2", null, null, null),
				tuple("record20", "2", "2", "2", null, null, null)
		);

		manager.startAllRobotsExecution();
		waitForBatchProcess();

		assertThatRecords(allRecords())
				.extractingMetadatas(IDENTIFIER, METADATA1, METADATA2, METADATA3, METADATA4, METADATA5, METADATA6).containsOnly(
				tuple("record1", "1", "0", "0", "100", null, null),
				tuple("record2", "1", "0", "1", "100", null, null),
				tuple("record3", "1", "0", "2", "100", null, null),
				tuple("record4", "1", "1", "0", "100", "110", null),
				tuple("record5", "1", "1", "1", "100", "110", "111"),
				tuple("record6", "1", "1", "2", "100", "110", "112"),
				tuple("record7", "1", "2", "0", "100", "120", null),
				tuple("record8", "1", "2", "1", "100", "120", "121"),
				tuple("record9", "1", "2", "2", "100", "120", "122"),
				tuple("record10", "1", "2", "2", null, null, null),

				tuple("record11", "2", "0", "0", "200", null, null),
				tuple("record12", "2", "0", "1", "200", null, null),
				tuple("record13", "2", "0", "2", "200", null, null),
				tuple("record14", "2", "1", "0", null, "210", null),
				tuple("record15", "2", "1", "1", null, null, "211"),
				tuple("record16", "2", "1", "2", null, null, "212"),
				tuple("record17", "2", "2", "0", null, "220", null),
				tuple("record18", "2", "2", "1", null, null, "221"),
				tuple("record19", "2", "2", "2", "Deux morceaux de robots", null, "222"),
				tuple("record20", "2", "2", "2", null, null, null)
		);
	}

	@Test
	public void whenGetListOfActionsAndSupportedTypesThenGoodValues()
			throws Exception {
		assertThat(manager.getSupportedSchemaTypes()).containsOnly("zeType", "anotherType");
		assertThat(manager.getRegisteredActionsFor("zeType")).extracting("code")
				.containsOnly(SET_METADATA1, SET_METADATA2, SET_METADATA3, SET_METADATA4, SET_METADATA5, SET_METADATA6,
						RunExtractorsActionExecutor.ID);
		assertThat(manager.getRegisteredActionsFor("anotherType")).extracting("code")
				.containsOnly(SET_METADATA1, SET_METADATA2, RunExtractorsActionExecutor.ID);
	}

	private List<Record> allRecords() {
		return getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(asList(zeSchema.type(), anotherSchema.type())).returnAll()));
	}

	// ---------------------------------------------------------

	private CriterionBuilder fromType(String type) {
		return new CriterionBuilder(type);
	}

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withAllTestUsers().withRobotsModule());
		defineSchemasManager().using(schemas);

		recordServices = getModelLayerFactory().newRecordServices();
		robotSchemas = new RobotSchemaRecordServices(zeCollection, getAppLayerFactory());
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				setupSchemas(types);
			}
		});
		manager = robotSchemas.getRobotsManager();

		manager.registerAction(SET_METADATA1, SET_METADATA1_PARAMETERS_SCHEMA, asList("zeType", "anotherType"),
				setMetadataExecutor(METADATA1));
		manager.registerAction(SET_METADATA2, SET_METADATA2_PARAMETERS_SCHEMA, asList("zeType", "anotherType"),
				setMetadataExecutor(METADATA2));
		manager.registerAction(SET_METADATA3, SET_METADATA3_PARAMETERS_SCHEMA, asList("zeType"), setMetadataExecutor(METADATA3));
		manager.registerAction(SET_METADATA4, SET_METADATA4_PARAMETERS_SCHEMA, asList("zeType"), setMetadataExecutor(METADATA4));
		manager.registerAction(SET_METADATA5, SET_METADATA5_PARAMETERS_SCHEMA, asList("zeType"), setMetadataExecutor(METADATA5));
		manager.registerAction(SET_METADATA6, SET_METADATA6_PARAMETERS_SCHEMA, asList("zeType"), setMetadataExecutor(METADATA6));
	}

	private ActionExecutor setMetadataExecutor(final Metadata metadata) {
		return new ActionExecutor() {
			@Override
			public Transaction execute(String robotId, ActionParameters actionParameters,
									   AppLayerFactory appLayerFactory,
									   List<Record> records, List<Record> processedRecords, boolean dryRun) {
				Transaction transaction = new Transaction();
				for (Record record : records) {
					transaction.add(record.set(metadata, actionParameters.get("value")));
					processedRecords.add(record);
				}
				return transaction;
			}
		};
	}

	private void setupSchemas(MetadataSchemaTypesBuilder types) {
		MetadataSchemaBuilder setMetadata1Parameters = types.getSchemaType(ActionParameters.SCHEMA_TYPE)
				.createCustomSchema(SET_METADATA1_PARAMETERS_SCHEMA);
		setMetadata1Parameters.create("value").setType(MetadataValueType.STRING);

		MetadataSchemaBuilder setMetadata2Parameters = types.getSchemaType(ActionParameters.SCHEMA_TYPE)
				.createCustomSchema(SET_METADATA2_PARAMETERS_SCHEMA);
		setMetadata2Parameters.create("value").setType(MetadataValueType.STRING);

		MetadataSchemaBuilder setMetadata3Parameters = types.getSchemaType(ActionParameters.SCHEMA_TYPE)
				.createCustomSchema(SET_METADATA3_PARAMETERS_SCHEMA);
		setMetadata3Parameters.create("value").setType(MetadataValueType.STRING);

		MetadataSchemaBuilder setMetadata4Parameters = types.getSchemaType(ActionParameters.SCHEMA_TYPE)
				.createCustomSchema(SET_METADATA4_PARAMETERS_SCHEMA);
		setMetadata4Parameters.create("value").setType(MetadataValueType.STRING);

		MetadataSchemaBuilder setMetadata5Parameters = types.getSchemaType(ActionParameters.SCHEMA_TYPE)
				.createCustomSchema(SET_METADATA5_PARAMETERS_SCHEMA);
		setMetadata5Parameters.create("value").setType(MetadataValueType.STRING);

		MetadataSchemaBuilder setMetadata6Parameters = types.getSchemaType(ActionParameters.SCHEMA_TYPE)
				.createCustomSchema(SET_METADATA6_PARAMETERS_SCHEMA);
		setMetadata6Parameters.create("value").setType(MetadataValueType.STRING);

		MetadataSchemaBuilder zeSchemaBuilder = types.getSchema(zeSchema.code());
		MetadataSchemaBuilder anotherSchemaBuilder = types.getSchema(anotherSchema.code());
		zeSchemaBuilder.create(METADATA1.getLocalCode()).setType(MetadataValueType.STRING);
		zeSchemaBuilder.create(METADATA2.getLocalCode()).setType(MetadataValueType.STRING);
		zeSchemaBuilder.create(METADATA3.getLocalCode()).setType(MetadataValueType.STRING);
		zeSchemaBuilder.create(METADATA4.getLocalCode()).setType(MetadataValueType.STRING);
		zeSchemaBuilder.create(METADATA5.getLocalCode()).setType(MetadataValueType.STRING);
		zeSchemaBuilder.create(METADATA6.getLocalCode()).setType(MetadataValueType.STRING);

		anotherSchemaBuilder.create(METADATA1.getLocalCode()).setType(MetadataValueType.STRING);
		anotherSchemaBuilder.create(METADATA2.getLocalCode()).setType(MetadataValueType.STRING);
		anotherSchemaBuilder.create(METADATA3.getLocalCode()).setType(MetadataValueType.STRING);
	}

	private ActionParameters setMetadata1To(String value) {
		ActionParameters actionParameters = robotSchemas.newActionParameters(SET_METADATA1_PARAMETERS_SCHEMA);
		actionParameters.set("value", value);
		try {
			recordServices.add(actionParameters);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return actionParameters;
	}

	private ActionParameters setMetadata2To(String value) {
		ActionParameters actionParameters = robotSchemas.newActionParameters(SET_METADATA2_PARAMETERS_SCHEMA);
		actionParameters.set("value", value);
		try {
			recordServices.add(actionParameters);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return actionParameters;
	}

	private ActionParameters setMetadata3To(String value) {
		ActionParameters actionParameters = robotSchemas.newActionParameters(SET_METADATA3_PARAMETERS_SCHEMA);
		actionParameters.set("value", value);
		try {
			recordServices.add(actionParameters);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return actionParameters;
	}

	private ActionParameters setMetadata4To(String value) {
		ActionParameters actionParameters = robotSchemas.newActionParameters(SET_METADATA4_PARAMETERS_SCHEMA);
		actionParameters.set("value", value);
		try {
			recordServices.add(actionParameters);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return actionParameters;
	}

	private ActionParameters setMetadata5To(String value) {
		ActionParameters actionParameters = robotSchemas.newActionParameters(SET_METADATA5_PARAMETERS_SCHEMA);
		actionParameters.set("value", value);
		try {
			recordServices.add(actionParameters);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return actionParameters;
	}

	private ActionParameters setMetadata6To(String value) {
		ActionParameters actionParameters = robotSchemas.newActionParameters(SET_METADATA6_PARAMETERS_SCHEMA);
		actionParameters.set("value", value);
		try {
			recordServices.add(actionParameters);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return actionParameters;
	}

	private Robot newRobot(String code) {
		return robotSchemas.newRobotWithId("robot" + code + "id").setCode(code).setTitle(code);
	}

	private Robot newC3P0Robot() {
		return robotSchemas.newRobotWithId("c3p0").setCode("C-3PO").setTitle("C-3PO");
	}

	private Robot newR2D2Robot() {
		return robotSchemas.newRobotWithId("r2d2").setCode("R2-D2").setTitle("R2-D2");
	}

	private Robot newROBRobot() {
		return robotSchemas.newRobotWithId("rob").setCode("ROB").setTitle("ROB");
	}

	private Robot newRoombaRobot() {
		return robotSchemas.newRobotWithId("roomba").setCode("ROOMBA").setTitle("Roomba");
	}

	private Robot newDroidekasRobot() {
		return robotSchemas.newRobotWithId("droidekas").setCode("DROIDEKAS").setTitle("Droidekas");
	}

	private Robot newOptimusPrimeRobot() {
		return robotSchemas.newRobotWithId("optimusprime").setCode("OPTIMUS").setTitle("Optimus Prime");
	}

	private Robot newTerminatorRobot() {
		return robotSchemas.newRobotWithId("terminator").setCode("TERMINATOR").setTitle("Ze Terminator");
	}

	private Robot newRoboCopRobot() {
		return robotSchemas.newRobotWithId("robocop").setCode("ROBOCOP").setTitle("RoboCop");
	}
}
