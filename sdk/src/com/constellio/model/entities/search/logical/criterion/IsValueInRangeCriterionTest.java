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

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsValueInRangeCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsValueInRangeCriterionTest extends ConstellioTest {

	@Mock Metadata textMetadata;
	@Mock Metadata referenceMetadata;
	@Mock Metadata numberMetadata;
	@Mock Metadata booleanMetadata;
	@Mock Metadata dateMetadata;
	@Mock Metadata contentMetadata;

	LocalDateTime date = new LocalDateTime(2001, 9, 15, 9, 40);
	LocalDateTime date2 = new LocalDateTime(2003, 9, 15, 9, 40);
	int numberValue1 = 1;
	int numberValue100 = 100;
	String atextValue = "a text value";
	String zetextValue = "ze text value";

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

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(numberValue1, numberValue100);

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo("numberMetadata:[1 TO 100]");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(atextValue, zetextValue);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[\"a text value\" TO \"ze text value\"] AND -textMetadata:\"__NULL__\"");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(date.toDate().getTime()));
		int offsetMillis2 = -1 * (DateTimeZone.getDefault().getOffset(date2.toDate().getTime()));

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(date, date2);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[" + date + "Z TO " + date2
						+ "Z] AND -textMetadata:\"4242-06-06T06:42:42.666Z\"");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(anInteger(), anInteger());

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(aString(), aString());

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(anInteger(), anInteger());

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(aString(), aString());

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(aDateTime(), aDateTime());

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(aString(), aString());

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		String value = "value*with*asterisks*";
		String value2 = "value with spaces";

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(value, value2);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[\"value*with*asterisks*\" TO \"value with spaces\"] AND -textMetadata:\"__NULL__\"");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		String value = "value?with?wildcards";
		String value2 = "value with spaces";

		IsValueInRangeCriterion criterion = new IsValueInRangeCriterion(value, value2);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[\"value?with?wildcards\" TO \"value with spaces\"] AND -textMetadata:\"__NULL__\"");
	}
}
