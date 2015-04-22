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
package com.constellio.app.ui.framework.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.ui.entities.TaxonomyVO;
import com.constellio.app.ui.framework.builders.TaxonomyToVOBuilder;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.MockedFactories;

public class TaxonomyVODataProviderTest extends ConstellioTest {

	TaxonomyVODataProvider dataProvider;
	@Mock TaxonomyToVOBuilder voBuilder;
	@Mock TaxonomiesManager taxonomiesManager;
	@Mock Taxonomy taxonomy1, taxonomy2;
	@Mock TaxonomyVO taxonomyVO1, taxonomyVO2;
	@Mock User admin;
	@Mock UserServices userServices;
	List<Taxonomy> taxonomies, sortedTaxonomies;
	List<TaxonomyVO> taxonomyVOs, sortedTaxonomyVOs;

	MockedFactories mockedFactories = new MockedFactories();

	@Before
	public void setUp()
			throws Exception {
		taxonomies = new ArrayList<>();
		taxonomies.add(taxonomy2);
		taxonomies.add(taxonomy1);

		taxonomyVOs = new ArrayList<>();
		taxonomyVOs.add(taxonomyVO2);
		taxonomyVOs.add(taxonomyVO1);

		when(mockedFactories.getModelLayerFactory().newUserServices()).thenReturn(userServices);
		when(userServices.getUserInCollection("admin", zeCollection)).thenReturn(admin);
		when(mockedFactories.getModelLayerFactory().getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(taxonomiesManager.getAvailableTaxonomiesInHomePage(admin)).thenReturn(taxonomies);

		when(admin.getUsername()).thenReturn("admin");

		when(taxonomy1.getCode()).thenReturn("taxo1");
		when(taxonomy2.getCode()).thenReturn("taxo2");

		when(taxonomyVO1.getCode()).thenReturn("taxo1");
		when(taxonomyVO2.getCode()).thenReturn("taxo2");

		when(voBuilder.build(taxonomy1)).thenReturn(taxonomyVO1);
		when(voBuilder.build(taxonomy2)).thenReturn(taxonomyVO2);

		dataProvider = spy(new TaxonomyVODataProvider(voBuilder, mockedFactories.getModelLayerFactory(), zeCollection, "admin"));
	}

	@Test
	public void whenGetTaxonomyVOsThenReturnThem()
			throws Exception {
		List<TaxonomyVO> taxonomyVOs = dataProvider.getTaxonomyVOs();

		assertThat(taxonomyVOs).hasSize(2);

		assertThat(taxonomyVOs.get(0).getCode()).isEqualTo(taxonomy1.getCode());
		assertThat(taxonomyVOs.get(1).getCode()).isEqualTo(taxonomy2.getCode());
	}

	@Test
	public void testSize()
			throws Exception {
		assertThat(dataProvider.size()).isEqualTo(2);
	}

	@Test
	public void testGetTaxonomyVOsCodes()
			throws Exception {
		assertThat(dataProvider.getTaxonomyVOsCodes()).containsOnly("taxo1", "taxo2");
	}
}