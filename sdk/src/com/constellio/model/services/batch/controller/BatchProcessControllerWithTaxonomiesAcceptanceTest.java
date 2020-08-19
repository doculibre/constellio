package com.constellio.model.services.batch.controller;

import com.constellio.data.utils.ConsoleLogger;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordLogicalDeleteOptions.LogicallyDeleteTaxonomyRecordsBehavior;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.LoadTest;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.DocumentSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.FolderSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1FirstSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1SecondSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2CustomSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2DefaultSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.TaxonomyRecords;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchProcessControllerWithTaxonomiesAcceptanceTest extends ConstellioTest {

	TaxonomiesManager taxonomiesManager;

	Users users = new Users();
	TwoTaxonomiesContainingFolderAndDocumentsSetup schemas =
			new TwoTaxonomiesContainingFolderAndDocumentsSetup(zeCollection);
	Taxonomy1FirstSchemaType taxonomy1FirstSchema = schemas.new Taxonomy1FirstSchemaType();
	Taxonomy1SecondSchemaType taxonomy1SecondSchema = schemas.new Taxonomy1SecondSchemaType();
	Taxonomy2DefaultSchema taxonomy2DefaultSchema = schemas.new Taxonomy2DefaultSchema();
	Taxonomy2CustomSchema taxonomy2CustomSchema = schemas.new Taxonomy2CustomSchema();
	FolderSchema folderSchema = schemas.new FolderSchema();
	DocumentSchema documentSchema = schemas.new DocumentSchema();

	TaxonomyRecords records;

	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	BatchProcessesManager batchProcessesManager;
	BatchProcessController batchProcessesController;
	int nbFolders;

	private static String taxo1Path(Record... records) {
		String collection = ""; // = "/zzeCollection"
		StringBuilder sb = new StringBuilder(collection + "/taxo1");
		for (Record record : records) {
			sb.append("/");
			sb.append(record.getId());
		}
		return sb.toString();
	}

	@Before
	public void setUp()
			throws Exception {

		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();


		givenCollection(zeCollection).withAllTest(users);
		defineSchemasManager().using(schemas);
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();

		taxonomiesManager.addTaxonomy(schemas.getTaxo1(), schemasManager);
		taxonomiesManager.addTaxonomy(schemas.getTaxo2(), schemasManager);
		taxonomiesManager.setPrincipalTaxonomy(schemas.getTaxo1(), schemasManager);

		records = schemas.givenTaxonomyRecords(recordServices);
		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();

	}

	// Confirm @SlowTest
	@Test
	public void given2000FoldersWhenMovingCategoryThenAllFoldersAreReindexed()
			throws Exception {
		int nbFolders = new BatchProcessControllerSetupWithTaxonomies()
				.add10_10_10_X_HierarchyOfFoldersWith(recordServices, records, folderSchema, 1);
		//assertThat(getTotalReindexedFolders()).isEqualTo(nbFolders);
		moveCategoryAndWaitForBatchProcessesToFinish();
		assertThat(getTotalReindexedFolders()).isEqualTo(nbFolders);
	}

	@Test
	public void given1000FoldersWhenMovingCategoryThenAllFoldersAreReindexed()
			throws Exception {
		int nbFolders = new BatchProcessControllerSetupWithTaxonomies()
				.add10_10_10_X_HierarchyOfFoldersWith(recordServices, records, folderSchema, 0);
		moveCategoryAndWaitForBatchProcessesToFinish();
		assertThat(getTotalReindexedFolders()).isEqualTo(nbFolders);
	}

	@LoadTest
	@Test
	public void whenMovingCategoryInA10MillionRecordsSystemTheBnSurvive()
			throws Exception {

		final AtomicBoolean lockThreadEnabled = new AtomicBoolean(true);

		new Thread() {

			@Override
			public void run() {
				while (lockThreadEnabled.get()) {

					try {
						recordServices.removeOldLocks();
						Thread.sleep(30000);
					} catch (Throwable t) {
					}
				}
			}
		}.start();

		int nbFolders = new BatchProcessControllerSetupWithTaxonomies()
				.add10_10_10_X_HierarchyOfFoldersWith(recordServices, records, folderSchema, 10000);
		assertThat(getInitialReindexedFolders()).isEqualTo(nbFolders);
		//System.out.println("Im tire, i need a ti dodo");
		//Thread.sleep(1000000);
		moveCategoryAndWaitForBatchProcessesToFinish();
		lockThreadEnabled.set(false);
		System.out.println("!!!");
		System.out.println(">> " + getTotalReindexedFolders());
		printRecordsNotReindexed();
		assertThat(getTotalReindexedFolders()).isEqualTo(nbFolders);
		//Vivaaant!

	}

	@LoadTest
	@Test
	public void whenAddingAnAuthorizationToAConceptWith1MillionRecordsSystemThenSurvive()
			throws Exception {
		final AtomicBoolean lockThreadEnabled = new AtomicBoolean(true);

		int nbFolders = new BatchProcessControllerSetupWithTaxonomies()
				.add10_10_10_X_HierarchyOfFoldersWith(recordServices, records, folderSchema, 1000);
		assertThat(countRecordsVisibleByDakota()).isEqualTo(0);

		setAutorisationToUsersOnConcept();
		lockThreadEnabled.set(false);
		System.out.println("!!!");
		System.out.println(">> " + countRecordsVisibleByDakota());
		assertThat(countRecordsVisibleByDakota()).isEqualTo(nbFolders + 2);

	}

	// Confirm @SlowTest
	@Test
	public void givenAPrincipalTaxonomyIsLogicallyDeletedWhileABatchProcessIsBeingExecutedThenAllLogicallyDeletedAndAllCorrectlyMoved()
			throws Exception {
		int nbFolders = new BatchProcessControllerSetupWithTaxonomies()
				.add10_10_10_X_HierarchyOfFoldersWith(recordServices, records, folderSchema, 10);

		moveCategoryAndLogicallyDeleteCategoryWhenBatchProcessesIsStarted();

		System.out.println("!!!");
		System.out.println(">> " + getTotalReindexedFolders());
		printRecordsNotReindexed();
		ReindexingServices reindexingServices = getModelLayerFactory().newReindexingServices();
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);
		assertThat(getTotalReindexedFolders()).isEqualTo(nbFolders);
	}

	//TODO Francis
	//@Test
	public void givenAPrincipalTaxonomyIsPhysicallyDeletedWhileABatchProcessIsBeingExecutedThenAllPhysicallyDeleted()
			throws Exception {
		new BatchProcessControllerSetupWithTaxonomies()
				.add10_10_10_X_HierarchyOfFoldersWith(recordServices, records, folderSchema, 10);
		moveCategoryAndPhysicallyDeleteCategoryWhenBatchProcessesIsStarted();
		System.out.println("!!!");
		System.out.println(">> " + getTotalReindexedFolders());
		printRecordsNotReindexed();
		assertThat(getTotalReindexedFolders()).isEqualTo(0);

	}

	private void moveCategoryAndWaitForBatchProcessesToFinish()
			throws RecordServicesException, InterruptedException {
		Record category = recordServices.getDocumentById("zeCollection_taxo1_firstTypeItem2_secondTypeItem1");

		Record newParent = recordServices.getDocumentById("zeCollection_taxo1_firstTypeItem2_firstTypeItem1");

		category.set(taxonomy1SecondSchema.parentOfType1(), newParent);
		recordServices.updateAsync(category);
		ReindexingServices reindexingServices = getModelLayerFactory().newReindexingServices();
		//		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		waitForBatchProcess();
	}

	private void setAutorisationToUsersOnConcept()
			throws RecordServicesException, InterruptedException {
		Record category = recordServices.getDocumentById("zeCollection_taxo1_firstTypeItem2_secondTypeItem1");

		AuthorizationAddRequest authorization1 = authorizationInCollection(zeCollection).forUsers(users.aliceIn(zeCollection))
				.on(category)
				.givingReadWriteAccess();
		AuthorizationAddRequest authorization2 = authorizationInCollection(zeCollection).forUsers(users.bobIn(zeCollection))
				.on(category)
				.givingReadWriteAccess();
		AuthorizationAddRequest authorization3 = authorizationInCollection(zeCollection)
				.forUsers(users.charlesIn(zeCollection)).on(category)
				.givingReadWriteAccess();
		AuthorizationAddRequest authorization4 = authorizationInCollection(zeCollection).forUsers(users.dakotaIn(zeCollection))
				.on(category)
				.givingReadWriteAccess();

		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		authorizationsServices.add(authorization1, User.GOD);
		authorizationsServices.add(authorization2, User.GOD);
		authorizationsServices.add(authorization3, User.GOD);
		authorizationsServices.add(authorization4, User.GOD);

		waitForBatchProcess();
	}

	private void moveCategoryAndLogicallyDeleteCategoryWhenBatchProcessesIsStarted()
			throws RecordServicesException, InterruptedException {
		final Record category = recordServices.getDocumentById("zeCollection_taxo1_firstTypeItem2_secondTypeItem1");
		Record newParent = recordServices.getDocumentById("zeCollection_taxo1_firstTypeItem2_firstTypeItem1");

		category.set(taxonomy1SecondSchema.parentOfType1(), newParent);
		recordServices.updateAsync(category);

		waitForBatchProcessAndDoSomethingWhenTheFirstBatchProcessIsStarted(new Runnable() {

			@Override
			public void run() {
				RecordLogicalDeleteOptions options = new RecordLogicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(
						LogicallyDeleteTaxonomyRecordsBehavior.LOGICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY);
				recordServices.logicallyDelete(category, User.GOD, options);
			}
		});
	}

	private void moveCategoryAndPhysicallyDeleteCategoryWhenBatchProcessesIsStarted()
			throws RecordServicesException, InterruptedException {
		final Record category = recordServices.getDocumentById("zeCollection_taxo1_firstTypeItem2_secondTypeItem1");

		RecordLogicalDeleteOptions options = new RecordLogicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(
				LogicallyDeleteTaxonomyRecordsBehavior.LOGICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY);
		recordServices.refresh(category);
		recordServices.logicallyDelete(category, User.GOD, options);
		recordServices.refresh(category);

		Record newParent = recordServices.getDocumentById("zeCollection_taxo1_firstTypeItem2_firstTypeItem1");
		category.set(taxonomy1SecondSchema.parentOfType1(), newParent);
		recordServices.updateAsync(category);

		waitForBatchProcessAndDoSomethingWhenTheFirstBatchProcessIsStarted(new Runnable() {

			@Override
			public void run() {
				recordServices.physicallyDelete(category, User.GOD);
			}
		});
	}

	private long getInitialReindexedFolders() {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		String path = taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1) + "/";
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(folderSchema.instance()).where(Schemas.PATH)
				.isStartingWithText(path);
		return searchServices.getResultsCount(condition);
	}

	private void printRecordsNotReindexed() {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		String path = taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1) + "/";
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(folderSchema.instance()).where(Schemas.PATH)
				.isStartingWithText(path);
		Iterator<Record> records = searchServices.recordsIterator(new LogicalSearchQuery(condition));
		List<String> lines = new ArrayList<>();
		lines.add("Not reindexed : " + getInitialReindexedFolders());
		while (records.hasNext()) {
			Record next = records.next();
			lines.add(next.getId());
		}
		ConsoleLogger.log(lines);
	}

	private long getTotalReindexedFolders() {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		String path = taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem1) + "/";
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(folderSchema.instance()).where(Schemas.PATH)
				.isStartingWithText(path);
		return searchServices.getResultsCount(condition);
	}

	private long getTotalReindexedFoldersThatAreInactive() {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		String path = taxo1Path(records.taxo1_firstTypeItem2, records.taxo1_firstTypeItem2_firstTypeItem1) + "/";
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(folderSchema.instance()).where(Schemas.PATH)
				.isStartingWithText(path);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		return searchServices.search(query.filteredByStatus(StatusFilter.DELETED)).size();
	}

	private long countRecordsVisibleByDakota() {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(zeCollection).returnAll());
		query.filteredWithUser(users.dakotaLIndienIn(zeCollection));
		return searchServices.getResultsCount(query);
	}
}
