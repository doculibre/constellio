package com.constellio.model.services.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordServicesAggregatedSumIntegerMetadatasAcceptTest extends ConstellioTest {

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private Users users = new Users();

	private RMSchemasRecordsServices rm;
	private RecordServices recordServices;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());
		givenBackgroundThreadsEnabled();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("document").getDefaultSchema().create("number")
						.setMultivalue(false).setType(MetadataValueType.INTEGER);

				types.getSchemaType("folder").getDefaultSchema().create("sum").setType(MetadataValueType.INTEGER)
						.defineDataEntry().asSum(
						types.getSchemaType("document").getDefaultSchema().getMetadata(Document.FOLDER),
						types.getSchemaType("document").getDefaultSchema().getMetadata("number"));
			}
		});

		RMTask task = rm.newRMTaskWithId("fakeTask");
		task.setTitle("title");
		recordServices.add(task);

		Folder folder = records.newFolderWithValuesAndId("fakeFolder");
		recordServices.add(folder);
		Folder folder2 = records.newFolderWithValuesAndId("fakeFolder2");
		recordServices.add(folder2);

		Document document = records.newDocumentWithIdIn("fakeDocument", folder);
		document.set(rm.document.metadata("number"), 10);
		recordServices.add(document);
		Document document2 = records.newDocumentWithIdIn("fakeDocument2", folder2);
		document2.set(rm.document.metadata("number"), 100);
		recordServices.add(document2);

		waitForBatchProcess();

		assertThat(rm.getDocument("fakeDocument").<Integer>get(rm.document.metadata("number"))).isEqualTo(10);
		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(10);

		assertThat(rm.getDocument("fakeDocument2").<Integer>get(rm.document.metadata("number"))).isEqualTo(100);
		assertThat(rm.getFolder("fakeFolder2").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(100);
	}

	@Test
	public void givenModifiedDocumentNumberThenIncrementFolderSum() throws Exception {
		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("number"), 15);
		recordServices.update(document.getWrappedRecord());

		assertThat(rm.getDocument("fakeDocument").<Integer>get(rm.document.metadata("number"))).isEqualTo(15);
		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(15);
	}

	@Test
	public void givenModifiedDocumentNumberAndModifiedFolderThenIncrementFolderSum() throws Exception {
		Transaction transaction = new Transaction();

		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("number"), 55);
		transaction.add(document);

		Folder folder = rm.getFolder("fakeFolder");
		folder.set(rm.folder.description(), "NewDescription");
		transaction.addUpdate(folder.getWrappedRecord());

		recordServices.execute(transaction);

		assertThat(rm.getDocument("fakeDocument").<Integer>get(rm.document.metadata("number"))).isEqualTo(55);
		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(55);
	}

	@Test
	public void givenMultipleModifiedDocumentNumberThenIncrementMultipleFolderSum() throws Exception {
		Transaction transaction = new Transaction();

		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("number"), 20);
		transaction.add(document);

		Document document2 = rm.getDocument("fakeDocument2");
		document2.set(rm.document.metadata("number"), 50);
		transaction.add(document2);

		recordServices.execute(transaction);

		assertThat(rm.getDocument("fakeDocument").<Integer>get(rm.document.metadata("number"))).isEqualTo(20);
		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(20);
		assertThat(rm.getFolder("fakeFolder").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();

		assertThat(rm.getDocument("fakeDocument2").<Integer>get(rm.document.metadata("number"))).isEqualTo(50);
		assertThat(rm.getFolder("fakeFolder2").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(50);
		assertThat(rm.getFolder("fakeFolder2").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();
	}


	@Test
	public void givenMultipleUnmodifiedOrZeroNumberValueThenNothingIncrementedAndNotMarkedForReindexing()
			throws Exception {
		Transaction transaction = new Transaction();

		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("number"), 0);
		transaction.add(document);

		Document document2 = rm.getDocument("fakeDocument2");
		document2.set(rm.document.metadata("number"), null);
		transaction.add(document2);

		recordServices.execute(transaction);

		assertThat(rm.getDocument("fakeDocument").<Integer>get(rm.document.metadata("number"))).isEqualTo(0);
		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(0);
		assertThat(rm.getFolder("fakeFolder").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();

		assertThat(rm.getDocument("fakeDocument2").<Integer>get(rm.document.metadata("number"))).isNull();
		assertThat(rm.getFolder("fakeFolder2").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(0);
		assertThat(rm.getFolder("fakeFolder2").<Boolean>get(Schemas.MARKED_FOR_REINDEXING)).isNull();
	}


	//	@Test
	//	public void whenCreateFolderWithoutLinearLengthThenContainerNotMarkedToReindex() throws Exception {
	//		Transaction transaction = new Transaction();
	//
	//		Folder folder = rm.newFolder().setTitle("test").
	//
	//		Document document2 = rm.getDocument("fakeDocument2");
	//		document2.set(rm.document.metadata("number"), 50);
	//		transaction.add(document2);
	//
	//		recordServices.execute(transaction);
	//
	//		assertThat(rm.getDocument("fakeDocument").get(rm.document.metadata("number"))).isEqualTo(20);
	//		assertThat(rm.getFolder("fakeFolder").get(rm.folder.metadata("sum"))).isEqualTo(20);
	//
	//		assertThat(rm.getDocument("fakeDocument2").get(rm.document.metadata("number"))).isEqualTo(50);
	//		assertThat(rm.getFolder("fakeFolder2").get(rm.folder.metadata("sum"))).isEqualTo(50);
	//	}


	@Test
	public void givenTwoModifiedDocumentNumberAndOneFolderSumThenIncrementFolderSumTwice() throws Exception {
		Document document2b = records.newDocumentWithIdIn("fakeDocument2b", rm.getFolder("fakeFolder2"));
		document2b.set(rm.document.metadata("number"), 5);
		recordServices.add(document2b);
		waitForBatchProcess();

		assertThat(rm.getDocument("fakeDocument2b").<Integer>get(rm.document.metadata("number"))).isEqualTo(5);
		assertThat(rm.getFolder("fakeFolder2").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(105);

		Transaction transaction = new Transaction();

		Document document2 = rm.getDocument("fakeDocument2");
		document2.set(rm.document.metadata("number"), 150);
		transaction.add(document2);

		document2b = rm.getDocument("fakeDocument2b");
		document2b.set(rm.document.metadata("number"), 30);
		transaction.add(document2b);

		recordServices.execute(transaction);

		assertThat(rm.getDocument("fakeDocument2").<Integer>get(rm.document.metadata("number"))).isEqualTo(150);
		assertThat(rm.getDocument("fakeDocument2b").<Integer>get(rm.document.metadata("number"))).isEqualTo(30);
		assertThat(rm.getFolder("fakeFolder2").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(180);
	}

	@Test
	public void givenModifiedDocumentNumberAndMultipleSchemaTypeSumThenIncrementAllSchemaTypeSum() throws Exception {
		createTaskSumMetadata();

		Task task = rm.getRMTask("fakeTask");

		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("task"), task);
		recordServices.add(document);
		waitForBatchProcess();

		Metadata taskSum = task.getSchema().getMetadata("sum");
		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(10);

		Transaction transaction = new Transaction();
		document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("number"), 15);
		transaction.add(document);

		recordServices.execute(transaction);

		assertThat(rm.getDocument("fakeDocument").<Integer>get(rm.document.metadata("number"))).isEqualTo(15);
		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(15);
		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(15);
	}

	@Test
	public void givenCreatedDocumentThenIncrementFolderSum() throws Exception {
		Document document3 = records.newDocumentWithIdIn("fakeDocument3", rm.getFolder("fakeFolder"));
		document3.set(rm.document.metadata("number"), 2);
		recordServices.add(document3);

		assertThat(rm.getDocument("fakeDocument3").<Integer>get(rm.document.metadata("number"))).isEqualTo(2);
		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(12);
	}

	@Test
	public void givenCreatedDocumentAndCreatedFolderThenCorrectSum() throws Exception {
		Transaction transaction = new Transaction();

		Folder folder4 = records.newFolderWithValuesAndId("fakeFolder4");
		transaction.add(folder4);

		Document document4 = records.newDocumentWithIdIn("fakeDocument4", folder4);
		document4.set(rm.document.metadata("number"), 3);
		transaction.add(document4);

		recordServices.execute(transaction);
		waitForBatchProcess();

		assertThat(rm.getDocument("fakeDocument4").<Integer>get(rm.document.metadata("number"))).isEqualTo(3);
		assertThat(rm.getFolder("fakeFolder4").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(3);
	}

	@Test
	public void givenDocumentAndDeletedTaskThenDecreaseSum() throws Exception {
		createTaskSumMetadata();

		Task task = rm.getRMTask("fakeTask");

		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("task"), task);
		recordServices.update(document);
		waitForBatchProcess();

		Metadata taskSum = task.getSchema().getMetadata("sum");
		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(10);

		recordServices.logicallyDelete(document.getWrappedRecord(), User.GOD);

		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(0);
	}

	@Test
	public void givenDocumentAndRestoredTaskThenIncreaseSum() throws Exception {
		createTaskSumMetadata();

		Task task = rm.getRMTask("fakeTask");

		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("task"), task);
		recordServices.update(document);
		waitForBatchProcess();

		Metadata taskSum = task.getSchema().getMetadata("sum");
		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(10);

		recordServices.logicallyDelete(document.getWrappedRecord(), User.GOD);

		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(0);

		recordServices.restore(document.getWrappedRecord(), User.GOD);

		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(10);
	}

	@Test
	public void givenDocumentWithAddedTaskReferenceThenIncreaseSum() throws Exception {
		createTaskSumMetadata();

		Task task = rm.getRMTask("fakeTask");

		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("task"), task);
		recordServices.update(document);

		Metadata taskSum = task.getSchema().getMetadata("sum");
		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(10);
	}

	@Test
	public void givenDocumentWithRemovedTaskReferenceThenDecreaseSum() throws Exception {
		createTaskSumMetadata();

		Task task = rm.getRMTask("fakeTask");

		Document document = rm.getDocument("fakeDocument");
		document.set(rm.document.metadata("task"), task);
		recordServices.update(document);

		Metadata taskSum = task.getSchema().getMetadata("sum");
		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(10);

		document.set(rm.document.metadata("task"), null);
		recordServices.update(document);

		assertThat(rm.getRMTask("fakeTask").<Integer>get(taskSum)).isEqualTo(0);
	}

	@Test
	public void givenDeletedDocumentAndDeletedFolderThenNoException() throws Exception {
		Document document = rm.getDocument("fakeDocument");
		recordServices.logicallyDelete(document.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(document.getWrappedRecord(), User.GOD);
		waitForBatchProcess();

		Folder folder = rm.getFolder("fakeFolder");
		recordServices.logicallyDelete(folder.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(folder.getWrappedRecord(), User.GOD);
		waitForBatchProcess();

		Document document2 = rm.getDocument("fakeDocument2");
		recordServices.logicallyDelete(document2.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(document2.getWrappedRecord(), User.GOD);
		waitForBatchProcess();

		Folder folder2 = rm.getFolder("fakeFolder2");
		recordServices.logicallyDelete(folder2.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(folder2.getWrappedRecord(), User.GOD);
		waitForBatchProcess();
	}

	@Test
	public void givenDocumentsWithModifiedParentFolderThenFoldersHaveCorrectSum() throws Exception {
		recordServices.update(rm.getDocument("fakeDocument2").setFolder("fakeFolder"));

		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(110);
		assertThat(rm.getFolder("fakeFolder2").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(0);

		Transaction transaction = new Transaction();
		transaction.add(rm.getDocument("fakeDocument2").setFolder("fakeFolder2"));
		transaction.add(rm.getDocument("fakeDocument").setFolder("fakeFolder2"));
		recordServices.execute(transaction);

		assertThat(rm.getFolder("fakeFolder").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(0);
		assertThat(rm.getFolder("fakeFolder2").<Integer>get(rm.folder.metadata("sum"))).isEqualTo(110);
	}

	private void createTaskSumMetadata() throws Exception {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("document").getDefaultSchema().create("task")
						.setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(types.getSchemaType(RMTask.SCHEMA_TYPE).getDefaultSchema());

				types.getSchemaType(Task.SCHEMA_TYPE).getDefaultSchema().create("sum").setType(MetadataValueType.INTEGER)
						.defineDataEntry().asSum(
						types.getSchemaType("document").getDefaultSchema().getMetadata("task"),
						types.getSchemaType("document").getDefaultSchema().getMetadata("number"));
			}
		});
		waitForBatchProcess();
	}

}