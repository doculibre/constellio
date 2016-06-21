package com.constellio.app.ui.pages.management.taxonomy;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;

public class TaxonomyManagementSearchPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock TaxonomyManagementSearchView view;
	@Mock SessionContext sessionContext;
	@Mock RecordVO recordVO;
	@Mock UserVO userVO;
	@Mock MetadataSchemaVO metadataSchemaVO;
	TaxonomyManagementSearchPresenter presenter;
	TaxonomiesManager taxonomiesManager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);
		when(userVO.getUsername()).thenReturn(admin);
		when(sessionContext.getCurrentUser()).thenReturn(userVO);
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

		presenter = new TaxonomyManagementSearchPresenter(view);
	}

	@Test
	public void givenNotPrincipalTaxonomyAndFreeSearchTextWhenSearchConceptsOfTaxonomyThenOk()
			throws Exception {

		String freeText = "X*";
		String taxonmyCode = RMTaxonomies.CLASSIFICATION_PLAN;
		String viewPath = configurePathWithParams(freeText, taxonmyCode);

		presenter.forParams(viewPath);

		List<RecordVODataProvider> dataProviders = presenter.getDataProviders();
		assertThat(dataProviders).hasSize(1);
		RecordVODataProvider dataProvider = dataProviders.get(0);
		assertThat(dataProvider.size()).isEqualTo(5);

		assertThat(dataProvider.listRecordVOs(0, dataProvider.size())).extracting("id").containsOnly(
				records.categoryId_X,
				records.categoryId_X100,
				records.categoryId_X110,
				records.categoryId_X120,
				records.categoryId_X13
		);

	}

	@Test
	public void givenPrincipalTaxonomyAndFreeSearchTextWhenSearchConceptsOfTaxonomyThenOk()
			throws Exception {

		String freeText = "Unit*";
		String taxonmyCode = RMTaxonomies.ADMINISTRATIVE_UNITS;
		String viewPath = configurePathWithParams(freeText, taxonmyCode);

		presenter.forParams(viewPath);

		List<RecordVODataProvider> dataProviders = presenter.getDataProviders();
		assertThat(dataProviders).hasSize(1);
		RecordVODataProvider dataProvider = dataProviders.get(0);
		assertThat(dataProvider.size()).isEqualTo(12);

		assertThat(dataProvider.listRecordVOs(0, dataProvider.size())).extracting("id").containsOnly(
				records.unitId_10,
				records.unitId_10a,
				records.unitId_11,
				records.unitId_11b,
				records.unitId_12,
				records.unitId_12b,
				records.unitId_12c,
				records.unitId_20,
				records.unitId_20d,
				records.unitId_20e,
				records.unitId_30,
				records.unitId_30c
		);
	}

	//	@Test
	//	public void givenUserWithoutAccessToAPrincipalTaxonomyWhenSearchConceptsOfTaxonomyThenOk()
	//			throws Exception {
	//
	//		removeAuthorizationsFromChuckInUnit10AndHierarchy();
	//		when(userVO.getUsername()).thenReturn(chuckNorris);
	//		when(sessionContext.getCurrentUser()).thenReturn(userVO);
	//		presenter = new TaxonomyManagementSearchPresenter(view);
	//		String freeText = "Unit*";
	//		String taxonmyCode = RMTaxonomies.ADMINISTRATIVE_UNITS;
	//		String viewPath = configurePathWithParams(freeText, taxonmyCode);
	//		presenter.forParams(viewPath);
	//
	//		List<RecordVODataProvider> dataProviders = presenter.getDataProviders();
	//		assertThat(dataProviders).hasSize(1);
	//		RecordVODataProvider dataProvider = dataProviders.get(0);
	//		assertThat(dataProvider.size()).isEqualTo(5);
	//
	//		for (RecordVO vo : dataProvider.listRecordVOs(0, dataProvider.size())) {
	//			System.out.println(vo.get(Schemas.CODE.getLocalCode()) + " " + vo.getTitle());
	//
	//		}
	//		assertThat(dataProvider.listRecordVOs(0, dataProvider.size())).extracting("id").containsOnly(
	//				records.unitId_20,
	//				records.unitId_20d,
	//				records.unitId_20e,
	//				records.unitId_30,
	//				records.unitId_30c
	//		);
	//	}

	//

	@Test
	public void whenDeletingANotDeletableTaxonomyThenShowErrorMessage() {
		when(recordVO.getId()).thenReturn(records.categoryId_X100);

		String freeText = "X*";
		String taxonmyCode = RMTaxonomies.CLASSIFICATION_PLAN;
		String viewPath = configurePathWithParams(freeText, taxonmyCode);
		presenter.forParams(viewPath);

		presenter.deleteButtonClicked(recordVO);

		verify(view).showErrorMessage($("TaxonomyManagementView.cannotDelete"));
	}

	@Test
	public void whenDeletingADeletableTaxonomyThenOk() {
		when(recordVO.getId()).thenReturn(records.categoryId_X13);
		when(recordVO.getSchema()).thenReturn(metadataSchemaVO);
		when(metadataSchemaVO.getCode()).thenReturn(Category.DEFAULT_SCHEMA);

		String freeText = "X*";
		String taxonmyCode = RMTaxonomies.CLASSIFICATION_PLAN;
		String viewPath = configurePathWithParams(freeText, taxonmyCode);
		presenter.forParams(viewPath);

		presenter.deleteButtonClicked(recordVO);
		presenter.forParams(viewPath);

		List<RecordVODataProvider> dataProviders = presenter.getDataProviders();

		RecordVODataProvider dataProvider = dataProviders.get(0);
		assertThat(dataProvider.getSchema().getCode()).isEqualTo(Category.DEFAULT_SCHEMA);
		assertThat(getRecordIdsFromDataProvider(dataProvider)).doesNotContain(records.categoryId_X13);

		verify(view).refreshTable();
	}

	//

	private List<String> getRecordIdsFromDataProvider(RecordVODataProvider dataProvider) {
		List<String> IDs = new ArrayList<>();
		for (RecordVO recordVO : dataProvider.listRecordVOs(0, dataProvider.size())) {
			IDs.add(recordVO.getId());
		}
		return IDs;
	}

	private String configurePathWithParams(String freeText, String taxonmyCode) {
		Map<String, String> params = new HashMap<>();
		params.put("taxonomyCode", taxonmyCode);
		params.put("q", freeText);
		return ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_SEARCH, params);
	}

	@Test
	public void removeAuthorizationsFromChuckInUnit10AndHierarchy() {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		List<Authorization> authorizations = authorizationsServices.getRecordAuthorizations(records.getChuckNorris());
		authorizationsServices.removeMultipleAuthorizationsOnRecord(authorizations, records.getUnit10().getWrappedRecord(),
				CustomizedAuthorizationsBehavior.DETACH);
		System.out.println(authorizations);
	}

}
