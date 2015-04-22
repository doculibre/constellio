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
import com.constellio.model.services.search.query.logical.criteria.IsNotNullCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsNotNullCriterionTest extends ConstellioTest {

	IsNotNullCriterion criterion;

	@Mock Metadata textMetadata, booleanMetadata, dateMetadata, numberMetadata;

	@Before
	public void setUp()
			throws Exception {
		criterion = new IsNotNullCriterion();

		when(textMetadata.getDataStoreCode()).thenReturn("textMetadata_s");
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);

		when(booleanMetadata.getDataStoreCode()).thenReturn("booleanMetadata_b");
		when(booleanMetadata.getType()).thenReturn(MetadataValueType.BOOLEAN);

		when(dateMetadata.getDataStoreCode()).thenReturn("dateMetadata_dt");
		when(dateMetadata.getType()).thenReturn(MetadataValueType.DATE_TIME);

		when(numberMetadata.getDataStoreCode()).thenReturn("numberMetadata_d");
		when(numberMetadata.getType()).thenReturn(MetadataValueType.NUMBER);
	}

	@Test
	public void givenTextMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("(*:* -textMetadata_s:\"__NULL__\")");
	}

	@Test
	public void givenBooleanMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		assertThat(criterion.getSolrQuery(booleanMetadata)).isEqualTo("(*:* -booleanMetadata_b:\"__NULL__\")");
	}

	@Test
	public void givenDateMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo("(*:* -dateMetadata_dt:\"4242-06-06T06:42:42.666Z\")");
	}

	@Test
	public void givenNumberMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo("(*:* -numberMetadata_d:\"" + Integer.MIN_VALUE + "\")");
	}
}
