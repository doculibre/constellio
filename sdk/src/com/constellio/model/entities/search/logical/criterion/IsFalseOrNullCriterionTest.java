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
package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsFalseOrNullCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsFalseOrNullCriterionTest extends ConstellioTest {
	@Mock Metadata textMetadata;
	@Mock Metadata booleanMetadata;
	private IsFalseOrNullCriterion criterion;

	@Before
	public void setUp() {
		willReturn("firstText").given(textMetadata).getCode();
		willReturn("boolean").given(booleanMetadata).getCode();
		willReturn("enabled_s").given(booleanMetadata).getDataStoreCode();
		willReturn(MetadataValueType.BOOLEAN).given(booleanMetadata).getType();
		willReturn(MetadataValueType.STRING).given(textMetadata).getType();

		criterion = new IsFalseOrNullCriterion();
	}

	@Test
	public void givenBooleanMetadataWhenIsValidForThenReturnTrue() {
		assertThat(criterion.isValidFor(booleanMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenIsValidForThenReturnFalse() {
		assertThat(criterion.isValidFor(textMetadata)).isFalse();
	}

	@Test
	public void givenTextMetadataWhenIsValidForThenReturnFalse() {
		assertThat(criterion.isValidFor(textMetadata)).isFalse();
	}

	@Test
	public void givenBooleanMetadataWhenGetSolrQueryThenValidSolrQuery() {
		String expectedQuery = "enabled_s:__FALSE__ OR enabled_s:__NULL__";
		String query = criterion.getSolrQuery(booleanMetadata);
		assertThat(query).isEqualTo(expectedQuery);
	}
}
