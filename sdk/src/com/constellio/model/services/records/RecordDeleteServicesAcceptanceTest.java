package com.constellio.model.services.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions;
import com.constellio.app.modules.rm.services.ValueListServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.trash.TrashServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class RecordDeleteServicesAcceptanceTest extends ConstellioTest {

	private static final String CUSTOM_TASK_SCHEMA_LOCAL_CODE = "zTaskSchema";
	private static final String CUSTOM_TASK_NEW_METADATA = "zTaskMeta";
	private static final String CUSTOM_FOLDER_SCHEMA_LOCAL_CODE = "zFolderSchema";
	private static final String CUSTOM_FOLDER_NEW_METADATA = "zFolderMeta";
	private static final String CUSTOM_VALUELIST_SCHEMA = "ddvValueListTest";
	private static final String CUSTOM_VALUELIST_NEW_METADATA = "zValueListMeta";

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
	private MetadataSchema customFolderSchema;
	private MetadataSchema customValueSchema;
	private Metadata zMeta;

	private Folder folder, folder2;
	private Document document, document2, document3;

	private QueryCounter queryCounter;

	@Before
	public void setUp()
			throws Exception {
		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);
		trashServices = new TrashServices(getAppLayerFactory().getModelLayerFactory(), zeCollection);
		admin = users.adminIn(zeCollection);
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		tasks = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		deleteService = new RecordDeleteServices(recordDao, getModelLayerFactory());

		createCustomTaskSchema();
		createCustomFolderSchema();
		initTests();

		folder = records.newFolderWithValuesAndId("fakeFolder");
		recordServices.add(folder);
		folder2 = records.newFolderWithValuesAndId("fakeFolder2");
		recordServices.add(folder2);

		document = records.newDocumentWithIdIn("fakeDocument2", folder2);
		recordServices.add(document);
		document2 = records.newDocumentWithIdIn("fakeDocument2b", folder2);
		recordServices.add(document2);
		document3 = records.newDocumentWithIdIn("fakeDocument2bb", folder2);
		recordServices.add(document3);

		queryCounter = new QueryCounter(getDataLayerFactory(), "RecordDeleteServicesAcceptanceTest");
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

	private void createCustomFolderSchema() {
		getModelLayerFactory()
				.getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {

				ValueListItemSchemaTypeBuilder builder = new ValueListItemSchemaTypeBuilder(types);
				MetadataSchemaTypeBuilder valueListSchemaType = builder
						.createValueListItemSchema(CUSTOM_VALUELIST_SCHEMA, null, ValueListItemSchemaTypeBuilderOptions.codeMetadataFacultative());

				MetadataSchemaBuilder defaultSchema = valueListSchemaType.getDefaultSchema();
				defaultSchema.create(CUSTOM_VALUELIST_NEW_METADATA).setType(STRING);

				MetadataSchemaTypeBuilder foldersSchemaType = types.getSchemaType(Folder.SCHEMA_TYPE);
				MetadataSchemaBuilder foldersSchema = foldersSchemaType.createCustomSchema(CUSTOM_FOLDER_SCHEMA_LOCAL_CODE);

				foldersSchema.create(CUSTOM_FOLDER_NEW_METADATA).setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(defaultSchema);
			}
		});
		customFolderSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(Folder.SCHEMA_TYPE).getSchema(CUSTOM_FOLDER_SCHEMA_LOCAL_CODE);
		customValueSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(CUSTOM_VALUELIST_SCHEMA).getDefaultSchema();
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
	}

	@Test
	public void givenReferencedMetadataWhenLogicallyThenPhysicallyDeletedThenError()
			throws Exception {

		Record valueTest = new TestRecord(customValueSchema, "testValueId")
				.set(customValueSchema.get(Schemas.TITLE_CODE), "titre de test Value")
				.set(customValueSchema.get(CUSTOM_VALUELIST_NEW_METADATA), "valeure texte contenue");

		Transaction transaction = new Transaction();
		transaction.add(valueTest);
		recordServices.execute(transaction);

		Transaction tr = new Transaction();

		Record folder3 = new TestRecord(customFolderSchema, "folder3")
				.set(customFolderSchema.get(Schemas.TITLE_CODE), "folder3")
				.set(customFolderSchema.get(Folder.ADMINISTRATIVE_UNIT_ENTERED), "unitId_10")
				.set(customFolderSchema.get(Folder.CATEGORY_ENTERED), category)
				.set(customFolderSchema.get(Folder.RETENTION_RULE_ENTERED), "ruleId_1")
				.set(customFolderSchema.get(Folder.COPY_STATUS_ENTERED), PRINCIPAL)
				.set(customFolderSchema.get(CUSTOM_FOLDER_NEW_METADATA), valueTest.getId())
				.set(customFolderSchema.get(Folder.OPENING_DATE), date(2000, 10, 4));

		tr.add(folder3);
		recordServices.execute(tr);

		List<ValidationError> validationErrors = deleteService.validateLogicallyThenPhysicallyDeletable(valueTest, users.adminIn(zeCollection)).getValidationErrors();

		assertThat(validationErrors.size() == 1);
		assertThat(validationErrors.get(0).getCode().equals("recordInHierarchyReferencedOutsideOfHierarchy"));
		assertThat(deleteService.validateLogicallyDeletable(valueTest, users.adminIn(zeCollection)).isEmpty()).isTrue();

	}

	@Test
	public void givenTaxonomyConceptWithDocumentsWhenLogicallyThenPhysicallyDeletedThenError()
			throws Exception {

		ValueListServices valueListServices = new ValueListServices(getAppLayerFactory(), zeCollection);

		Map<Language, String> mapLangueTitle = new HashMap<>();
		mapLangueTitle.put(Language.French, "Ze taxonomy");

		Taxonomy taxonomy = valueListServices.createTaxonomy(mapLangueTitle, true);
		Metadata metadata = valueListServices.createAMultivalueClassificationMetadataInGroup(
				taxonomy, Document.SCHEMA_TYPE, "Ze taxonomy", "Ze taxonomy tab label");

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		MetadataSchema schema = rm.getTypes().getDefaultSchema(taxonomy.getSchemaTypes().get(0));

		Record rootConcept = new TestRecord(schema, "rootConcept")
				.set(Schemas.CODE, "A").set(Schemas.TITLE, "Root concept");
		Record childConcept = new TestRecord(schema, "childConcept").set(schema.get("parent"), "rootConcept")
				.set(Schemas.CODE, "A1").set(Schemas.TITLE, "Child concept");
		Document aDocument = rm.newDocument().setTitle("Ze doc")
				.setFolder(records.getFolder_A03()).set(metadata, asList(childConcept));
		Transaction transaction = new Transaction();
		transaction.add(rootConcept);
		transaction.add(childConcept);
		transaction.add(aDocument);
		recordServices.execute(transaction);


		assertThat(deleteService.validateLogicallyThenPhysicallyDeletable(childConcept, users.adminIn(zeCollection)).isEmpty()).isFalse();
		assertThat(deleteService.validateLogicallyDeletable(childConcept, users.adminIn(zeCollection)).isEmpty()).isTrue();

	}

	//@Test
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
	public void givenRecordsWithSimilarNamesThenMakeSureThatOnlyCorrectRecordsAreDeleted() {
		recordServices.logicallyDelete(folder.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(folder.getWrappedRecord(), User.GOD);

		assertThat(rm.getFolder("fakeFolder2")).isNotNull();

		recordServices.logicallyDelete(document.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(document.getWrappedRecord(), User.GOD);

		assertThat(rm.getDocument("fakeDocument2b")).isNotNull();
		assertThat(rm.getDocument("fakeDocument2bb")).isNotNull();
	}

	@Test
	public void whenLogicallyDeletingThenSetLogicallyDeletedStatusToAllRecordInHierarchyAndExecuteTransaction()
			throws Exception {

		deleteService.logicallyDelete(recordServices.getDocumentById(folder2.getId()), users.adminIn(zeCollection));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		Record folderRecord2 = recordServices.getDocumentById(folder2.getId());
		assertThat(folderRecord2.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isTrue();
		assertThat(folderRecord2.<LocalDateTime>get(Schemas.LOGICALLY_DELETED_ON)).isNotNull();
		Record documentRecord = recordServices.getDocumentById(document.getId());
		assertThat(documentRecord.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isTrue();
		assertThat(documentRecord.<LocalDateTime>get(Schemas.LOGICALLY_DELETED_ON)).isNotNull();
		Record documentRecord2 = recordServices.getDocumentById(document2.getId());
		assertThat(documentRecord2.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isTrue();
		assertThat(documentRecord2.<LocalDateTime>get(Schemas.LOGICALLY_DELETED_ON)).isNotNull();
		Record documentRecord3 = recordServices.getDocumentById(document3.getId());
		assertThat(documentRecord3.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isTrue();
		assertThat(documentRecord3.<LocalDateTime>get(Schemas.LOGICALLY_DELETED_ON)).isNotNull();
	}

	@Test
	public void whenRestoringThenSetLogicallyDeletedStatusToAllRecordInHierarchyAndExecuteTransaction()
			throws Exception {
		Transaction transaction = new Transaction();
		transaction.add(folder2.set(Schemas.LOGICALLY_DELETED_STATUS, true)
				.set(Schemas.LOGICALLY_DELETED_ON, TimeProvider.getLocalDateTime()).getWrappedRecord());
		transaction.add(document.set(Schemas.LOGICALLY_DELETED_STATUS, true)
				.set(Schemas.LOGICALLY_DELETED_ON, TimeProvider.getLocalDateTime()).getWrappedRecord());
		transaction.add(document2.set(Schemas.LOGICALLY_DELETED_STATUS, true)
				.set(Schemas.LOGICALLY_DELETED_ON, TimeProvider.getLocalDateTime()).getWrappedRecord());
		transaction.add(document3.set(Schemas.LOGICALLY_DELETED_STATUS, true)
				.set(Schemas.LOGICALLY_DELETED_ON, TimeProvider.getLocalDateTime()).getWrappedRecord());
		recordServices.execute(transaction);

		deleteService.restore(recordServices.getDocumentById(folder2.getId()), users.adminIn(zeCollection));

		Record folderRecord2 = recordServices.getDocumentById(folder2.getId());
		assertThat(folderRecord2.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isFalse();
		assertThat(folderRecord2.<LocalDateTime>get(Schemas.LOGICALLY_DELETED_ON)).isNull();
		Record documentRecord = recordServices.getDocumentById(document.getId());
		assertThat(documentRecord.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isFalse();
		assertThat(documentRecord.<LocalDateTime>get(Schemas.LOGICALLY_DELETED_ON)).isNull();
		Record documentRecord2 = recordServices.getDocumentById(document2.getId());
		assertThat(documentRecord2.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isFalse();
		assertThat(documentRecord2.<LocalDateTime>get(Schemas.LOGICALLY_DELETED_ON)).isNull();
		Record documentRecord3 = recordServices.getDocumentById(document3.getId());
		assertThat(documentRecord3.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isFalse();
		assertThat(documentRecord3.<LocalDateTime>get(Schemas.LOGICALLY_DELETED_ON)).isNull();
	}
}
