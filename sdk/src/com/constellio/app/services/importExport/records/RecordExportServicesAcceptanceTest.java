package com.constellio.app.services.importExport.records;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
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
import com.constellio.app.ui.pages.imports.ExportPresenterServices;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.entities.structures.MapStringStringStructureFactory;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.RecordsOfSchemaTypesIterator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.tasks.model.wrappers.TaskStatusType.IN_PROGRESS;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.returnAll;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
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
	public static final LocalDateTime LOCAL_DATE_TIME = new LocalDateTime();
	public static final String EMAIL_TEMPLATE = "Template";
	public static final String EMAIL_SUBJECT = "Subject";

	@Before
	public void setup() {

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
	public void whenExportingAndImportingMapStringStringStructure()
			throws Exception {
		final String MAP_STRING_STRING_STRUCTURE = "mapStringListStringStucture";
		final String MAP_STRING_STRING_STRUCTURE_MULTIVALUE = "mapStringListStringStructure";

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withTasksModule(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users).withTasksModule());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_STRING_STRUCTURE)
						.setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringStringStructureFactory.class);
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_STRING_STRUCTURE_MULTIVALUE)
						.setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringStringStructureFactory.class).setMultivalue(true);
			}
		});

		getModelLayerFactory().getMetadataSchemasManager().modify("anotherCollection", new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_STRING_STRUCTURE)
						.setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringStringStructureFactory.class);
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_STRING_STRUCTURE_MULTIVALUE)
						.setType(MetadataValueType.STRUCTURE)
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
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, EmailToSend.SCHEMA_TYPE)));

		RMSchemasRecordsServices rmFromAnOtherCollection = new RMSchemasRecordsServices("anotherCollection",
				getAppLayerFactory());
		List<EmailToSend> emailToSendFromAnOtherCollectionList = rmFromAnOtherCollection.searchEmailToSends(returnAll());

		EmailToSend emailToSendFromAnOtherCollection = emailToSendFromAnOtherCollectionList.get(0);
		List<MapStringStringStructure> mapStringStringStructureListFromAnOtherCollection = emailToSendFromAnOtherCollection
				.get(MAP_STRING_STRING_STRUCTURE_MULTIVALUE);

		MapStringStringStructure mapStringStringStructureFromAnOtherCollection = emailToSendFromAnOtherCollection
				.get(MAP_STRING_STRING_STRUCTURE);

		// Assert MultiValued
		MapStringStringStructure mapStringStringStructureFromAnOtherCollection1 = mapStringStringStructureListFromAnOtherCollection
				.get(0);
		MapStringStringStructure mapStringStringStructureFromAnOtherCollection2 = mapStringStringStructureListFromAnOtherCollection
				.get(1);

		assertThat(mapStringStringStructureFromAnOtherCollection2.get("key4")).isEqualTo("value4");
		assertThat(mapStringStringStructureFromAnOtherCollection2.get("key3")).isEqualTo("value3");
		assertThat(mapStringStringStructureFromAnOtherCollection1.get("key2")).isEqualTo("value2");
		assertThat(mapStringStringStructureFromAnOtherCollection1.get("key1")).isEqualTo("value1");

		assertThat(mapStringStringStructureFromAnOtherCollection.get("key1")).isEqualTo("value1");
		assertThat(mapStringStringStructureFromAnOtherCollection.get("key2")).isEqualTo("value2");
	}

	@Test
	public void whenExportingAndImportingMapStringListStringStructure()
			throws Exception {
		final String MAP_STRING_LIST_STRING_STRUCTURE = "mapStringListStringStucture";
		final String MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE = "mapStringListStringStructure";

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withTasksModule(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users).withTasksModule());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_LIST_STRING_STRUCTURE)
						.setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringListStringStructureFactory.class);
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE)
						.setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringListStringStructureFactory.class).setMultivalue(true);
			}
		});

		getModelLayerFactory().getMetadataSchemasManager().modify("anotherCollection", new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_LIST_STRING_STRUCTURE)
						.setType(MetadataValueType.STRUCTURE)
						.defineStructureFactory(MapStringListStringStructureFactory.class);
				types.getSchema(EmailToSend.DEFAULT_SCHEMA).create(MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE)
						.setType(MetadataValueType.STRUCTURE)
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
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(EmailToSend.SCHEMA_TYPE)))));

		RMSchemasRecordsServices rmFromAnOtherCollection = new RMSchemasRecordsServices("anotherCollection",
				getAppLayerFactory());
		List<EmailToSend> emailToSendFromAnOtherCollectionList = rmFromAnOtherCollection.searchEmailToSends(returnAll());

		EmailToSend emailToSendFromAnOtherCollection = emailToSendFromAnOtherCollectionList.get(0);
		List<MapStringListStringStructure> mapStringListStringStructureListFromAnOtherCollection = emailToSendFromAnOtherCollection
				.get(MAP_STRING_LIST_STRING_STRUCTURE_MULTIVALUE);

		MapStringListStringStructure mapStringListStringStructureFromAnOtherCollection = emailToSendFromAnOtherCollection
				.get(MAP_STRING_LIST_STRING_STRUCTURE);

		List<String> listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection.get("list1");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Hello1", "Hello2", "Hello3");

		listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection.get("list2");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Sup1", "Sup2", "Sup3");

		// Assert MultiValued
		MapStringListStringStructure mapStringListStringStructureFromAnOtherCollection1 = mapStringListStringStructureListFromAnOtherCollection
				.get(0);
		MapStringListStringStructure mapStringListStringStructureFromAnOtherCollection2 = mapStringListStringStructureListFromAnOtherCollection
				.get(1);

		listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection1.get("list1");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Hello1", "Hello2", "Hello3");

		listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection1.get("list2");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Sup1", "Sup2", "Sup3");

		listStringFromAnOtherCollection = mapStringListStringStructureFromAnOtherCollection2.get("list3");
		assertThatRecords(listStringFromAnOtherCollection).containsOnly("Sup1", "Sup2", "Sup3");
	}

	@Test
	public void whenExportingAndImportingEmailAddress()
			throws Exception {
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
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(EmailToSend.SCHEMA_TYPE)))));

		RMSchemasRecordsServices rmFromAnOtherCollection = new RMSchemasRecordsServices("anotherCollection",
				getAppLayerFactory());

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

	private ContentVersionDataSummary upload(String resource) {
		InputStream inputStream = DemoTestRecords.class.getResourceAsStream("RMTestRecords_" + resource);
		return getModelLayerFactory().getContentManager().upload(inputStream);
	}

	@Test
	public void whenExportingAndImportingTask()
			throws Exception {
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

		rmTask.setReminders(asList(reminders1, reminders2));

		recordServices.add(rmTask);

		List<RMTask> zeCollectionTasks = rm.searchRMTasks(returnAll());

		assertThat(zeCollectionTasks).hasSize(1);

		List<Tuple> exportedTasksIds = new ArrayList<>();
		List<List<TaskReminder>> exportedReminders = new ArrayList<>();
		List<List<TaskFollower>> exportedFollowers = new ArrayList<>();
		for (Task task : zeCollectionTasks) {
			exportedTasksIds.add(new Tuple(task.getId()));
			exportedReminders.add(task.getReminders());
			exportedFollowers.add(task.getTaskFollowers());
		}

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		List<RMTask> listSearchTask = rmAnotherCollection.searchRMTasks(returnAll());

		assertThat(listSearchTask).hasSize(0);

		exportThenImportInAnotherCollection(
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (
						asList(AdministrativeUnit.SCHEMA_TYPE, TaskStatus.SCHEMA_TYPE, RMTask.SCHEMA_TYPE)))));

		listSearchTask = rmAnotherCollection.searchRMTasks(returnAll());

		assertThatRecords(listSearchTask).extractingMetadatas(Schemas.LEGACY_ID.getLocalCode())
				.containsAll(exportedTasksIds);

		assertThat(listSearchTask.size()).isEqualTo(zeCollectionTasks.size());

		assertThatRecords(listSearchTask).is((Condition<? super List<Object>>) containingAllReminders(exportedReminders));
		assertThatRecords(listSearchTask).is((Condition<? super List<Object>>) containingAllFollowers(exportedFollowers));
	}

	@Test
	public void whenExportingAndImportingReport()
			throws Exception {
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
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(Report.SCHEMA_TYPE)))));

		List<Record> listRecordReport;

		RMSchemasRecordsServices rmFromAnOtherCollection = new RMSchemasRecordsServices("anotherCollection",
				getAppLayerFactory());
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
	public void whenExportingAndImportingSameSystemReport()
			throws Exception {
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

		recordServices.add(report);

		MetadataSchemasManager schemasManager = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchema metadataSchemaTypes = schemasManager.getSchemaTypes(zeCollection).getSchema(Report.DEFAULT_SCHEMA);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(metadataSchemaTypes).returnAll());

		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		RecordExportOptions recordExportOptions = options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(
				Report.SCHEMA_TYPE)))).setForSameSystem(true);

		File file = exportToZip(recordExportOptions);

		List<Record> recordtoDelete = searchServices.search(query);

		recordServices.logicallyDelete(recordtoDelete.get(0), records.getAdmin());
		recordServices.physicallyDelete(recordtoDelete.get(0), records.getAdmin());

		importFromZip(file, zeCollection);

		List<Record> listRecordReport;

		listRecordReport = getModelLayerFactory().newSearchServices().search(query);

		assertThatRecords(listRecordReport).extractingMetadatas("columnsCount", "title",
				"linesCount", "schemaTypeCode").contains(tuple(1.0, TITLE, 1.0, SCHEMA_TYPE_CODE));

		Record record = listRecordReport.get(0);
		Report reportFromAnOtherCollection = rm.wrapReport(record);

		assertThat(reportFromAnOtherCollection.getReportedMetadata().get(0).getXPosition()).isEqualTo(1);
		assertThat(reportFromAnOtherCollection.getReportedMetadata().get(0).getYPosition()).isEqualTo(2);
		assertThat(reportFromAnOtherCollection.getReportedMetadata().get(1).getXPosition()).isEqualTo(3);
		assertThat(reportFromAnOtherCollection.getReportedMetadata().get(1).getYPosition()).isEqualTo(4);
	}

	@Test
	public void whenExportingAndImportingEvent() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withAllTest(users)
						.withRMTest(records).withEvents(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(Event.SCHEMA_TYPE, Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						MediumType.SCHEMA_TYPE, Category.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE,
						ContainerRecordType.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE, Document.SCHEMA_TYPE,
						DocumentType.SCHEMA_TYPE)))));

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		assertThatRecords(rmAnotherCollection.searchEvents(ALL)).extractingMetadatas("username", "title")
				.containsOnly(tuple("charles", "Abeille"),
						tuple("charles", null), tuple("charles", null),
						tuple("charles", null), tuple("charles", "Belette"),
						tuple("charles", "Bob 'Elvis' Gratton"), tuple("charles", "Chuck Norris"),
						tuple("charles", "Bob 'Elvis' Gratton"), tuple("charles", "Chuck Norris"),
						tuple("charles", "Gandalf Leblanc"), tuple("charles", "group1"),
						tuple("charles", "group2"), tuple("charles", "Aigle"),
						tuple("bob", null), tuple("dakota", null),
						tuple("bob", null), tuple("bob", "Aigle"),
						tuple("bob", "30_C_01"), tuple("bob", "Aigle"),
						tuple("charles", "Alouette"), tuple("admin", null),
						tuple("charles", null), tuple("charles", null));
	}


	@Test
	public void whenExportingAndImportingFolder()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Folder abeillefolderFromAnOtherCollection = null;
		Folder abeilleFolderFromZeCollection = null;

		List<Folder> folderList = rmSchemasRecordsServices.searchFolders(ALL);

		boolean isFound = false;

		for (Folder folderFromZeCollection : folderList) {
			isFound = false;
			if (folderFromZeCollection.getTitle().equals("Abeille")) {
				abeilleFolderFromZeCollection = folderFromZeCollection;
				isFound = true;
				break;
			}
		}

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		abeilleFolderFromZeCollection.setFormModifiedOn(new LocalDateTime());
		abeilleFolderFromZeCollection.setFormCreatedOn(new LocalDateTime());
		//abeilleFolderFromZeCollection.setFormCreatedBy(records.getAdmin());
		//abeilleFolderFromZeCollection.setFormModifiedBy(records.getAdmin());

		recordServices.update(abeilleFolderFromZeCollection.getWrappedRecord());

		if (!isFound) {
			fail("Folder abeille not found.");
		}

		exportThenImportInAnotherCollection(
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						MediumType.SCHEMA_TYPE, Category.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE,
						ContainerRecordType.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE, Document.SCHEMA_TYPE,
						DocumentType.SCHEMA_TYPE)))));

		SearchServices searchService = getModelLayerFactory().newSearchServices();

		RMSchemasRecordsServices rmSchemasRecordsServicesAnOtherCollection = new RMSchemasRecordsServices("anotherCollection",
				getAppLayerFactory());

		List<Folder> folderListFromAnOtherCollection = rmSchemasRecordsServicesAnOtherCollection.searchFolders(ALL);

		for (Folder folderFromAnOtherCollection : folderListFromAnOtherCollection) {
			isFound = false;

			for (Folder folderFromZeCollection : folderList) {

				if (folderFromAnOtherCollection.getTitle().equals(folderFromZeCollection.getTitle())) {
					if (folderFromAnOtherCollection.getTitle().equals("Abeille")) {
						abeillefolderFromAnOtherCollection = folderFromAnOtherCollection;
					}

					isFound = true;
					break;
				}
			}
			assertThat(isFound).isTrue();
		}

		assertThat(abeillefolderFromAnOtherCollection.getRetentionRule()).isNotNull();
		assertThat(abeillefolderFromAnOtherCollection.getArchivisticStatus())
				.isEqualTo(abeilleFolderFromZeCollection.getArchivisticStatus());
		assertThat(abeillefolderFromAnOtherCollection.getFormCreatedOn())
				.isEqualTo(abeilleFolderFromZeCollection.getFormCreatedOn().withMillisOfSecond(0));
		assertThat(abeillefolderFromAnOtherCollection.getFormModifiedOn())
				.isEqualTo(abeilleFolderFromZeCollection.getFormModifiedOn().withMillisOfSecond(0));
		//sertThat(abeillefolderFromAnOtherCollection.getFormCreatedBy()).isEqualTo(abeilleFolderFromZeCollection.getFormCreatedBy());
		//assertThat(abeillefolderFromAnOtherCollection.getFormModifiedBy()).isEqualTo(abeilleFolderFromZeCollection.getFormModifiedBy());
	}


	@Test
	public void whenExportingAndImportingFolderWithContainer()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Transaction tx = new Transaction();
		tx.add(records.getFolder_A01().setContainer(records.getContainerBac01()));
		tx.add(records.getContainerBac01().setLegacyId("zeGreatContainerId"));
		getModelLayerFactory().newRecordServices().execute(tx);

		Folder abeillefolderFromAnOtherCollection = null;
		Folder abeilleFolderFromZeCollection = null;

		List<Folder> folderList = rm.searchFolders(ALL);

		boolean isFound = false;

		for (Folder folderFromZeCollection : folderList) {
			isFound = false;
			if (folderFromZeCollection.getTitle().equals("Abeille")) {
				abeilleFolderFromZeCollection = folderFromZeCollection;
				isFound = true;
				break;
			}
		}

		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		abeilleFolderFromZeCollection.setFormModifiedOn(new LocalDateTime());
		abeilleFolderFromZeCollection.setFormCreatedOn(new LocalDateTime());
		//abeilleFolderFromZeCollection.setFormCreatedBy(records.getAdmin());
		//abeilleFolderFromZeCollection.setFormModifiedBy(records.getAdmin());

		recordServices.update(abeilleFolderFromZeCollection.getWrappedRecord());

		if (!isFound) {
			fail("Folder abeille not found.");
		}

		exportThenImportInAnotherCollection(
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						MediumType.SCHEMA_TYPE, Category.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE,
						ContainerRecordType.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE, Document.SCHEMA_TYPE,
						DocumentType.SCHEMA_TYPE)))));

		SearchServices searchService = getModelLayerFactory().newSearchServices();

		RMSchemasRecordsServices rmSchemasRecordsServicesAnOtherCollection = new RMSchemasRecordsServices("anotherCollection",
				getAppLayerFactory());

		List<Folder> folderListFromAnOtherCollection = rmSchemasRecordsServicesAnOtherCollection.searchFolders(ALL);

		for (Folder folderFromAnOtherCollection : folderListFromAnOtherCollection) {
			isFound = false;

			for (Folder folderFromZeCollection : folderList) {

				if (folderFromAnOtherCollection.getTitle().equals(folderFromZeCollection.getTitle())) {
					if (folderFromAnOtherCollection.getTitle().equals("Abeille")) {
						abeillefolderFromAnOtherCollection = folderFromAnOtherCollection;
					}

					isFound = true;
					break;
				}
			}
			assertThat(isFound).isTrue();
		}

		assertThat(abeillefolderFromAnOtherCollection.getContainer())
				.isNotEqualTo(records.containerId_bac01);
	}

	@Test
	public void whenExportingAndImportingFolderSameSystem()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		Toggle.ALLOWS_CREATION_OF_RECORDS_WITH_NON_PADDED_ID.enable();

		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		List<Folder> folderList = rmSchemasRecordsServices.searchFolders(returnAll());

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		int originalSize = folderList.size();

		RecordExportOptions recordExportOptions = options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(
				Folder.SCHEMA_TYPE)))).setForSameSystem(true);

		Folder updatedFolder = folderList.get(1);
		Folder deletedFolder = rmSchemasRecordsServices.getFolder("A05");

		updatedFolder.setFormCreatedOn(new LocalDateTime().plusYears(1000));
		updatedFolder.setFormModifiedOn(new LocalDateTime().plusYears(1000));

		recordServices.update(updatedFolder.getWrappedRecord(), records.getAdmin());

		LocalDateTime originalFormCreatedOn = updatedFolder.getFormCreatedOn();
		LocalDateTime originalFormModifiedOn = updatedFolder.getFormModifiedOn();
		String originalTitle = updatedFolder.getTitle();

		File file = exportToZip(recordExportOptions);

		recordServices.logicallyDelete(deletedFolder.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(deletedFolder.getWrappedRecord(), User.GOD);

		updatedFolder.setFormCreatedOn(new LocalDateTime().plusYears(50));
		updatedFolder.setFormModifiedOn(new LocalDateTime().plusYears(50));
		updatedFolder.setTitle("Some Title");
		recordServices.update(updatedFolder.getWrappedRecord(), records.getAdmin());

		importFromZip(file, zeCollection);

		folderList = rmSchemasRecordsServices.searchFolders(returnAll());

		SearchServices searchService = getModelLayerFactory().newSearchServices();

		assertThat(folderList.size()).isEqualTo(originalSize);
		assertThat(findRecordByTitle(deletedFolder.getTitle(), folderList)).isNotNull();
		Folder revertedFolder = (Folder) findRecordByTitle(originalTitle, folderList);

		assertThat(revertedFolder.getFormCreatedOn()).isEqualTo(originalFormCreatedOn.withMillisOfSecond(0));
		assertThat(revertedFolder.getFormModifiedOn()).isEqualTo(originalFormModifiedOn.withMillisOfSecond(0));
		assertThat(revertedFolder.getTitle()).isEqualTo(originalTitle);

		assertThat(revertedFolder.getRetentionRule()).isNotNull();
		assertThat(revertedFolder.getArchivisticStatus()).isEqualTo(updatedFolder.getArchivisticStatus());
	}

	private List<Document> getDocumentWithContent(List<Document> documentList) {
		List<Document> arrayListDocument = new ArrayList<>();

		for (Document document : documentList) {
			if (document.getContent() != null) {
				arrayListDocument.add(document);
			}
		}

		return arrayListDocument;
	}

	@Test
	public void whenExportingAndImportingDocument()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withDocumentsHavingContent().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Document abeilleDocumentFromAnOtherCollection = null;
		Document documentFromZeCollection = null;

		List<Document> documentList = rmSchemasRecordsServices.searchDocuments(ALL);

		List<Document> documentWithContentZeCollection = getDocumentWithContent(documentList);

		documentFromZeCollection = documentList.get(0);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		LocalDateTime zeDateTime = new LocalDateTime(2000, 1, 1, 1, 1);
		documentFromZeCollection.setCreatedOn(zeDateTime);
		documentFromZeCollection.setModifiedOn(zeDateTime.plusSeconds(1));
		documentFromZeCollection.setFormCreatedOn(zeDateTime.plusSeconds(2));
		documentFromZeCollection.setFormModifiedOn(zeDateTime.plusSeconds(3));

		String originalTitle = documentFromZeCollection.getTitle();

		recordServices.update(documentFromZeCollection.getWrappedRecord());

		exportThenImportInAnotherCollection(
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						MediumType.SCHEMA_TYPE, Category.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE,
						ContainerRecordType.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE, Document.SCHEMA_TYPE,
						DocumentType.SCHEMA_TYPE)))));

		RMSchemasRecordsServices rmSchemasRecordsServicesAnOtherCollection = new RMSchemasRecordsServices("anotherCollection",
				getAppLayerFactory());

		List<Document> documentListFromAnOtherCollection = rmSchemasRecordsServicesAnOtherCollection.searchDocuments(ALL);

		List<Document> documentListWithContentAnOtherCollection = getDocumentWithContent(documentListFromAnOtherCollection);

		assertThat(documentListWithContentAnOtherCollection.size()).isEqualTo(documentWithContentZeCollection.size());

		Document updatedDocument = (Document) findRecordByTitle(originalTitle, documentListFromAnOtherCollection);

		assertThat(documentListFromAnOtherCollection.size()).isEqualTo(documentList.size());

		for (Document documentZeCollection : documentList) {
			boolean found = false;

			for (Document documentAnOtherCollection : documentListFromAnOtherCollection) {
				if (documentAnOtherCollection.getTitle().equals(documentZeCollection.getTitle())) {
					found = true;
				}
			}

			assertThat(found).isTrue();
		}

		assertThat(updatedDocument.getCreatedOn()).isEqualTo(zeDateTime);
		assertThat(updatedDocument.getModifiedOn()).isEqualTo(zeDateTime.plusSeconds(1));
		assertThat(updatedDocument.getFormCreatedOn()).isEqualTo(zeDateTime.plusSeconds(2));
		assertThat(updatedDocument.getFormModifiedOn()).isEqualTo(zeDateTime.plusSeconds(3));
	}

	@Test
	public void whenExportingAndImportingDocumentSameSystem()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
						.withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		List<Document> documentList = rmSchemasRecordsServices.searchDocuments(returnAll());
		List<Document> documentWithContentZeCollection = getDocumentWithContent(documentList);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		int originalSize = documentList.size();

		RecordExportOptions recordExportOptions = options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(
				Document.SCHEMA_TYPE)))).setForSameSystem(true);

		Document updatedDocument = documentList.get(1);
		Document deletedDocument = documentList.get(0);

		updatedDocument.setFormCreatedOn(new LocalDateTime().plusYears(1000));
		updatedDocument.setFormModifiedOn(new LocalDateTime().plusYears(1000));

		recordServices.update(updatedDocument.getWrappedRecord(), records.getAdmin());

		LocalDateTime originalFormCreatedOn = updatedDocument.getFormCreatedOn();
		LocalDateTime originalFormModifiedOn = updatedDocument.getFormModifiedOn();
		String originalTitle = updatedDocument.getTitle();

		File file = exportToZip(recordExportOptions);

		recordServices.logicallyDelete(deletedDocument.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(deletedDocument.getWrappedRecord(), User.GOD);
		System.out.println(deletedDocument.getId());

		updatedDocument.setFormCreatedOn(new LocalDateTime().plusYears(50));
		updatedDocument.setFormModifiedOn(new LocalDateTime().plusYears(50));
		updatedDocument.setTitle("Some Title");
		recordServices.update(updatedDocument.getWrappedRecord(), records.getAdmin());

		importFromZip(file, zeCollection);

		List<Document> documentListFromAnOtherCollection = rmSchemasRecordsServices.searchDocuments(returnAll());

		List<Document> documentListWithContentAnOtherCollection = getDocumentWithContent(documentListFromAnOtherCollection);
		assertThat(documentListWithContentAnOtherCollection.size()).isEqualTo(documentWithContentZeCollection.size());

		assertThat(documentListFromAnOtherCollection.size()).isEqualTo(originalSize);
		assertThat(findRecordByTitle(deletedDocument.getTitle(), documentListFromAnOtherCollection)).isNotNull();
		Document revertedFolder = (Document) findRecordByTitle(originalTitle, documentListFromAnOtherCollection);

		assertThat(revertedFolder.getFormCreatedOn()).isEqualTo(originalFormCreatedOn.withMillisOfSecond(0));
		assertThat(revertedFolder.getFormModifiedOn()).isEqualTo(originalFormModifiedOn.withMillisOfSecond(0));
		assertThat(revertedFolder.getTitle()).isEqualTo(originalTitle);

		assertThat(revertedFolder.getRetentionRule()).isNotNull();
		assertThat(revertedFolder.getArchivisticStatus()).isEqualTo(updatedDocument.getArchivisticStatus());
	}

	private RecordWrapper findRecordByTitle(String title, List<? extends RecordWrapper> list) {
		RecordWrapper record = null;

		for (RecordWrapper currentRecord : list) {
			if (currentRecord.getTitle().equals(title)) {
				record = currentRecord;
				break;
			}
		}

		return record;
	}

	@Test
	public void whenExportingAndImportingDecommissionList()
			throws Exception {
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
		for (DecommissioningList decommissioningList : exportedDecommissiongLists) {
			exportedDecommissiongListsIds.add(new Tuple(decommissioningList.getId()));
			exportedDecommissiongListsFolderDetailss.add(decommissioningList.getFolderDetails());
			exportedDecommissiongListsContainerDetailss.add(decommissioningList.getContainerDetails());
			exportedDecommissiongListsValidations.add(decommissioningList.getValidations());
		}

		DecommissioningList decommissioningListWithContent = exportedDecommissiongLists.get(0);

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		List<DecommissioningList> listSearchDecommissiongList = rmAnotherCollection.searchDecommissioningLists(returnAll());

		assertThat(listSearchDecommissiongList).hasSize(0);
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		Content content = contentManager.createMajor(records.getAdmin(), "Contract.docx", records.upload("contrat.docx"));
		Content content2 = contentManager.createMajor(records.getAdmin(), "proces.docx", records.upload("proces.docx"));

		decommissioningListWithContent.setDocumentsReportContent(content);
		decommissioningListWithContent.setFoldersReportContent(content2);

		assertThat(listSearchDecommissiongList).hasSize(0);

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		recordServices.update(exportedDecommissiongLists.get(0));

		exportThenImportInAnotherCollection(
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (
						asList(AdministrativeUnit.SCHEMA_TYPE, Document.SCHEMA_TYPE, DocumentType.SCHEMA_TYPE,
								Folder.SCHEMA_TYPE, DecommissioningList.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE,
								Category.SCHEMA_TYPE, MediumType.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE,
								ContainerRecordType.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE, User.SCHEMA_TYPE, Group.SCHEMA_TYPE)))));

		listSearchDecommissiongList = rmAnotherCollection.searchDecommissioningLists(returnAll());

		List<DecommissioningList> decomListWithDocumentsReportContent = getDecomListWIthDocumentsReportContent(
				listSearchDecommissiongList);
		List<DecommissioningList> decomListWithFoldersReportContent = getDecomListWithFoldersReportContent(
				listSearchDecommissiongList);

		assertThat(decomListWithDocumentsReportContent).hasSize(1);
		assertThat(decomListWithFoldersReportContent).hasSize(1);

		assertThat(decomListWithDocumentsReportContent.get(0).getTitle()).isEqualTo(exportedDecommissiongLists.get(0).getTitle());
		assertThat(decomListWithFoldersReportContent.get(0).getTitle()).isEqualTo(exportedDecommissiongLists.get(0).getTitle());

		assertThat(decomListWithDocumentsReportContent.get(0).getDocumentsReportContent().getLastMajorContentVersion().getHash())
				.isEqualTo(
						exportedDecommissiongLists.get(0).getDocumentsReportContent().getLastMajorContentVersion().getHash());

		assertThat(decomListWithDocumentsReportContent.get(0).getFoldersReportContent().getLastMajorContentVersion().getHash())
				.isEqualTo(
						exportedDecommissiongLists.get(0).getFoldersReportContent().getLastMajorContentVersion().getHash());

		assertThat(
				decomListWithDocumentsReportContent.get(0).getDocumentsReportContent().getLastMajorContentVersion().getFilename())
				.isEqualTo(
						exportedDecommissiongLists.get(0).getDocumentsReportContent().getLastMajorContentVersion().getFilename());

		assertThat(
				decomListWithDocumentsReportContent.get(0).getFoldersReportContent().getLastMajorContentVersion().getFilename())
				.isEqualTo(
						exportedDecommissiongLists.get(0).getFoldersReportContent().getLastMajorContentVersion().getFilename());

		assertThatRecords(listSearchDecommissiongList).extractingMetadatas(Schemas.LEGACY_ID.getLocalCode())
				.contains(exportedDecommissiongListsIds.toArray(new Tuple[0]));

		assertThat(listSearchDecommissiongList.size()).isEqualTo(exportedDecommissiongLists.size());

		assertThatRecords(listSearchDecommissiongList)
				.is((Condition<? super List<Object>>) containingAllFolderDetails(exportedDecommissiongListsFolderDetailss));
		assertThatRecords(listSearchDecommissiongList)
				.is((Condition<? super List<Object>>) containingAllContainerDetails(exportedDecommissiongListsContainerDetailss));
		assertThatRecords(listSearchDecommissiongList)
				.is((Condition<? super List<Object>>) containingAllValidations(exportedDecommissiongListsValidations));
	}

	@Test
	public void whenExportingDeletingAndImportingSameSystemDecommissionList()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList());

		Toggle.ALLOWS_CREATION_OF_RECORDS_WITH_NON_PADDED_ID.enable();

		RMSchemasRecordsServices rmZeCollection = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		List<DecommissioningList> exportedDecommissiongLists = rmZeCollection.searchDecommissioningLists(returnAll());
		List<Tuple> exportedDecommissiongListsIds = new ArrayList<>();
		List<List<DecomListFolderDetail>> exportedDecommissiongListsFolderDetailss = new ArrayList<>();
		List<List<DecomListContainerDetail>> exportedDecommissiongListsContainerDetailss = new ArrayList<>();
		List<List<DecomListValidation>> exportedDecommissiongListsValidations = new ArrayList<>();
		List<DecommissioningList> decommissioningListList = new ArrayList<>();
		for (DecommissioningList decommissioningList : exportedDecommissiongLists) {
			decommissioningListList.add(decommissioningList);
			exportedDecommissiongListsIds.add(new Tuple(decommissioningList.getId()));
			exportedDecommissiongListsFolderDetailss.add(decommissioningList.getFolderDetails());
			exportedDecommissiongListsContainerDetailss.add(decommissioningList.getContainerDetails());
			exportedDecommissiongListsValidations.add(decommissioningList.getValidations());
		}

		RecordExportOptions recordExportOptions = options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(
				DecommissioningList.SCHEMA_TYPE)))).setForSameSystem(true);

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		Content content = contentManager.createMajor(records.getAdmin(), "Contract.docx", records.upload("contrat.docx"));
		Content content2 = contentManager.createMajor(records.getAdmin(), "proces.docx", records.upload("proces.docx"));

		DecommissioningList decommissioningListWithContent = exportedDecommissiongLists.get(0);

		decommissioningListWithContent.setDocumentsReportContent(content);
		decommissioningListWithContent.setFoldersReportContent(content2);

		RecordServices recordService = getModelLayerFactory().newRecordServices();

		recordService.update(decommissioningListWithContent);

		File file = exportToZip(recordExportOptions);

		// Delete rows.
		recordService.logicallyDelete(decommissioningListWithContent.getWrappedRecord(), User.GOD);
		recordService.physicallyDelete(decommissioningListWithContent.getWrappedRecord(), User.GOD);

		importFromZip(file, zeCollection);

		List<DecommissioningList> listSearchDecommissiongList = rmZeCollection.searchDecommissioningLists(returnAll());

		List<DecommissioningList> decomListWithDocumentsReportContent = getDecomListWIthDocumentsReportContent(
				listSearchDecommissiongList);
		List<DecommissioningList> decomListWithFoldersReportContent = getDecomListWithFoldersReportContent(
				listSearchDecommissiongList);

		assertThat(decomListWithDocumentsReportContent.get(0).getTitle()).isEqualTo(exportedDecommissiongLists.get(0).getTitle());
		assertThat(decomListWithFoldersReportContent.get(0).getTitle()).isEqualTo(exportedDecommissiongLists.get(0).getTitle());

		assertThat(decomListWithDocumentsReportContent.get(0).getDocumentsReportContent().getLastMajorContentVersion().getHash())
				.isEqualTo(
						exportedDecommissiongLists.get(0).getDocumentsReportContent().getLastMajorContentVersion().getHash());

		assertThat(decomListWithDocumentsReportContent.get(0).getFoldersReportContent().getLastMajorContentVersion().getHash())
				.isEqualTo(
						exportedDecommissiongLists.get(0).getFoldersReportContent().getLastMajorContentVersion().getHash());

		assertThat(
				decomListWithDocumentsReportContent.get(0).getDocumentsReportContent().getLastMajorContentVersion().getFilename())
				.isEqualTo(
						exportedDecommissiongLists.get(0).getDocumentsReportContent().getLastMajorContentVersion().getFilename());

		assertThat(
				decomListWithDocumentsReportContent.get(0).getFoldersReportContent().getLastMajorContentVersion().getFilename())
				.isEqualTo(
						exportedDecommissiongLists.get(0).getFoldersReportContent().getLastMajorContentVersion().getFilename());

		assertThatRecords(listSearchDecommissiongList).extractingMetadatas(Schemas.IDENTIFIER.getLocalCode())
				.contains(exportedDecommissiongListsIds.toArray(new Tuple[0]));

		assertThat(listSearchDecommissiongList.size()).isEqualTo(exportedDecommissiongLists.size());

		assertThatRecords(listSearchDecommissiongList)
				.is((Condition<? super List<Object>>) containingAllFolderDetails(exportedDecommissiongListsFolderDetailss));
		assertThatRecords(listSearchDecommissiongList)
				.is((Condition<? super List<Object>>) containingAllContainerDetails(exportedDecommissiongListsContainerDetailss));
		assertThatRecords(listSearchDecommissiongList)
				.is((Condition<? super List<Object>>) containingAllValidations(exportedDecommissiongListsValidations));
	}

	private List<DecommissioningList> getDecomListWIthDocumentsReportContent(List<DecommissioningList> documentList) {
		List<DecommissioningList> arrayListDocument = new ArrayList<>();

		for (DecommissioningList decommissioningList : documentList) {
			if (decommissioningList.getDocumentsReportContent() != null) {
				arrayListDocument.add(decommissioningList);
			}
		}

		return arrayListDocument;
	}

	private List<DecommissioningList> getDecomListWithFoldersReportContent(List<DecommissioningList> documentList) {
		List<DecommissioningList> arrayListDocument = new ArrayList<>();

		for (DecommissioningList decommissioningList : documentList) {
			if (decommissioningList.getFoldersReportContent() != null) {
				arrayListDocument.add(decommissioningList);
			}
		}

		return arrayListDocument;
	}

	@Test
	public void whenExportingAndImportingSameSystemCategory()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		Toggle.ALLOWS_CREATION_OF_RECORDS_WITH_NON_PADDED_ID.enable();

		final String MESSAGE = "Message";
		final User user = records.getAdmin();

		Comment comment = new Comment();
		comment.setUser(records.getAdmin());
		comment.setMessage(MESSAGE);

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		Transaction transaction = new Transaction();

		Category category = records.getCategory_X().setComments(asList(comment));
		List<Category> childCategories = rm
				.searchCategorys(where(Schemas.PATH_PARTS).isContainingText(records.categoryId_X));
		transaction.update(category.getWrappedRecord());

		recordServices.execute(transaction);

		RecordExportOptions recordExportOptions = options
				.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(Category.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE))))
				.setForSameSystem(true);

		File file = exportToZip(recordExportOptions);

		recordServices
				.physicallyDeleteNoMatterTheStatus(category.getWrappedRecord(), User.GOD, new RecordPhysicalDeleteOptions());

		importFromZip(file, zeCollection);

		Category categoryFromAnOtherCollection = rm.getCategory(records.categoryId_X);

		assertThat(categoryFromAnOtherCollection.getComments().size()).isEqualTo(1);

		Comment commentFromAnOtherCollection = categoryFromAnOtherCollection.getComments().get(0);
		assertThat(commentFromAnOtherCollection.getMessage()).isEqualTo(MESSAGE);
		assertThat(commentFromAnOtherCollection.getUsername()).isEqualTo(user.getUsername());
		List<Category> childCategories2 = rm
				.searchCategorys(where(Schemas.PATH_PARTS).isContainingText(records.categoryId_X));

		for (Category categorie1 : childCategories) {
			boolean isFound = false;

			for (Category categorie2 : childCategories2) {
				if (categorie1.getId().equals(categorie2.getId())) {
					isFound = true;
					break;
				}
			}

			assertThat(isFound).isTrue();

		}
	}

	@Test
	public void whenExportingAndImportingComment()
			throws Exception {
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
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(User.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE,
						Category.SCHEMA_TYPE, Group.SCHEMA_TYPE)))));

		RMSchemasRecordsServices rmAnotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		Category categoryFromAnOtherCollection = rmAnotherCollection.getCategoryWithCode("X");

		assertThat(categoryFromAnOtherCollection.getComments().size()).isEqualTo(1);

		Comment commentFromAnOtherCollection = categoryFromAnOtherCollection.getComments().get(0);
		assertThat(commentFromAnOtherCollection.getMessage()).isEqualTo(MESSAGE);
		assertThat(commentFromAnOtherCollection.getUsername()).isEqualTo(user.getUsername());
	}

	@Test
	public void whenExportingAndImportingSingleValueComment()
			throws Exception {
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
		category.getWrappedRecord().set(getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getMetadata(Category.DEFAULT_SCHEMA + "_" + Category.COMMENTS), comment);
		transaction.update(category.getWrappedRecord());

		recordServices.execute(transaction);

		exportThenImportInAnotherCollection(
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(User.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE,
						Category.SCHEMA_TYPE, Group.SCHEMA_TYPE)))));

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
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, DocumentType.SCHEMA_TYPE)));

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
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (
						asList(AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE)))));

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
	public void whenExportingFolderRetentionRuleInSameSystemThenExported()
			throws Exception {
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		RetentionRule retentionRule = rm.newRetentionRule();

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

		recordService.add(retentionRule);

		int retentioRuleListSize = rm.searchRetentionRules(ALL).size();

		// GetCopyRetentionRule.
		// Save avec une transaction.
		RecordExportOptions recordExportOptions = options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(
				RetentionRule.SCHEMA_TYPE)))).setForSameSystem(true);

		// Category.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE
		File file = exportToZip(recordExportOptions);

		recordService.logicallyDelete(retentionRule.getWrappedRecord(), records.getAdmin());
		recordService.physicallyDelete(retentionRule.getWrappedRecord(), records.getAdmin());

		importFromZip(file, zeCollection);

		RetentionRule currentRetentionRule = rm.getRetentionRuleWithCode(CODE);
		List<RetentionRule> listRetentionRule = rm.searchRetentionRules(ALL);
		List<CopyRetentionRule> retentionRuleList = currentRetentionRule.getCopyRetentionRules();

		// Test primary rententionRule.

		assertThat(listRetentionRule).hasSize(retentioRuleListSize);

		assertPrincipalCopyRetentionRule(retentionRuleList.get(0));
		assertSecondaryCopyRetentionRule(retentionRuleList.get(1));
	}

	@Test
	public void whenExportingDocumentRetentionRuleThenExported()
			throws RecordServicesException {
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
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (
						asList(AdministrativeUnit.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE)))));

		RMSchemasRecordsServices rmAnOtherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());

		RetentionRule currentRetentionRule = rmAnOtherCollection.getRetentionRuleWithCode(CODE);

		assertPrincipalCopyRetentionRule(currentRetentionRule.getPrincipalDefaultDocumentCopyRetentionRule());
		assertSecondaryCopyRetentionRule(currentRetentionRule.getSecondaryDefaultDocumentCopyRetentionRule());

		assertPrincipalCopyRetentionRule(currentRetentionRule.getDocumentCopyRetentionRules().get(0));

		assertThat(currentRetentionRule.getDocumentCopyRetentionRules()).isNotNull();
	}

	@Test
	public void whenExportingRecordWithCustomSchemaKeepsSchemaAndMetadataWhenImported()
			throws RecordServicesException {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus(),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		List<Document> documentList = rmSchemasRecordsServices.searchDocuments(returnAll());

		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		RecordExportOptions recordExportOptions = options
				.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, asList(Folder.SCHEMA_TYPE, AdministrativeUnit.SCHEMA_TYPE,
						MediumType.SCHEMA_TYPE, Category.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE,
						ContainerRecordType.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE, Document.SCHEMA_TYPE,
						DocumentType.SCHEMA_TYPE)));

		Document documentWithCustomSchema = documentList.get(0);
		documentWithCustomSchema.setType(rmSchemasRecordsServices.getDocumentTypeWithCode(DocumentType.EMAIL_DOCUMENT_TYPE));
		documentWithCustomSchema.changeSchemaTo(Email.SCHEMA);
		documentWithCustomSchema.set(Email.EMAIL_CC_TO, asList("jeff"));

		recordServices.update(documentWithCustomSchema.getWrappedRecord(), records.getAdmin());

		File file = exportToZip(recordExportOptions);

		importFromZip(file, "anotherCollection");

		RMSchemasRecordsServices anotherCollectionRM = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		documentList = anotherCollectionRM.searchDocuments(where(anotherCollectionRM.document_email.emailCCTo())
				.isNotNull());//where(Schemas.LEGACY_ID).isEqualTo(documentWithCustomSchema.getId()));
		Document importedEmail = documentList.get(0);

		assertThat(documentList).hasSize(1);
		assertThatRecords(importedEmail)
				.extractingMetadatas(Schemas.SCHEMA.getLocalCode(), Document.TYPE + ".code", Email.EMAIL_CC_TO).containsOnly(
				tuple(Email.SCHEMA, DocumentType.EMAIL_DOCUMENT_TYPE, asList("jeff"))
		);
	}


	@Test
	public void whenExportingAuthorizationsThenCanBeImportedInOtherSystem() throws Exception {
		final String ANOTHER_COLLECTION = "anotherCollection";
		prepareSystem(
				withZeCollection().withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withAllTest(users)
						.withRMTest(records).withFoldersAndContainersOfEveryStatus(),
				withCollection(ANOTHER_COLLECTION).withConstellioRMModule().withAllTest(users));
		RMSchemasRecordsServices rmSchemasRecordsServicesSourceCollection = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RMSchemasRecordsServices rmOfTargetCollection = new RMSchemasRecordsServices(ANOTHER_COLLECTION, getAppLayerFactory());

		AuthorizationsServices authorizationsServices = getAppLayerFactory().getModelLayerFactory().newAuthorizationsServices();
		authorizationsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_C01).givingReadWriteAccess());
		authorizationsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_C02).givingNegativeReadAccess());
		authorizationsServices.add(authorizationForUsers(users.sasquatchIn(zeCollection)).on(records.folder_C03).giving("M"));
		authorizationsServices.add(authorizationForUsers(users.edouardLechatIn(zeCollection)).on(records.unitId_30c).givingNegativeReadWriteAccess());
		//TODO Add on documents

		//		Transaction tx = new Transaction();
		//		tx.add(rmOfTargetCollection.newStorageSpace().setCode("S01").setTitle("S01"));
		//		tx.add(rmOfTargetCollection.newStorageSpace().setCode("S01-01").setTitle("S01-01"));
		//		tx.add(rmOfTargetCollection.newStorageSpace().setCode("S01-02").setTitle("S01-02"));
		//		tx.add(rmOfTargetCollection.newStorageSpace().setCode("S02").setTitle("S02"));
		//		tx.add(rmOfTargetCollection.newStorageSpace().setCode("S02-01").setTitle("S02-01"));
		//		tx.add(rmOfTargetCollection.newStorageSpace().setCode("S02-02").setTitle("S02-02"));
		//		tx.add(rmOfTargetCollection.newContainerRecordType().setCode("B22x22").setTitle("B22x22"));
		//		getModelLayerFactory().newRecordServices().execute(tx);

		exportThenImportInAnotherCollection(
				options.setRecordsToExportIterator(new RecordsOfSchemaTypesIterator(getModelLayerFactory(), zeCollection, (asList(
						AdministrativeUnit.SCHEMA_TYPE,
						MediumType.SCHEMA_TYPE, Category.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE,
						ContainerRecordType.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE,
						DocumentType.SCHEMA_TYPE)))));

		//Given
		List<Authorization> sourceAuthorizationsList = rmSchemasRecordsServicesSourceCollection.searchSolrAuthorizationDetailss(returnAll());
		assertThatRecords(sourceAuthorizationsList).extractingMetadatas("roles", "principals.title", "target").containsOnly(
				tuple(asList("READ", "WRITE", "DELETE"), asList("Dakota L'Indien", "Gandalf Leblanc"), "unitId_10"),
				tuple(asList("READ", "WRITE"), asList("Bob 'Elvis' Gratton", "Charles-François Xavier", "System Admin"), "unitId_10"),
				tuple(asList("U"), asList("Dakota L'Indien", "System Admin"), "unitId_11"),
				tuple(asList("M"), asList("Dakota L'Indien", "Gandalf Leblanc"), "unitId_10"),
				tuple(asList("U"), asList("Bob 'Elvis' Gratton", "Charles-François Xavier", "System Admin"), "unitId_10"),
				tuple(asList("READ", "WRITE", "DELETE"), asList("Edouard Lechat", "Gandalf Leblanc"), "unitId_11"),
				tuple(asList("READ", "WRITE"), asList("Dakota L'Indien", "System Admin"), "unitId_11"),
				tuple(asList("M"), asList("Edouard Lechat", "Gandalf Leblanc"), "unitId_11"),
				tuple(asList("READ", "WRITE", "DELETE"), asList("Edouard Lechat", "Gandalf Leblanc"), "unitId_12"),
				tuple(asList("U"), asList("Dakota L'Indien", "System Admin", "Edouard Lechat", "Bob 'Elvis' Gratton", "System Admin"), "unitId_12"),
				tuple(asList("READ", "WRITE"), asList("Dakota L'Indien", "System Admin", "Edouard Lechat", "Bob 'Elvis' Gratton", "System Admin"), "unitId_12"),
				tuple(asList("M"), asList("Edouard Lechat", "Gandalf Leblanc"), "unitId_12"),
				tuple(asList("READ", "WRITE"), asList("Edouard Lechat", "Bob 'Elvis' Gratton", "System Admin"), "unitId_30"),
				tuple(asList("U"), asList("Edouard Lechat", "Bob 'Elvis' Gratton", "System Admin"), "unitId_30"),
				tuple(asList("READ", "WRITE", "DELETE"), asList("Gandalf Leblanc"), "unitId_30"),
				tuple(asList("M"), asList("Gandalf Leblanc"), "unitId_30"),
				tuple(asList("M"), asList("Big Foot"), "C03"),
				tuple(asList("READ"), asList("Big Foot"), "C02"),
				tuple(asList("READ", "WRITE"), asList("Big Foot"), "C01"),
				tuple(asList("READ", "WRITE"), asList("Edouard Lechat"), "unitId_30c")
		);

		List<Authorization> targetAuthorizationsListsBeforeImport = rmOfTargetCollection.searchSolrAuthorizationDetailss(returnAll());
		assertThat(targetAuthorizationsListsBeforeImport.isEmpty());

		//When
		RecordExportOptions options = new ExportPresenterServices(zeCollection, getAppLayerFactory())
				.buildOptionsForExportingAdministrativeUnitsAndItsContent(false, asList("unitId_30"), true);
		File file = exportToZip(options);
		importFromZip(file, ANOTHER_COLLECTION);


		//Then
		List<Authorization> targetAuthorizationsListsAfterImport = rmOfTargetCollection.searchSolrAuthorizationDetailss(returnAll());
		assertThatRecords(targetAuthorizationsListsAfterImport).extractingMetadatas("roles", "principals.title", "target.legacyIdentifier").containsOnly(

				tuple(asList("READ", "WRITE"), asList("Edouard Lechat", "Bob 'Elvis' Gratton", "System Admin"), "unitId_30"),
				tuple(asList("U"), asList("Edouard Lechat", "Bob 'Elvis' Gratton", "System Admin"), "unitId_30"),
				tuple(asList("READ", "WRITE", "DELETE"), asList("Gandalf Leblanc"), "unitId_30"),
				tuple(asList("M"), asList("Gandalf Leblanc"), "unitId_30"),
				tuple(asList("M"), asList("Big Foot"), "C03"),
				tuple(asList("READ"), asList("Big Foot"), "C02"),
				tuple(asList("READ", "WRITE"), asList("Big Foot"), "C01"),
				tuple(asList("READ", "WRITE"), asList("Edouard Lechat"), "unitId_30c")
		);

		for (Authorization authorization : targetAuthorizationsListsAfterImport) {
			for (String principalId : authorization.getPrincipals()) {
				Record record = getModelLayerFactory().newRecordServices().realtimeGetRecordSummaryById(principalId);
				assertThat(record).isNull();
				assertThat(record.getCollection()).isEqualTo("anotherCollection");
			}
		}

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

	private File exportToZip(RecordExportOptions options) {
		return new RecordExportServices(getAppLayerFactory()).exportRecordsAndZip(SDK_STREAM, options);
	}

	private void importFromZip(File zipFile, String collection) {
		ImportDataProvider importDataProvider = null;
		try {
			importDataProvider = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile);

			UserServices userServices = getModelLayerFactory().newUserServices();
			User user = userServices.getUserInCollection("admin", collection);
			BulkImportParams importParams = BulkImportParams.STRICT();
			importParams.setThreads(1);
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
		File zipFile = new RecordExportServices(getAppLayerFactory()).exportRecordsAndZip(SDK_STREAM, options);
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

	private Condition<? super List<DecommissioningList>> containingAllFolderDetails(
			final List<List<DecomListFolderDetail>> comparator) {
		return new Condition<List<DecommissioningList>>() {
			@Override
			public boolean matches(List<DecommissioningList> comparedLists) {
				for (DecommissioningList list : comparedLists) {
					boolean wasFound = false;
					List<DecomListFolderDetail> folderDetails = list.getFolderDetails();
					for (List<DecomListFolderDetail> comparatorDetailList : comparator) {
						if (folderDetails.containsAll(comparatorDetailList)) {
							wasFound = true;
							break;
						}
					}

					if (!wasFound) {
						return false;
					}
				}
				return true;
			}
		}.describedAs("containing all folderDetails in " + comparator);
	}

	private Condition<? super List<DecommissioningList>> containingAllContainerDetails(
			final List<List<DecomListContainerDetail>> comparator) {
		return new Condition<List<DecommissioningList>>() {
			@Override
			public boolean matches(List<DecommissioningList> comparedLists) {
				for (DecommissioningList list : comparedLists) {
					boolean wasFound = false;
					List<DecomListContainerDetail> containerDetails = list.getContainerDetails();
					for (List<DecomListContainerDetail> comparatorDetailList : comparator) {
						if (containerDetails.containsAll(comparatorDetailList)) {
							wasFound = true;
							break;
						}
					}

					if (!wasFound) {
						return false;
					}
				}
				return true;
			}
		}.describedAs("containing all containerDetails in " + comparator);
	}

	private Condition<? super List<DecommissioningList>> containingAllValidations(
			final List<List<DecomListValidation>> comparator) {
		return new Condition<List<DecommissioningList>>() {
			@Override
			public boolean matches(List<DecommissioningList> comparedLists) {
				for (DecommissioningList list : comparedLists) {
					boolean wasFound = false;
					List<DecomListValidation> validations = list.getValidations();
					for (List<DecomListValidation> comparatorDetailList : comparator) {
						if (validations.containsAll(comparatorDetailList)) {
							wasFound = true;
							break;
						}
					}

					if (!wasFound) {
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
				for (Task task : comparedTasks) {
					boolean wasFound = false;
					List<TaskReminder> reminders = task.getReminders();
					for (List<TaskReminder> comparatorReminders : comparator) {
						if (reminders.containsAll(comparatorReminders)) {
							wasFound = true;
							break;
						}
					}

					if (!wasFound) {
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
				for (Task task : comparedTasks) {
					boolean wasFound = false;
					List<TaskFollower> followers = task.getTaskFollowers();
					for (List<TaskFollower> comparatorFollowers : comparator) {
						if (followers.containsAll(comparatorFollowers)) {
							wasFound = true;
							break;
						}
					}

					if (!wasFound) {
						return false;
					}
				}
				return true;
			}
		}.describedAs("containing all followers in " + comparator);
	}
}
