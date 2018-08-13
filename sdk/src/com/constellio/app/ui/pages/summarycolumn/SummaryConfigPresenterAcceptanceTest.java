package com.constellio.app.ui.pages.summarycolumn;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
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

public class SummaryConfigPresenterAcceptanceTest extends ConstellioTest {

	@Mock
	SummaryConfigView view;
	MockedNavigation navigator;
	@Mock
	SessionContext sessionContext;

	SummaryConfigPresenter summaryConfigPresenter;
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

		summaryConfigPresenter = new SummaryConfigPresenter(view, Folder.DEFAULT_SCHEMA);

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenOneMetatSummaryThenMetadataHaveMetadataSummy() {

		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		summaryConfigPresenter.getMetadatas();
		SummaryConfigParams summaryConfigParams = new SummaryConfigParams();
		summaryConfigParams.setPrefix("a");
		summaryConfigParams.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);
		summaryConfigParams.setMetadataVO(builder.build(Schemas.TITLE, FakeSessionContext.adminInCollection(zeCollection)));

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams);

		Metadata metadata = summaryConfigPresenter.getSummaryMetadata();

		List summaryComlomnList = (List) metadata.getCustomParameter().get(SummaryConfigPresenter.SUMMARY_CONFIG);

		assertThat(summaryComlomnList).isNotNull();
		assertThat(summaryComlomnList.size()).isEqualTo(1);
		assertThat(summaryConfigParams.getMetadataVO().getCode()).isEqualTo(Schemas.TITLE.getCode());
		assertThat(summaryConfigParams.getPrefix()).isEqualTo("a");
		assertThat(summaryConfigParams.getDisplayCondition()).isEqualTo(SummaryConfigParams.DisplayCondition.ALWAYS);
	}

	@Test
	public void givenOneMetatSummaryInFolderEmployeThenMetadataHaveMetadataSummy() {
		SummaryConfigPresenter summaryConfigPresenterFolderEmploye = new SummaryConfigPresenter(view, "folder_employe");

		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		SummaryConfigParams summaryConfigParams1 = new SummaryConfigParams();
		summaryConfigParams1.setPrefix("a");
		summaryConfigParams1.setDisplayCondition(SummaryConfigParams.DisplayCondition.ALWAYS);
		summaryConfigParams1.setMetadataVO(builder.build(rmSchemasRecordsServices.folder.description(), FakeSessionContext.adminInCollection(zeCollection)));

		summaryConfigPresenter.addMetadaForSummary(summaryConfigParams1);

		summaryConfigPresenterFolderEmploye.getMetadatas();
		SummaryConfigParams summaryConfigParams2 = new SummaryConfigParams();
		summaryConfigParams2.setPrefix("b");
		summaryConfigParams2.setDisplayCondition(SummaryConfigParams.DisplayCondition.COMPLETED);
		summaryConfigParams2.setMetadataVO(builder.build(rmSchemasRecordsServices.folderSchemaType().getSchema("employe").get("title"), FakeSessionContext.adminInCollection(zeCollection)));

		summaryConfigPresenterFolderEmploye.addMetadaForSummary(summaryConfigParams2);

		Metadata metadata = summaryConfigPresenterFolderEmploye.getSummaryMetadata();

		List summaryColomnList = (List) metadata.getCustomParameter().get(SummaryConfigPresenter.SUMMARY_CONFIG);

		Map<String, Object> summarymap = (Map<String, Object>) summaryColomnList.get(0);

		assertThat(summaryColomnList).isNotNull();
		assertThat(summaryColomnList.size()).isEqualTo(1);
		assertThat(summarymap.get(SummaryConfigPresenter.METADATA_CODE)).isEqualTo(rmSchemasRecordsServices.folderSchemaType().getSchema("employe").get("title").getCode());
		assertThat(summarymap.get(SummaryConfigPresenter.PREFIX)).isEqualTo("b");
		assertThat(summarymap.get(SummaryConfigPresenter.IS_ALWAYS_SHOWN)).isEqualTo(false);
	}
}
