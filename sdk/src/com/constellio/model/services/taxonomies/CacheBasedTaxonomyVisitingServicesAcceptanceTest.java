package com.constellio.model.services.taxonomies;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheBasedTaxonomyVisitingServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	CacheBasedTaxonomyVisitingServices visitingServices;
	Users users = new Users();
	private TasksSchemasRecordsServices tasksSchemas;
	private Task task;
	private Task subTask;
	private Task subSubTask;
	private RecordServices recordServices;
	private User alice;
	private User chuck;
	private LocalDate now = LocalDate.now();
	SearchServices searchServices;

	@Test
	public void whenIteratingTaxonomiesFromCategoryThenIterateOverAll() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(records.getCategory_X().getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).contains(records.categoryId_X, records.categoryId_X100, records.folder_A24, records.document_A79).hasSize(323);
	}

	@Test
	public void whenIteratingTaxonomiesFromChildCategoryThenIterateOverAll() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(records.getCategory_X100().getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).contains(records.categoryId_X100, records.folder_A01, records.document_A79).hasSize(321);
	}

	@Test
	public void whenIteratingTaxonomiesFromFolderThenIterateOverAll() throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(records.getFolder_A79().getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).contains(records.folder_A79, records.document_A79).hasSize(6);
	}

	@Test
	public void whenIteratingTaxonomiesFromDocumentThenIterateOverAll() throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(records.getDocumentWithContent_A19().getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).containsExactly(records.document_A19);
	}

	@Test
	public void whenIteratingTaxonomiesFromContainerThenIterateOverAll() throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(records.getContainerBac14().get(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).containsExactly(records.containerId_bac14);
	}

	@Test
	public void whenIteratingTaxonomiesFromUnitThenIterateOverAll() throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(records.getUnit10().get(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).contains(records.unitId_10, records.unitId_10a, records.folder_A01, records.document_A79).hasSize(323);
	}

	@Test
	public void whenIteratingTaxonomiesFromChildUnitThenIterateOverAll() throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(records.getUnit10a().get(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).contains(records.unitId_10a, records.folder_A01, records.document_A19).hasSize(233);
	}

	@Test
	public void whenIteratingTaxonomiesFromTaskThenIterateOverAll() throws Exception {
		createTasksStructure();
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(task.getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).contains(task.getId(), subTask.getId(), subSubTask.getId()).hasSize(3);
	}

	@Test
	public void whenIteratingTaxonomiesFromSubTaskThenIterateOverAll() throws Exception {
		createTasksStructure();
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(subTask.getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).contains(subTask.getId(), subSubTask.getId()).hasSize(2);
	}

	@Test
	public void whenIteratingTaxonomiesFromSubSubTaskThenIterateOverAll() throws Exception {
		createTasksStructure();
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(subSubTask.getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE;
		});
		assertThat(visitedIds).containsOnly(subSubTask.getId());
	}

	@Test
	public void whenIteratingTaxonomiesFromTaskWithStatusStop() throws Exception {
		createTasksStructure();
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(task.getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.STOP;
		});
		assertThat(visitedIds).containsOnly(task.getId());
	}

	@Test
	public void whenIteratingTaxonomiesFromSubTaskWithStatusContinueNoDeeperInThisNode() throws Exception {
		createTasksStructure();
		visitingServices = new CacheBasedTaxonomyVisitingServices(getModelLayerFactory());
		List<String> visitedIds = new ArrayList<>();
		visitingServices.visit(subTask.getWrappedRecord(), item -> {
			visitedIds.add(item.getRecord().getId());
			return TaxonomyVisitingStatus.CONTINUE_NO_DEEPER_IN_THIS_NODE;
		});
		assertThat(visitedIds).containsOnly(subTask.getId());
	}

	private void createTasksStructure() throws Exception {

		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));
		givenTimeIs(now);
		inCollection(zeCollection).giveWriteAndDeleteAccessTo(admin);

		recordServices = getModelLayerFactory().newRecordServices();
		tasksSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());

		searchServices = getModelLayerFactory().newSearchServices();
		alice = users.aliceIn(zeCollection);
		chuck = users.chuckNorrisIn(zeCollection);

		Transaction transaction = new Transaction();
		task = tasksSchemas.newTask().setTitle("taskAssignedByChuckToAlice").setAssigner(chuck.getId())
				.setAssignationDate(now).setAssignee(alice.getId());
		transaction.add(task.setCreatedBy(chuck.getId()));

		subTask = tasksSchemas.newTask().setTitle("subTaskAssignedByChuckToAlice")
				.setParentTask(task.getId());
		transaction.add(subTask);

		subSubTask = tasksSchemas.newTask().setTitle("subSubTaskAssignedByChuckToAlice")
				.setParentTask(subTask.getId());
		transaction.add(subSubTask);

		recordServices.execute(transaction);
	}
}
