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
import com.constellio.model.services.search.query.logical.criteria.IsGreaterOrEqualThanCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsGreaterOrEqualThanCriterionTest extends ConstellioTest {

	@Mock Metadata textMetadata;
	@Mock Metadata referenceMetadata;
	@Mock Metadata numberMetadata;
	@Mock Metadata booleanMetadata;
	@Mock Metadata dateMetadata;
	@Mock Metadata contentMetadata;

	String textValue = "text value";
	LocalDateTime date = new LocalDateTime(2000, 10, 20, 10, 50);
	int numberValue12 = 12;

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

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(numberValue12);

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo("numberMetadata:[12 TO *]");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(textValue);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[\"text value\" TO *] AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(date.toDate().getTime()));

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(date);

		assertThat(criterion.getSolrQuery(dateMetadata))
				.isEqualTo("dateTimeMetadata:[" + date + "Z TO *] AND (*:* -(dateTimeMetadata:\"4242-06-06T06:42:42.666Z\")) ");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(anInteger());

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(aString());

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(anInteger());

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(aString());

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(aDateTime());

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(aString());

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {

		String value = "value with spaces";

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[\"value with spaces\" TO *] AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		String value = "value*with*asterisks*";

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[\"value*with*asterisks*\" TO *] AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		String value = "value?with?wildcards?";

		IsGreaterOrEqualThanCriterion criterion = new IsGreaterOrEqualThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[\"value?with?wildcards?\" TO *] AND (*:* -(textMetadata:\"__NULL__\"))");
	}

}
