package com.constellio.app.services.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.englishMessages;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FixRecordIdsExecutorAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void whenMigratingIdsThenAllDependenciesFixed() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withRobotsModule().withConstellioESModule().withDocumentsDecommissioningList().withEvents());

		getDataLayerFactory().getSequencesManager().set("mySeqA01abc", 42L);

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		LogicalSearchQuery findAbeille = new LogicalSearchQuery(from(rm.folder.schemaType()).where(rm.folder.title()).isEqualTo("Abeille"));
		LogicalSearchQuery findEvents = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.type()).isContainingText("folder"));
		assertThat(rm.searchFolders(findAbeille).stream().map(r->r.getId()).collect(toList())).containsOnly("A01");
		assertThat(rm.searchEvents(findEvents).stream().map(r->r.getRecordId()).collect(toList())).contains("A01").contains("A05");

		ensureNoIntegrityErrors();


		new FixRecordIdsExecutor(getAppLayerFactory()).fixAllIds(true, true);

		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		Toggle.CACHES_ENABLED.enable();
		restartLayers();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		List<String> abeilleNow = rm.searchFolders(findAbeille).stream().map(r->r.getId()).collect(toList());
		assertThat(abeilleNow).doesNotContain("A01").hasSize(1);
		ensureNoIntegrityErrors();

		findEvents = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.type()).isContainingText("folder"));
		assertThat(rm.searchEvents(findEvents).stream().map(r->r.getRecordId()).collect(toList())).doesNotContain("A01").doesNotContain("A05");

		assertThat(getDataLayerFactory().getSequencesManager().getLastSequenceValue("mySeq" + abeilleNow.get(0) + "abc")).isEqualTo(42L);

	}


	@Test(expected = RuntimeException.class)
	public void givenFoldersAndDocumentsIdsTransformationNotAllowedWhenMigratingNonNumericIdsThenRuntimeException() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withRobotsModule().withConstellioESModule().withDocumentsDecommissioningList().withEvents());


		new FixRecordIdsExecutor(getAppLayerFactory()).fixAllIds(false, true);



	}

	private void ensureNoIntegrityErrors() {
		SystemCheckManager checkManager = getAppLayerFactory().getSystemCheckManager();
		checkManager.startSystemCheck(false);

		while (checkManager.isSystemCheckResultsRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		//Test data have custom schemas not starting with USR... not a validation we want to do
		List<String> englishMessages = englishMessages(checkManager.getLastSystemCheckResults().getErrors().getValidationErrors())
				.stream().filter(m->!m.contains("begin with USR")).collect(toList());;


		assertThat(englishMessages).isEmpty();
	}

	@Test
	public void whenMigratingIdsThenCopyRuleRetentionStructModified() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withRobotsModule().withConstellioESModule().withDocumentsDecommissioningList().withEvents());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		LogicalSearchQuery findAbeille = new LogicalSearchQuery(from(rm.folder.schemaType()).where(rm.folder.title()).isEqualTo("Abeille"));
		LogicalSearchQuery findEvents = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.type()).isContainingText("folder"));
		LogicalSearchQuery findRetentionRule = new LogicalSearchQuery(from(rm.retentionRule.schemaType()).where(rm.retentionRule.title()).isEqualTo("Règle de conservation #1"));
		assertThat(rm.searchFolders(findAbeille).stream().map(r -> r.getId()).collect(toList())).containsOnly("A01");
		assertThat(rm.searchEvents(findEvents).stream().map(r -> r.getRecordId()).collect(toList())).contains("A01").contains("A05");
		assertThat(rm.searchRetentionRules(findRetentionRule).stream().map(r -> r.getId()).collect(toList()).contains("ruleId_1"));

		ensureNoIntegrityErrors();


		new FixRecordIdsExecutor(getAppLayerFactory()).fixAllIds(true, true);

		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		Toggle.CACHES_ENABLED.enable();
		restartLayers();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		List<String> abeilleNow = rm.searchFolders(findAbeille).stream().map(r -> r.getId()).collect(toList());
		assertThat(abeilleNow).doesNotContain("A01").hasSize(1);
		ensureNoIntegrityErrors();

		findEvents = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.type()).isContainingText("folder"));
		assertThat(rm.searchEvents(findEvents).stream().map(r -> r.getRecordId()).collect(toList())).doesNotContain("A01").doesNotContain("A05");

		findRetentionRule = new LogicalSearchQuery(from(rm.retentionRule.schemaType()).where(rm.retentionRule.title()).isEqualTo("Règle de conservation #1"));
		assertThat(rm.searchRetentionRules(findRetentionRule).stream().map(r -> r.getId()).collect(toList())).doesNotContain("ruleId_1");

	}

	@Test
	public void whenMigratingIdsThenCommentAndTaskFollowerStructModified() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withRobotsModule().withConstellioESModule().withDocumentsDecommissioningList().withEvents());


		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		User userBob = rm.newUserWithId("Bob");
		try {
			rm.executeTransaction(new Transaction(userBob.getWrappedRecord()));
		} catch (RecordServicesException e) {
			throw new RuntimeException("Could not execute transaction1.");
		}

		Record taskRecord = rm.newRMTask()
				.setTaskFollowers(asList(new TaskFollower().setFollowerId("Bob"))).setTitle("tempTask")
				.setComments(asList(new Comment("test", userBob, new LocalDateTime()))).getWrappedRecord();
		try {
			rm.executeTransaction(new Transaction(taskRecord));
		} catch (RecordServicesException e) {
			throw new RuntimeException("Could not execute transaction2.");
		}

		LogicalSearchQuery findAbeille = new LogicalSearchQuery(from(rm.folder.schemaType()).where(rm.folder.title()).isEqualTo("Abeille"));
		LogicalSearchQuery findEvents = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.type()).isContainingText("folder"));
		LogicalSearchQuery findTask = new LogicalSearchQuery(from(rm.userTask.schemaType()).returnAll());
		assertThat(rm.searchFolders(findAbeille).stream().map(r -> r.getId()).collect(toList())).containsOnly("A01");
		assertThat(rm.searchEvents(findEvents).stream().map(r -> r.getRecordId()).collect(toList())).contains("A01").contains("A05");

		List<List<Comment>> tasksComments = rm.searchRMTasks(findTask).stream().map(r -> r.getComments()).collect(toList());
		List<List<TaskFollower>> tasksFollowers = rm.searchRMTasks(findTask).stream().map(r -> r.getTaskFollowers()).collect(toList());

		assertThat(tasksComments.size()).isEqualTo(1);
		List<Comment> taskComments = tasksComments.get(0);
		assertThat(taskComments.size()).isEqualTo(1);
		Comment comment = taskComments.get(0);

		assertThat(comment.getUserId()).isEqualTo("Bob");

		assertThat(tasksFollowers.size()).isEqualTo(1);
		List<TaskFollower> taskFollowers = tasksFollowers.get(0);
		assertThat(taskFollowers.size()).isEqualTo(1);
		TaskFollower taskFollower = taskFollowers.get(0);

		assertThat(taskFollower.getFollowerId()).isEqualTo("Bob");

		ensureNoIntegrityErrors();


		new FixRecordIdsExecutor(getAppLayerFactory()).fixAllIds(true, true);

		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		Toggle.CACHES_ENABLED.enable();
		restartLayers();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		List<String> abeilleNow = rm.searchFolders(findAbeille).stream().map(r -> r.getId()).collect(toList());
		assertThat(abeilleNow).doesNotContain("A01").hasSize(1);
		ensureNoIntegrityErrors();

		findEvents = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.type()).isContainingText("folder"));
		assertThat(rm.searchEvents(findEvents).stream().map(r -> r.getRecordId()).collect(toList())).doesNotContain("A01").doesNotContain("A05");

		findTask = new LogicalSearchQuery(from(rm.userTask.schemaType()).returnAll());
		tasksComments = rm.searchRMTasks(findTask).stream().map(r -> r.getComments()).collect(toList());
		tasksFollowers = rm.searchRMTasks(findTask).stream().map(r -> r.getTaskFollowers()).collect(toList());

		assertThat(tasksComments.size()).isEqualTo(1);
		taskComments = tasksComments.get(0);
		assertThat(taskComments.size()).isEqualTo(1);
		comment = taskComments.get(0);

		assertThat(comment.getUserId()).isNotEqualTo("Bob");

		assertThat(tasksFollowers.size()).isEqualTo(1);
		taskFollowers = tasksFollowers.get(0);
		assertThat(taskFollowers.size()).isEqualTo(1);
		taskFollower = taskFollowers.get(0);

		assertThat(taskFollower.getFollowerId()).isNotEqualTo("Bob");
	}

	@Test
	public void whenMigratingIdsThenContentStructModified() {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withRobotsModule().withConstellioESModule().withDocumentsDecommissioningList().withEvents());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		User userAlice = rm.newUserWithId("Alice");
		try {
			rm.executeTransaction(new Transaction(userAlice.getWrappedRecord()));
		} catch (RecordServicesException e) {
			throw new RuntimeException("Could not execute transaction1.");
		}

		File currentClassFile = customGetCurrentFilePath(this.getClass(), ".java");
		ContentManager contentManager = getModelLayerFactory().getContentManager();

		ContentVersionDataSummary versionDataSummary = null;
		try {
			versionDataSummary = contentManager.upload(currentClassFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unhandled error.");
		}

		LogicalSearchQuery findAbeille = new LogicalSearchQuery(from(rm.folder.schemaType()).where(rm.folder.title()).isEqualTo("Abeille"));
		LogicalSearchQuery findEvents = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.type()).isContainingText("folder"));
		LogicalSearchQuery findDocuments = new LogicalSearchQuery(from(rm.document.schemaType())
				.where(rm.document.hasContent()).isTrue()
				.andWhere(rm.document.title()).isEqualTo("test"));
		assertThat(rm.searchFolders(findAbeille).stream().map(r -> r.getId()).collect(toList())).containsOnly("A01");
		assertThat(rm.searchEvents(findEvents).stream().map(r -> r.getRecordId()).collect(toList())).contains("A01").contains("A05");

		Content content = contentManager.createMajor(userAlice, "Same-file", versionDataSummary).checkOut(userAlice);

		Record documentRecord = rm.newDocument().setFolder(rm.searchFolders(findAbeille).get(0))
				.setContent(content).setTitle("test").getWrappedRecord();

		try {
			rm.executeTransaction(new Transaction(documentRecord));
		} catch (RecordServicesException e) {
			throw new RuntimeException("Could not execute transaction2.");
		}

		List<Content> contents = rm.searchDocuments(findDocuments).stream().map(r -> r.getContent()).collect(toList());

		assertThat(contents.size()).isEqualTo(1);
		Content contentFromSearch = contents.get(0);
		assertThat(contentFromSearch.getCheckoutUserId()).isEqualTo("Alice");
		ensureNoIntegrityErrors();

		new FixRecordIdsExecutor(getAppLayerFactory()).fixAllIds(true, true);

		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		Toggle.CACHES_ENABLED.enable();
		restartLayers();

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		List<String> abeilleNow = rm.searchFolders(findAbeille).stream().map(r -> r.getId()).collect(toList());
		assertThat(abeilleNow).doesNotContain("A01").hasSize(1);
		ensureNoIntegrityErrors();

		findEvents = new LogicalSearchQuery(from(rm.event.schemaType()).where(rm.event.type()).isContainingText("folder"));
		assertThat(rm.searchEvents(findEvents).stream().map(r -> r.getRecordId()).collect(toList())).doesNotContain("A01").doesNotContain("A05");
		
		findDocuments = new LogicalSearchQuery(from(rm.document.schemaType())
				.where(rm.document.hasContent()).isTrue()
				.andWhere(rm.document.title()).isEqualTo("test"));

		contents = rm.searchDocuments(findDocuments).stream().map(r -> r.getContent()).collect(toList());

		assertThat(contents.size()).isEqualTo(1);
		contentFromSearch = contents.get(0);
		assertThat(contentFromSearch.getCheckoutUserId()).isNotEqualTo("Alice");

	}

	@Test
	public void whenMigratingIdsThenDecomListDetailsAndValidationAreModified() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withRobotsModule().withConstellioESModule().withDocumentsDecommissioningList().withEvents());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		DecommissioningList decommissioningList = rm.searchDecommissioningLists(new LogicalSearchQuery(from(rm.decommissioningList.schemaType()).where(Schemas.IDENTIFIER).isNotNull())).get(0);
		assertThat(decommissioningList.getValidations()).isEmpty();

		User user = rm.newUserWithId("newUser");
		LocalDate now = LocalDate.now();
		decommissioningList.addValidationRequest(user, now);
		assertThat(decommissioningList.getValidations()).isNotEmpty();

		try {
			rm.executeTransaction(new Transaction(user, decommissioningList));
		} catch (RecordServicesException e) {
			throw new RuntimeException("Could not execute transaction2.");
		}

		List<DecommissioningList> oldDecommissionLists = rm.searchDecommissioningLists(new LogicalSearchQuery(from(rm.decommissioningList.schemaType()).where(Schemas.IDENTIFIER).isNotNull()));
		final Map<DecommissioningList, BiConsumer<DecommissioningList, Map<RecordId, RecordId>>> comparators = new HashMap<>();

		oldDecommissionLists.forEach(oldDecommissionList -> {
			final List<String> oldFolderIds = oldDecommissionList.getFolders();
			final List<DecomListFolderDetail> oldFolderDetails = oldDecommissionList.getFolderDetails();

			final List<String> oldContainerIds = oldDecommissionList.getContainers();
			final List<DecomListContainerDetail> oldContainerDetails = oldDecommissionList.getContainerDetails();

			final List<DecomListValidation> oldValidations = oldDecommissionList.getValidations();

			comparators.put(oldDecommissionList, (newDecommissionList, fromToMap) -> {
				List<String> newFolderIds = newDecommissionList.getFolders();
				List<DecomListFolderDetail> newFolderDetails = newDecommissionList.getFolderDetails();

				List<String> newContainerIds = newDecommissionList.getContainers();
				List<DecomListContainerDetail> newContainerDetails = newDecommissionList.getContainerDetails();

				final List<DecomListValidation> newValidations = newDecommissionList.getValidations();

				assertThat(newFolderIds.size()).isEqualTo(oldFolderIds.size());
				assertThat(newFolderIds).containsExactly(oldFolderIds.stream().map(oldFolder -> getNewIdFromOldId(oldFolder, fromToMap)).toArray(String[]::new));
				assertThat(newFolderDetails.stream().map(DecomListFolderDetail::getFolderId).collect(toList()))
						.containsExactly(oldFolderDetails.stream().map(DecomListFolderDetail::getFolderId).map(id -> getNewIdFromOldId(id, fromToMap)).toArray(String[]::new));
				oldFolderDetails.stream().forEach(oldFolderDetail -> {
					DecomListFolderDetail newFolderDetail = newFolderDetails.stream().filter(possibleNewFolderDetail -> possibleNewFolderDetail.getFolderId().equals(getNewIdFromOldId(oldFolderDetail.getFolderId(), fromToMap))).findFirst().get();

					if (newFolderDetail.getContainerRecordId() == null) {
						assertThat(newFolderDetail.getContainerRecordId()).isEqualTo(oldFolderDetail.getContainerRecordId());
					} else {
						assertThat(newFolderDetail.getContainerRecordId()).isEqualTo(getNewIdFromOldId(oldFolderDetail.getContainerRecordId(), fromToMap));
					}
				});

				assertThat(newContainerIds.size()).isEqualTo(oldContainerIds.size());
				assertThat(newContainerIds).containsExactly(oldContainerIds.stream().map(oldContainerId -> getNewIdFromOldId(oldContainerId, fromToMap)).toArray(String[]::new));
				assertThat(newContainerDetails.stream().map(DecomListContainerDetail::getContainerRecordId).collect(toList()))
						.containsExactly(oldContainerDetails.stream().map(DecomListContainerDetail::getContainerRecordId).map(id -> getNewIdFromOldId(id, fromToMap)).toArray(String[]::new));


				assertThat(newValidations.size()).isEqualTo(oldValidations.size());
				assertThat(newValidations.stream().map(DecomListValidation::getUserId).collect(toList()))
						.containsExactly(oldValidations.stream().map(DecomListValidation::getUserId).map(id -> getNewIdFromOldId(id, fromToMap)).toArray(String[]::new));
			});
		});

		ensureNoIntegrityErrors();

		final Map<RecordId, Record> oldRecordIdToRecordWithNewIdMap = new FixRecordIdsExecutor(getAppLayerFactory()).fixAllIds(true, true);
		final Map<RecordId, RecordId> fromToIdMaps = oldRecordIdToRecordWithNewIdMap.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getRecordId()));

		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		Toggle.CACHES_ENABLED.enable();
		restartLayers();

		final RMSchemasRecordsServices newRm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		ensureNoIntegrityErrors();

		oldDecommissionLists.forEach(oldDecommissiongList ->
				comparators
						.get(oldDecommissiongList)
						.accept(newRm.getDecommissioningList(getNewIdFromOldId(oldDecommissiongList.getId(), fromToIdMaps)), fromToIdMaps)
		);
	}

	@Test
	public void whenMigratingIdsThenBorrowingAreModified() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withRobotsModule().withConstellioESModule().withDocumentsDecommissioningList().withEvents());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		BorrowingServices borrowingServices = new BorrowingServices(zeCollection, getModelLayerFactory());
		User user = rm.newUserWithId("newUser").setCollectionAllAccess(true);
		LocalDate now = LocalDate.now();

		try {
			rm.executeTransaction(new Transaction(user));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}

		List<Record> folders = rm.searchFolders(new LogicalSearchQuery(from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isNotNull())).stream()
				.filter(folder -> {
					boolean canBorrow;
					try {
						borrowingServices.validateCanBorrow(user, folder, now);
						canBorrow = true;
					} catch (BorrowingServicesRunTimeException e) {
						canBorrow = false;
					}

					return canBorrow;
				})
				.map(RecordWrapper::getWrappedRecord).limit(5).collect(toList());

		assertThat(folders).isNotEmpty();
		try {
			borrowingServices.borrowFolders(folders, now, now.plusDays(2), user, user, BorrowingType.BORROW, false);
		} catch (RecordServicesException e) {
			throw new RuntimeException("Could not borrow folders.");
		}

		try {
			borrowingServices.borrowFolders(folders, now, now.plusDays(2), user, user, BorrowingType.BORROW, false);
			fail("Should not be able to borrow twice");
		} catch (RecordServicesException | BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed e) {
		}

		ensureNoIntegrityErrors();
		final Map<RecordId, Record> oldRecordIdToRecordWithNewIdMap = new FixRecordIdsExecutor(getAppLayerFactory()).fixAllIds(true, true);
		final Map<RecordId, RecordId> fromToIdMaps = oldRecordIdToRecordWithNewIdMap.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getRecordId()));

		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		Toggle.CACHES_ENABLED.enable();
		restartLayers();

		final RMSchemasRecordsServices newRm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		ensureNoIntegrityErrors();

	}

	@Test
	public void whenMigratingIdsThenRetentionRuleDocumentTypeAreModified() {
		final Map<RetentionRule, BiConsumer<RetentionRule, Map<RecordId, RecordId>>> comparators = new HashMap<>();

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent().withRobotsModule().withConstellioESModule().withDocumentsDecommissioningList().withEvents());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		List<RetentionRule> oldRetentionRules = rm.searchRetentionRules(new LogicalSearchQuery(from(rm.retentionRule.schemaType()).where(Schemas.IDENTIFIER).isNotNull()));


		oldRetentionRules.forEach(oldRetentionRule -> {
			final List<String> oldDocumentTypesIds = oldRetentionRule.getDocumentTypes();
			final List<RetentionRuleDocumentType> oldDocumentTypesDetails = oldRetentionRule.getDocumentTypesDetails();

			comparators.put(oldRetentionRule, (newRetentionRule, fromToIdMap) -> {
				List<String> newDocumentTypesIds = newRetentionRule.getDocumentTypes();
				List<RetentionRuleDocumentType> newDocumentTypesDetails = newRetentionRule.getDocumentTypesDetails();

				assertThat(newDocumentTypesIds.size()).isEqualTo(oldDocumentTypesIds.size());
				assertThat(newDocumentTypesIds).containsExactly(oldDocumentTypesIds.stream().map(id -> getNewIdFromOldId(id, fromToIdMap)).toArray(String[]::new));

				assertThat(newDocumentTypesDetails.size()).isEqualTo(oldDocumentTypesDetails.size());
				assertThat(newDocumentTypesDetails.stream().map(RetentionRuleDocumentType::getDocumentTypeId).collect(toList()))
						.containsExactly(newDocumentTypesDetails.stream().map(RetentionRuleDocumentType::getDocumentTypeId).toArray(String[]::new));
			});
		});

		ensureNoIntegrityErrors();
		final Map<RecordId, Record> oldRecordIdToRecordWithNewIdMap = new FixRecordIdsExecutor(getAppLayerFactory()).fixAllIds(true, true);
		final Map<RecordId, RecordId> fromToIdMaps = oldRecordIdToRecordWithNewIdMap.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().getRecordId()));

		Toggle.USE_CACHE_FOR_QUERY_EXECUTION.enable();
		Toggle.CACHES_ENABLED.enable();
		restartLayers();

		final RMSchemasRecordsServices newRm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);

		ensureNoIntegrityErrors();

		oldRetentionRules.forEach(oldRetentionRule ->
				comparators
						.get(oldRetentionRule)
						.accept(newRm.getRetentionRule(getNewIdFromOldId(oldRetentionRule.getId(), fromToIdMaps)), fromToIdMaps)
		);
	}

	public static String getNewIdFromOldId(String id, Map<RecordId, RecordId> fromToMap) {
		RecordId newId = fromToMap.get(fromToMap.keySet().stream().filter(key -> key.stringValue().equals(id)).findFirst().orElse(null));
		return newId != null ? newId.stringValue() : id;
	}

	public static File customGetCurrentFilePath(Class clazz, String partialName) {
		String completeName = clazz.getCanonicalName().replace(".", File.separator) + partialName;

		File file = new File(new FoldersLocator().getSDKProject(), "src" + File.separator + completeName);

		if (!file.exists()) {
			throw new RuntimeException("No such file '" + file.getAbsolutePath() + "'");
		}

		return file;
	}
}
