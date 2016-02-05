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
import com.constellio.model.services.search.query.logical.criteria.IsLessOrEqualThanCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsLessOrEqualThanCriterionTest extends ConstellioTest {

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

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(numberValue12);

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"numberMetadata:[* TO 12] AND (*:* -(numberMetadata:\"" + Integer.MIN_VALUE + "\"))");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(textValue);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[* TO \"text value\"] AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(date.toDate().getTime()));

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(date);

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo(
				"dateTimeMetadata:[* TO " + date + "Z] AND (*:* -(dateTimeMetadata:\"" + CriteriaUtils
						.getNullDateValue() + "\"))");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(anInteger());

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(aString());

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(anInteger());

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(aString());

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(aDateTime());

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(aString());

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {

		String value = "value with spaces";

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[* TO \"value with spaces\"] AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		String value = "value*with*asterisks*";

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[* TO \"value*with*asterisks*\"] AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		String value = "value?with?wildcards?";

		IsLessOrEqualThanCriterion criterion = new IsLessOrEqualThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:[* TO \"value?with?wildcards?\"] AND (*:* -(textMetadata:\"__NULL__\"))");
	}
}
