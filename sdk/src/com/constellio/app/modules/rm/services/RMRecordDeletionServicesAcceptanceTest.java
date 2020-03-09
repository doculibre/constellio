package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Constelio on 2016-11-01.
 */
@SlowTest
public class RMRecordDeletionServicesAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	RecordServices recordServices;
	RMSchemasRecordsServices rm;

	@Before
	public void setup() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withTasksModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenCleaningAnAdministrativeUnitThenFoldersAndDocumentsNoLongerExist() {
		Record administrativeUnit = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
				.where(rm.administrativeUnit.code()).isEqualTo("30C"));

		long oldTotalNumFolder = searchServices.getResultsCount(from(rm.folder.schema()).returnAll());
		long oldTotalNumDocument = searchServices.getResultsCount(from(rm.document.schema()).returnAll());
		long oldNumFolderInAdminUnit = searchServices.getResultsCount(from(rm.folder.schema()).where(Schemas.PRINCIPAL_PATH)
				.isContainingText(administrativeUnit.getId()));
		long oldNumDocumentInAdminUnit = searchServices.getResultsCount(from(rm.document.schema()).where(Schemas.PRINCIPAL_PATH)
				.isContainingText(administrativeUnit.getId()));
		assertThat(oldTotalNumFolder).isNotEqualTo(0);
		assertThat(oldTotalNumDocument).isNotEqualTo(0);
		assertThat(oldNumFolderInAdminUnit).isNotEqualTo(0);
		assertThat(oldNumDocumentInAdminUnit).isNotEqualTo(0);

		RMRecordDeletionServices.cleanAdministrativeUnit(zeCollection, administrativeUnit.getId(), getAppLayerFactory());

		long newTotalNumFolder = searchServices.getResultsCount(from(rm.folder.schema()).returnAll());
		long newTotalNumDocument = searchServices.getResultsCount(from(rm.document.schema()).returnAll());
		long newNumFolderInAdminUnit = searchServices.getResultsCount(from(rm.folder.schema()).where(Schemas.PRINCIPAL_PATH)
				.isContainingText(administrativeUnit.getId()));
		long newNumDocumentInAdminUnit = searchServices.getResultsCount(from(rm.document.schema()).where(Schemas.PRINCIPAL_PATH)
				.isContainingText(administrativeUnit.getId()));
		assertThat(newNumFolderInAdminUnit).isEqualTo(0);
		assertThat(newNumDocumentInAdminUnit).isEqualTo(0);
		assertThat(newTotalNumFolder).isEqualTo(oldTotalNumFolder - oldNumFolderInAdminUnit);
		assertThat(newTotalNumDocument).isEqualTo(oldTotalNumDocument - oldNumDocumentInAdminUnit);
	}

	@Test
	public void whenCleaningAnAdministrativeUnitThenContainersNoLongerExist() {
		Record administrativeUnit = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
				.where(rm.administrativeUnit.code()).isEqualTo("30C"));

		long oldTotalNumContainer = searchServices.getResultsCount(from(rm.containerRecord.schema()).returnAll());
		long oldNumContainerInAdminUnit = searchServices.getResultsCount(from(rm.containerRecord.schema()).where(Schemas.PRINCIPAL_PATH)
				.isContainingText(administrativeUnit.getId()));
		assertThat(oldTotalNumContainer).isNotEqualTo(0);
		assertThat(oldNumContainerInAdminUnit).isNotEqualTo(0);

		RMRecordDeletionServices.cleanAdministrativeUnit(zeCollection, administrativeUnit.getId(), getAppLayerFactory());

		long newTotalNumContainer = searchServices.getResultsCount(from(rm.containerRecord.schema()).returnAll());
		long newNumContainerInAdminUnit = searchServices.getResultsCount(from(rm.containerRecord.schema()).where(Schemas.PRINCIPAL_PATH)
				.isContainingText(administrativeUnit.getId()));
		assertThat(newNumContainerInAdminUnit).isEqualTo(0);
		assertThat(newTotalNumContainer).isEqualTo(oldTotalNumContainer - oldNumContainerInAdminUnit);
	}

	@Test
	public void whenCleaningAnAdministrativeUnitThenDecommissioningListAreDeleted() {
		Record administrativeUnit = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
				.where(rm.administrativeUnit.code()).isEqualTo("10A"));
		Record folder = searchServices.search(new LogicalSearchQuery().setCondition(
				from(rm.folder.schema()).where(Schemas.IDENTIFIER).isEqualTo("A49"))).get(0);
		Record document = searchServices.search(new LogicalSearchQuery().setCondition(
				from(rm.document.schema()).where(Schemas.TITLE)
						.isEqualTo("Grenouille - Document procès verbal numérique avec un autre exemplaire"))).get(0);
		Record container = searchServices.search(new LogicalSearchQuery().setCondition(
				from(rm.containerRecord.schema()).where(Schemas.TITLE).isEqualTo("10_A_04"))).get(0);

		List<DecommissioningList> decommissioningLists = getDecommissioningListsThatContainsAnyOf(asList(folder.getId()),
				asList(document.getId()), asList(container.getId()));
		assertThat(decommissioningLists).isNotEmpty();

		RMRecordDeletionServices.cleanAdministrativeUnit(zeCollection, administrativeUnit.getId(), getAppLayerFactory());

		List<DecommissioningList> decommissioningListsAfterCleaning = rm.searchDecommissioningLists(
				where(Schemas.IDENTIFIER).isIn(extractIdentifier(decommissioningLists)));
		assertThat(decommissioningListsAfterCleaning).isEmpty();

		long numberOfDecomListContainingRecords = getDecommissioningListsThatContainsAnyOf(asList(folder.getId()),
				asList(document.getId()), asList(container.getId())).size();
		assertThat(numberOfDecomListContainingRecords).isEqualTo(0);
	}

	@Test
	public void whenCleaningAnAdministrativeUnitThenLinkedTaskAreDeleted()
			throws Exception {
		Record administrativeUnitContainingTasks = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
				.where(rm.administrativeUnit.code()).isEqualTo("30C"));
		Record document = searchServices.search(new LogicalSearchQuery().setCondition(
				from(rm.document.schema()).where(Schemas.TITLE)
						.isEqualTo("Grenouille - Document procès verbal numérique avec un autre exemplaire"))).get(0);
		Record folder = searchServices.search(new LogicalSearchQuery().setCondition(
				from(rm.folder.schema()).where(Schemas.IDENTIFIER).isEqualTo("A49"))).get(0);

		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RMTask taskDocument = new RMTask(taskSchemas.newTask().setTitle("taskDocument")).setLinkedDocuments(asList(document.getId()))
				.setAdministrativeUnit(administrativeUnitContainingTasks.getId());
		RMTask taskFolder = new RMTask(taskSchemas.newTask().setTitle("taskFolder")).setLinkedFolders(asList(folder.getId()))
				.setAdministrativeUnit(administrativeUnitContainingTasks.getId());
		RMTask taskWithBoth = new RMTask(taskSchemas.newTask().setTitle("taskWithBoth")).setLinkedDocuments(asList(document.getId()))
				.setLinkedFolders(asList(folder.getId())).setAdministrativeUnit(administrativeUnitContainingTasks.getId());
		recordServices.add(taskDocument.getWrappedRecord());
		recordServices.add(taskFolder.getWrappedRecord());
		recordServices.add(taskWithBoth.getWrappedRecord());

		RMRecordDeletionServices.cleanAdministrativeUnit(zeCollection, administrativeUnitContainingTasks.getId(), getAppLayerFactory());

		Record newTaskDocument = searchServices.searchSingleResult(from(taskSchemas.userTask.schema())
				.where(Schemas.IDENTIFIER).isEqualTo(taskDocument.getId()));
		Record newTaskFolder = searchServices.searchSingleResult(from(taskSchemas.userTask.schema())
				.where(Schemas.IDENTIFIER).isEqualTo(taskDocument.getId()));
		Record newTaskWithBoth = searchServices.searchSingleResult(from(taskSchemas.userTask.schema())
				.where(Schemas.IDENTIFIER).isEqualTo(taskDocument.getId()));
		assertThat(newTaskDocument).isNull();
		assertThat(newTaskFolder).isNull();
		assertThat(newTaskWithBoth).isNull();
	}

	@Test
	public void whenCleaningAnAdministrativeUnitThenTasksFromOtherUnitUnlinkReferencedDocsAndFolders()
			throws Exception {
		Record administrativeUnitContainingTasks = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
				.where(rm.administrativeUnit.code()).isEqualTo("30C"));
		Record administrativeUnitCleaned = searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
				.where(rm.administrativeUnit.code()).isEqualTo("10A"));
		Record document = searchServices.search(new LogicalSearchQuery().setCondition(
				from(rm.document.schema()).where(Schemas.TITLE)
						.isEqualTo("Grenouille - Document procès verbal numérique avec un autre exemplaire"))).get(0);
		Record folder = searchServices.search(new LogicalSearchQuery().setCondition(
				from(rm.folder.schema()).where(Schemas.IDENTIFIER).isEqualTo("A49"))).get(0);

		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(zeCollection, getAppLayerFactory());
		RMTask taskDocument = new RMTask(taskSchemas.newTask().setTitle("taskDocument")).setLinkedDocuments(asList(document.getId()))
				.setAdministrativeUnit(administrativeUnitContainingTasks.getId());
		RMTask taskFolder = new RMTask(taskSchemas.newTask().setTitle("taskFolder")).setLinkedFolders(asList(folder.getId()))
				.setAdministrativeUnit(administrativeUnitContainingTasks.getId());
		RMTask taskWithBoth = new RMTask(taskSchemas.newTask().setTitle("taskWithBoth")).setLinkedDocuments(asList(document.getId()))
				.setLinkedFolders(asList(folder.getId())).setAdministrativeUnit(administrativeUnitContainingTasks.getId());
		recordServices.add(taskDocument.getWrappedRecord());
		recordServices.add(taskFolder.getWrappedRecord());
		recordServices.add(taskWithBoth.getWrappedRecord());

		assertThat(getLinkedRecords(taskDocument)).hasSize(1);
		assertThat(getLinkedRecords(taskFolder)).hasSize(1);
		assertThat(getLinkedRecords(taskWithBoth)).hasSize(2);

		RMRecordDeletionServices.cleanAdministrativeUnit(zeCollection, administrativeUnitCleaned.getId(), getAppLayerFactory());

		Record newTaskDocument = searchServices.searchSingleResult(from(taskSchemas.userTask.schema())
				.where(Schemas.IDENTIFIER).isEqualTo(taskDocument.getId()));
		Record newTaskFolder = searchServices.searchSingleResult(from(taskSchemas.userTask.schema())
				.where(Schemas.IDENTIFIER).isEqualTo(taskDocument.getId()));
		Record newTaskWithBoth = searchServices.searchSingleResult(from(taskSchemas.userTask.schema())
				.where(Schemas.IDENTIFIER).isEqualTo(taskDocument.getId()));

		assertThat(newTaskDocument).isNotNull();
		assertThat(newTaskFolder).isNotNull();
		assertThat(newTaskWithBoth).isNotNull();
		assertThat(getLinkedRecords(newTaskDocument)).isEmpty();
		assertThat(getLinkedRecords(newTaskFolder)).isEmpty();
		assertThat(getLinkedRecords(newTaskWithBoth)).isEmpty();
	}

	public List<String> extractIdentifier(List<DecommissioningList> decommissioningLists) {
		List<String> identifierList = new ArrayList<>();
		for (DecommissioningList decommissioningList : decommissioningLists) {
			identifierList.add(decommissioningList.getId());
		}
		return identifierList;
	}

	public List<DecommissioningList> getDecommissioningListsThatContainsAnyOf(List<String> folderIDs,
																			  List<String> documentIDs,
																			  List<String> containerIDs) {
		return rm.searchDecommissioningLists(anyConditions(
				//TODO::JOLA-where(rm.decommissioningList.folders()).isContaining(folderIDs),
				//TODO::JOLA-where(rm.decommissioningList.documents()).isContaining(documentIDs),
				where(rm.decommissioningList.containers()).isContaining(containerIDs)
		));
	}

	public List<String> getLinkedRecords(RMTask task) {
		ArrayList<String> linkedRecords = new ArrayList<>(task.getLinkedDocuments());
		linkedRecords.addAll(task.getLinkedFolders());
		return linkedRecords;
	}

	public List<String> getLinkedRecords(Record task) {
		MetadataSchema schema = getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getSchema(task.getSchemaCode());
		List<String> linkedDocuments = task.get(schema.getMetadata(RMTask.LINKED_DOCUMENTS));
		List<String> linkedFolder = task.get(schema.getMetadata(RMTask.LINKED_DOCUMENTS));
		ArrayList<String> linkedRecords = new ArrayList<>();
		linkedRecords.addAll(linkedDocuments);
		linkedRecords.addAll(linkedFolder);
		return linkedRecords;
	}
}
