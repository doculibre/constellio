package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SearchRetentionRulesPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock SearchRetentionRulesView view;
	@Mock SessionContext sessionContext;
	@Mock RecordVO recordVO;
	@Mock UserVO userVO;
	@Mock MetadataSchemaVO metadataSchemaVO;
	SearchRetentionRulesPresenter presenter;

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

		presenter = new SearchRetentionRulesPresenter(view);
	}

	@Test
	public void givenNotPrincipalTaxonomyAndFreeSearchTextWhenSearchConceptsOfTaxonomyThenOk()
			throws Exception {

		String freeText = records.getRule1().getCode();
		presenter.forParams(freeText);
		presenter.viewAssembled();

		RecordVODataProvider dataProvider = presenter.getDataProvider();

		assertThat(dataProvider.size()).isEqualTo(1);
		assertThat(dataProvider.getRecordVO(0).getId()).isEqualTo(records.ruleId_1);
	}
}