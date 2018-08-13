package com.constellio.app.ui.pages.unicitymetadataconf;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.summarycolumn.SummaryConfigPresenter;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.MockedNavigation;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class FolderUniqueKeyConfiguratiorPresenterAcceptanceTest extends ConstellioTest {
	@Mock
	FolderUniqueKeyConfiguratorView view;
	MockedNavigation navigator;
	@Mock
	SessionContext sessionContext;

	FolderUniqueKeyConfiguratorPresenter folderUniqueKeyConfiguratorPresenter;
	Users users;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rmSchemasRecordsServices;

	@Before
	public void setUp() {
		users = new Users();
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users));

		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

		folderUniqueKeyConfiguratorPresenter = new FolderUniqueKeyConfiguratorPresenter(view, Folder.DEFAULT_SCHEMA);

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenOneMetatSummaryThenMetadataHaveMetadataSummy() {

		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		folderUniqueKeyConfiguratorPresenter.getMetadatas();
		FolderUniqueKeyParams folderUniqueKeyParams = new FolderUniqueKeyParams();
		folderUniqueKeyParams.setMetadataVO(builder.build(Schemas.TITLE, FakeSessionContext.adminInCollection(zeCollection)));

		folderUniqueKeyConfiguratorPresenter.addMetadaForUnicity(folderUniqueKeyParams);

		Metadata metadata = folderUniqueKeyConfiguratorPresenter.getFolderUnicityMetadata();

		List summaryComlomnList = (List) metadata.getCustomParameter().get(FolderUniqueKeyConfiguratorPresenter.UNIQUE_KEY_CONFIG);

		assertThat(summaryComlomnList).isNotNull();
		assertThat(summaryComlomnList.size()).isEqualTo(1);
		assertThat(folderUniqueKeyParams.getMetadataVO().getCode()).isEqualTo(Schemas.TITLE.getCode());
	}

	@Test
	public void givenOneMetatSummaryInFolderEmployeThenMetadataHaveMetadataSummy() {
		FolderUniqueKeyConfiguratorPresenter folderUnicityMetadataParams = new FolderUniqueKeyConfiguratorPresenter(view, "folder_employe");

		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		FolderUniqueKeyParams folderUniqueKeyParams1 = new FolderUniqueKeyParams();
		folderUniqueKeyParams1.setMetadataVO(builder.build(rmSchemasRecordsServices.folder.description(), FakeSessionContext.adminInCollection(zeCollection)));

		folderUniqueKeyConfiguratorPresenter.addMetadaForUnicity(folderUniqueKeyParams1);

		folderUnicityMetadataParams.getMetadatas();
		FolderUniqueKeyParams folderUniqueKeyParams2 = new FolderUniqueKeyParams();
		folderUniqueKeyParams2.setMetadataVO(builder.build(rmSchemasRecordsServices.folderSchemaType().getSchema("employe").get("title"), FakeSessionContext.adminInCollection(zeCollection)));

		folderUnicityMetadataParams.addMetadaForUnicity(folderUniqueKeyParams2);

		Metadata metadata = folderUnicityMetadataParams.getFolderUnicityMetadata();

		List summaryColomnList = (List) metadata.getCustomParameter().get(FolderUniqueKeyConfiguratorPresenter.UNIQUE_KEY_CONFIG);

		Map<String, Object> summarymap = (Map<String, Object>) summaryColomnList.get(0);

		assertThat(summaryColomnList).isNotNull();
		assertThat(summaryColomnList.size()).isEqualTo(1);
		assertThat(summarymap.get(SummaryConfigPresenter.METADATA_CODE)).isEqualTo(rmSchemasRecordsServices.folderSchemaType().getSchema("employe").get("title").getCode());
	}
}
