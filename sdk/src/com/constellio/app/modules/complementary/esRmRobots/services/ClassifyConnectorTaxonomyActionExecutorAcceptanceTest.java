package com.constellio.app.modules.complementary.esRmRobots.services;

import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderDirectlyInThePlanActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderInParentFolderActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.actions.ClassifyConnectorFolderInTaxonomyActionExecutor;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderDirectlyInThePlanActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInParentFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.services.ClassifyConnectorHelper.ClassifiedRecordPathInfo;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmbRuntimeException.ConnectorSmbRuntimeException_CannotDownloadSmbDocument;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorInstanciator;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.wrappers.RobotLog;
import com.constellio.app.modules.robots.services.RobotSchemaRecordServices;
import com.constellio.app.ui.pages.search.criteria.CriterionBuilder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordValidatorParams;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.complementary.esRmRobots.model.enums.ActionAfterClassification.*;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.ADMINISTRATIVE_UNITS;
import static com.constellio.app.modules.rm.constants.RMTaxonomies.CLASSIFICATION_PLAN;
import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;
import static com.constellio.model.entities.schemas.Schemas.LEGACY_ID;
import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ClassifyConnectorTaxonomyActionExecutorAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;

	private ConnectorInstance<?> connectorInstance;
	private ConnectorInstance<?> anotherConnectorInstance;
	private ConnectorManager connectorManager;
	private ESSchemasRecordsServices es;
	private RobotSchemaRecordServices robotsSchemas;

	private String robotId = "zeTerminator";

	private String share, domain, username, password;

    private String adminUnit1 = "adminUnit1";
    private String adminUnit11 = "adminUnit11";
    private String adminUnit2 = "adminUnit2";
    private String adminUnit21 = "adminUnit21";
    private String adminUnit22 = "adminUnit22";
    private String folderA = "folderA";
    private String folderB = "folderB";
    private String folderAA = "folderAA";
    private String folderAAA = "folderAAA";
    private String folderAAB = "folderAAB";
    private String folderABA = "folderABA";
    private String folderAB = "folderAB";
    private String folderC = "folderC";
    private String folderCC = "folderCC";
    private String folderD = "folderD";
    private String folderDD = "folderDD";
    private String folderE = "folderE";

	private String documentA1 = "documentA1";
	private String documentA2 = "documentA2";
	private String documentB3 = "documentB3";
	private String documentB7JustDeleted = "documentB7JustDeleted";
	private String documentAA4 = "documentAA4";
	private String documentAA5 = "documentAA5";
	private String documentAAA6 = "documentAAA6";
	private String documentAAB7 = "documentAAB7";
	private String documentAB8 = "documentAB8";
	private String documentABA9 = "documentABA9";

	private String documentMalPlace = "documentC1";
	private String documentBienPlace = "documentC2";

	private String documentA1TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/1.txt";

	private String documentA2TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/2.txt";
	private String documentB3TaxoURL = "smb://AU2 Admin Unit2/AU21 Admin Unit21/B/3.txt";
	private String documentAA4TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/4.txt";
	private String documentAA5TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/5.txt";
	private String documentAAA6TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAA/6.txt";
	private String documentAAB7TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAB/7.txt";
	private String documentB7JustDeletedTaxoURL = "smb://AU2 Admin Unit2/AU21 Admin Unit21/B/7.txt";
	private String documentAB8TaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AB/8.txt";

	//TODO Support major/minor
	//TODO Fix problem when running a test directly

	@Spy
	ConnectorSmb connectorSmb;
	private String folderATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/";

	private String folderAATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/";
	private String folderABTaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AB/";
	private String folderAAATaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAA/";
	private String folderAABTaxoURL = "smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAB/";
	private String folderBTaxoURL = "smb://AU2 Admin Unit2/AU21 Admin Unit21/B/";

	private String folderCTaxoURL = "smb://X/X13/C/";
	private String folderCCTaxoURL = "smb://X/X13/C/CC/";
	private String folderDTaxoURL = "smb://X/X13/D/";
	private String folderETaxoURL = "smb://TEST/ALLO/OUI";
	private String folderDDTaxoURL = "smb://X13/X/C/";
	private String documentMalPlaceTaxoURL = "smb://X/X13/test.txt";
	private String documentBienPlaceTaxoURL = "smb://X/X13/C/test.txt";

	private String folderANoTaxoURL = "smb://host.ext/section/A/";
	private String folderAANoTaxoURL = "smb://host.ext/section/A/AA/";
	private String folderABNoTaxoURL = "smb://host.ext/section/A/AB/";
	private String folderAAANoTaxoURL = "smb://host.ext/section/A/AA/AAA/";
	private String folderAABNoTaxoURL = "smb://host.ext/section/A/AA/AAB/";
	private String folderBNoTaxoURL = "smb://host.ext/section/B/";
	private String documentA1NoTaxoURL = "smb://host.ext/section/A/1.txt";
	private String documentA2NoTaxoURL = "smb://host.ext/section/A/2.txt";
	private String documentB3NoTaxoURL = "smb://host.ext/section/B/3.txt";
	private String documentAA4NoTaxoURL = "smb://host.ext/section/A/AA/4.txt";
	private String documentAA5NoTaxoURL = "smb://host.ext/section/A/AA/5.txt";
	private String documentAAA6NoTaxoURL = "smb://host.ext/section/AAA/6.txt";

	private ContentManager contentManager;

	LocalDate squatreNovembre2010 = new LocalDate(2010, 11, 4), squatreNovembre = new LocalDate(2010, 11, 4);
	LocalDate squatreNovembre2015 = new LocalDate(2015, 11, 4);

	LocalDateTime timeOfMyLife = new LocalDateTime(2017, 3, 9, 10, 18, 30);

	@Before
	public void setUp()
			throws Exception {

		givenRollbackCheckDisabled();
		givenDisabledAfterTestValidations();
		givenHashingEncodingIs(BASE64_URL_ENCODED);
		notAUnitItest = true;
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withRobotsModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		contentManager = getModelLayerFactory().getContentManager();

		inCollection(zeCollection).giveReadAccessTo(gandalf);
		Users users = new Users().setUp(getModelLayerFactory().newUserServices());

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		connectorManager = es.getConnectorManager();

		share = "zeShare";//SDKPasswords.testSmbShare();
		domain = "zeDomain";//SDKPasswords.testSmbDomain();
		username = "zeUsername";//SDKPasswords.testSmbUsername();
		password = "zePassword";//SDKPasswords.testSmbPassword();

		recordServices.update(users.bobIn(zeCollection).setManualTokens("rtoken1"));
		recordServices.update(users.chuckNorrisIn(zeCollection).setManualTokens("rtoken1", "rtoken2"));

		es.getConnectorManager().setConnectorInstanciator(new ConnectorInstanciator() {
			@Override
			public Connector instanciate(ConnectorInstance connectorInstance) {
				return connectorSmb;
			}
		});

		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				ConnectorSmbDocument connectorSmbDocument = (ConnectorSmbDocument) invocation.getArguments()[0];
				String resourceName = (String) invocation.getArguments()[1];
				String name = connectorSmbDocument.getTitle();
				try {
					File file = getTestResourceFile(name);

					return getIOLayerFactory().newIOServices().newFileInputStream(file, resourceName);
				} catch (RuntimeException e) {
					throw new ConnectorSmbRuntimeException_CannotDownloadSmbDocument(connectorSmbDocument, e);
				}
			}
		}).when(connectorSmb).getInputStream(any(ConnectorSmbDocument.class), anyString());

		connectorInstance = connectorManager.createConnector(es.newConnectorSmbInstance()
				.setTitle("zeConnector")
				.setCode("zeConnector")
				.setEnabled(false)
				.setSeeds(asList(share)).setUsername(username).setPassword(password).setDomain(domain)
				.setTraversalCode("zeTraversal"));

		anotherConnectorInstance = connectorManager
				.createConnector(es.newConnectorSmbInstance()
						.setTitle("anotherConnector")
						.setCode("anotherConnector")
						.setEnabled(false)
						.setSeeds(asList(share)).setUsername(username).setPassword(password).setDomain(domain)
						.setTraversalCode("anotherConnectorTraversal"));

		es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		robotsSchemas = new RobotSchemaRecordServices(zeCollection, getAppLayerFactory());
		notAUnitItest = false;

	}

	private void withACustomRequiredMetadataInSchemas() {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("aCustomRequiredMetadata").setType(MetadataValueType.STRING)
						.setDefaultRequirement(
								true);
				types.getSchema(Document.DEFAULT_SCHEMA).create("aCustomRequiredMetadata").setType(MetadataValueType.STRING)
						.setDefaultRequirement(true);
			}
		});
	}

	@Test
	public void whenGetPathPartsThenOnlyNonConceptParts()
			throws Exception {
		givenFetchedTaxonomyWithValidFoldersButNoDocuments();

		assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/").isNull();

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/Dossier A/")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(unitRecord("AU11"), "Dossier A"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(unitRecord("AU11"), "A"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AB/")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(null, "AB"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AB/8.txt")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(null, "8.txt"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/Dossier A/")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(unitRecord("AU11"), "Dossier A"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/Dossier A/Sous-dossier AB/")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(null, "Sous-dossier AB"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/Dossier A/Sous-dossier AB/6.txt")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(null, "6.txt"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/AU11/")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(unitRecord("AU11"), "AU11"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/AU11 Mon dossier/")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(unitRecord("AU11"), "AU11 Mon dossier"));

        assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/AU11 Mon dossier/8.txt")
                .isEqualToComparingFieldByField(new ClassifiedRecordPathInfo(null, "8.txt"));

        //		assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/").isEqualTo("A");
        //		assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAB").isEqualTo("AAB");
        //		assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAB/").isEqualTo("AAB");
        //		assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AA/AAB/7.txt").isEqualTo("7.txt");
        //
        //		assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/1.txt").isEqualTo("1.txt");
        //
        //		assertThatPathPartsForClassification("smb://AU1 Admin Unit1/AU11 Admin Unit11/A/AU1 test/1.txt").isEqualTo("1.txt");
    }

    private Record unitRecord(String code) {
        return rm.getAdministrativeUnitWithCode(code).getWrappedRecord();
    }

    private ObjectAssert<ClassifiedRecordPathInfo> assertThatPathPartsForClassification(String url) {
        String metadataCode = AdministrativeUnit.DEFAULT_SCHEMA + "_" + Schemas.CODE.getLocalCode();
        Metadata codeMetadata = rm.getTypes().getMetadata(metadataCode);

        ClassifyConnectorHelper helper = new ClassifyConnectorHelper(getModelLayerFactory().newRecordServices());
        return assertThat(helper.extractInfoFromPath(url, "smb://", " ", codeMetadata));
    }

    @Test
    public void givenFolderMappingWithoutErrorsOrMissingValuesThenAllFieldsMapped()
            throws Exception {
        notAUnitItest = true;

        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setFolderMapping(folderMapping).setDefaultCategory(records.categoryId_X)
                .setDefaultAdminUnit(adminUnit22));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        recordServices.logicallyDelete(record(adminUnit21), users.adminIn(zeCollection));
        recordServices.physicallyDelete(record(adminUnit21), users.adminIn(zeCollection));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderATaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getTitle()).isEqualTo("Le dossier A");
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderA.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderA.getKeywords()).containsOnly("mot1", "mot2", "mot3");
		assertThat(folderA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderA.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAATaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getTitle()).isEqualTo("Le dossier AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAA.getKeywords()).containsOnly("mot1", "mot2");
		assertThat(folderAA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAA.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderAA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAB = getFolderByLegacyId(folderABTaxoURL);
		assertThat(folderAB.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAB.getTitle()).isEqualTo("Le dossier AB");
		assertThat(folderAB.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAB.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAB.getKeywords()).containsOnly("mot1", "mot2");
		assertThat(folderAB.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAB.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderAB.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAA = getFolderByLegacyId(folderAAATaxoURL);
		assertThat(folderAAA.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAA.getTitle()).isEqualTo("Le dossier AAA");
		assertThat(folderAAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAA.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAAA.getKeywords()).containsOnly("mot1", "mot2");
		assertThat(folderAAA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAAA.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderAAA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAB = getFolderByLegacyId(folderAABTaxoURL);
		assertThat(folderAAB.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAB.getTitle()).isEqualTo("Le dossier AAB");
		assertThat(folderAAB.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAB.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAAB.getKeywords()).containsOnly("mot1", "mot2");
		assertThat(folderAAB.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAAB.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderAAB.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderB = getFolderByLegacyId(folderBTaxoURL);
		assertThat(folderB.getParentFolder()).isNull();
		assertThat(folderB.getTitle()).isEqualTo("Le dossier B");
		assertThat(folderB.getRetentionRule()).isEqualTo(records.ruleId_2);
		assertThat(folderB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderB.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-12-14");
		assertThat(folderB.getKeywords()).containsOnly("mot2", "mot4");
		assertThat(folderB.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderB.getAdministrativeUnit()).isEqualTo(adminUnit22);
		assertThat(folderB.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void whenClassifyingConnectorFoldersUsingClassificationPlanThenOK()
            throws Exception {
        notAUnitItest = true;

        folderATaxoURL = "smb://X/X13/A/";
        folderAATaxoURL = "smb://X/X13/A/AA/";
        folderABTaxoURL = "smb://X/X13/A/AB/";
        folderAAATaxoURL = "smb://X/X100/C";
        folderAABTaxoURL = "smb://X/X100/D";
        folderBTaxoURL = "smb://Z/Ze42/B/";

        givenFetchedTaxonomyWithValidFoldersButNoDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(CLASSIFICATION_PLAN).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setDefaultCategory(records.categoryId_Z111).setDefaultRetentionRule(records.ruleId_1)
                .setDefaultCopyStatus(CopyType.PRINCIPAL).setDefaultAdminUnit(adminUnit11).setDefaultOpenDate(new LocalDate()));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        //Deleting a category, this will cause the folderBTaxoURL to be placed in default rubric
        recordServices.logicallyDelete(records.getCategory_ZE42().getWrappedRecord(), users.adminIn(zeCollection));
        recordServices.physicallyDelete(records.getCategory_ZE42().getWrappedRecord(), users.adminIn(zeCollection));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		//folderATaxoURL = "smb://X/X13/A/";
		Folder folderA = getFolderByLegacyId(folderATaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getTitle()).isEqualTo("A");
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderA.getCategory()).isEqualTo(records.categoryId_X13);
		assertThat(folderA.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAATaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getTitle()).isEqualTo("AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getCategory()).isEqualTo(records.categoryId_X13);
		assertThat(folderAA.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderAA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAB = getFolderByLegacyId(folderABTaxoURL);
		assertThat(folderAB.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAB.getTitle()).isEqualTo("AB");
		assertThat(folderAB.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAB.getCategory()).isEqualTo(records.categoryId_X13);
		assertThat(folderAB.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderAB.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAA = getFolderByLegacyId(folderAAATaxoURL);
		assertThat(folderAAA.getParentFolder()).isNull();
		assertThat(folderAAA.getTitle()).isEqualTo("AAA");
		assertThat(folderAAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAA.getCategory()).isEqualTo(records.categoryId_X100);
		assertThat(folderAAA.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderAAA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAB = getFolderByLegacyId(folderAABTaxoURL);
		assertThat(folderAAA.getParentFolder()).isNull();
		assertThat(folderAAB.getTitle()).isEqualTo("AAB");
		assertThat(folderAAB.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAB.getCategory()).isEqualTo(records.categoryId_X100);
		assertThat(folderAAB.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderAAB.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        Folder folderB = getFolderByLegacyId(folderBTaxoURL);
        assertThat(folderB.getParentFolder()).isNull();
        assertThat(folderB.getTitle()).isEqualTo("B");
        assertThat(folderB.getRetentionRule()).isEqualTo(records.ruleId_1);
        assertThat(folderB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
        assertThat(folderB.getCategory()).isEqualTo(records.categoryId_Z111);
        assertThat(folderB.getAdministrativeUnit()).isEqualTo(adminUnit11);
        assertThat(folderB.getCreatedByRobot()).isEqualTo(robotId);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenClassifyingConnectorFoldersUsingClassificationPlanOtherCasesThenOk() throws Exception {
        notAUnitItest = true;
		givenDisabledAfterTestValidations();
        givenFetchedFoldersAndDocumentsForRobots();

        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(CLASSIFICATION_PLAN).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setDefaultCategory(records.categoryId_Z111).setDefaultRetentionRule(records.ruleId_1)
                .setDefaultCopyStatus(CopyType.PRINCIPAL).setDefaultAdminUnit(adminUnit11).setDefaultOpenDate(new LocalDate()));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		// Séparateurs nom
		Folder folderCarotte = getFolderByLegacyId(folderCTaxoURL);
        assertThat(folderCarotte.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderCarotte.getParentFolder()).isNull();
		assertThat(folderCarotte.getTitle()).isEqualTo("C - Carotte");
		assertThat(folderCarotte.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderCarotte.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderCarotte.getCategory()).isEqualTo(records.categoryId_X13);
		assertThat(folderCarotte.getAdministrativeUnit()).isEqualTo(adminUnit11);
		assertThat(folderCarotte.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderCarotte.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		// Sous-dossier
		Folder folderCC = getFolderByLegacyId(folderCCTaxoURL);
		assertThat(folderCC.getParentFolder()).isEqualTo(folderCarotte.getId());
		assertThat(folderCC.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderCC.getTitle()).isEqualTo("C - Sous-dossier");
		assertThat(folderCC.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderCC.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderCC.getCategory()).isEqualTo(records.categoryId_X13);
		assertThat(folderCC.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderCC.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderD = getFolderByLegacyId(folderDTaxoURL);
		assertThat(folderD.getParentFolder()).isNull();
		assertThat(folderD.getTitle()).isEqualTo("D                                           Test");
		assertThat(folderD.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderD.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        // Code mal placé
        Folder folderDD = getFolderByLegacyId(folderDDTaxoURL);
        assertThat(folderDD.getParentFolder()).isNull();
        assertThat(folderDD.getTitle()).isEqualTo("DossierAvecCodeAlenvers");
        assertThat(folderDD.getCategory()).isEqualTo(records.categoryId_Z111);
        assertThat(folderDD.getRetentionRule()).isEqualTo(records.ruleId_1);
        assertThat(folderDD.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
        assertThat(folderDD.getCreatedByRobot()).isEqualTo(robotId);

        // Code inexistant
        Folder folderE = getFolderByLegacyId(folderETaxoURL);
        assertThat(folderE.getParentFolder()).isNull();
        assertThat(folderE.getTitle()).isEqualTo("DossierAvecFauxCodeClassification");
        assertThat(folderE.getCategory()).isEqualTo(records.categoryId_Z111);
        assertThat(folderE.getRetentionRule()).isEqualTo(records.ruleId_1);
        assertThat(folderE.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
        assertThat(folderE.getCreatedByRobot()).isEqualTo(robotId);

        // Document placé ailleurs
        List<Document> docs = rm.searchDocuments(where(LEGACY_ID).isEqualTo(documentMalPlaceTaxoURL));
        for (Document document : docs) {
            assertThat(document).isNull();
        }

        // Document bien placé dans le plan
        docs = rm.searchDocuments(where(LEGACY_ID).isEqualTo(documentBienPlaceTaxoURL));
        for (Document document : docs) {
            assertThat(document).isNotNull();
            assertThat(document.getFolder()).isEqualTo(rm.getFolderWithLegacyId(folderCTaxoURL).getId());
            assertThat(document.getRetentionRule()).isEqualTo(records.ruleId_1);
            assertThat(document.getTitle()).isEqualTo("1.txt");
            assertThat(document.getCreatedByRobot()).isEqualTo(robotId);
            assertThat(document.getFolderCategory()).isEqualTo(records.categoryId_X13);
        }

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenNoMappingNoTargetTaxonomyAdnInvalidHierarchyThenCreateRMFolderWithDefaultValues()
            throws Exception {
        notAUnitItest = true;
        givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath();
        ClassifyConnectorFolderDirectlyInThePlanActionParameters parameters = ClassifyConnectorFolderDirectlyInThePlanActionParameters
                .wrap(robotsSchemas
                        .newActionParameters(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setActionAfterClassification(DO_NOTHING)
                .setDefaultCategory(records.categoryId_X).setDefaultAdminUnit(
                        records.unitId_10).setDefaultCopyStatus(CopyType.PRINCIPAL).setDefaultRetentionRule(
                        records.ruleId_3).setDefaultOpenDate(squatreNovembre));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("/"))
                .setAction(ClassifyConnectorFolderDirectlyInThePlanActionExecutor.ID).setCode("terminator")
                .setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderANoTaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("A");
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAANoTaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAB = getFolderByLegacyId(folderABNoTaxoURL);
		assertThat(folderAB.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAB.getTitle()).isEqualTo("AB");
		assertThat(folderAB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAA = getFolderByLegacyId(folderAAANoTaxoURL);
		assertThat(folderAAA.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAA.getTitle()).isEqualTo("AAA");
		assertThat(folderAAA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAB = getFolderByLegacyId(folderAABNoTaxoURL);
		assertThat(folderAAB.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAB.getTitle()).isEqualTo("AAB");
		assertThat(folderAAB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderB = getFolderByLegacyId(folderBNoTaxoURL);
		assertThat(folderB.getParentFolder()).isNull();
		assertThat(folderB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderB.getTitle()).isEqualTo("B");
		assertThat(folderB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenFolderAlreadyClassifiedUsingDirectInThePlanActionWhenRerunRobotWithDifferentParametersThenUnchanged()
            throws Exception {
        notAUnitItest = true;
        givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath();
        ClassifyConnectorFolderDirectlyInThePlanActionParameters parameters = ClassifyConnectorFolderDirectlyInThePlanActionParameters
                .wrap(robotsSchemas
                        .newActionParameters(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setActionAfterClassification(DO_NOTHING)
                .setDefaultCategory(records.categoryId_X).setDefaultAdminUnit(
                        records.unitId_10).setDefaultCopyStatus(CopyType.PRINCIPAL).setDefaultRetentionRule(
                        records.ruleId_3).setDefaultOpenDate(squatreNovembre2010));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("/"))
                .setAction(ClassifyConnectorFolderDirectlyInThePlanActionExecutor.ID).setCode("terminator")
                .setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderANoTaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("A");
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderA.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(folderA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAANoTaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAA.getAdministrativeUnit()).isEqualTo(records.unitId_10);
		assertThat(folderAA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        recordServices.update(parameters.setActionAfterClassification(DO_NOTHING)
                .setDefaultCategory(records.categoryId_X13).setDefaultAdminUnit(
                        records.unitId_30).setDefaultCopyStatus(CopyType.SECONDARY).setDefaultRetentionRule(
                        records.ruleId_4).setDefaultOpenDate(squatreNovembre2015));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

        folderA = getFolderByLegacyId(folderANoTaxoURL);
        assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("A");
        assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_3);
        assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
        assertThat(folderA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
        assertThat(folderA.getCategory()).isEqualTo(records.categoryId_X);
        assertThat(folderA.getAdministrativeUnit()).isEqualTo(records.unitId_10);
        assertThat(folderA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		folderAA = getFolderByLegacyId(folderAANoTaxoURL);
        assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("AA");
        assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_3);
        assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
        assertThat(folderAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
        assertThat(folderAA.getCategory()).isEqualTo(records.categoryId_X);
        assertThat(folderAA.getAdministrativeUnit()).isEqualTo(records.unitId_10);
        assertThat(folderAA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);
    }

    @Test
    public void whenClassifyingFoldersDirectlyInThePlanMultipleTimeThenOverwrite()
            throws Exception {
        notAUnitItest = true;
        givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath();
        ClassifyConnectorFolderDirectlyInThePlanActionParameters parameters = ClassifyConnectorFolderDirectlyInThePlanActionParameters
                .wrap(robotsSchemas
                        .newActionParameters(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters
                .setActionAfterClassification(DO_NOTHING)
                .setDefaultCategory(records.categoryId_X)
                .setDefaultAdminUnit(records.unitId_10)
                .setDefaultUniformSubdivision(records.subdivId_1)
                .setDefaultCopyStatus(CopyType.PRINCIPAL)
                .setDefaultRetentionRule(records.ruleId_1)
                .setDefaultOpenDate(squatreNovembre));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("/"))
                .setAction(ClassifyConnectorFolderDirectlyInThePlanActionExecutor.ID).setCode("terminator")
                .setTitle("terminator"));

		//1- First execution
		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();
		assertThat(rm.searchFolders(where(LEGACY_ID).isNotNull())).extracting("legacyId", "title", Folder.UNIFORM_SUBDIVISION, Folder.FORM_MODIFIED_ON, Folder.FORM_CREATED_ON).containsOnly(
				tuple(folderANoTaxoURL, "A", records.subdivId_1, timeOfMyLife, timeOfMyLife),
				tuple(folderAANoTaxoURL, "AA", records.subdivId_1, timeOfMyLife, timeOfMyLife),
				tuple(folderABNoTaxoURL, "AB", records.subdivId_1, timeOfMyLife, timeOfMyLife),
				tuple(folderAAANoTaxoURL, "AAA", records.subdivId_1, timeOfMyLife, timeOfMyLife),
				tuple(folderAABNoTaxoURL, "AAB", records.subdivId_1, timeOfMyLife, timeOfMyLife),
				tuple(folderBNoTaxoURL, "B", records.subdivId_1, timeOfMyLife, timeOfMyLife)
		);

		assertThat(rm.searchDocuments(where(LEGACY_ID).isNotNull()))
				.extracting("title", "content.currentVersion.hash", "content.currentVersion.version", Document.FORM_MODIFIED_ON, Document.FORM_CREATED_ON)
				.containsOnly(
						tuple("1.txt", "F-roHxDf6G8Ks_bQjnaxc1fPjuw=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("2.txt", "B_Y1uv947wtmT6zR294q3eAkHOs=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("4.txt", "fRNOVjfA_c-w6xobmII_eIPU6s4=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("3.txt", "LhTJnquyaSPRtdZItiSx0UNkpcc=", "1.0", timeOfMyLife, timeOfMyLife)
				);

		//2- Start the robot again, nothing happens
		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();
		assertThat(rm.searchFolders(where(LEGACY_ID).isNotNull())).extracting("legacyId", "title", Folder.FORM_MODIFIED_ON, Folder.FORM_CREATED_ON).containsOnly(
				tuple(folderANoTaxoURL, "A", timeOfMyLife, timeOfMyLife),
				tuple(folderAANoTaxoURL, "AA", timeOfMyLife, timeOfMyLife),
				tuple(folderABNoTaxoURL, "AB", timeOfMyLife, timeOfMyLife),
				tuple(folderAAANoTaxoURL, "AAA", timeOfMyLife, timeOfMyLife),
				tuple(folderAABNoTaxoURL, "AAB", timeOfMyLife, timeOfMyLife),
				tuple(folderBNoTaxoURL, "B", timeOfMyLife, timeOfMyLife)
		);

		assertThat(rm.searchDocuments(where(LEGACY_ID).isNotNull()))
				.extracting("title", "content.currentVersion.hash", "content.currentVersion.version", Document.FORM_MODIFIED_ON, Document.FORM_CREATED_ON)
				.containsOnly(
						tuple("1.txt", "F-roHxDf6G8Ks_bQjnaxc1fPjuw=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("2.txt", "B_Y1uv947wtmT6zR294q3eAkHOs=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("3.txt", "LhTJnquyaSPRtdZItiSx0UNkpcc=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("4.txt", "fRNOVjfA_c-w6xobmII_eIPU6s4=", "1.0", timeOfMyLife, timeOfMyLife)
				);

		//2- Start the robot again, with a modified connector document
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation)
					throws Throwable {
				ConnectorSmbDocument connectorSmbDocument = (ConnectorSmbDocument) invocation.getArguments()[0];
				String resourceName = (String) invocation.getArguments()[1];
				String name = connectorSmbDocument.getTitle();
				try {
					//We simulate a modification of content by downloading the content of another file
					if ("1.txt".equals(name)) {
						name = "2.txt";
					}
					if ("3.txt".equals(name)) {
						name = "4.txt";
					}
					File file = getTestResourceFile(name);
					return getIOLayerFactory().newIOServices().newFileInputStream(file, resourceName);
				} catch (RuntimeException e) {
					throw new ConnectorSmbRuntimeException_CannotDownloadSmbDocument(connectorSmbDocument, e);
				}
			}
		}).when(connectorSmb).getInputStream(any(ConnectorSmbDocument.class), anyString());
		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();
		assertThat(rm.searchFolders(where(LEGACY_ID).isNotNull())).extracting("legacyId", "title", Folder.FORM_MODIFIED_ON, Folder.FORM_CREATED_ON).containsOnly(
				tuple(folderANoTaxoURL, "A", timeOfMyLife, timeOfMyLife),
				tuple(folderAANoTaxoURL, "AA", timeOfMyLife, timeOfMyLife),
				tuple(folderABNoTaxoURL, "AB", timeOfMyLife, timeOfMyLife),
				tuple(folderAAANoTaxoURL, "AAA", timeOfMyLife, timeOfMyLife),
				tuple(folderAABNoTaxoURL, "AAB", timeOfMyLife, timeOfMyLife),
				tuple(folderBNoTaxoURL, "B", timeOfMyLife, timeOfMyLife)
		);

		assertThat(rm.searchDocuments(where(LEGACY_ID).isNotNull()))
				.extracting("title", "content.currentVersion.hash", "content.currentVersion.version", Document.FORM_MODIFIED_ON, Document.FORM_CREATED_ON)
				.containsOnly(
						tuple("1.txt", "B_Y1uv947wtmT6zR294q3eAkHOs=", "2.0", timeOfMyLife, timeOfMyLife),
						tuple("2.txt", "B_Y1uv947wtmT6zR294q3eAkHOs=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("3.txt", "fRNOVjfA_c-w6xobmII_eIPU6s4=", "2.0", timeOfMyLife, timeOfMyLife),
						tuple("4.txt", "fRNOVjfA_c-w6xobmII_eIPU6s4=", "1.0", timeOfMyLife, timeOfMyLife)
				);
	}

    @Test
    public void whenClassifyingFoldersDirectlyInThePlanMultipleTimeThenReactivateLogicallyDeleted()
            throws Exception {
        notAUnitItest = true;
        givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath();
        ClassifyConnectorFolderDirectlyInThePlanActionParameters parameters = ClassifyConnectorFolderDirectlyInThePlanActionParameters
                .wrap(robotsSchemas
                        .newActionParameters(ClassifyConnectorFolderDirectlyInThePlanActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setActionAfterClassification(DO_NOTHING)
                .setDefaultCategory(records.categoryId_X)
                .setDefaultAdminUnit(records.unitId_10)
                .setDefaultCopyStatus(CopyType.PRINCIPAL)
                .setDefaultRetentionRule(records.ruleId_1)
                .setDefaultOpenDate(squatreNovembre));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("/"))
                .setAction(ClassifyConnectorFolderDirectlyInThePlanActionExecutor.ID).setCode("terminator")
                .setTitle("terminator"));

		//1- First execution
		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();
		assertThat(rm.searchFolders(where(LEGACY_ID).isNotNull().andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()))
				.extracting("legacyId", "title", Folder.FORM_MODIFIED_ON, Folder.FORM_CREATED_ON).containsOnly(
				tuple(folderANoTaxoURL, "A", timeOfMyLife, timeOfMyLife),
				tuple(folderAANoTaxoURL, "AA", timeOfMyLife, timeOfMyLife),
				tuple(folderABNoTaxoURL, "AB", timeOfMyLife, timeOfMyLife),
				tuple(folderAAANoTaxoURL, "AAA", timeOfMyLife, timeOfMyLife),
				tuple(folderAABNoTaxoURL, "AAB", timeOfMyLife, timeOfMyLife),
				tuple(folderBNoTaxoURL, "B", timeOfMyLife, timeOfMyLife)
		);

		assertThat(rm.searchDocuments(where(LEGACY_ID).isNotNull().andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()))
				.extracting("title", "content.currentVersion.hash", "content.currentVersion.version", Document.FORM_MODIFIED_ON, Document.FORM_CREATED_ON)
				.containsOnly(
						tuple("1.txt", "F-roHxDf6G8Ks_bQjnaxc1fPjuw=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("2.txt", "B_Y1uv947wtmT6zR294q3eAkHOs=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("3.txt", "LhTJnquyaSPRtdZItiSx0UNkpcc=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("4.txt", "fRNOVjfA_c-w6xobmII_eIPU6s4=", "1.0", timeOfMyLife, timeOfMyLife)
				);

		//2- Logically delete two folders and one document
		recordServices.logicallyDelete(rm.getFolderWithLegacyId(folderAANoTaxoURL).getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(rm.getFolderWithLegacyId(folderBNoTaxoURL).getWrappedRecord(), User.GOD);
		recordServices.logicallyDelete(rm.getDocumentByLegacyId(documentA1NoTaxoURL).getWrappedRecord(), User.GOD);
		assertThat(rm.searchFolders(where(LEGACY_ID).isNotNull().andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()))
				.extracting("legacyId", "title").containsOnly(
				tuple(folderANoTaxoURL, "A"),
				tuple(folderABNoTaxoURL, "AB")
		);
		assertThat(rm.searchDocuments(where(LEGACY_ID).isNotNull().andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()))
				.extracting("title", "content.currentVersion.hash", "content.currentVersion.version", Document.FORM_MODIFIED_ON, Document.FORM_CREATED_ON)
				.containsOnly(
						tuple("2.txt", "B_Y1uv947wtmT6zR294q3eAkHOs=", "1.0", timeOfMyLife, timeOfMyLife)
				);

		//3- Start the robot again, folders and documents are back alive
		robotsSchemas.getRobotsManager().startAllRobotsExecution();
		waitForBatchProcess();
		assertThat(rm.searchFolders(where(LEGACY_ID).isNotNull().andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()))
				.extracting("legacyId", "title", Folder.FORM_MODIFIED_ON, Folder.FORM_CREATED_ON).containsOnly(
				tuple(folderANoTaxoURL, "A", timeOfMyLife, timeOfMyLife),
				tuple(folderAANoTaxoURL, "AA", timeOfMyLife, timeOfMyLife),
				tuple(folderABNoTaxoURL, "AB", timeOfMyLife, timeOfMyLife),
				tuple(folderAAANoTaxoURL, "AAA", timeOfMyLife, timeOfMyLife),
				tuple(folderAABNoTaxoURL, "AAB", timeOfMyLife, timeOfMyLife),
				tuple(folderBNoTaxoURL, "B", timeOfMyLife, timeOfMyLife)
		);

		assertThat(rm.searchDocuments(where(LEGACY_ID).isNotNull().andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull()))
				.extracting("title", "content.currentVersion.hash", "content.currentVersion.version", Document.FORM_MODIFIED_ON, Document.FORM_CREATED_ON)
				.containsOnly(
						tuple("1.txt", "F-roHxDf6G8Ks_bQjnaxc1fPjuw=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("2.txt", "B_Y1uv947wtmT6zR294q3eAkHOs=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("3.txt", "LhTJnquyaSPRtdZItiSx0UNkpcc=", "1.0", timeOfMyLife, timeOfMyLife),
						tuple("4.txt", "fRNOVjfA_c-w6xobmII_eIPU6s4=", "1.0", timeOfMyLife, timeOfMyLife)
				);
	}

    @Test
    public void givenNoMappingNoTargetTaxonomyAdnInvalidHierarchyThenCreateRMFolderInDefaultParent()
            throws Exception {
        notAUnitItest = true;
        givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath();
        ClassifyConnectorFolderInParentFolderActionParameters parameters = ClassifyConnectorFolderInParentFolderActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setActionAfterClassification(DO_NOTHING).setDefaultParentFolder(records.folder_A07)
                .setDefaultOpenDate(squatreNovembre));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("/"))
                .setAction(ClassifyConnectorFolderInParentFolderActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderANoTaxoURL);
		assertThat(folderA.getParentFolder()).isEqualTo(records.folder_A07);
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("A");
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAANoTaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAB = getFolderByLegacyId(folderABNoTaxoURL);
		assertThat(folderAB.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAB.getTitle()).isEqualTo("AB");
		assertThat(folderAB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAA = getFolderByLegacyId(folderAAANoTaxoURL);
		assertThat(folderAAA.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAA.getTitle()).isEqualTo("AAA");
		assertThat(folderAAA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAB = getFolderByLegacyId(folderAABNoTaxoURL);
		assertThat(folderAAB.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAB.getTitle()).isEqualTo("AAB");
		assertThat(folderAAB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderB = getFolderByLegacyId(folderBNoTaxoURL);
		assertThat(folderB.getParentFolder()).isEqualTo(records.folder_A07);
		assertThat(folderB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderB.getTitle()).isEqualTo("B");
		assertThat(folderB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenFolderAlreadyClassifiedUsingDirectInFolderActionWhenRerunRobotWithDifferentParametersThenUnchanged()
            throws Exception {
        notAUnitItest = true;
        givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath();
        ClassifyConnectorFolderInParentFolderActionParameters parameters = ClassifyConnectorFolderInParentFolderActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setActionAfterClassification(DO_NOTHING).setDefaultParentFolder(records.folder_A07)
                .setDefaultOpenDate(squatreNovembre2010));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("/"))
                .setAction(ClassifyConnectorFolderInParentFolderActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderANoTaxoURL);
		assertThat(folderA.getParentFolder()).isEqualTo(records.folder_A07);
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("A");
		assertThat(folderA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAANoTaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        recordServices.update(parameters.setActionAfterClassification(DO_NOTHING).setDefaultParentFolder(records.folder_A08)
                .setDefaultOpenDate(squatreNovembre2015));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

        folderA = getFolderByLegacyId(folderANoTaxoURL);
        assertThat(folderA.getParentFolder()).isEqualTo(records.folder_A07);
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("A");
		assertThat(folderA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		folderAA = getFolderByLegacyId(folderAANoTaxoURL);
        assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("AA");
        assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_3);
        assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
        assertThat(folderAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
        assertThat(folderAA.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenFoldersAndDocumentsImportedWhenFolderReimportedThenDocumentsUpdated()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithFoldersAndDocuments();
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema().get("aCustomRequiredMetadata")
                        .setDefaultRequirement(false);
            }
        });
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setFolderMapping(folderMapping)
                .setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Document document = getDocumentByLegacyId(folderATaxoURL + "1.txt");
		assertThat(document.getTitle()).isEqualTo("1.txt");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		document = getDocumentByLegacyId(folderBTaxoURL + "3.txt");
		assertThat(document.getTitle()).isEqualTo("3.txt");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		recordServices.add(es.newConnectorSmbDocumentWithId("documentA8", connectorInstance)
				.setTitle("8.txt").setUrl(folderATaxoURL + "8.txt").setParsedContent("Document A8 content").setParent(folderA).setLastModified(timeOfMyLife)
				.setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN));
		recordServices.update(es.getConnectorSmbDocument(documentA1).setTitle("9.txt"));

        Content documentMapping = contentManager.createMajor(users.adminIn(zeCollection), "testDocumentMapping.csv",
                contentManager.upload(getTestResourceInputStream("testDocumentMapping.csv")));
        recordServices.update(parameters.setDocumentMapping(documentMapping));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		document = getDocumentByLegacyId(folderATaxoURL + "1.txt");
		assertThat(strContentOf(document)).isEqualTo("Document 9");
		assertThat(document.getTitle()).isEqualTo("Le document 1");
		assertThat(document.getAuthor()).isEqualTo("Rob Robinson");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		document = getDocumentByLegacyId(folderBTaxoURL + "3.txt");
		assertThat(document.getTitle()).isEqualTo("Le document 3");
		assertThat(document.getAuthor()).isEqualTo("Dan Danielson");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		document = getDocumentByLegacyId(folderATaxoURL + "8.txt");
		assertThat(document.getTitle()).isEqualTo("8.txt");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void whenRobotClassifyingValidFoldersWithoutDocumentsThenAllFoldersClassifiedProperly()
            throws Exception {
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setFolderMapping(folderMapping).setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		assertThat(getFolderByLegacyId(folderATaxoURL).getTitle()).isEqualTo("Le dossier A");
		assertThat(getFolderByLegacyId(folderATaxoURL).getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(getFolderByLegacyId(folderATaxoURL).getFormCreatedOn()).isEqualTo(timeOfMyLife);
		assertThat(getFolderByLegacyId(folderAATaxoURL).get(Folder.ADMINISTRATIVE_UNIT)).isEqualTo(adminUnit11);
		assertThat(getFolderByLegacyId(folderAATaxoURL).getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(getFolderByLegacyId(folderAATaxoURL).getFormCreatedOn()).isEqualTo(timeOfMyLife);
		assertThat(rm.getFolder(getFolderByLegacyId(folderABTaxoURL).getParentFolder()).get(LEGACY_ID.getLocalCode()))
				.isEqualTo(folderATaxoURL);
		assertThat(getFolderByLegacyId(folderABTaxoURL).getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(getFolderByLegacyId(folderABTaxoURL).getFormCreatedOn()).isEqualTo(timeOfMyLife);
		assertThat(rm.getFolder(getFolderByLegacyId(folderAAATaxoURL).getParentFolder()).get(LEGACY_ID.getLocalCode()))
				.isEqualTo(folderAATaxoURL);
		assertThat(getFolderByLegacyId(folderAAATaxoURL).getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(getFolderByLegacyId(folderAAATaxoURL).getFormCreatedOn()).isEqualTo(timeOfMyLife);
		assertThat(getFolderByLegacyId(folderBTaxoURL).getTitle()).isEqualTo("Le dossier B");
		assertThat(getFolderByLegacyId(folderBTaxoURL).getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(getFolderByLegacyId(folderBTaxoURL).getFormCreatedOn()).isEqualTo(timeOfMyLife);
		assertThat(getFolderByLegacyId(folderBTaxoURL).get(Folder.ADMINISTRATIVE_UNIT)).isEqualTo(adminUnit21);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenFolderMappingWithMissingEntriesThenMissingFoldersUseDefaultValues()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().get("aCustomRequiredMetadata")
                        .setDefaultRequirement(false);
                types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().get(Folder.OPENING_DATE).setDefaultRequirement(false);
            }
        });
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMappingWithMissingEntries.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMappingWithMissingEntries.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(
                parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(EXCLUDE_DOCUMENTS)
                        .setFolderMapping(folderMapping).setDelimiter(" ").setDefaultCategory(records.categoryId_X)
                        .setDefaultCopyStatus(CopyType.PRINCIPAL).setDefaultRetentionRule(records.ruleId_3));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderATaxoURL);
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAATaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAA = getFolderByLegacyId(folderAAATaxoURL);
		assertThat(folderAAA.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAA.getTitle()).isEqualTo("Le dossier AAA");
		assertThat(folderAAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAB = getFolderByLegacyId(folderAABTaxoURL);
		assertThat(folderAAB.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAB.getTitle()).isEqualTo("AAB");
		assertThat(folderAAB.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAB.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenFoldersImportedWithoutMappingWhenReimportingWithMappingThenAllFieldsMapped()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().get("aCustomRequiredMetadata")
                        .setDefaultRequirement(false);
                types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().get(Folder.OPENING_DATE).setDefaultRequirement(false);
            }
        });
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setDefaultCategory(records.categoryId_X).setDefaultAdminUnit(records.unitId_12)
                .setDefaultCopyStatus(CopyType.PRINCIPAL).setDefaultRetentionRule(records.ruleId_3)
                .setDefaultOpenDate(squatreNovembre));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderATaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("A");
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        recordServices.update(parameters.setFolderMapping(folderMapping));
        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		folderA = getFolderByLegacyId(folderATaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("Le dossier A");
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderA.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAATaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("Le dossier AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAB = getFolderByLegacyId(folderABTaxoURL);
		assertThat(folderAB.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAB.getTitle()).isEqualTo("Le dossier AB");
		assertThat(folderAB.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAB.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenExcludingImportedFoldersFromConnectorWhenImportingThenFoldersExcluded()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithFoldersAndDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        Content documentMapping = contentManager.createMajor(users.adminIn(zeCollection), "testDocumentMapping.csv",
                contentManager.upload(getTestResourceInputStream("testDocumentMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(EXCLUDE_DOCUMENTS)
                .setDelimiter(" ").setFolderMapping(folderMapping).setDocumentMapping(documentMapping)
                .setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

        assertThat(es.getConnectorSmbInstance(es.getConnectorSmbFolder(folderA).getConnector()).getExclusions())
                .containsOnly(documentA1TaxoURL, documentA2TaxoURL, documentB3TaxoURL, documentAA4TaxoURL, documentAA5TaxoURL,
                        documentAAA6TaxoURL, documentB7JustDeletedTaxoURL);

        assertThatAllDocuments().extracting("id", "fetched", "logicallyDeletedStatus").containsOnly(
                tuple(documentA1, false, true),
                tuple(documentA2, false, true),
                tuple(documentAA4, false, true),
                tuple(documentAA5, false, true),
                tuple(documentAAA6, false, true),
                tuple(documentB3, false, true),
                tuple(documentB7JustDeleted, false, true)
        );

        assertThatAllFolders().extracting("id", "fetched", "logicallyDeletedStatus").containsOnly(
                tuple(folderA, true, false),
                tuple(folderAA, true, false),
                tuple(folderAAA, true, false),
                tuple(folderAAB, true, false),
                tuple(folderAB, true, false),
                tuple(folderB, true, false)
        );

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    public static class NoAB_OR_1_Validator implements RecordValidator {

        @Override
        public void validate(RecordValidatorParams params) {
            String title = params.getValidatedRecord().get(Schemas.TITLE);
            if (title.contains("1") || title.contains("3")) {
                params.getValidationErrors().add(NoAB_OR_1_Validator.class, "No 1, 3");
            }
        }
    }

    @Test
    public void givenActionDeletingOriginalDocumentsThenOnlyDeleteDocumentsThatWereValid()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        ArgumentCaptor<ConnectorDocument> deletedConnectorDocuments = ArgumentCaptor.forClass(ConnectorDocument.class);
        doNothing().when(connectorSmb).deleteFile(deletedConnectorDocuments.capture());

        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchema(Document.DEFAULT_SCHEMA).defineValidators().add(NoAB_OR_1_Validator.class);
            }
        });

        givenFetchedTaxonomyWithFoldersAndDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        Content documentMapping = contentManager.createMajor(users.adminIn(zeCollection), "testDocumentMapping.csv",
                contentManager.upload(getTestResourceInputStream("testDocumentMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters
                .setInTaxonomy(ADMINISTRATIVE_UNITS)
                .setActionAfterClassification(DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM)
                .setDelimiter(" ")
                .setFolderMapping(folderMapping)
                .setDocumentMapping(documentMapping)
                .setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        assertThatAllDocuments().extracting("id", "fetched", "logicallyDeletedStatus").containsOnly(
                tuple(documentA1, true, false),
                tuple(documentA2, true, false),
                tuple(documentAA4, true, false),
                tuple(documentAA5, true, false),
                tuple(documentAAA6, true, false),
                tuple(documentB3, true, false),
                tuple(documentB7JustDeleted, true, false)
        );

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

        assertThatAllDocuments().extracting("id", "fetched", "logicallyDeletedStatus").containsOnly(
                tuple(documentA1, true, false),
                tuple(documentA2, false, true),
                tuple(documentAA4, false, true),
                tuple(documentAA5, false, true),
                tuple(documentAAA6, false, true),
                tuple(documentB3, true, false),
                tuple(documentB7JustDeleted, false, true)
        );

        assertThatAllFolders().extracting("id", "fetched", "logicallyDeletedStatus").containsOnly(
                tuple(folderA, true, false),
                tuple(folderAA, true, false),
                tuple(folderAAA, true, false),
                tuple(folderAAB, true, false),
                tuple(folderAB, true, false),
                tuple(folderB, true, false)
        );

        //Folders are not excluded
        assertThat(es.getConnectorSmbInstance(es.getConnectorSmbFolder(folderA).getConnector()).getExclusions()).containsOnly(
                documentA2TaxoURL,
                documentAA4TaxoURL,
                documentAA5TaxoURL,
                documentAAA6TaxoURL,
                documentB7JustDeletedTaxoURL);

        assertThatAllLogs().extracting("title").containsOnly(
                "Démarrage du robot dans la collection zeCollection",
                "Document '" + documentA2TaxoURL + "' supprimé suite à sa classification dans Constellio",
                "Document '" + documentAA4TaxoURL + "' supprimé suite à sa classification dans Constellio",
                "Document '" + documentAA5TaxoURL + "' supprimé suite à sa classification dans Constellio",
                "Document '" + documentAAA6TaxoURL + "' supprimé suite à sa classification dans Constellio",
                "Document '" + documentB7JustDeletedTaxoURL + "' supprimé suite à sa classification dans Constellio",
                "Exécution terminée : 7 document(s) / 6 répertoire(s) chargé(s)"
        );

        assertThat(deletedConnectorDocuments.getAllValues()).extracting("id")
                .containsOnly(documentA2, documentAA4, documentAA5, documentAAA6, documentB7JustDeleted);
    }

    private ListAssert<ConnectorSmbFolder> assertThatAllFolders() {
        LogicalSearchQuery query = new LogicalSearchQuery(from(es.connectorSmbFolder.schemaType()).returnAll());
        return assertThat(es.searchConnectorSmbFolders(query));
    }

    private ListAssert<ConnectorSmbDocument> assertThatAllDocuments() {
        LogicalSearchQuery query = new LogicalSearchQuery(from(es.connectorSmbDocument.schemaType()).returnAll());
        return assertThat(es.searchConnectorSmbDocuments(query));
    }

    @Test
    public void givenAnErrorOccurredDuringTheTransactionThenDocumentAreNotMarkedAsUnfetchedNorDeletedOnOriginalLocation()
            throws Exception {
        givenDisabledAfterTestValidations();
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        ArgumentCaptor<ConnectorDocument> deletedConnectorDocuments = ArgumentCaptor.forClass(ConnectorDocument.class);
        doNothing().when(connectorSmb).deleteFile(deletedConnectorDocuments.capture());

        givenFetchedTaxonomyWithFoldersAndDocuments();
        Content documentMapping = contentManager.createMajor(users.adminIn(zeCollection), "testDocumentMapping.csv",
                contentManager.upload(getTestResourceInputStream("testDocumentMapping.csv")));
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS)
                .setActionAfterClassification(DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM)
                .setDelimiter(" ")
                .setFolderMapping(folderMapping)
                .setDocumentMapping(documentMapping)
                .setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE)
                .setSearchCriterion(new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                        .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {
            @Override
            public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
                if (event.getSchemaTypeCode().equals(Document.SCHEMA_TYPE)) {
                    String title = event.getRecord().get(Schemas.TITLE);
                    if (title.contains("4")) {
                        String id = event.getRecord().getId();
                        try {
                            recordServices.add(robotsSchemas.newRobotLogWithId(id).setRobot(robotId).setTitle("Mouhahahaha"));
                        } catch (RecordServicesException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        assertThatAllDocuments().extracting("id", "fetched", "logicallyDeletedStatus").containsOnly(
                tuple(documentA1, true, false),
                tuple(documentA2, true, false),
                tuple(documentAA4, true, false),
                tuple(documentAA5, true, false),
                tuple(documentAAA6, true, false),
                tuple(documentB3, true, false),
                tuple(documentB7JustDeleted, true, false)
        );

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

        assertThatAllDocuments().extracting("id", "fetched", "logicallyDeletedStatus").containsOnly(
                tuple(documentA1, false, true),
                tuple(documentA2, false, true),
                tuple(documentAA4, true, false),
                tuple(documentAA5, true, false),
                tuple(documentAAA6, true, false),
                tuple(documentB3, false, true),
                tuple(documentB7JustDeleted, false, true)
        );

        assertThatAllFolders().extracting("id", "fetched", "logicallyDeletedStatus").containsOnly(
                tuple(folderA, true, false),
                tuple(folderAA, true, false),
                tuple(folderAAA, true, false),
                tuple(folderAAB, true, false),
                tuple(folderAB, true, false),
                tuple(folderB, true, false)
        );

        //Folders are not excluded
        assertThat(es.getConnectorSmbInstance(es.getConnectorSmbFolder(folderA).getConnector()).getExclusions()).containsOnly(
                documentA1TaxoURL,
                documentA2TaxoURL,
                documentB3TaxoURL,
                documentB7JustDeletedTaxoURL);

        assertThatAllLogs().extracting("title").contains(
                "Document '" + documentA1TaxoURL + "' supprimé suite à sa classification dans Constellio",
                "Document '" + documentA2TaxoURL + "' supprimé suite à sa classification dans Constellio",
                "Document '" + documentB3TaxoURL + "' supprimé suite à sa classification dans Constellio",
                "Document '" + documentB7JustDeletedTaxoURL + "' supprimé suite à sa classification dans Constellio"
        );

        assertThat(deletedConnectorDocuments.getAllValues()).extracting("id")
                .containsOnly(documentA1, documentA2, documentB3, documentB7JustDeleted);
    }

    @Test
    public void givenFoldersMissingRequiredValuesThenNotProcessedAndErrorAddedToRobotAndSmbFolderNotExcluded()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(EXCLUDE_DOCUMENTS)
                .setDelimiter(" ").setFolderMapping(folderMapping));

        recordServices.add(robotsSchemas.newRobotWithId("terminator").setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

        List<RobotLog> loggedErrors = getRobotLogsForRobot("terminator");
        assertThat(loggedErrors.size()).isEqualTo(8);
        assertThat(es.getConnectorSmbInstance(es.getConnectorSmbFolder(folderA).getConnector()).getExclusions())
                .isEmpty();

        assertThatRecord(recordServices.getDocumentById(folderA))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderAA))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderAAA))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderAAB))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderAB))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderB))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);

        assertThat(rm.getFolderWithLegacyId(folderATaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderAATaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderAAATaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderAABTaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderABTaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderBTaxoURL)).isNull();

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenFoldersMissingRequiredValuesThenNotProcessedAndErrorAddedToRobotAndSmbFolderNotExcludedAndDocumentsNotDeleted()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithFoldersAndDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS)
                .setActionAfterClassification(DELETE_DOCUMENTS_ON_ORIGINAL_SYSTEM)
                .setDelimiter(" ").setFolderMapping(folderMapping));

        recordServices.add(robotsSchemas.newRobotWithId("terminator").setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

        List<RobotLog> loggedErrors = getRobotLogsForRobot("terminator");
        assertThat(loggedErrors.size()).isEqualTo(8);
        assertThat(es.getConnectorSmbInstance(es.getConnectorSmbFolder(folderA).getConnector()).getExclusions())
                .isEmpty();

        assertThatRecord(recordServices.getDocumentById(folderA))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderAA))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderAAA))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderAAB))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderAB))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);
        assertThatRecord(recordServices.getDocumentById(folderB))
                .hasMetadataValue(Schemas.FETCHED, true).hasNoMetadataValue(Schemas.LOGICALLY_DELETED_STATUS);

        assertThat(rm.getFolderWithLegacyId(folderATaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderAATaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderAAATaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderAABTaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderABTaxoURL)).isNull();
        assertThat(rm.getFolderWithLegacyId(folderBTaxoURL)).isNull();

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenFolderMappingWithCustomTypeFoldersAndCustomMetadataThenAllFieldsMapped()
            throws Exception {
        withACustomRequiredMetadataInSchemas();
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("customFolder").create("customMeta")
                        .setType(MetadataValueType.STRING);
            }
        });
        recordServices.add(rm.newFolderType().setTitle("customFolder").setCode("customFolder").setLinkedSchema("customFolder"));

        notAUnitItest = true;
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMappingWithCustomTypes.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMappingWithCustomTypes.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setFolderMapping(folderMapping).setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderATaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("Le dossier A");
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderA.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderA.getKeywords()).containsOnly("mot1", "mot2", "mot3");
		assertThat(folderA.getSchemaCode()).isEqualTo("folder_customFolder");
		assertThat(folderA.get("customMeta")).isEqualTo("valeur A");
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAATaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("Le dossier AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAA.getKeywords()).containsOnly("mot1", "mot2");
		assertThat(folderAA.getSchemaCode()).isEqualTo("folder_customFolder");
		assertThat(folderAA.get("customMeta")).isEqualTo("valeur AA");
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAB = getFolderByLegacyId(folderABTaxoURL);
		assertThat(folderAB.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAB.getTitle()).isEqualTo("Le dossier AB");
		assertThat(folderAB.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAB.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAB.getKeywords()).containsOnly("mot1", "mot2");
		assertThat(folderAB.getSchemaCode()).isEqualTo("folder_customFolder");
		assertThat(folderAB.get("customMeta")).isEqualTo("valeur AB");
		assertThat(folderAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAA = getFolderByLegacyId(folderAAATaxoURL);
		assertThat(folderAAA.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAA.getTitle()).isEqualTo("Le dossier AAA");
		assertThat(folderAAA.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAA.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAAA.getKeywords()).containsOnly("mot1", "mot2");
		assertThat(folderAAA.getSchemaCode()).isEqualTo("folder_customFolder");
		assertThat(folderAAA.get("customMeta")).isEqualTo("valeur AAA");
		assertThat(folderAAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAB = getFolderByLegacyId(folderAABTaxoURL);
		assertThat(folderAAB.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAB.getTitle()).isEqualTo("Le dossier AAB");
		assertThat(folderAAB.getRetentionRule()).isEqualTo(records.ruleId_1);
		assertThat(folderAAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAB.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-11-04");
		assertThat(folderAAB.getKeywords()).containsOnly("mot1", "mot2");
		assertThat(folderAAB.getSchemaCode()).isEqualTo("folder_customFolder");
		assertThat(folderAAB.get("customMeta")).isEqualTo("valeur AAB");
		assertThat(folderAAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderB = getFolderByLegacyId(folderBTaxoURL);
		assertThat(folderB.getParentFolder()).isNull();
		assertThat(folderB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderB.getTitle()).isEqualTo("Le dossier B");
		assertThat(folderB.getRetentionRule()).isEqualTo(records.ruleId_2);
		assertThat(folderB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderB.getOpenDate().toString("yyyy-MM-dd")).isEqualTo("2015-12-14");
		assertThat(folderB.getKeywords()).containsOnly("mot2", "mot4");
		assertThat(folderB.getSchemaCode()).isEqualTo("folder_default");
		assertThat(folderB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenConnectorHaveExtraMetadatasThenCopiedToFoldersAndDocumentsWhenMatched()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithFoldersAndDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        Content documentMapping = contentManager.createMajor(users.adminIn(zeCollection), "testDocumentMapping.csv",
                contentManager.upload(getTestResourceInputStream("testDocumentMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setDocumentMapping(documentMapping).setFolderMapping(folderMapping)
                .setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                MetadataSchemaBuilder smbFolder = types.getSchemaType(ConnectorSmbFolder.SCHEMA_TYPE)
                        .getSchema(connectorInstance.getId());
                MetadataSchemaBuilder smbDocument = types.getSchemaType(ConnectorSmbDocument.SCHEMA_TYPE)
                        .getSchema(connectorInstance.getId());
                MetadataSchemaBuilder folder = types.getSchema(Folder.DEFAULT_SCHEMA);
                MetadataSchemaBuilder document = types.getSchema(Document.DEFAULT_SCHEMA);
                smbFolder.create("meta1").setType(MetadataValueType.NUMBER).setMultivalue(true);
                smbFolder.create("MAPmeta2").setType(MetadataValueType.STRING).setMultivalue(false);
                smbFolder.create("meta3").setType(MetadataValueType.STRING).setMultivalue(true);

                smbDocument.create("MAPmeta1").setType(MetadataValueType.STRING).setMultivalue(false);
                smbDocument.create("meta2").setType(MetadataValueType.DATE).setMultivalue(false);

                folder.create("USRmeta1").setType(MetadataValueType.NUMBER).setMultivalue(true);
                folder.create("USRmeta2").setType(MetadataValueType.STRING).setMultivalue(false);

                //Different
                folder.create("meta3").setType(MetadataValueType.STRING).setMultivalue(false);

                document.create("meta1").setType(MetadataValueType.STRING).setMultivalue(false);

                //Different
                document.create("meta2").setType(MetadataValueType.DATE_TIME).setMultivalue(false);
            }
        });

        MetadataSchema smbFolder = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
                .getSchemaType(ConnectorSmbFolder.SCHEMA_TYPE).getSchema(connectorInstance.getId());

        MetadataSchema smbDocument = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
                .getSchemaType(ConnectorSmbDocument.SCHEMA_TYPE).getSchema(connectorInstance.getId());

        ConnectorSmbFolder connectorSmbFolderA = es.getConnectorSmbFolder(folderA);
        connectorSmbFolderA.set("meta1", asList(42.666, 666.42));
        connectorSmbFolderA.set("MAPmeta2", "value");
        connectorSmbFolderA.set("meta3", asList("value1", "value2"));

        ConnectorSmbDocument connectorSmbDocumentA1 = es.getConnectorSmbDocument(documentA1);
        connectorSmbDocumentA1.set("MAPmeta1", "value3");
        connectorSmbDocumentA1.set("meta2", new LocalDate());

        recordServices.update(connectorSmbFolderA);
        recordServices.update(connectorSmbDocumentA1);

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderATaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("Le dossier A");
		assertThat(folderA.get("USRmeta1")).isEqualTo(asList(42.666, 666.42));
		assertThat(folderA.get("USRmeta2")).isEqualTo("value");
		assertThat(folderA.get("meta3")).isNull();
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Document document = getDocumentByLegacyId(documentA1TaxoURL);
		assertThat(document.getTitle()).isEqualTo("Le document 1");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.get("meta1")).isEqualTo("value3");
		assertThat(document.get("meta2")).isNull();
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);
	}

    @Test
    public void givenFoldersWithCustomDocumentsAndDocumentMappingThenAllFieldsMapped()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithFoldersAndDocuments();
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchemaType(Document.SCHEMA_TYPE).createCustomSchema("customDocument").create("customMeta")
                        .setType(MetadataValueType.STRING);
            }
        });
        recordServices.add(
                rm.newDocumentType().setTitle("customDocument").setCode("customDocument").setLinkedSchema("customDocument"));
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        Content documentMapping = contentManager
                .createMajor(users.adminIn(zeCollection), "testDocumentMappingWithCustomTypes.csv",
                        contentManager.upload(getTestResourceInputStream("testDocumentMappingWithCustomTypes.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setDocumentMapping(documentMapping).setFolderMapping(folderMapping)
                .setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Document document = getDocumentByLegacyId(folderATaxoURL + "1.txt");
		assertThat(document.getTitle()).isEqualTo("Le document 1");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getAuthor()).isEqualTo("Rob Robinson");
		assertThat(document.getSchemaCode()).isEqualTo("document_customDocument");
		assertThat(document.get("customMeta")).isEqualTo("valeur 1");
		assertThat(document.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		document = getDocumentByLegacyId(folderBTaxoURL + "3.txt");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getTitle()).isEqualTo("Le document 3");
		assertThat(document.getAuthor()).isEqualTo("Dan Danielson");
		assertThat(document.getCreatedByRobot()).isEqualTo(robotId);
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void whenRobotClassifyingValidFoldersWithDocumentsAndDocumentMappingThenAllDocumentsMapped()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithFoldersAndDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        Content documentMapping = contentManager.createMajor(users.adminIn(zeCollection), "testDocumentMapping.csv",
                contentManager.upload(getTestResourceInputStream("testDocumentMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setDocumentMapping(documentMapping).setFolderMapping(folderMapping)
                .setDefaultCategory(records.categoryId_X));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Document document = getDocumentByLegacyId(folderATaxoURL + "1.txt");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getTitle()).isEqualTo("Le document 1");
		assertThat(document.getAuthor()).isEqualTo("Rob Robinson");
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		document = getDocumentByLegacyId(folderBTaxoURL + "3.txt");
		assertThat(document.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(document.getTitle()).isEqualTo("Le document 3");
		assertThat(document.getAuthor()).isEqualTo("Dan Danielson");
		assertThat(document.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenNoFolderMappingButDefaultValuesThenAllFoldersUsingDefaultValues()
            throws Exception {
        notAUnitItest = true;
        withACustomRequiredMetadataInSchemas();
        getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().get("aCustomRequiredMetadata")
                        .setDefaultRequirement(false);
                types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema().get(Folder.OPENING_DATE).setDefaultRequirement(false);
            }
        });
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();

        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setDefaultCategory(records.categoryId_X).setDefaultCopyStatus(CopyType.PRINCIPAL)
                .setDefaultOpenDate(squatreNovembre).setDefaultRetentionRule(records.ruleId_3)
                .setDefaultAdminUnit(records.unitId_12));

        recordServices.add(robotsSchemas.newRobotWithId(robotId).setActionParameters(parameters)
                .setSchemaFilter(ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(
                        new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

		Folder folderA = getFolderByLegacyId(folderATaxoURL);
		assertThat(folderA.getParentFolder()).isNull();
		assertThat(folderA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderA.getTitle()).isEqualTo("A");
		assertThat(folderA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAA = getFolderByLegacyId(folderAATaxoURL);
		assertThat(folderAA.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAA.getTitle()).isEqualTo("AA");
		assertThat(folderAA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAB = getFolderByLegacyId(folderABTaxoURL);
		assertThat(folderAB.getParentFolder()).isEqualTo(folderA.getId());
		assertThat(folderAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAB.getTitle()).isEqualTo("AB");
		assertThat(folderAB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAB.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAA = getFolderByLegacyId(folderAAATaxoURL);
		assertThat(folderAAA.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAA.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAA.getTitle()).isEqualTo("AAA");
		assertThat(folderAAA.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAAA.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAA.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAAA.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAAA.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderAAB = getFolderByLegacyId(folderAABTaxoURL);
		assertThat(folderAAB.getParentFolder()).isEqualTo(folderAA.getId());
		assertThat(folderAAB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderAAB.getTitle()).isEqualTo("AAB");
		assertThat(folderAAB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderAAB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderAAB.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderAAB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderAAB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

		Folder folderB = getFolderByLegacyId(folderBTaxoURL);
		assertThat(folderB.getParentFolder()).isNull();
		assertThat(folderB.getFormModifiedOn()).isEqualTo(timeOfMyLife);
		assertThat(folderB.getTitle()).isEqualTo("B");
		assertThat(folderB.getRetentionRule()).isEqualTo(records.ruleId_3);
		assertThat(folderB.getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(folderB.getCategory()).isEqualTo(records.categoryId_X);
		assertThat(folderB.getOpenDate()).isEqualTo(timeOfMyLife.toLocalDate());
		assertThat(folderB.getFormCreatedOn()).isEqualTo(timeOfMyLife);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenOneFolderWhenClassifyingConnectorTaxonomyThenClassified()
            throws Exception {
        withACustomRequiredMetadataInSchemas();
        Transaction transaction = new Transaction();
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit1)).setCode("AU1").setTitle("Admin Unit1");
        transaction.add(es.newConnectorSmbFolderWithId(folderA, connectorInstance))
                .setTitle("A").setUrl("smb://AU1 Admin Unit1/A/");
        recordServices.execute(transaction);

        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setFolderMapping(folderMapping).setDefaultCategory(records.categoryId_X).setDefaultUniformSubdivision(records.subdivId_1));

        Record folderARecord = recordServices.getDocumentById(folderA);
        classifyConnectorFolderInTaxonomy(folderARecord, parameters);

        Metadata legacyIdMetadata = rm.defaultFolderSchema().get(LEGACY_ID.getLocalCode());
        Metadata titleMetadata = rm.defaultFolderSchema().get(Schemas.TITLE.getLocalCode());
        Metadata uniformSubdivisionMetadata = rm.defaultFolderSchema().get(Folder.UNIFORM_SUBDIVISION);
        assertThat(recordServices.getRecordByMetadata(legacyIdMetadata, "smb://AU1 Admin Unit1/A/").get(titleMetadata))
                .isEqualTo("Le dossier A");
        assertThat(recordServices.getRecordByMetadata(legacyIdMetadata, "smb://AU1 Admin Unit1/A/").get(uniformSubdivisionMetadata))
                .isEqualTo(records.subdivId_1);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void whenDirectlyClassifyingValidFoldersWithoutDocumentsThenAllFoldersClassifiedProperly()
            throws Exception {
        withACustomRequiredMetadataInSchemas();
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();
        Content folderMapping = contentManager.createMajor(users.adminIn(zeCollection), "testFolderMapping.csv",
                contentManager.upload(getTestResourceInputStream("testFolderMapping.csv")));
        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setActionAfterClassification(DO_NOTHING)
                .setDelimiter(" ").setFolderMapping(folderMapping).setDefaultCategory(records.categoryId_X));

        Record folderARecord = recordServices.getDocumentById(folderA);
        classifyConnectorFolderInTaxonomy(folderARecord, parameters);

        Record folderAARecord = recordServices.getDocumentById(folderAA);
        classifyConnectorFolderInTaxonomy(folderAARecord, parameters);

        Record folderABRecord = recordServices.getDocumentById(folderAB);
        classifyConnectorFolderInTaxonomy(folderABRecord, parameters);

        assertThat(getFolderByLegacyId(folderATaxoURL).getTitle()).isEqualTo("Le dossier A");
        assertThat(getFolderByLegacyId(folderAATaxoURL).get(Folder.ADMINISTRATIVE_UNIT)).isEqualTo(adminUnit11);
        assertThat(rm.getFolder(getFolderByLegacyId(folderABTaxoURL).getParentFolder()).get(LEGACY_ID.getLocalCode()))
                .isEqualTo(folderATaxoURL);

        verify(connectorSmb, never()).deleteFile(any(ConnectorDocument.class));
    }

    @Test
    public void givenNonConceptConnectorFoldersHaveDelimitersInTheirNameThenImportedCorrectly()
            throws Exception {

        Transaction transaction = new Transaction();
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit1)).setCode("AU1").setTitle(adminUnit1);
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit11)).setCode("AU11").setTitle(adminUnit11).setParent(adminUnit1);
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit2)).setCode("AU2").setTitle(adminUnit2);

        //Recognized as AU1
        transaction.add(es.newConnectorSmbFolderWithId("smbFolder1", connectorInstance)).setTitle("AU1 Ze admin unit")
                .setUrl("smb://AU1 Ze admin unit/");

        //Recognized as AU11
        transaction.add(es.newConnectorSmbFolderWithId("smbFolder2", connectorInstance)).setTitle("AU11 Ze child admin unit")
                .setUrl("smb://AU1 Ze admin unit/AU11 Ze child admin unit/");

        //Recognized as a folder in AU1
        transaction.add(es.newConnectorSmbFolderWithId("smbFolder4", connectorInstance)).setTitle("Folder A")
                .setUrl("smb://AU1 Ze admin unit/Folder A/");

        //Recognized as a sub folder in AU1
        transaction.add(es.newConnectorSmbFolderWithId("smbFolder5", connectorInstance)).setTitle("Sub folder in A")
                .setParent("smbFolder4").setUrl("smb://AU1 Ze admin unit/Folder A/Sub folder in A/");

        //Recognized as a sub folder in AU1
        transaction.add(es.newConnectorSmbFolderWithId("smbFolder6", connectorInstance)).setTitle("Sub sub folder in A")
                .setParent("smbFolder5").setUrl("smb://AU1 Ze admin unit/Folder A/Sub folder in A/Sub sub folder in A/");

        //Recognized as a sub folder in AU1
        transaction.add(es.newConnectorSmbFolderWithId("smbFolder7", connectorInstance)).setTitle("AU1 Another sub folder in A")
                .setParent("smbFolder4").setUrl("smb://AU1 Ze admin unit/Folder A/AU1 Another sub folder in A/");

        //Recognized as a sub folder in AU1
        transaction.add(es.newConnectorSmbFolderWithId("smbFolder8", connectorInstance)).setTitle("AU1 Ze folder")
                .setParent("smbFolder7").setUrl("smb://AU1 Ze admin unit/Folder A/AU1 Another sub folder in A/AU1 Ze folder/");

        recordServices.execute(transaction);

        ClassifyConnectorFolderInTaxonomyActionParameters parameters = ClassifyConnectorFolderInTaxonomyActionParameters
                .wrap(robotsSchemas.newActionParameters(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE));
        recordServices.add(parameters.setInTaxonomy(ADMINISTRATIVE_UNITS).setDelimiter(" ")
                .setDefaultAdminUnit(records.unitId_10).setDefaultCategory(records.categoryId_X100)
                .setDefaultRetentionRule(records.ruleId_1).setDefaultCopyStatus(CopyType.PRINCIPAL)
                .setDefaultOpenDate(new LocalDate()));

        recordServices.add(robotsSchemas.newRobotWithId("terminator").setActionParameters(parameters).setSchemaFilter(
                ConnectorSmbFolder.SCHEMA_TYPE).setSearchCriterion(new CriterionBuilder(ConnectorSmbFolder.SCHEMA_TYPE)
                .where(es.connectorSmbFolder.url()).isContainingText("smb://"))
                .setAction(ClassifyConnectorFolderInTaxonomyActionExecutor.ID).setCode("terminator").setTitle("terminator"));

        robotsSchemas.getRobotsManager().startAllRobotsExecution();
        waitForBatchProcess();

        List<RobotLog> loggedErrors = getRobotLogsForRobot("terminator");
        assertThat(loggedErrors.size()).isEqualTo(2);

        assertThatRecord(rm.getFolderWithLegacyId("smb://AU1 Ze admin unit/")).isNull();
        assertThatRecord(rm.getFolderWithLegacyId("smb://AU1 Ze admin unit/AU11 Ze child admin unit/")).isNull();

        Folder classifiedSmbFolder4 = rm.getFolderWithLegacyId("smb://AU1 Ze admin unit/Folder A/");
        Folder classifiedSmbFolder5 = rm.getFolderWithLegacyId(
                "smb://AU1 Ze admin unit/Folder A/Sub folder in A/");
        Folder classifiedSmbFolder6 = rm
                .getFolderWithLegacyId("smb://AU1 Ze admin unit/Folder A/Sub folder in A/Sub sub folder in A/");
        Folder classifiedSmbFolder7 = rm.getFolderWithLegacyId("smb://AU1 Ze admin unit/Folder A/AU1 Another sub folder in A/");
        Folder classifiedSmbFolder8 = rm
                .getFolderWithLegacyId("smb://AU1 Ze admin unit/Folder A/AU1 Another sub folder in A/AU1 Ze folder/");

        assertThatRecord(classifiedSmbFolder4)
                .hasMetadata(rm.folder.parentFolder(), null)
                .hasMetadata(rm.folder.administrativeUnit(), adminUnit1);

        assertThatRecord(classifiedSmbFolder5)
                .hasMetadata(rm.folder.parentFolder(), classifiedSmbFolder4.getId())
                .hasMetadata(rm.folder.administrativeUnit(), adminUnit1);

        assertThatRecord(classifiedSmbFolder6)
                .hasMetadata(rm.folder.parentFolder(), classifiedSmbFolder5.getId())
                .hasMetadata(rm.folder.administrativeUnit(), adminUnit1);

        assertThatRecord(classifiedSmbFolder7)
                .hasMetadata(rm.folder.parentFolder(), classifiedSmbFolder4.getId())
                .hasMetadata(rm.folder.administrativeUnit(), adminUnit1);

        assertThatRecord(classifiedSmbFolder8)
                .hasMetadata(rm.folder.parentFolder(), classifiedSmbFolder7.getId())
                .hasMetadata(rm.folder.administrativeUnit(), adminUnit1);
    }

    //When delete, given error in transaction, then not deleted
    //when delete, log

    // ---------------------------------------

    private Document getDocumentByLegacyId(String path) {
        Metadata legacyIdMetadata = rm.defaultDocumentSchema().get(LEGACY_ID.getLocalCode());
        return rm.wrapDocument(recordServices.getRecordByMetadata(legacyIdMetadata, path));
    }

    private List<Record> getDocumentsInFolder(Folder folder) {
        LogicalSearchCondition condition = from(rm.defaultDocumentSchema())
                .where(rm.defaultDocumentSchema().getMetadata(Document.FOLDER)).isEqualTo(folder.getId());
        return searchServices.search(new LogicalSearchQuery(condition));
    }

    private List<RobotLog> getRobotLogsForRobot(String robotId) {
        MetadataSchema robotLogSchema = robotsSchemas.robotLog.schema();
        LogicalSearchCondition condition = from(robotLogSchema)
                .where(robotLogSchema.getMetadata(RobotLog.ROBOT)).isEqualTo(robotId);
        return robotsSchemas.wrapRobotLogs(searchServices.search(new LogicalSearchQuery(condition)));
    }

    private void givenFetchedTaxonomyWithFoldersAndDocuments()
            throws RecordServicesException {
        givenFetchedTaxonomyWithValidFoldersButNoDocuments();

		Transaction transaction = new Transaction();
		transaction.add(es.newConnectorSmbDocumentWithId(documentA1, connectorInstance))
				.setTitle("1.txt").setUrl(folderATaxoURL + "1.txt").setParsedContent("Document A1 content").setParent(
				folderA).setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA2, connectorInstance))
				.setTitle("2.txt").setUrl(folderATaxoURL + "2.txt").setParsedContent("Document A2 content").setParent(folderA).setLastModified(timeOfMyLife)
				.setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3, connectorInstance).setModifiedOn(timeOfMyLife))
				.setTitle("3.txt").setUrl(folderBTaxoURL + "3.txt").setParsedContent("Document B3 content").setParent(folderB).setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife)
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentB7JustDeleted, connectorInstance))
				.setTitle("7.txt").setUrl(folderBTaxoURL + "7.txt").setParsedContent("Document B7").setParent(folderB).setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife)
				.setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA4, connectorInstance))
				.setTitle("4.txt").setUrl(folderAATaxoURL + "4.txt").setParsedContent("Document AA4 content").setParent(folderAA).setLastModified(timeOfMyLife)
				.setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA5, connectorInstance))
				.setTitle("5.txt").setUrl(folderAATaxoURL + "5.txt").setParent(folderAA).setLastModified(timeOfMyLife)
				.setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentAAA6, connectorInstance))
				.setTitle("6.txt").setUrl(folderAAATaxoURL + "6.txt").setParent(folderAAA).setLastModified(timeOfMyLife)
				.setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);
		recordServices.execute(transaction);
	}

    private Folder getFolderByLegacyId(String path) {
        Metadata legacyIdMetadata = rm.defaultFolderSchema().get(LEGACY_ID.getLocalCode());
        return rm.wrapFolder(recordServices.getRecordByMetadata(legacyIdMetadata, path));
    }

    private void givenFetchedTaxonomyWithValidFoldersButNoDocuments()
            throws RecordServicesException {
        Transaction transaction = new Transaction();
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit1)).setCode("AU1").setTitle(adminUnit1);
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit11)).setCode("AU11").setTitle(adminUnit11).setParent(adminUnit1);
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit2)).setCode("AU2").setTitle(adminUnit2);
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit21)).setCode("AU21").setTitle(adminUnit21).setParent(adminUnit2);
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit22)).setCode("AU22").setTitle(adminUnit22).setParent(adminUnit2);

        transaction.add(es.newConnectorSmbFolderWithId(folderA, connectorInstance)).setTitle("A").setUrl(folderATaxoURL)
				.setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderAA, connectorInstance)).setTitle("AA").setUrl(folderAATaxoURL)
                .setParent(folderA).setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderAB, connectorInstance)).setTitle("AB").setUrl(folderABTaxoURL)
                .setParent(folderA).setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);;
        transaction.add(es.newConnectorSmbFolderWithId(folderAAA, connectorInstance)).setTitle("AAA").setUrl(folderAAATaxoURL)
                .setParent(folderAA).setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderAAB, connectorInstance)).setTitle("AAB").setUrl(folderAABTaxoURL)
                .setParent(folderAA).setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderB, connectorInstance)).setTitle("B").setUrl(folderBTaxoURL)
				.setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);

        recordServices.execute(transaction);

    }

    private void givenFetchedFoldersAndDocumentsWithoutValidTaxonomyPath()
            throws RecordServicesException {
        Transaction transaction = new Transaction();

        transaction.add(es.newConnectorSmbFolderWithId(folderA, connectorInstance)).setTitle("A").setUrl(folderANoTaxoURL)
				.setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderAA, connectorInstance)).setTitle("AA").setUrl(folderAANoTaxoURL)
                .setParent(folderA).setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderAB, connectorInstance)).setTitle("AB").setUrl(folderABNoTaxoURL)
                .setParent(folderA).setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderAAA, connectorInstance)).setTitle("AAA").setUrl(folderAAANoTaxoURL)
                .setParent(folderAA).setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderAAB, connectorInstance)).setTitle("AAB").setUrl(folderAABNoTaxoURL)
                .setParent(folderAA).setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);
        transaction.add(es.newConnectorSmbFolderWithId(folderB, connectorInstance)).setTitle("B").setUrl(folderBNoTaxoURL)
				.setCreatedOn(timeOfMyLife).setLastModified(timeOfMyLife);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA1, connectorInstance))
				.setTitle("1.txt").setUrl(documentA1NoTaxoURL).setParsedContent("Document A1 content").setParent(
				folderA).setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentA2, connectorInstance))
				.setTitle("2.txt").setUrl(documentA2NoTaxoURL).setParsedContent("Document A2 content").setParent(folderA).setLastModified(timeOfMyLife)
				.setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentB3, connectorInstance))
				.setTitle("3.txt").setUrl(documentB3NoTaxoURL).setParsedContent("Document B3 content").setParent(folderB).setLastModified(timeOfMyLife)
				.setCreatedOn(timeOfMyLife).setManualTokens("rtoken1");

		transaction.add(es.newConnectorSmbDocumentWithId(documentAA4, connectorInstance))
				.setTitle("4.txt").setUrl(documentAA4NoTaxoURL).setParsedContent("Document AA4 content").setParent(folderAA).setLastModified(timeOfMyLife)
				.setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

        recordServices.execute(transaction);
    }

    @Test
    public void givenFetchedFoldersAndDocumentsForRobots() throws RecordServicesException {

        Transaction transaction = new Transaction();

        transaction.add(rm.newAdministrativeUnitWithId(adminUnit1)).setCode("AU1").setTitle(adminUnit1);
        transaction.add(rm.newAdministrativeUnitWithId(adminUnit11)).setCode("AU11").setTitle(adminUnit11).setParent(adminUnit1);

		transaction.add(es.newConnectorSmbFolderWithId(folderC, connectorInstance)).setTitle("C - Carotte").setUrl(folderCTaxoURL)
                .setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife);

		transaction.add(es.newConnectorSmbFolderWithId(folderD, connectorInstance)).setTitle("D                                           Test").setUrl(folderDTaxoURL)
                .setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife);

		transaction.add(es.newConnectorSmbFolderWithId(folderCC, connectorInstance)).setTitle("C - Sous-dossier").setUrl(folderCCTaxoURL).setParent(folderC)
                .setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife);

		transaction.add(es.newConnectorSmbFolderWithId(folderE, connectorInstance)).setTitle("DossierAvecFauxCodeClassification").setUrl(folderETaxoURL)
                .setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife);

		transaction.add(es.newConnectorSmbFolderWithId(folderDD, connectorInstance)).setTitle("DossierAvecCodeAlenvers").setUrl(folderDDTaxoURL)
                .setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife);

		transaction.add(es.newConnectorSmbDocumentWithId(documentMalPlace, connectorInstance))
				.setTitle("1.txt").setParent(folderC).setUrl(documentMalPlaceTaxoURL)
				.setParsedContent("Document A2 content").setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

		transaction.add(es.newConnectorSmbDocumentWithId(documentBienPlaceTaxoURL, connectorInstance))
				.setTitle("1.txt").setParent(folderC).setUrl(documentBienPlaceTaxoURL)
				.setParsedContent("Document A2 content").setLastModified(timeOfMyLife).setCreatedOn(timeOfMyLife).setManualTokens(PUBLIC_TOKEN);

        recordServices.execute(transaction);
    }

    private ListAssert<RobotLog> assertThatAllLogs() {
        LogicalSearchQuery query = new LogicalSearchQuery(from(robotsSchemas.robotLog.schemaType()).returnAll());
        return assertThat(robotsSchemas.searchRobotLogs(query));
    }

    private String strContentOf(Document document) {
        String hash = document.getContent().getCurrentVersion().getHash();
        InputStream inputStream = getModelLayerFactory().getContentManager().getContentInputStream(hash, SDK_STREAM);
        try {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void classifyConnectorFolderInTaxonomy(Record connectorFolder, ClassifyConnectorFolderInTaxonomyActionParameters params) {
        ClassifyConnectorRecordInTaxonomyExecutor builder = new ClassifyConnectorRecordInTaxonomyExecutor(
                connectorFolder, params, es.getAppLayerFactory(), users.adminIn(zeCollection), robotId, new ArrayList<Record>(), false);
        builder.execute();

    }
}
