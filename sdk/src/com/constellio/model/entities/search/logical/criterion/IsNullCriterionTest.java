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
import com.constellio.model.services.search.query.logical.criteria.IsNullCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsNullCriterionTest extends ConstellioTest {
	private IsNullCriterion criterion;

	@Mock Metadata metadata;

	@Before
	public void setUp() {
		criterion = new IsNullCriterion();
	}

	@Test
	public void givenStringMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("string_s");
		when(metadata.getType()).thenReturn(MetadataValueType.STRING);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("string_s:\"__NULL__\"");
	}

	@Test
	public void givenTextMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("text_s");
		when(metadata.getType()).thenReturn(MetadataValueType.TEXT);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("text_s:\"__NULL__\"");
	}

	@Test
	public void givenBooleanMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("boolean_b");
		when(metadata.getType()).thenReturn(MetadataValueType.BOOLEAN);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("boolean_b:\"__NULL__\"");
	}

	@Test
	public void givenReferenceMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("reference_s");
		when(metadata.getType()).thenReturn(MetadataValueType.REFERENCE);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("reference_s:\"__NULL__\"");
	}

	@Test
	public void givenStructureMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("structure_s");
		when(metadata.getType()).thenReturn(MetadataValueType.STRUCTURE);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("structure_s:\"__NULL__\"");
	}

	@Test
	public void givenContentMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("content_s");
		when(metadata.getType()).thenReturn(MetadataValueType.CONTENT);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("content_s:\"__NULL__\"");
	}

	@Test
	public void givenEnumMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("enum_s");
		when(metadata.getType()).thenReturn(MetadataValueType.ENUM);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("enum_s:\"__NULL__\"");
	}

	@Test
	public void givenDateMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("date_d");
		when(metadata.getType()).thenReturn(MetadataValueType.DATE);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("date_d:\"4242-06-06T06:42:42.666Z\"");
	}

	@Test
	public void givenDateTimeMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("dateTime_dt");
		when(metadata.getType()).thenReturn(MetadataValueType.DATE_TIME);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("dateTime_dt:\"4242-06-06T06:42:42.666Z\"");
	}

	@Test
	public void givenNumberMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		when(metadata.getDataStoreCode()).thenReturn("number_d");
		when(metadata.getType()).thenReturn(MetadataValueType.NUMBER);

		assertThat(criterion.getSolrQuery(metadata)).isEqualTo("number_d:\"" + Integer.MIN_VALUE + "\"");
	}
}
