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

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.CriteriaUtils;
import com.constellio.model.services.search.query.logical.criteria.IsNotEqualCriterion;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;

public class IsNotEqualCriterionTest extends ConstellioTest {

	@Mock Metadata textMetadata;
	@Mock Metadata referenceMetadata;
	@Mock Metadata numberMetadata;
	@Mock Metadata booleanMetadata;
	@Mock Metadata dateMetadata;
	@Mock Metadata contentMetadata;
	LocalDateTime date = new LocalDateTime(2000, 10, 20, 10, 50);
	String textValue = "text value";
	int numberValue1 = 1;
	int numberValue12 = 12;
	int numberValue100 = 100;
	Boolean booleanFalseValue = Boolean.FALSE;
	Boolean booleanTrueValue = Boolean.TRUE;

	@Before
	public void setUp()
			throws Exception {
		when(textMetadata.getDataStoreCode()).thenReturn("textMetadata");
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);

		when(referenceMetadata.getDataStoreCode()).thenReturn("referenceMetadata");
		when(referenceMetadata.getType()).thenReturn(MetadataValueType.REFERENCE);

		when(numberMetadata.getDataStoreCode()).thenReturn("numberMetadata");
		when(numberMetadata.getType()).thenReturn(MetadataValueType.NUMBER);

		when(booleanMetadata.getDataStoreCode()).thenReturn("booleanMetadata");
		when(booleanMetadata.getType()).thenReturn(MetadataValueType.BOOLEAN);

		when(dateMetadata.getDataStoreCode()).thenReturn("dateTimeMetadata");
		when(dateMetadata.getType()).thenReturn(MetadataValueType.DATE_TIME);

		when(contentMetadata.getDataStoreCode()).thenReturn("contentMetadata");
		when(contentMetadata.getType()).thenReturn(MetadataValueType.CONTENT);
	}

	@Test
	public void whenGettingSolrQueryThenQueryIsCorrect() {

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(numberValue12);

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"(*:* -(numberMetadata:\"12\" OR numberMetadata:\"" + Integer.MIN_VALUE + "\"))");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {
		IsNotEqualCriterion criterion = new IsNotEqualCriterion(textValue);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"(*:* -(textMetadata:\"text value\" OR textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenRecordWhenGettingSolrQueryThenQueryIsCorrect() {
		Record record = new TestRecord("code", "zeCollection", "zeId");

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(record);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"(*:* -(textMetadata:\"zeId\" OR textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenNullWhenGettingSolrQueryThenQueryIsCorrect() {
		Record record = new TestRecord("code", "zeCollection", "zeId");

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(null);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"(*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenBooleanWhenGettingSolrQueryThenQueryIsCorrect() {
		IsNotEqualCriterion criterion = new IsNotEqualCriterion(booleanTrueValue);

		assertThat(criterion.getSolrQuery(booleanMetadata)).isEqualTo(
				"(*:* -(booleanMetadata:\"__TRUE__\" OR booleanMetadata:\"__NULL__\"))");

		criterion = new IsNotEqualCriterion(booleanFalseValue);

		assertThat(criterion.getSolrQuery(booleanMetadata)).isEqualTo(
				"(*:* -(booleanMetadata:\"__FALSE__\" OR booleanMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(date.toDate().getTime()));

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(date);

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo(
				"(*:* -(dateTimeMetadata:\"" + date + "Z\" OR dateTimeMetadata:\"" + CriteriaUtils
						.getNullDateValue() + "\"))");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(anInteger());

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(aString());

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsNotEqualCriterion criterion = new IsNotEqualCriterion(anInteger());

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(aString());

		assertThat(criterion.isValidFor(booleanMetadata)).isTrue();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(aDateTime());

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(aString());

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {

		String value = "value with spaces";

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"(*:* -(textMetadata:\"value with spaces\" OR textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		String value = "value*with*asterisks*";

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"(*:* -(textMetadata:\"value*with*asterisks*\" OR textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		String value = "value?with?wildcards?";

		IsNotEqualCriterion criterion = new IsNotEqualCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"(*:* -(textMetadata:\"value?with?wildcards?\" OR textMetadata:\"__NULL__\"))");
	}
}
