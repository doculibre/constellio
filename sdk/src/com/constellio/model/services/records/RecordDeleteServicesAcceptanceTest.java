package com.constellio.model.services.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
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
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords;
import com.constellio.model.services.records.utils.SortOrder;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.trash.TrashServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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

		assertThat(deleteService.isLogicallyThenPhysicallyDeletable(childConcept, users.adminIn(zeCollection))).isFalse();
		//		assertThat(deleteService.isLogicallyDeletable(childConcept, users.adminIn(zeCollection))).isTrue();

		//		newWebDriver();
		//		waitUntilICloseTheBrowsers();

		//
		//		deleteService.logicallyDelete(parentFolderInCategory_A.getWrappedRecord(), null);
		//		recordServices.refresh(parentFolderInCategory_A);
		//		try {
		//			deleteService.physicallyDelete(parentFolderInCategory_A.getWrappedRecord(), null,
		//					new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
		//			fail("should find dependent references");
		//		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords e) {
		//			Set<String> relatedRecords = e.getRecordsIdsWithUnremovableReferences();
		//			assertThat(relatedRecords).contains(taskReferencesFolderB.getId())
		//					.doesNotContain(subFolder_B.getId(), category.getId());
		//
		//			recordServices.refresh(parentFolderInCategory_A);
		//
		//			//TODO Nouha, pourquoi la catégorie serait nulle, c'est un champ obligatoire??
		//			assertThat(parentFolderInCategory_A.getCategory()).isNull();
		//		}
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

	//@Test
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

	//@Test
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

	@Test
	public void givenRecordInHierarchyWhenGetAllRecordsInHierarchySortOrderAscendingThenRecordsSortedAscending() {
		List<Record> orderedRecords =
				deleteService.getAllRecordsInHierarchy(records.getFolder_A02().getWrappedRecord(), SortOrder.ASCENDING);
		assertThat(orderedRecords.get(0)).isEqualTo(records.getFolder_A02().getWrappedRecord());
	}

	@Test
	public void givenRecordInHierarchyWhenGetAllRecordsInHierarchySortOrderDescendingThenRecordsSortedDescending() {
		List<Record> orderedRecords =
				deleteService.getAllRecordsInHierarchy(records.getFolder_A02().getWrappedRecord(), SortOrder.DESCENDING);
		assertThat(orderedRecords.get(orderedRecords.size() - 1)).isEqualTo(records.getFolder_A02().getWrappedRecord());
	}
}
