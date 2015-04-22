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
import com.constellio.model.services.search.query.logical.criteria.IsTrueCriterion;
import com.constellio.sdk.tests.ConstellioTest;

/**
 * Created by maxime on 7/30/14.
 */
public class IsTrueCriterionTest extends ConstellioTest {
	@Mock Metadata textMetadata;
	@Mock Metadata booleanMetadata;
	private IsTrueCriterion truecriterion;

	@Before
	public void setUp() {
		willReturn("firstText").given(textMetadata).getCode();
		willReturn("boolean").given(booleanMetadata).getCode();
		willReturn("enabled_s").given(booleanMetadata).getDataStoreCode();
		willReturn(MetadataValueType.BOOLEAN).given(booleanMetadata).getType();
		willReturn(MetadataValueType.STRING).given(textMetadata).getType();

		truecriterion = new IsTrueCriterion();
	}

	@Test
	public void givenBooleanMetadataWhenIsValidForThenReturnTrue() {
		assertThat(truecriterion.isValidFor(booleanMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenIsValidForThenReturnFalse() {
		assertThat(truecriterion.isValidFor(textMetadata)).isFalse();
	}

	@Test
	public void givenBooleanMetadataWhenGetSolrQueryThenValidSolrQuery() {
		String expectedQuery = "enabled_s:__TRUE__";
		String query = truecriterion.getSolrQuery(booleanMetadata);
		assertThat(query).isEqualTo(expectedQuery);
	}
}
