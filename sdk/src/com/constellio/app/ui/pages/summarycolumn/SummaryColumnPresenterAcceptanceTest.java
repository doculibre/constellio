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

public class SummaryColumnPresenterAcceptanceTest extends ConstellioTest {

	@Mock
	SummaryColumnView view;
	MockedNavigation navigator;
	@Mock
	SessionContext sessionContext;

	SummaryColumnPresenter summaryColumnPresenter;
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

		summaryColumnPresenter = new SummaryColumnPresenter(view, Folder.DEFAULT_SCHEMA);

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenOneMetatSummaryThenMetadataHaveMetadataSummy() {

		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		summaryColumnPresenter.getMetadatas();
		SummaryColumnParams summaryColumnParams = new SummaryColumnParams();
		summaryColumnParams.setPrefix("a");
		summaryColumnParams.setDisplayCondition(SummaryColumnParams.DisplayCondition.ALWAYS);
		summaryColumnParams.setMetadataVO(builder.build(Schemas.TITLE, FakeSessionContext.adminInCollection(zeCollection)));

		summaryColumnPresenter.addMetadaForSummary(summaryColumnParams);

		Metadata metadata = summaryColumnPresenter.getSummaryMetadata();

		List summaryComlomnList = (List) metadata.getCustomParameter().get(SummaryColumnPresenter.SUMMARY_COLOMN);

		assertThat(summaryComlomnList).isNotNull();
		assertThat(summaryComlomnList.size()).isEqualTo(1);
		assertThat(summaryColumnParams.getMetadataVO().getCode()).isEqualTo(Schemas.TITLE.getCode());
		assertThat(summaryColumnParams.getPrefix()).isEqualTo("a");
		assertThat(summaryColumnParams.getDisplayCondition()).isEqualTo(SummaryColumnParams.DisplayCondition.ALWAYS);
	}

	@Test
	public void givenOneMetatSummaryInFolderEmployeThenMetadataHaveMetadataSummy() {
		SummaryColumnPresenter summaryColumnPresenterFolderEmploye = new SummaryColumnPresenter(view, "folder_employe");

		MetadataToVOBuilder builder = new MetadataToVOBuilder();
		SummaryColumnParams summaryColumnParams1 = new SummaryColumnParams();
		summaryColumnParams1.setPrefix("a");
		summaryColumnParams1.setDisplayCondition(SummaryColumnParams.DisplayCondition.ALWAYS);
		summaryColumnParams1.setMetadataVO(builder.build(rmSchemasRecordsServices.folder.description(), FakeSessionContext.adminInCollection(zeCollection)));

		summaryColumnPresenter.addMetadaForSummary(summaryColumnParams1);

		summaryColumnPresenterFolderEmploye.getMetadatas();
		SummaryColumnParams summaryColumnParams2 = new SummaryColumnParams();
		summaryColumnParams2.setPrefix("b");
		summaryColumnParams2.setDisplayCondition(SummaryColumnParams.DisplayCondition.COMPLETED);
		summaryColumnParams2.setMetadataVO(builder.build(rmSchemasRecordsServices.folderSchemaType().getSchema("employe").get("title"), FakeSessionContext.adminInCollection(zeCollection)));

		summaryColumnPresenterFolderEmploye.addMetadaForSummary(summaryColumnParams2);

		Metadata metadata = summaryColumnPresenterFolderEmploye.getSummaryMetadata();

		List summaryColomnList = (List) metadata.getCustomParameter().get(SummaryColumnPresenter.SUMMARY_COLOMN);

		Map<String, Object> summarymap = (Map<String, Object>) summaryColomnList.get(0);

		assertThat(summaryColomnList).isNotNull();
		assertThat(summaryColomnList.size()).isEqualTo(1);
		assertThat(summarymap.get(SummaryColumnPresenter.METADATA_CODE)).isEqualTo(rmSchemasRecordsServices.folderSchemaType().getSchema("employe").get("title").getCode());
		assertThat(summarymap.get(SummaryColumnPresenter.PREFIX)).isEqualTo("b");
		assertThat(summarymap.get(SummaryColumnPresenter.IS_ALWAYS_SHOWN)).isEqualTo(false);
	}
}
