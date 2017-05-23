package com.constellio.app.services.importExport.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminder;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.schemas.bulkImport.BulkImportParams;
import com.constellio.app.services.schemas.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.*;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.assertj.core.groups.Tuple;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.IN_PROGRESS;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

public class RecordExportServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordExportOptions options = new RecordExportOptions();
	Users users = new Users();

	public static final String TITLE = "Title1";
	public static final String CODE = "CODE1";
	public static final String DESCRIPTION = "DESCRIPTION1";
	public static final String CONTENT_TYPES_COMMENT = "CONTENT_TYPES_COMMENT1";
	public static final String ACTIVE_RETENTION_COMMENT = "ACTIVE_RETENTION_COMMENT";
	public static final RetentionPeriod ACTIVE_RETENTION_PERIOD = RetentionPeriod.OPEN_888;
	public static final String SEMI_ACTIVE_RETENTION_COMMENT = "SEMI_ACTIVE_RETENTION_COMMENT";
	public static final RetentionPeriod SEMI_ACTIVE_RETENTION_PERIOD = RetentionPeriod.OPEN_888;
	public static final String INACTIVE_DISPOSAL_COMMENT = "DISPOSAL_COMMENT";
	public static final DisposalType INACTIVE_DISPOSAL_TYPE = DisposalType.DESTRUCTION;
	public static final Integer OPEN_ACTIVE_RETENTION_PERIOD = new Integer(100);
	public static final boolean REQUIRED_COPYRULE_FIELD = true;
	public static final String SET_ID = "ID1";
	public static final String PROCESS = "PROCESS";
	public static final String TASK_ID = "TASK_ID";
	public static final String EMAIL_1 = "constelio1@constellio.com";
	public static final String NAME_1 = "constelio1";
	public static final String EMAIL_2 = "constelio12@constellio.com";
	public static final String NAME_2 = "constelio2";
	public static final LocalDateTime LOCAL_DATE_TIME =new LocalDateTime();
	public static final String EMAIL_TEMPLATE = "Template";
	public static final String EMAIL_SUBJECT = "Subject";

	@Before
	public void setup()
	{

	}

	@Test(expected = RecordExportServicesRuntimeException.ExportServicesRuntimeException_NoRecords.class)
	public void givenEmptyCollectionWhenExportRecordsThenExceptionThrown()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(options);
	}

	@Test
	public void whenExportingAndImportingMapStringStringStructure() throws Exception {
		final String MAP_STRING_STRING_STRUCTURE = "mapStringListStringStucture";
		final String MAP_STRING_STRING_STRUCTURE_MULTIVALUE = "mapStringListStringStructure";

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withTasksModule(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users).withTasksModule());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_STRING_STRUCTURE).setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringStringStructureFactory.class);
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_STRING_STRUCTURE_MULTIVALUE).setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringStringStructureFactory.class).setMultivalue(true);
			}
		});

		getModelLayerFactory().getMetadataSchemasManager().modify("anotherCollection", new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_STRING_STRUCTURE).setType(MetadataValueType.STRUCTURE)
				.defineStructureFactory(MapStringStringStructureFactory.class);
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_STRING_STRUCTURE_MULTIVALUE).setType(MetadataValueType.STRUCTURE)
				.defineStructureFactory(MapStringStringStructureFactory.class).setMultivalue(true);
			}
		});

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		EmailToSend emailToSend = rm.newEmailToSend();

		MapStringStringStructure mapStringStringStructure = new MapStringStringStructure();
		List<MapStringStringStructure> mapStringStringStructureList = new ArrayList<>();
		MapStringStringStructure mapStringStringStructure2 = new MapStringStringStructure();

		mapStringStringStructure.put("key1", "value1");
		mapStringStringStructure.put("key2", "value2");

		mapStringStringStructure2.put("key3", "value3");
		mapStringStringStructure2.put("key4", "value4");

		mapStringStringStructureList.add(mapStringStringStructure);
		mapStringStringStructureList.add(mapStringStringStructure2);

		emailToSend.set(MAP_STRING_STRING_STRUCTURE, mapStringStringStructure);
		emailToSend.set(MAP_STRING_STRING_STRUCTURE_MULTIVALUE, mapStringStringStructureList);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		recordServices.add(emailToSend);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(EmailToSend.SCHEMA_TYPE)));

		RMSchemasRecordsServices rmFromAnOtherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());
		List<EmailToSend> emailToSendFromAnOtherCollectionList = rmFromAnOtherCollection.searchEmailToSends(returnAll());

		EmailToSend emailToSendFromAnOtherCollection = emailToSendFromAnOtherCollectionList.get(0);
		List<MapStringStringStructure> mapStringStringStructureListFromAnOtherCollection = emailToSendFromAnOtherCollection.get(MAP_STRING_STRING_STRUCTURE_MULTIVALUE);

		MapStringStringStructure mapStringStringStructureFromAnOtherCollection = emailToSendFromAnOtherCollection.get(MAP_STRING_STRING_STRUCTURE);

		// Assert MultiValued
		MapStringStringStructure mapStringStringStructureFromAnOtherCollection1 = mapStringStringStructureListFromAnOtherCollection.get(0);
		MapStringStringStructure mapStringStringStructureFromAnOtherCollection2 = mapStringStringStructureListFromAnOtherCollection.get(1);

		assertThat(mapStringStringStructureFromAnOtherCollection2.get("key4")).isEqualTo("value4");
		assertThat(mapStringStringStructureFromAnOtherCollection2.get("key3")).isEqualTo("value3");
		assertThat(mapStringStringStructureFromAnOtherCollection1.get("key2")).isEqualTo("value2");
		assertThat(mapStringStringStructureFromAnOtherCollection1.get("key1")).isEqualTo("value1");

		assertThat(mapStringStringStructureFromAnOtherCollection.get("key1")).isEqualTo("value1");
		assertThat(mapStringStringStructureFromAnOtherCollection.get("key2")).isEqualTo("value2");
	}

	@Test
	public void whenExportingAndImportingMapStringListStringStructure() throws Exception {
		final String MAP_STRING_LIST_STRING_STRUCTURE = "mapStringListStringStucture";
		final String MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE = "mapStringListStringStructure";

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withTasksModule(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users).withTasksModule());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_LIST_STRING_STRUCTURE).setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringListStringStructureFactory.class);
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE).setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringListStringStructureFactory.class).setMultivalue(true);
			}
		});

		getModelLayerFactory().getMetadataSchemasManager().modify("anotherCollection", new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_LIST_STRING_STRUCTURE).setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringListStringStructureFactory.class);
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE).setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringListStringStructureFactory.class).setMultivalue(true);
				;
			}
		});

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		EmailToSend emailToSend = rm.newEmailToSend();

		MapStringListStringStructure mapStringListStringStructure = new MapStringListStringStructure();
		List<MapStringListStringStructure> mapStringListStringStructureList = new ArrayList<>();
		MapStringListStringStructure mapStringListStringStructure2 = new MapStringListStringStructure();


		List<String> listString = asList("Hello1", "Hello2", "Hello3");
		List<String> listString2 = asList("Sup1", "Sup2", "Sup3");

		mapStringListStringStructure.put("list1", listString);
		mapStringListStringStructure.put("list2", listString2);
		mapStringListStringStructure2.put("list3", listString2);

		mapStringListStringStructureList.add(mapStringListStringStructure);
		mapStringListStringStructureList.add(mapStringListStringStructure2);

		emailToSend.set(MAP_STRING_LIST_STRING_STRUCTURE, mapStringListStringStructure);
		emailToSend.set(MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE, mapStringListStringStructureList);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		recordServices.add(emailToSend);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(EmailToSend.SCHEMA_TYPE)));

		RMSchemasRecordsServices rmFromAnOtherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());
		List<EmailToSend> emailToSendFromAnOtherCollectionList = rmFromAnOtherCollection.searchEmailToSends(returnAll());

		EmailToSend emailToSendFromAnOtherCollection = emailToSendFromAnOtherCollectionList.get(0);
		List<MapStringListStringStructure> mapStringListStringStructureListFromAnOtherCollection = emailToSendFromAnOtherCollection.get(MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE);

		MapStringListStringStructure mapStringListStringStructureFromAnOtherCollection = emailToSendFromAnOtherCollection.get(MAP_STRING_LIST_STRING_STRUCTURE);

		List<String> listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection.get("list1");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Hello1", "Hello2", "Hello3");

		listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection.get("list2");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Sup1", "Sup2", "Sup3");

		// Assert MultiValued
		MapStringListStringStructure mapStringListStringStructureFromAnOtherCollection1 = mapStringListStringStructureListFromAnOtherCollection.get(0);
		MapStringListStringStructure mapStringListStringStructureFromAnOtherCollection2 = mapStringListStringStructureListFromAnOtherCollection.get(1);

		listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection1.get("list1");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Hello1", "Hello2", "Hello3");

		listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection1.get("list2");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Sup1", "Sup2", "Sup3");

		listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection2.get("list3");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Sup1", "Sup2", "Sup3");
	}


	@Test
	public void whenExportingAndImportingEmailAddress() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withTasksModule(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users).withTasksModule());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		EmailToSend emailToSend = rm.newEmailToSend();

		EmailAddress emailAddress1 = new EmailAddress();
		emailAddress1.setEmail(EMAIL_1);
		emailAddress1.setName(NAME_1);

		EmailAddress emailAddress2 = new EmailAddress();
		emailAddress2.setName(NAME_2);
		emailAddress2.setEmail(EMAIL_2);


		List<EmailAddress> emailAddressList = asList(emailAddress1, emailAddress2);

		emailToSend.setFrom(emailAddress1).setBCC(emailAddressList).setCC(emailAddressList).setTo(emailAddressList)
				.setSendOn(LOCAL_DATE_TIME).setSubject(EMAIL_SUBJECT).setTemplate(EMAIL_TEMPLATE);

		emailToSend.setCreatedOn(LOCAL_DATE_TIME);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		recordServices.add(emailToSend);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(EmailToSend.SCHEMA_TYPE)));

		RMSchemasRecordsServices rmFromAnOtherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		List<EmailToSend> emailToSendFromAnOtherCollectionList = rmFromAnOtherCollection.searchEmailToSends(returnAll());

		assertThat(emailToSendFromAnOtherCollectionList.size()).isEqualTo(1);

		EmailToSend emailToSendFromAnOtherCollection = emailToSendFromAnOtherCollectionList.get(0);

		assertThat(emailToSendFromAnOtherCollection.getFrom().getEmail()).isEqualTo(EMAIL_1);
		assertThat(emailToSendFromAnOtherCollection.getFrom().getName()).isEqualTo(NAME_1);

		assertEmailAddress(emailToSendFromAnOtherCollection.getBCC().get(0),
				emailToSendFromAnOtherCollection.getBCC().get(1));


		assertEmailAddress(emailToSendFromAnOtherCollection.getCC().get(0),
				emailToSendFromAnOtherCollection.getCC().get(1));


		assertEmailAddress(emailToSendFromAnOtherCollection.getTo().get(0),
				emailToSendFromAnOtherCollection.getTo().get(1));

		assertThat(emailToSend.getSendOn()).isEqualTo(LOCAL_DATE_TIME);
		assertThat(emailToSend.getCreatedOn()).isEqualTo(LOCAL_DATE_TIME);

		assertThat(emailToSend.getSubject()).isEqualTo(EMAIL_SUBJECT);
		assertThat(emailToSend.getTemplate()).isEqualTo(EMAIL_TEMPLATE);
	}

	private void assertEmailAddress(EmailAddress emailAddress1, EmailAddress emailAddress2) {
		assertThat(emailAddress1.getEmail()).isEqualTo(EMAIL_1);
		assertThat(emailAddress1.getName()).isEqualTo(NAME_1);
		assertThat(emailAddress2.getEmail()).isEqualTo(EMAIL_2);
		assertThat(emailAddress2.getName()).isEqualTo(NAME_2);
	}

	@Test
	public void whenExportingAndImportingTask() throws Exception {
		prepareSystem(
		withZeCollection().withConstellioRMModule().withAllTest(users)
				.withRMTest(records).withTasksModule(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users).withTasksModule());

		TasksSchemasRecordsServices schemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RMTask rmTask = rm.newRMTask();
		rmTask.setTitle(TITLE);
		rmTask.setAdministrativeUnit(records.getUnit10());

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		TaskStatus zeStatus = schemas.newTaskStatus().setCode("zeStatus").setStatusType(IN_PROGRESS).setTitle("status title");

		recordServices.add(zeStatus);

		rmTask.setStatus(zeStatus.getId());

		TaskFollower taskFollower1 = new TaskFollower();
		taskFollower1.setFollowerId(records.getAdmin().getId());
		taskFollower1.setFollowTaskDeleted(true);
		taskFollower1.setFollowTaskCompleted(true);
		taskFollower1.setFollowSubTasksModified(true);
		taskFollower1.setFollowTaskAssigneeModified(true);
		taskFollower1.setFollowTaskStatusModified(true);

		TaskFollower taskFollower2 = new TaskFollower();
		taskFollower2.setFollowerId(records.getDakota_managerInA_userInB().getId());

		rmTask.setTaskFollowers(asList(taskFollower1, taskFollower2));

		TaskReminder reminders1 = new TaskReminder();
		reminders1.setNumberOfDaysToRelativeDate(5);
		reminders1.setBeforeRelativeDate(false);
		reminders1.setProcessed(true);
		reminders1.setFixedDate(LocalDate.parse("2017-05-17"));
		reminders1.setRelativeDateMetadataCode(RMTask.DUE_DATE);

		TaskReminder reminders2 = new TaskReminder();
		reminders1.setNumberOfDaysToRelativeDate(7);
		reminders1.setBeforeRelativeDate(false);
		reminders1.setProcessed(false);
		reminders1.setFixedDate(LocalDate.parse("2017-05-16"));
		reminders1.setFixedDate(LocalDate.parse("2017-05-16"));
		reminders2.setRelativeDateMetadataCode(RMTask.START_DATE);

		rmTask.setReminders(asList(reminders1,reminders2));

		recordServices.add(rmTask);

		List<RMTask> zeCollectionTasks = rm.searchRMTasks(returnAll());

		assertThat(zeCollectionTasks).hasSize(1);

		List<Tuple> exportedTasksIds = new ArrayList<>();
		List<List<TaskReminder>> exportedReminders = new ArrayList<>();
		List<List<TaskFollower>> exportedFollowers = new ArrayList<>();
		for(Task task: zeCollectionTasks) {
			exportedTasksIds.add(new Tuple(task.getId()));
			exportedReminders.add(task.getReminders());
			exportedFollowers.add(task.getTaskFollowers());
		}

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		List<RMTask> listSearchTask = rmAnotherCollection.searchRMTasks(returnAll());

		assertThat(listSearchTask).hasSize(0);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(AdministrativeUnit.SCHEMA_TYPE, TaskStatus.SCHEMA_TYPE, RMTask.SCHEMA_TYPE)));

		listSearchTask = rmAnotherCollection.searchRMTasks(returnAll());

		assertThatRecords(listSearchTask).extractingMetadatas(Schemas.LEGACY_ID.getLocalCode())
				.containsAll(exportedTasksIds);

		assertThat(listSearchTask.size()).isEqualTo(zeCollectionTasks.size());

		assertThatRecords(listSearchTask).is((Condition<? super List<Object>>) containingAllReminders(exportedReminders));
		assertThatRecords(listSearchTask).is((Condition<? super List<Object>>) containingAllFollowers(exportedFollowers));
	}

	@Test
	public void whenExportingAndImportingReport() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));


		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		final String TITLE = "title";
		final String SCHEMA_TYPE_CODE = Document.DEFAULT_SCHEMA;

		Report report = rm.newReport();
		report.setColumnsCount(1);
		report.setLinesCount(1);
		report.setTitle(TITLE);
		report.setSchemaTypeCode(SCHEMA_TYPE_CODE);

		ReportedMetadata reportedMetadata1 = new ReportedMetadata("title", 1);
		reportedMetadata1.setYPosition(2);
		ReportedMetadata reportedMetadata2 = new ReportedMetadata("id", 3);
		reportedMetadata2.setYPosition(4);

		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();
		reportedMetadataList.add(reportedMetadata1);
		reportedMetadataList.add(reportedMetadata2);

		report.setReportedMetadata(reportedMetadataList);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();

		transaction.add(report);

		recordServices.execute(transaction);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(Report.SCHEMA_TYPE)));

		List<Record> listRecordReport;

		RMSchemasRecordsServices rmFromAnOtherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());
		MetadataSchemasManager schemasManager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchema metadataSchemaTypes = schemasManager.getSchemaTypes("anotherCollection").getSchema(Report.DEFAULT_SCHEMA);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(metadataSchemaTypes).returnAll());

		listRecordReport = getModelLayerFactory().newSearchServices().search(query);

		assertThatRecords(listRecordReport).extractingMetadatas("columnsCount", "title",
				"linesCount", "schemaTypeCode").contains(tuple(1.0, TITLE, 1.0, SCHEMA_TYPE_CODE));

		Record record = listRecordReport.get(0);
		Report reportFromAnOtherCollection = rmFromAnOtherCollection.wrapReport(record);

		assertThat(reportFromAnOtherCollection.getReportedMetadata().get(0).getXPosition()).isEqualTo(1);
		assertThat(reportFromAnOtherCollection.getReportedMetadata().get(0).getYPosition()).isEqualTo(2);
		assertThat(reportFromAnOtherCollection.getReportedMetadata().get(1).getXPosition()).isEqualTo(3);
		assertThat(reportFromAnOtherCollection.getReportedMetadata().get(1).getYPosition()).isEqualTo(4);
	}


	@Test
	public void whenExportingAndImportingSameSystemDecommissionList()
	{
		prepareSystem(
			withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users)
					.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList());

		RMSchemasRecordsServices rmZeCollection = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		List<DecommissioningList> exportedDecommissiongLists = rmZeCollection.searchDecommissioningLists(returnAll());
		List<Tuple> exportedDecommissiongListsIds = new ArrayList<>();
		List<List<DecomListFolderDetail>> exportedDecommissiongListsFolderDetailss = new ArrayList<>();
		List<List<DecomListContainerDetail>> exportedDecommissiongListsContainerDetailss = new ArrayList<>();
		List<List<DecomListValidation>> exportedDecommissiongListsValidations = new ArrayList<>();
		List<DecommissioningList> decommissioningListList = new ArrayList<>();
		for(DecommissioningList decommissioningList: exportedDecommissiongLists) {
			decommissioningListList.add(decommissioningList);
			exportedDecommissiongListsIds.add(new Tuple(decommissioningList.getId()));
			exportedDecommissiongListsFolderDetailss.add(decommissioningList.getFolderDetails());
			exportedDecommissiongListsContainerDetailss.add(decommissioningList.getContainerDetails());
			exportedDecommissiongListsValidations.add(decommissioningList.getValidations());
		}



		RecordExportOptions recordExportOptions = options.setExportedSchemaTypes(asList(
						DecommissioningList.SCHEMA_TYPE)).setForSameSystem(true);

		File file = exportToZip(recordExportOptions);

		// Delete rows.
		RecordServices recordService = getModelLayerFactory().newRecordServices();

		for(DecommissioningList decommissioningList: decommissioningListList) {
			recordService.logicallyDelete(decommissioningList.getWrappedRecord(), User.GOD);
			recordService.physicallyDelete(decommissioningList.getWrappedRecord(), User.GOD);
		}

		importFromZip(file, zeCollection);


		List<DecommissioningList> listSearchDecommissiongList = rmZeCollection.searchDecommissioningLists(returnAll());

		assertThatRecords(listSearchDecommissiongList).extractingMetadatas(Schemas.LEGACY_ID.getLocalCode())
				.contains(exportedDecommissiongListsIds.toArray(new Tuple[0]));

		assertThat(listSearchDecommissiongList.size()).isEqualTo(exportedDecommissiongLists.size());

		assertThatRecords(listSearchDecommissiongList).is((Condition<? super List<Object>>) containingAllFolderDetails(exportedDecommissiongListsFolderDetailss));
		assertThatRecords(listSearchDecommissiongList).is((Condition<? super List<Object>>) containingAllContainerDetails(exportedDecommissiongListsContainerDetailss));
		assertThatRecords(listSearchDecommissiongList).is((Condition<? super List<Object>>) containingAllValidations(exportedDecommissiongListsValidations));
	}

	@Test
	public void whenExportingAndImportingDecommissionList()
	{
 		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rmZeCollection = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		List<DecommissioningList> exportedDecommissiongLists = rmZeCollection.searchDecommissioningLists(returnAll());
		List<Tuple> exportedDecommissiongListsIds = new ArrayList<>();
		List<List<DecomListFolderDetail>> exportedDecommissiongListsFolderDetailss = new ArrayList<>();
		List<List<DecomListContainerDetail>> exportedDecommissiongListsContainerDetailss = new ArrayList<>();
		List<List<DecomListValidation>> exportedDecommissiongListsValidations = new ArrayList<>();
		for(DecommissioningList decommissioningList: exportedDecommissiongLists) {
			exportedDecommissiongListsIds.add(new Tuple(decommissioningList.getId()));
			exportedDecommissiongListsFolderDetailss.add(decommissioningList.getFolderDetails());
			exportedDecommissiongListsContainerDetailss.add(decommissioningList.getContainerDetails());
			exportedDecommissiongListsValidations.add(decommissioningList.getValidations());
		}

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		List<DecommissioningList> listSearchDecommissiongList = rmAnotherCollection.searchDecommissioningLists(returnAll());

		assertThat(listSearchDecommissiongList).hasSize(0);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(AdministrativeUnit.SCHEMA_TYPE, Document.SCHEMA_TYPE, DocumentType.SCHEMA_TYPE,
						Folder.SCHEMA_TYPE,	DecommissioningList.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE,
						Category.SCHEMA_TYPE, MediumType.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE,
						ContainerRecordType.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE, User.SCHEMA_TYPE, Group.SCHEMA_TYPE)));

		listSearchDecommissiongList = rmAnotherCollection.searchDecommissioningLists(returnAll());

		assertThatRecords(listSearchDecommissiongList).extractingMetadatas(Schemas.LEGACY_ID.getLocalCode())
				.contains(exportedDecommissiongListsIds.toArray(new Tuple[0]));

		assertThat(listSearchDecommissiongList.size()).isEqualTo(exportedDecommissiongLists.size());

		assertThatRecords(listSearchDecommissiongList).is((Condition<? super List<Object>>) containingAllFolderDetails(exportedDecommissiongListsFolderDetailss));
		assertThatRecords(listSearchDecommissiongList).is((Condition<? super List<Object>>) containingAllContainerDetails(exportedDecommissiongListsContainerDetailss));
		assertThatRecords(listSearchDecommissiongList).is((Condition<? super List<Object>>) containingAllValidations(exportedDecommissiongListsValidations));
	}

	@Test
	public void whenExportingAndImportingComment() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));
		final String MESSAGE = "Message";
		final User user = records.getAdmin();

		Comment comment = new Comment();
		comment.setUser(records.getAdmin());
		comment.setMessage(MESSAGE);


		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();

		Category category = records.getCategory_X().setComments(asList(comment));
		transaction.update(category.getWrappedRecord());

		recordServices.execute(transaction);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(User.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE,
						Category.SCHEMA_TYPE, Group.SCHEMA_TYPE)));


		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		Category categoryFromAnOtherCollection = rmAnotherCollection.getCategoryWithCode("X");

		assertThat(categoryFromAnOtherCollection.getComments().size()).isEqualTo(1);

		Comment commentFromAnOtherCollection = categoryFromAnOtherCollection.getComments().get(0);
		assertThat(commentFromAnOtherCollection.getMessage()).isEqualTo(MESSAGE);
		assertThat(commentFromAnOtherCollection.getUsername()).isEqualTo(user.getUsername());
	}

	@Test
	public void whenExportingAndImportingSingleValueComment() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));
		final String MESSAGE = "Message";
		final User user = records.getAdmin();
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Category.SCHEMA_TYPE).getMetadata(Category.COMMENTS).setMultivalue(false);
			}
		});
		getModelLayerFactory().getMetadataSchemasManager().modify("anotherCollection", new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Category.SCHEMA_TYPE).getMetadata(Category.COMMENTS).setMultivalue(false);
			}
		});

		Comment comment = new Comment();
		comment.setUser(records.getAdmin());
		comment.setMessage(MESSAGE);


		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();

		Category category = records.getCategory_X();
		category.getWrappedRecord().set(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getMetadata(Category.DEFAULT_SCHEMA+"_"+Category.COMMENTS), comment);
		transaction.update(category.getWrappedRecord());

		recordServices.execute(transaction);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(asList(User.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE,
						Category.SCHEMA_TYPE, Group.SCHEMA_TYPE)));

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		Category categoryFromAnOtherCollection = rmAnotherCollection.getCategoryWithCode("X");

		Comment commentFromAnOtherCollection = categoryFromAnOtherCollection.get(Category.COMMENTS);
		assertThat(commentFromAnOtherCollection.getMessage()).isEqualTo(MESSAGE);
		assertThat(commentFromAnOtherCollection.getUsername()).isEqualTo(user.getUsername());
	}

	@Test
	public void whenExportingSpecificExportValueLists() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(
				options.setExportValueLists(true));

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		assertThatRecords(rmAnotherCollection.searchDocumentTypes(ALL)).extractingMetadatas("legacyIdentifier", "code", "title")
				.contains(
						tuple("documentTypeId_1", "1", "Livre de recettes"), tuple("documentTypeId_2", "2", "Typologie"),
						tuple("documentTypeId_3", "3", "Petit guide"), tuple("documentTypeId_4", "4", "Histoire"),
						tuple("documentTypeId_5", "5", "Calendrier des réunions"), tuple("documentTypeId_6", "6",
								"Dossier de réunion : avis de convocation, ordre du jour, procès-verbal, extraits de procès-verbaux, résolutions, documents déposés, correspondance"),
						tuple("documentTypeId_7", "7", "Notes de réunion"), tuple("documentTypeId_8", "8",
								"Dossiers des administrateurs : affirmations solennelles, serments de discrétion"),
						tuple("documentTypeId_9", "9", "Contrat"), tuple("documentTypeId_10", "10", "Procès-verbal"));

	}



	@Test
	public void whenExportingFolderRetentionRuleThenExported()
			throws Exception {
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RetentionRule retentionRule = rm.newRetentionRule();

		Transaction transaction = new Transaction();

		ArrayList<CopyRetentionRule> arrayList = new ArrayList<>();

		CopyRetentionRule copyRetentionRule1 = getPrimaryCopyRetentionRule();
		CopyRetentionRule copyRetentionRule2 = getCopySecondaryRetentionRule();

		arrayList.add(copyRetentionRule1);
		arrayList.add(copyRetentionRule2);

		retentionRule.setTitle(TITLE);
		retentionRule.setCode(CODE);
		retentionRule.setResponsibleAdministrativeUnits(true);


		retentionRule.setCopyRetentionRules(arrayList);

		RecordServices recordService = getModelLayerFactory().newRecordServices();

		transaction.add(retentionRule);

		recordService.execute(transaction);

		// GetCopyRetentionRule.
		// Save avec une transaction.

		// Category.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE
		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(
						asList(AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE)));

		RMSchemasRecordsServices rmAnOtherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		RetentionRule currentRetentionRule = rmAnOtherCollection.getRetentionRuleWithCode(CODE);

		List<CopyRetentionRule> retentionRuleList = currentRetentionRule.getCopyRetentionRules();

		// Test primary rententionRule.

		assertPrincipalCopyRetentionRule(retentionRuleList.get(0));
		assertSecondaryCopyRetentionRule(retentionRuleList.get(1));


		assertThatRecords(rmAnOtherCollection.searchAdministrativeUnits(ALL)).extractingMetadatas("code", "title", "parent.code")
				.containsOnly(
						tuple("10A", "Unité 10-A", "10"), tuple("11B", "Unité 11-B", "11"), tuple("11", "Unité 11", "10"),
						tuple("12", "Unité 12", "10"), tuple("20", "Unité 20", null), tuple("30", "Unité 30", null),
						tuple("10", "Unité 10", null), tuple("30C", "Unité 30-C", "30"), tuple("12B", "Unité 12-B", "12"),
						tuple("12C", "Unité 12-C", "12"), tuple("20D", "Unité 20-D", "20"), tuple("20E", "Unité 20-E", "20")
				);

	}

	@Test
	public void whenExportingDocumentRetentionRuleThenExported() throws RecordServicesException {
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RetentionRule retentionRule = rm.newRetentionRule();

		Transaction transaction = new Transaction();

		ArrayList<CopyRetentionRule> arrayList = new ArrayList<>();

		CopyRetentionRule copyRetentionRule1 = getPrimaryCopyRetentionRule();
		CopyRetentionRule copyRetentionRule2 = getCopySecondaryRetentionRule();

		CopyRetentionRule copyRetentionRule3 = getPrimaryCopyRetentionRule();

		copyRetentionRule3.setTypeId(records.documentTypeId_1);

		arrayList.add(copyRetentionRule1);
		arrayList.add(copyRetentionRule2);

		retentionRule.setTitle(TITLE);
		retentionRule.setCode(CODE);
		retentionRule.setResponsibleAdministrativeUnits(true);

		retentionRule.setDocumentCopyRetentionRules(copyRetentionRule3);

		retentionRule.setScope(RetentionRuleScope.DOCUMENTS);

		retentionRule.setPrincipalDefaultDocumentCopyRetentionRule(copyRetentionRule1);
		retentionRule.setSecondaryDefaultDocumentCopyRetentionRule(copyRetentionRule2);

		RecordServices recordService = getModelLayerFactory().newRecordServices();
		transaction.add(retentionRule);

		recordService.execute(transaction);

		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(
						asList(AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE)));

		RMSchemasRecordsServices rmAnOtherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		RetentionRule currentRetentionRule = rmAnOtherCollection.getRetentionRuleWithCode(CODE);

		assertPrincipalCopyRetentionRule(currentRetentionRule.getPrincipalDefaultDocumentCopyRetentionRule());
		assertSecondaryCopyRetentionRule(currentRetentionRule.getSecondaryDefaultDocumentCopyRetentionRule());

		assertPrincipalCopyRetentionRule(currentRetentionRule.getDocumentCopyRetentionRules().get(0));

		assertThat(currentRetentionRule.getDocumentCopyRetentionRules()).isNotNull();
	}

	private CopyRetentionRule getPrimaryCopyRetentionRule() {
		return new CopyRetentionRule().setCopyType(CopyType.PRINCIPAL).setCode(CODE)
                    .setTitle(TITLE)
                    .setDescription(DESCRIPTION).setContentTypesComment(CONTENT_TYPES_COMMENT)
                    .setActiveRetentionComment(ACTIVE_RETENTION_COMMENT)
                    .setActiveRetentionPeriod(ACTIVE_RETENTION_PERIOD).setSemiActiveRetentionComment(SEMI_ACTIVE_RETENTION_COMMENT)
                    .setSemiActiveRetentionPeriod(SEMI_ACTIVE_RETENTION_PERIOD)
                    .setInactiveDisposalComment(INACTIVE_DISPOSAL_COMMENT).setInactiveDisposalType(INACTIVE_DISPOSAL_TYPE)
                    .setOpenActiveRetentionPeriod(OPEN_ACTIVE_RETENTION_PERIOD)
                    .setEssential(REQUIRED_COPYRULE_FIELD).setId(SET_ID).setMediumTypeIds(records.PA_MD).setIgnoreActivePeriod(false);
	}

	private CopyRetentionRule getCopySecondaryRetentionRule() {
		return new CopyRetentionRule().setCopyType(CopyType.SECONDARY).setCode(CODE)
                    .setTitle(TITLE)
                    .setDescription(DESCRIPTION).setContentTypesComment(CONTENT_TYPES_COMMENT)
                    .setActiveRetentionComment(ACTIVE_RETENTION_COMMENT)
                    .setActiveRetentionPeriod(ACTIVE_RETENTION_PERIOD).setSemiActiveRetentionComment(SEMI_ACTIVE_RETENTION_COMMENT)
                    .setSemiActiveRetentionPeriod(SEMI_ACTIVE_RETENTION_PERIOD)
                    .setInactiveDisposalComment(INACTIVE_DISPOSAL_COMMENT).setInactiveDisposalType(INACTIVE_DISPOSAL_TYPE)
                    .setOpenActiveRetentionPeriod(OPEN_ACTIVE_RETENTION_PERIOD)
                    .setEssential(REQUIRED_COPYRULE_FIELD).setId(SET_ID).setMediumTypeIds(records.PA_MD).setIgnoreActivePeriod(true);
	}

	public void assertSecondaryCopyRetentionRule(CopyRetentionRule copyRetentionRule) {

		// Test secondary rententionRule.

		assertThat(copyRetentionRule.getCopyType()).isEqualTo(CopyType.SECONDARY);
		assertThat(copyRetentionRule.getCode()).isEqualTo(CODE);
		assertThat(copyRetentionRule.getTitle()).isEqualTo(TITLE);
		assertThat(copyRetentionRule.getDescription()).isEqualTo(DESCRIPTION);
		assertThat(copyRetentionRule.getContentTypesComment()).isEqualTo(CONTENT_TYPES_COMMENT);
		assertThat(copyRetentionRule.getActiveRetentionPeriod()).isEqualTo(ACTIVE_RETENTION_PERIOD);
		assertThat(copyRetentionRule.getSemiActiveRetentionComment()).isEqualTo(SEMI_ACTIVE_RETENTION_COMMENT);
		assertThat(copyRetentionRule.getSemiActiveRetentionPeriod()).isEqualTo(SEMI_ACTIVE_RETENTION_PERIOD);
		assertThat(copyRetentionRule.getInactiveDisposalComment()).isEqualTo(INACTIVE_DISPOSAL_COMMENT);
		assertThat(copyRetentionRule.getInactiveDisposalType()).isEqualTo(INACTIVE_DISPOSAL_TYPE);
		assertThat(copyRetentionRule.getActiveRetentionPeriod()).isEqualTo(ACTIVE_RETENTION_PERIOD);
		assertThat(copyRetentionRule.isEssential()).isEqualTo(REQUIRED_COPYRULE_FIELD);
		assertThat(copyRetentionRule.getId()).isEqualTo(SET_ID);
		assertThat(copyRetentionRule.getMediumTypeIds()).isNotNull();
		assertThat(copyRetentionRule.getTitle()).isEqualTo(TITLE);
		assertThat(copyRetentionRule.getCode()).isEqualTo(CODE);
		assertThat(copyRetentionRule.getTypeId()).isNull();
		assertThat(copyRetentionRule.isIgnoreActivePeriod()).isTrue();
	}

	public void assertPrincipalCopyRetentionRule(CopyRetentionRule copyRetentionRule) {
		assertThat(copyRetentionRule.getCopyType()).isEqualTo(CopyType.PRINCIPAL);
		assertThat(copyRetentionRule.getCode()).isEqualTo(CODE);
		assertThat(copyRetentionRule.getTitle()).isEqualTo(TITLE);
		assertThat(copyRetentionRule.getDescription()).isEqualTo(DESCRIPTION);
		assertThat(copyRetentionRule.getContentTypesComment()).isEqualTo(CONTENT_TYPES_COMMENT);
		assertThat(copyRetentionRule.getActiveRetentionPeriod()).isEqualTo(ACTIVE_RETENTION_PERIOD);
		assertThat(copyRetentionRule.getSemiActiveRetentionComment()).isEqualTo(SEMI_ACTIVE_RETENTION_COMMENT);
		assertThat(copyRetentionRule.getSemiActiveRetentionPeriod()).isEqualTo(SEMI_ACTIVE_RETENTION_PERIOD);
		assertThat(copyRetentionRule.getInactiveDisposalComment()).isEqualTo(INACTIVE_DISPOSAL_COMMENT);
		assertThat(copyRetentionRule.getInactiveDisposalType()).isEqualTo(INACTIVE_DISPOSAL_TYPE);
		assertThat(copyRetentionRule.getActiveRetentionPeriod()).isEqualTo(ACTIVE_RETENTION_PERIOD);
		assertThat(copyRetentionRule.isEssential()).isEqualTo(REQUIRED_COPYRULE_FIELD);
		assertThat(copyRetentionRule.getId()).isEqualTo(SET_ID);
		assertThat(copyRetentionRule.getMediumTypeIds()).isNotNull();
		assertThat(copyRetentionRule.isIgnoreActivePeriod()).isFalse();
	}

	private File exportToZip(RecordExportOptions optios)
	{
		return new RecordExportServices(getAppLayerFactory()).exportRecords(zeCollection, SDK_STREAM, options);
	}

	private void importFromZip(File zipFile, String collection) {
		ImportDataProvider importDataProvider = null;
		try {
			importDataProvider = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile);

			UserServices userServices = getModelLayerFactory().newUserServices();
			User user = userServices.getUserInCollection("admin", collection);
			BulkImportParams importParams = BulkImportParams.STRICT();
			LoggerBulkImportProgressionListener listener = new LoggerBulkImportProgressionListener();
			try {
				new RecordsImportServices(getModelLayerFactory()).bulkImport(importDataProvider, listener, user, importParams);
			} catch (ValidationException e) {
				e.printStackTrace();
				fail(StringUtils.join(i18n.asListOfMessages(e.getValidationErrors()), "\n"));

			}
		} finally {
			getIOLayerFactory().newIOServices().deleteQuietly(zipFile);
		}
	}

	private void exportThenImportInAnotherCollection(RecordExportOptions options) {
		File zipFile = new RecordExportServices(getAppLayerFactory()).exportRecords(zeCollection, SDK_STREAM, options);
		ImportDataProvider importDataProvider = null;
		try {
			importDataProvider = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile);

			UserServices userServices = getModelLayerFactory().newUserServices();
			User user = userServices.getUserInCollection("admin", "anotherCollection");
			BulkImportParams importParams = BulkImportParams.STRICT();
			LoggerBulkImportProgressionListener listener = new LoggerBulkImportProgressionListener();
			try {
				new RecordsImportServices(getModelLayerFactory()).bulkImport(importDataProvider, listener, user, importParams);
			} catch (ValidationException e) {
				e.printStackTrace();
				fail(StringUtils.join(i18n.asListOfMessages(e.getValidationErrors()), "\n"));

			}
		} finally {
			getIOLayerFactory().newIOServices().deleteQuietly(zipFile);
		}
	}

	private Condition<? super List<DecommissioningList>> containingAllFolderDetails(final List<List<DecomListFolderDetail>> comparator) {
		return new Condition<List<DecommissioningList>>() {
			@Override
			public boolean matches(List<DecommissioningList> comparedLists) {
				for(DecommissioningList list: comparedLists) {
					boolean wasFound = false;
					List<DecomListFolderDetail> folderDetails = list.getFolderDetails();
					for(List<DecomListFolderDetail> comparatorDetailList: comparator) {
						if(folderDetails.containsAll(comparatorDetailList)) {
							wasFound = true;
							break;
						}
					}

					if(!wasFound) {
						return false;
					}
				}
				return true;
			}
		}.describedAs("containing all folderDetails in " + comparator);
	}

	private Condition<? super List<DecommissioningList>> containingAllContainerDetails(final List<List<DecomListContainerDetail>> comparator) {
		return new Condition<List<DecommissioningList>>() {
			@Override
			public boolean matches(List<DecommissioningList> comparedLists) {
				for(DecommissioningList list: comparedLists) {
					boolean wasFound = false;
					List<DecomListContainerDetail> containerDetails = list.getContainerDetails();
					for(List<DecomListContainerDetail> comparatorDetailList: comparator) {
						if(containerDetails.containsAll(comparatorDetailList)) {
							wasFound = true;
							break;
						}
					}

					if(!wasFound) {
						return false;
					}
				}
				return true;
			}
		}.describedAs("containing all containerDetails in " + comparator);
	}

	private Condition<? super List<DecommissioningList>> containingAllValidations(final List<List<DecomListValidation>> comparator) {
		return new Condition<List<DecommissioningList>>() {
			@Override
			public boolean matches(List<DecommissioningList> comparedLists) {
				for(DecommissioningList list: comparedLists) {
					boolean wasFound = false;
					List<DecomListValidation> validations = list.getValidations();
					for(List<DecomListValidation> comparatorDetailList: comparator) {
						if(validations.containsAll(comparatorDetailList)) {
							wasFound = true;
							break;
						}
					}

					if(!wasFound) {
						return false;
					}
				}
				return true;
			}
		}.describedAs("containing all validations in " + comparator);
	}

	private Condition<? super List<Task>> containingAllReminders(final List<List<TaskReminder>> comparator) {
		return new Condition<List<Task>>() {
			@Override
			public boolean matches(List<Task> comparedTasks) {
				for(Task task: comparedTasks) {
					boolean wasFound = false;
					List<TaskReminder> reminders = task.getReminders();
					for(List<TaskReminder> comparatorReminders: comparator) {
						if(reminders.containsAll(comparatorReminders)) {
							wasFound = true;
							break;
						}
					}

					if(!wasFound) {
						return false;
					}
				}
				return true;
			}
		}.describedAs("containing all reminders in " + comparator);
	}

	private Condition<? super List<Task>> containingAllFollowers(final List<List<TaskFollower>> comparator) {
		return new Condition<List<Task>>() {
			@Override
			public boolean matches(List<Task> comparedTasks) {
				for(Task task: comparedTasks) {
					boolean wasFound = false;
					List<TaskFollower> followers = task.getTaskFollowers();
					for(List<TaskFollower> comparatorFollowers: comparator) {
						if(followers.containsAll(comparatorFollowers)) {
							wasFound = true;
							break;
						}
					}

					if(!wasFound) {
						return false;
					}
				}
				return true;
			}
		}.describedAs("containing all followers in " + comparator);
	}
}
