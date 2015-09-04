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
package com.constellio.model.entities.records.wrappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.wrappers.structure.FacetOrderType;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;

public class FacetAcceptanceTest extends ConstellioTest {
	SchemasRecordsServices recordsServices;
	RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection()
		);

		recordsServices = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenCreatingAFacetAndLoadingThenOK()
			throws Exception {

		MapStringStringStructure fieldValuesLabel = new MapStringStringStructure();
		fieldValuesLabel.put("zeFieldValue", "zeLabel");
		fieldValuesLabel.put("zeFieldValue2", "zeLabel");

		MapStringStringStructure queryValues = new MapStringStringStructure();
		queryValues.put("zeLabel", "zeUltimateSolrQuery");
		queryValues.put("zeLabel1", "zeUltimateSolrQuery1");

		Facet facetField = recordsServices.newFacetField("zeFacet")
				.setTitle("My document")
				.setElementPerPage(2)
				.setPages(5)
				.setFieldDataStoreCode("aField")
				.setOrder(1)
				.setOrderResult(FacetOrderType.ALPHABETICAL)
				.setFieldValuesLabel(fieldValuesLabel);

		Facet facetQuery = recordsServices.newFacetQuery("zeFacetQuery")
				.setTitle("My document")
				.setElementPerPage(5)
				.setPages(10)
				.setFieldDataStoreCode("aFieldCode")
				.setOrder(2)
				.setOrderResult(FacetOrderType.RELEVANCE)
				.setListQueries(queryValues);

		recordServices.add(facetField);
		recordServices.add(facetQuery);

		Facet newFacetField = recordsServices.getFacet("zeFacet");
		Facet newFacetQuery = recordsServices.getFacet("zeFacetQuery");

		assertThat(newFacetField).isEqualToComparingFieldByField(facetField);
		assertThat(newFacetQuery).isEqualToComparingFieldByField(facetQuery);
	}
}
