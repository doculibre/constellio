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
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsStartingWithTextCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsStartingWithTextCriterionTest extends ConstellioTest {

	@Mock Metadata textMetadata;
	@Mock Metadata booleanMetadata;

	String value = "value";

	@Before
	public void setUp()
			throws Exception {
		when(textMetadata.getDataStoreCode()).thenReturn("textMetadata");
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);

		when(booleanMetadata.getDataStoreCode()).thenReturn("booleanMetadata");
		when(booleanMetadata.getType()).thenReturn(MetadataValueType.BOOLEAN);
	}

	@Test
	public void whenGettingSolrQueryThenQueryIsCorrect() {
		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value*");
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {
		value = "value with spaces";

		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value\\ with\\ spaces*");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {
		value = "value*with*asterisks";

		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value\\*with\\*asterisks*");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {
		value = "value?with?wildcards";

		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value\\?with\\?wildcards*");
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenWrongTypeMetadataWhenCheckingIsValidForThenReturnFalse() {
		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}
}
