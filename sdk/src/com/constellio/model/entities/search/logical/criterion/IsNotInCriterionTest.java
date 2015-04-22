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

import java.util.Arrays;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.CriteriaUtils;
import com.constellio.model.services.search.query.logical.criteria.IsNotInCriterion;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;

public class IsNotInCriterionTest extends ConstellioTest {

	@Mock Metadata textMetadata;
	@Mock Metadata textMetadata1;
	@Mock Metadata referenceMetadata;
	@Mock Metadata numberMetadata;
	@Mock Metadata booleanMetadata;
	@Mock Metadata dateMetadata;
	@Mock Metadata contentMetadata;
	LocalDateTime date = new LocalDateTime(2000, 10, 20, 10, 50);
	LocalDateTime date2 = new LocalDateTime(2002, 10, 20, 22, 50);
	String textValue = "text value";
	String textValue2 = "text value2";
	int numberValueNegative1 = -1;
	int numberValue12 = 12;
	int numberValue100 = 100;

	@Before
	public void setUp()
			throws Exception {
		when(textMetadata.getDataStoreCode()).thenReturn("textMetadata");
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(textMetadata.isMultivalue()).thenReturn(true);

		when(referenceMetadata.getDataStoreCode()).thenReturn("referenceMetadata");
		when(referenceMetadata.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(referenceMetadata.isMultivalue()).thenReturn(true);

		when(numberMetadata.getDataStoreCode()).thenReturn("numberMetadata");
		when(numberMetadata.getType()).thenReturn(MetadataValueType.NUMBER);
		when(numberMetadata.isMultivalue()).thenReturn(true);

		when(booleanMetadata.getDataStoreCode()).thenReturn("booleanMetadata");
		when(booleanMetadata.getType()).thenReturn(MetadataValueType.BOOLEAN);
		when(booleanMetadata.isMultivalue()).thenReturn(true);

		when(dateMetadata.getDataStoreCode()).thenReturn("dateTimeMetadata");
		when(dateMetadata.getType()).thenReturn(MetadataValueType.DATE_TIME);
		when(dateMetadata.isMultivalue()).thenReturn(true);

		when(contentMetadata.getDataStoreCode()).thenReturn("contentMetadata");
		when(contentMetadata.getType()).thenReturn(MetadataValueType.CONTENT);
		when(contentMetadata.isMultivalue()).thenReturn(true);
	}

	@Test
	public void givenOneValueWhenGettingSolrQueryThenQueryIsCorrect() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(numberValue12));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"*:* -numberMetadata:\"12\" -numberMetadata:\"" + Integer.MIN_VALUE + "\"");
	}

	@Test
	public void givenThreeValuesWhenGettingSolrQueryThenQueryIsCorrect() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(numberValue12, numberValue100, numberValueNegative1));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"*:* -numberMetadata:\"12\" -numberMetadata:\"100\" -numberMetadata:\"-1\" -numberMetadata:\""
						+ Integer.MIN_VALUE + "\"");
	}

	@Test
	public void whenGettingSolrQueryWithRecordsThenQueryIsCorrect() {
		Record record1 = new TestRecord("a", "zeCollection", "id1");
		Record record2 = new TestRecord("a", "zeCollection", "id2");

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(record1, record2));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"*:* -textMetadata:\"id1\" -textMetadata:\"id2\" -textMetadata:\"__NULL__\"");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(textValue, textValue2));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"*:* -textMetadata:\"text\\ value\" -textMetadata:\"text\\ value2\" -textMetadata:\"__NULL__\"");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(date, date2));

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo(
				"*:* -dateTimeMetadata:\"" + date + "Z\" -dateTimeMetadata:\"" + date2
						+ "Z\" -dateTimeMetadata:\"" + CriteriaUtils.getNullDateValue() + "\"");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(anInteger()));

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(aString()));

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(anInteger()));

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(aDateTime()));

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(aString()));

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value with spaces";

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"*:* -textMetadata:\"value\\ with\\ spaces\" -textMetadata:\"__NULL__\"");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value*with*asterisks*";

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"*:* -textMetadata:\"value\\*with\\*asterisks\\*\" -textMetadata:\"__NULL__\"");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value?with?wildcards?";

		IsNotInCriterion criterion = new IsNotInCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"*:* -textMetadata:\"value\\?with\\?wildcards\\?\" -textMetadata:\"__NULL__\"");
	}
}
