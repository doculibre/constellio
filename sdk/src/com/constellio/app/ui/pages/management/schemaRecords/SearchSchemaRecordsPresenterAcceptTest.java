package com.constellio.app.ui.pages.management.schemaRecords;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.google.gwt.dev.util.collect.HashMap;

public class SearchSchemaRecordsPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock SearchSchemaRecordsView view;
	@Mock SessionContext sessionContext;
	@Mock RecordVO recordVO;
	@Mock UserVO userVO;
	@Mock MetadataSchemaVO metadataSchemaVO;
	SearchSchemaRecordsPresenter presenter;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		when(userVO.getUsername()).thenReturn(admin);
		when(sessionContext.getCurrentUser()).thenReturn(userVO);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

		presenter = new SearchSchemaRecordsPresenter(view);
	}

	@Test
	public void givenNotPrincipalTaxonomyAndFreeSearchTextWhenSearchConceptsOfTaxonomyThenOk()
			throws Exception {

		String viewPath = configureParams();

		presenter.forParams(viewPath);

		RecordVODataProvider dataProvider = presenter.getDataProvider();
		assertThat(dataProvider.size()).isEqualTo(1);
		assertThat(dataProvider.getRecordVO(0).getId()).isEqualTo(records.documentTypeId_1);
	}

	private String configureParams() {
		Map<String, String> params = new HashMap<>();
		String schemaCode = "ddvDocumentType_default";
		String freeText = "Livre de recettes";
		params.put("q", freeText);
		params.put("schemaCode", schemaCode);
		return ParamUtils.addParams(NavigatorConfigurationService.SEARCH_SCHEMA_RECORDS, params);
	}

}