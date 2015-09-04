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
package com.constellio.app.ui.pages.search.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class FacetSelectionsFactoryTest extends ConstellioTest {

	FacetSelectionsFactory factory;
	Set<String> selectedValues;

	@Before
	public void setUp()
			throws Exception {

		selectedValues = new HashSet<>();
		selectedValues.add("value1");
		selectedValues.add("value2");
		selectedValues.add("value3");
		selectedValues.add("value4");

		factory = new FacetSelectionsFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		FacetSelections facetSelections = new FacetSelections();
		assertThat(facetSelections.isDirty()).isFalse();

		facetSelections = new FacetSelections();
		facetSelections.setFacetField("type_default_code");
		assertThat(facetSelections.isDirty()).isTrue();

		facetSelections = new FacetSelections();
		facetSelections.setSelectedValues(selectedValues);
		assertThat(facetSelections.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {

		FacetSelections facetSelections = new FacetSelections();
		facetSelections.setFacetField("type_default_code");
		facetSelections.setSelectedValues(selectedValues);

		String stringValue = factory.toString(facetSelections);
		FacetSelections builtFacetSelections = (FacetSelections) factory.build(stringValue);
		String stringValue2 = factory.toString(builtFacetSelections);

		assertThat(builtFacetSelections).isEqualTo(facetSelections);
		assertThat(stringValue2).isEqualTo(stringValue);
		assertThat(builtFacetSelections.isDirty()).isFalse();

	}
}
