package com.constellio.app.ui.pages.management.facet;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetOrderType;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class ListFacetConfigurationAcceptTest extends ConstellioTest {
	@Mock ListFacetConfigurationView view;
	ListFacetConfigurationPresenter presenter;

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
		);

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));

		presenter = new ListFacetConfigurationPresenter(view);
	}

	@Test
	public void openPage()
			throws Exception {
		givenFacetListSaved();
		ConstellioWebDriver driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		driver.navigateTo().url(NavigatorConfigurationService.LIST_FACET_CONFIGURATION);
		waitUntilICloseTheBrowsers();
	}

	private void givenFacetListSaved()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(getFacetForTitleType("zeFacet", FacetType.FIELD).getWrappedRecord());
		transaction.add(getFacetForTitleType("zeFacet1", FacetType.FIELD).getWrappedRecord());
		transaction.add(getFacetForTitleType("zeUltimateFacet", FacetType.QUERY).getWrappedRecord());
		transaction.add(getFacetForTitleType("zeChuckFacet", FacetType.QUERY).getWrappedRecord());

		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	private Facet getFacetForTitleType(String title, FacetType type) {
		SchemasRecordsServices schemasRecords = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		MapStringStringStructure newValues = new MapStringStringStructure();
		for (int i = 0; i < 10; ++i) {
			newValues.put("zeLabel" + i, "zeValue" + i);
		}

		Facet facet = null;
		switch (type) {
		case FIELD:
			facet = schemasRecords.newFacetField();
			facet.setFieldValuesLabel(newValues);
			break;
		case QUERY:
			facet = schemasRecords.newFacetQuery();
			facet.setListQueries(newValues);
			break;
		}

		facet.setOrder(1);
		facet.setPages(10);
		facet.setFieldDataStoreCode("full_s");
		facet.setOrderResult(FacetOrderType.ALPHABETICAL);
		facet.setElementPerPage(2);
		facet.setTitle(title);
		facet.setFacetType(type);

		return facet;
	}
}
