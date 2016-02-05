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
import com.constellio.model.services.search.query.logical.criteria.CriteriaUtils;
import com.constellio.model.services.search.query.logical.criteria.IsLessThanCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsLessThanCriterionTest extends ConstellioTest {

	static int MIN_VALUE = Integer.MIN_VALUE;
	@Mock Metadata textMetadata;
	@Mock Metadata referenceMetadata;
	@Mock Metadata numberMetadata;
	@Mock Metadata booleanMetadata;
	@Mock Metadata dateMetadata;
	@Mock Metadata contentMetadata;
	String textValue = "text value";
	LocalDateTime date = new LocalDateTime(2000, 10, 20, 10, 50);
	int numberValue1 = 1;
	int numberValue12 = 12;
	int numberValue100 = 100;

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

		IsLessThanCriterion criterion = new IsLessThanCriterion(numberValue12);

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"numberMetadata:{* TO 12} AND (*:* -(numberMetadata:\"" + Integer.MIN_VALUE + "\"))");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsLessThanCriterion criterion = new IsLessThanCriterion(textValue);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:{* TO \"text value\"} AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(date.toDate().getTime()));
		date = date.plusMillis(offsetMillis);

		IsLessThanCriterion criterion = new IsLessThanCriterion(date);

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo(
				"dateTimeMetadata:{* TO " + date + "Z} AND (*:* -(dateTimeMetadata:\"" + CriteriaUtils
						.getNullDateValue() + "\")) ");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessThanCriterion criterion = new IsLessThanCriterion(anInteger());

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessThanCriterion criterion = new IsLessThanCriterion(aString());

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessThanCriterion criterion = new IsLessThanCriterion(anInteger());

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessThanCriterion criterion = new IsLessThanCriterion(aString());

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessThanCriterion criterion = new IsLessThanCriterion(aDateTime());

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessThanCriterion criterion = new IsLessThanCriterion(aString());

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {

		String value = "value with spaces";

		IsLessThanCriterion criterion = new IsLessThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:{* TO \"value with spaces\"} AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		String value = "value*with*asterisks*";

		IsLessThanCriterion criterion = new IsLessThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:{* TO \"value*with*asterisks*\"} AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		String value = "value?with?wildcards?";

		IsLessThanCriterion criterion = new IsLessThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:{* TO \"value?with?wildcards?\"} AND (*:* -(textMetadata:\"__NULL__\"))");
	}
}
