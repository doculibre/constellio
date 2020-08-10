package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.services.RMRecordDeletionServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementViewImpl;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by Constellio on 2017-03-16.
 */
// Confirm @SlowTest
public class RMCleanAdministrativeUnitButtonExtensionAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	RecordServices recordServices;
	RMSchemasRecordsServices rm;
	RMCleanAdministrativeUnitButtonExtension extension;
	@Mock TaxonomyManagementViewImpl view;
	@Mock RecordVO recordVO;
	SessionContext sessionContext;
	MockedNavigation navigator;
	Users users = new Users();

	@Before
	public void setup() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioESModule().withTasksModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();
		recordServices = getModelLayerFactory().newRecordServices();
		extension = new RMCleanAdministrativeUnitButtonExtension(zeCollection, getAppLayerFactory());

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.navigate()).thenReturn(navigator);
		when(recordVO.getId()).thenReturn(records.unitId_30c);
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

		extension.decorateMainComponentBeforeViewAssembledOnViewEntered(new DecorateMainComponentAfterInitExtensionParams(view));
		extension.cleanAdministrativeUnitButtonClicked(recordVO, users.adminIn(zeCollection));

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

	@Test
	public void whenCleaningAdministrativeUnitWithOnlyOneRuleCheckIfRetentionRuleIsNotLongerLinkAndIsNowResponsible()
			throws Exception {
		AdministrativeUnit administrativeUnit = rm.wrapAdministrativeUnit(searchServices.searchSingleResult(from(rm.administrativeUnit.schema())
				.where(rm.administrativeUnit.code()).isEqualTo("10A")));
		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

		CopyRetentionRule principal5_2_T = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");
		CopyRetentionRule secondary2_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "2-0-D");
		RetentionRule retentionRule = rm.newRetentionRule()
				.setTitle("rule1")
				.setCode("r1")
				.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D))
				.setAdministrativeUnits(Collections.singletonList(administrativeUnit));

		Transaction t = new Transaction();
		t.add(retentionRule);
		recordServices.execute(t);

		assertThat(retentionRule.getAdministrativeUnits()).hasSize(1).containsOnly(administrativeUnit.getId());

		RMRecordDeletionServices.cleanAdministrativeUnit(zeCollection, administrativeUnit.getId(), getAppLayerFactory());
		RetentionRule rule = rm.getRetentionRule(retentionRule.getId());
		assertThat(rule).isNotNull();
		assertThat(rule.isResponsibleAdministrativeUnits()).isTrue();
	}

	@Test
	public void whenCleaningAdministrativeUnitWithOnlyOneRuleButMultipleAdministrativeUnitCheckIfRetentionRuleIsNotLongerLinkAndIsNotResponsible()
			throws Exception {
		AdministrativeUnit administrativeUnit1 = records.getUnit10();
		AdministrativeUnit administrativeUnit2 = records.getUnit11();
		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

		CopyRetentionRule principal5_2_T = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");
		CopyRetentionRule secondary2_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "2-0-D");
		RetentionRule retentionRule = rm.newRetentionRule()
				.setTitle("rule1")
				.setCode("r1")
				.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D))
				.setAdministrativeUnits(asList(administrativeUnit1, administrativeUnit2));

		Transaction t = new Transaction();
		t.add(retentionRule);
		recordServices.execute(t);

		assertThat(retentionRule.getAdministrativeUnits()).hasSize(2).containsOnly(administrativeUnit1.getId(), administrativeUnit2.getId());

		RMRecordDeletionServices.cleanAdministrativeUnit(zeCollection, administrativeUnit1.getId(), getAppLayerFactory());
		RetentionRule rule = rm.getRetentionRule(retentionRule.getId());
		assertThat(rule).isNotNull();
		assertThat(rule.isResponsibleAdministrativeUnits()).isFalse();
		assertThat(rule.getAdministrativeUnits()).hasSize(1).containsOnly(administrativeUnit2.getId());
	}

	@Test
	public void whenCleaningAdministrativeUnitWithMultipleRulesCheckIfRetentionRulesAreNoLongerLinked()
			throws Exception {
		AdministrativeUnit administrativeUnit1 = records.getUnit10();
		CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();
		CopyRetentionRule principal5_2_T = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");
		CopyRetentionRule secondary2_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "2-0-D");
		List<RetentionRule> rules = asList(
				rm.newRetentionRule()
						.setTitle("rule1")
						.setCode("r1")
						.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D))
						.setAdministrativeUnits(Collections.singletonList(administrativeUnit1)),
				rm.newRetentionRule()
						.setTitle("rule2")
						.setCode("r2")
						.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D))
						.setAdministrativeUnits(asList(administrativeUnit1, records.getUnit11b())),
				rm.newRetentionRule()
						.setTitle("rule3")
						.setCode("r3")
						.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D))
						.setAdministrativeUnits(Collections.singletonList(administrativeUnit1))
		);
		Transaction t = new Transaction();
		t.addAll(rules);
		recordServices.execute(t);

		List<String> ids = new ArrayList<>();
		for (RetentionRule retentionRule : rules) {
			if (retentionRule.getCode().equals("r2")) {
				assertThat(retentionRule.getAdministrativeUnits()).hasSize(2).containsOnly(administrativeUnit1.getId(), records.getUnit11b().getId());
			} else {
				assertThat(retentionRule.getAdministrativeUnits()).hasSize(1).containsOnly(administrativeUnit1.getId());

			}
			ids.add(retentionRule.getId());
		}

		RMRecordDeletionServices.cleanAdministrativeUnit(zeCollection, administrativeUnit1.getId(), getAppLayerFactory());

		List<RetentionRule> updatedRules = rm.getRetentionRules(ids);
		for (RetentionRule updatedRule : updatedRules) {
			if (updatedRule.getCode().equals("r2")) {
				assertThat(updatedRule.getAdministrativeUnits()).hasSize(1).containsOnly(records.getUnit11b().getId());
				assertThat(updatedRule.isResponsibleAdministrativeUnits()).isFalse();
			} else {
				assertThat(updatedRule.getAdministrativeUnits()).isEmpty();
				assertThat(updatedRule.isResponsibleAdministrativeUnits()).isTrue();
			}
		}
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
				where(rm.decommissioningList.folders()).isContaining(folderIDs),
				where(rm.decommissioningList.documents()).isContaining(documentIDs),
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
