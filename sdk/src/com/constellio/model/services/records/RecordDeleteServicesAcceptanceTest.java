package com.constellio.model.services.records;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Set;

import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.trash.TrashServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

@InDevelopmentTest
public class RecordDeleteServicesAcceptanceTest extends ConstellioTest {

	private static final String CUSTOM_TASK_SCHEMA_LOCAL_CODE = "zTaskSchema";
	private static final String CUSTOM_TASK_NEW_METADATA = "zTaskMeta";
	TrashServices trashServices;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	private RecordServices recordServices;
	User admin;
	private SearchServices searchServices;
	private RMSchemasRecordsServices rm;
	private TasksSchemasRecordsServices tasks;
	private RecordDeleteServices deleteService;

	Category category;
	Folder parentFolderInCategory_A, subFolder_B;
	Task taskReferencesFolderB;
	private MetadataSchema customTaskSchema;
	private Metadata zMeta;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		users.setUp(getModelLayerFactory().newUserServices());
		trashServices = new TrashServices(getAppLayerFactory().getModelLayerFactory(), zeCollection);
		admin = users.adminIn(zeCollection);
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		deleteService = new RecordDeleteServices(recordDao, getModelLayerFactory());

		createCustomTaskSchema();
		initTests();

	}

	private void createCustomTaskSchema() {
		getModelLayerFactory()
				.getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder tasksSchemaType = types.getSchemaType(Task.SCHEMA_TYPE);
				MetadataSchemaBuilder tasksSchema = tasksSchemaType.createCustomSchema(CUSTOM_TASK_SCHEMA_LOCAL_CODE);
				MetadataSchemaBuilder folderSchemaBuilder = types.getDefaultSchema(Folder.SCHEMA_TYPE);
				tasksSchema.create(CUSTOM_TASK_NEW_METADATA).setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(folderSchemaBuilder).setDefaultRequirement(true);
			}
		});
		customTaskSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Task.SCHEMA_TYPE).getSchema(CUSTOM_TASK_SCHEMA_LOCAL_CODE);
		zMeta = customTaskSchema.getMetadata(CUSTOM_TASK_NEW_METADATA);
	}

	private void initTests()
			throws RecordServicesException {

		Transaction transaction = new Transaction();
		transaction.add(category = rm.newCategory().setCode("zCat").setTitle("Ze category")
				.setRetentionRules(asList(records.ruleId_1)));
		transaction.add(parentFolderInCategory_A = records.getFolder_A01().setCategoryEntered(category));
		transaction.add(subFolder_B = rm.newFolderWithId("subFolder_B").setParentFolder(parentFolderInCategory_A)
				.setTitle("subFolder_B").setOpenDate(TimeProvider.getLocalDate()));

		taskReferencesFolderB = tasks.wrapTask(tasks.create(customTaskSchema, "taskReferencesFolderB"));
		transaction.add(taskReferencesFolderB.set(zMeta.getLocalCode(), subFolder_B).setTitle("zTask"));
		recordServices.execute(transaction);

		//Nouha : Le record 'parentFolderInCategory_A' n'était pas sauvegardé
	}

	@Test
	public void givenRecordRefereedByOtherRecordsWhenPhysicallyDeleteFromTrashAndGetNonBreakableLinksThenOk()
			throws Exception {
		deleteService.logicallyDelete(parentFolderInCategory_A.getWrappedRecord(), null);
		recordServices.refresh(parentFolderInCategory_A);
		try {
			deleteService.physicallyDelete(parentFolderInCategory_A.getWrappedRecord(), null,
					new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
			fail("should find dependent references");
		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords e) {
			Set<String> relatedRecords = e.getRecordsIdsWithUnremovableReferences();
			assertThat(relatedRecords).contains(taskReferencesFolderB.getId())
					.doesNotContain(subFolder_B.getId(), category.getId());

			recordServices.refresh(parentFolderInCategory_A);

			//TODO Nouha, pourquoi la catégorie serait nulle, c'est un champ obligatoire??
			assertThat(parentFolderInCategory_A.getCategory()).isNull();
		}
	}

	@Test
	public void givenRecordRefereedByOtherRecordsWhenPhysicallyDeleteFromTrashAndGetNonBreakableLinksThenOk2()
			throws Exception {

		recordServices.refresh(parentFolderInCategory_A);
		assertThat(parentFolderInCategory_A.getCategoryEntered()).isEqualTo(category.getId());

		deleteService.logicallyDelete(category.getWrappedRecord(), null);
		recordServices.refresh(category);
		try {
			deleteService.physicallyDelete(category.getWrappedRecord(), null,
					new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
			recordServices.refresh(parentFolderInCategory_A);
			assertThat(parentFolderInCategory_A.getCategoryEntered()).isNull();
			fail("should find dependent references");
		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords e) {
			Set<String> relatedRecords = e.getRecordsIdsWithUnremovableReferences();

			//TODO Nouha : Pourquoi subFolder_B? Ce dossier n'a pas de référence vers category
			assertThat(relatedRecords).contains(parentFolderInCategory_A.getId(), subFolder_B.getId());
			assertThat(relatedRecords).doesNotContain(taskReferencesFolderB.getId());
		}
	}

	@Test
	public void givenRecordRefereedByOtherRecordsWhenPhysicallyDeleteFromTrashAndGetNonBreakableLinksThenOk3()
			throws Exception {

		deleteService.logicallyDelete(parentFolderInCategory_A.getWrappedRecord(), null);
		deleteService.logicallyDelete(category.getWrappedRecord(), null);
		deleteService.logicallyDelete(taskReferencesFolderB.getWrappedRecord(), null);

		recordServices.refresh(category);

		//TODO Nouha : On ne peut pas faire ça, car des dossiers se retrouvent sans catégorie, ce qui cause une erreur de validation
		deleteService
				.physicallyDelete(category.getWrappedRecord(), null,
						new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));

		parentFolderInCategory_A = rm.getFolder(parentFolderInCategory_A.getId());
		deleteService
				.physicallyDelete(parentFolderInCategory_A.getWrappedRecord(), null,
						new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
	}
}
