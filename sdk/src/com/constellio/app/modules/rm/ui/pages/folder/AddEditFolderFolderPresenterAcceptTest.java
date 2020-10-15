package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddEditFolderFolderPresenterAcceptTest extends ConstellioTest {

	@Mock AddEditFolderView view;
	MockedNavigation navigator;
	RMTestRecords records = new RMTestRecords(zeCollection);
	AddEditFolderPresenter presenter;
	SessionContext sessionContext;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;
	UserServices userServices;
	@Mock FolderRecordActionsServices folderRecordActionsServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		navigator = new MockedNavigation();
		when(view.navigate()).thenReturn(navigator);

		presenter = spy(new AddEditFolderPresenter(view, null));
		when(presenter.newFolderRecordActionsServices()).thenReturn(folderRecordActionsServices);

		doNothing().when(presenter).navigateToFolderDisplay(any(String.class));

		doReturn("type1").when(presenter).getTypeFieldValue();
		doNothing().when(presenter).commitForm();
		doNothing().when(presenter).reloadForm();

		buildDefaultContainer();
	}

	@Test
	public void givenNewFolderTypeLinkedToNewFolderSchemaWhenReloadFormWithNewTypeThenDoNotSetValueInAutomaticMetadatas()
			throws Exception {

		givenNewFolderTypeLinkedToNewFolderSchema();

		presenter.forParams("");
		presenter.reloadFormAfterFolderTypeChange();
	}

	@Test
	public void givenNewFolderInContainerWithEnoughSizeThanNoErrorMessage() {
		presenter.forParams("");
		FolderVO folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("");
		folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(100.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));
	}

	@Test
	public void givenNewFolderInContainerWithoutEnoughSizeThanErrorThrown() {
		presenter.forParams("");
		FolderVO folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(101.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, times(1)).showErrorMessage(any(String.class));
	}

	@Test
	public void givenEditedFolderInNewContainerWithEnoughSizeThanNoErrorMessage() {
		when(folderRecordActionsServices.isEditActionPossible(any(Record.class), any(User.class))).thenReturn(true);
		
		presenter.forParams("");
		FolderVO folderVO = buildFolderVO();
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("id%3D" + folderVO.getId());
		folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));
	}

	@Test
	public void givenEditedFolderInNewContainerWithoutEnoughSizeThanErrorThrown() {
		when(folderRecordActionsServices.isEditActionPossible(any(Record.class), any(User.class))).thenReturn(true);
		
		presenter.forParams("");
		FolderVO folderVO = buildFolderVO();
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("id%3D" + folderVO.getId());
		folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(101.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, times(1)).showErrorMessage(any(String.class));
	}

	@Test
	public void givenEditedFolderInOldContainerWithEnoughSizeThanNoErrorMessage() {
		when(folderRecordActionsServices.isEditActionPossible(any(Record.class), any(User.class))).thenReturn(true);
		
		presenter.forParams("testBoite100");
		FolderVO folderVO = buildFolderVO();
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("id%3D" + folderVO.getId());
		folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(100.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));
	}

	@Test
	public void givenEditedFolderInOldContainerWithoutEnoughSizeThanErrorThrown() {
		when(folderRecordActionsServices.isEditActionPossible(any(Record.class), any(User.class))).thenReturn(true);
		
		presenter.forParams("testBoite100");
		FolderVO folderVO = buildFolderVO();
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("id%3D" + folderVO.getId());
		folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(101.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, times(1)).showErrorMessage(any(String.class));
	}

	private void givenNewFolderTypeLinkedToNewFolderSchema()
			throws Exception {
		givenNewCustomFolderSchema();

		FolderType folderType1 = rmSchemasRecordsServices.newFolderTypeWithId("type1");
		folderType1.setCode("type1");
		folderType1.setDescription("type1");
		folderType1.setTitle("type1");
		folderType1.setLinkedSchema("folder_USRcustom1");
		recordServices.add(folderType1);
	}

	private void givenNewCustomFolderSchema()
			throws OptimisticLocking {
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder typesBuilder = metadataSchemasManager.modify(zeCollection);
		typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).createCustomSchema("USRcustom1");
		metadataSchemasManager.saveUpdateSchemaTypes(typesBuilder);
	}

	@Test
	public void givenSearchableDateMetadataThenInfoIsOnlyFoundWhenNeeded() throws Exception {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.OPENING_DATE).setSearchable(true);
			}
		});
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.DATE_FORMAT, "yyyy-MM-dd");
		recordServices.update(records.getFolder_A01().setOpenDate(new LocalDate("2018-02-11")));
		reindex();
		waitForBatchProcess();

		List<String> queriesWithResults = getQueriesWithResults(asList("2018-02-11", "11-02-2018", "2018-02", "2018-11", "2018", "02", "11", "2", "1", "asdf"));
		assertThat(queriesWithResults).containsOnly("2018-02-11", "2018");

		getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.DATE_FORMAT, "dd-MM-yyyy");
		reindex();
		waitForBatchProcess();

		queriesWithResults = getQueriesWithResults(asList("2018-02-11", "11-02-2018", "2018-02", "2018-11", "2018", "02", "11", "2", "1", "asdf"));
		assertThat(queriesWithResults).containsOnly("11-02-2018", "2018");
	}

	@Test
	public void givenSearchableMultivalueDateMetadataThenInfoIsOnlyFoundWhenNeeded() throws Exception {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create("test").setMultivalue(true).setType(MetadataValueType.DATE).setSearchable(true);
			}
		});
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.DATE_FORMAT, "yyyy-MM-dd");
		Folder folder1 = records.getFolder_A01().set("test", asList(new LocalDate("2018-02-11")));
		recordServices.update(folder1);
		reindex();
		waitForBatchProcess();

		List<String> queriesWithResults = getQueriesWithResults(asList("2018-02-11", "11-02-2018", "2018-02", "2018-11", "2018", "02", "11", "2", "1", "asdf"));
		assertThat(queriesWithResults).containsOnly("2018-02-11", "2018");

		Folder folder2 = records.getFolder_A01().set("test", asList(new LocalDate("2019-03-12"), new LocalDate("2020-04-13")));
		recordServices.update(folder2);
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.DATE_FORMAT, "dd-MM-yyyy");
		reindex();
		waitForBatchProcess();

		queriesWithResults = getQueriesWithResults(asList("2019-03-12", "12-03-2019", "2019-03", "2019-12", "2019", "03", "12", "3", "1", "asdf",
				"2020-04-13", "13-04-2020", "2020-04", "2020-13", "2020", "04", "13", "4", "2018-02-11"));
		assertThat(queriesWithResults).containsOnly("12-03-2019", "2019", "13-04-2020", "2020");
	}

	private List<String> getQueriesWithResults(List<String> possibleQueries) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(LogicalSearchQueryOperators.from(rmSchemasRecordsServices.folder.schemaType()).returnAll());
		List<String> queriesWithResults = new ArrayList<>();
		for (String freeText : possibleQueries) {
			if (searchServices.searchRecordIds(query.setFreeTextQuery(freeText)).contains(records.getFolder_A01().getId())) {
				queriesWithResults.add(freeText);
			}
		}

		return queriesWithResults;
	}

	private FolderVO buildFolderVO() {
		FolderVO folderVO = new FolderToVOBuilder().build(presenter.newRecord(), RecordVO.VIEW_MODE.FORM, view.getSessionContext());
		folderVO.setAdministrativeUnit(records.unitId_10);
		folderVO.setCategory(records.categoryId_X);
		folderVO.setRetentionRule(records.ruleId_1);
		folderVO.setTitle("newFolder");
		return folderVO;
	}

	private void buildDefaultContainer() throws RecordServicesException {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices.add(rm.newContainerRecordWithId("testBoite100").setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE)
				.setAdministrativeUnit(records.getUnit10a()).setTitle("testBoite100").setTemporaryIdentifier("testBoite100")
				.setCapacity(100D).setType(recordServices.getDocumentById(records.containerTypeId_boite22x22)));
	}
}
