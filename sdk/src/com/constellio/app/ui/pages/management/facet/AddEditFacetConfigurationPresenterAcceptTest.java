package com.constellio.app.ui.pages.management.facet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.constellio.sdk.tests.MockedFactories;
import com.constellio.sdk.tests.MockedNavigation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class AddEditFacetConfigurationPresenterAcceptTest extends ConstellioTest {
	@Mock AddEditFacetConfigurationView view;
	MockedNavigation navigator;
	AddEditFacetConfigurationPresenter presenter;

	RMTestRecords records = new RMTestRecords(zeCollection);
	private String savedFacet;
	private SchemasRecordsServices schemasRecords;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		navigator = new MockedNavigation();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigate()).thenReturn(navigator);

		presenter = spy(new AddEditFacetConfigurationPresenter(view));
		schemasRecords = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@Test
	public void givenEditAndSaveThenConfigurationSaved() {
		givenFacetSavedForType(FacetType.FIELD);
		presenter.forParams(savedFacet, true);

		Facet facet = schemasRecords.getFacet(savedFacet);

		facet.setElementPerPage(100);
		MapStringStringStructure newValues = new MapStringStringStructure();
		newValues.put("zeLabel", "zeValue");
		newValues.put("zeLabel1", "zeValue1");
		newValues.put("zeLabel2", "zeValue2");
		facet.setFieldValuesLabel(newValues);
		facet.setPages(20);
		facet.setFieldDataStoreCode("code_s");

		presenter.saveButtonClicked(getVOForRecord(facet.getWrappedRecord()));

		Record record = getModelLayerFactory().newRecordServices().getDocumentById(facet.getId());
		Facet loadFacet = new Facet(record, facet.getMetadataSchemaTypes());

		assertThat(loadFacet.getElementPerPage()).isEqualTo(facet.getElementPerPage());
		assertThat(loadFacet.getOrder()).isEqualTo(facet.getOrder());
		assertThat(loadFacet.getPages()).isEqualTo(facet.getPages());
	}

	private void givenFacetSavedForType(FacetType type) {
		Facet facet = schemasRecords.newFacetField();
		MapStringStringStructure newValues = new MapStringStringStructure();
		newValues.put("zeLabel", "zeValue");
		newValues.put("zeLabel1", "zeValue1");
		newValues.put("zeLabel2", "zeValue2");
		facet.setFieldValuesLabel(newValues);
		facet.setOrder(1);
		facet.setPages(10);
		facet.setElementPerPage(2);
		facet.setFacetType(type);
		facet.setTitle("zeFacet");
		facet.setFieldDataStoreCode("code_s");

		RecordVO facetToSave = new RecordToVOBuilder().build(facet.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());

		presenter.saveButtonClicked(facetToSave);

		savedFacet = presenter.getRecordVO().getId();
	}

	private RecordVO getVOForRecord(Record record) {
		return new RecordToVOBuilder().build(record, VIEW_MODE.FORM, view.getSessionContext());
	}
}
