package com.constellio.app.modules.rm.ui.pages.folder;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static org.mockito.Mockito.*;

public class AddEditFolderFolderPresenterAcceptTest extends ConstellioTest {

	@Mock AddEditFolderView view;
	MockedNavigation navigator;
	RMTestRecords records = new RMTestRecords(zeCollection);
	AddEditFolderPresenter presenter;
	SessionContext sessionContext;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	MetadataSchemasManager metadataSchemasManager;
	RecordServices recordServices;

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

		sessionContext = FakeSessionContext.chuckNorrisInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		navigator = new MockedNavigation();
		when(view.navigate()).thenReturn(navigator);

		presenter = spy(new AddEditFolderPresenter(view));

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
		presenter.forParams("");
		FolderVO folderVO = buildFolderVO();
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("id%3D"+folderVO.getId());
		folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));
	}

	@Test
	public void givenEditedFolderInNewContainerWithoutEnoughSizeThanErrorThrown() {
		presenter.forParams("");
		FolderVO folderVO = buildFolderVO();
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("id%3D"+folderVO.getId());
		folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(101.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, times(1)).showErrorMessage(any(String.class));
	}

	@Test
	public void givenEditedFolderInOldContainerWithEnoughSizeThanNoErrorMessage() {
		presenter.forParams("testBoite100");
		FolderVO folderVO = buildFolderVO();
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("id%3D"+folderVO.getId());
		folderVO = buildFolderVO();
		folderVO.setContainer("testBoite100");
		folderVO.setLinearSize(100.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));
	}

	@Test
	public void givenEditedFolderInOldContainerWithoutEnoughSizeThanErrorThrown() {
		presenter.forParams("testBoite100");
		FolderVO folderVO = buildFolderVO();
		folderVO.setLinearSize(50.0);
		doReturn(folderVO).when(presenter).getFolderVO();
		presenter.saveButtonClicked();
		verify(view, never()).showErrorMessage(any(String.class));

		presenter.forParams("id%3D"+folderVO.getId());
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
