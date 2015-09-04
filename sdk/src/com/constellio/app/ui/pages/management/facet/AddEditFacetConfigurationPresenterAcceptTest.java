/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.facet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.ConstellioNavigator;
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
	@Mock ConstellioNavigator navigator;
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

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.adminInCollection(zeCollection));
		when(view.navigateTo()).thenReturn(navigator);
		doNothing().when(navigator).listFacetConfiguration();

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
		facet.setFieldDataStoreCode("");

		presenter.saveButtonClicked(getVOForRecord(facet.getWrappedRecord()));

		Record record = getModelLayerFactory().newRecordServices().getDocumentById(facet.getId());
		Facet loadFacet = new Facet(record, facet.getMetadataSchemaTypes());

		assertThat(loadFacet.getElementPerPage()).isEqualTo(facet.getElementPerPage());
		assertThat(loadFacet.getOrder()).isEqualTo(1);
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
		facet.setFieldDataStoreCode("");

		RecordVO facetToSave = new RecordToVOBuilder().build(facet.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());

		presenter.saveButtonClicked(facetToSave);

		savedFacet = presenter.getRecordVO().getId();
	}

	private RecordVO getVOForRecord(Record record) {
		return new RecordToVOBuilder().build(record, VIEW_MODE.FORM, view.getSessionContext());
	}
}
