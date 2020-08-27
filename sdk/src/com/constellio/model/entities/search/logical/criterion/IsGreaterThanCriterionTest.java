package com.constellio.model.entities.search.logical.criterion;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsGreaterThanCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsStartingWithTextCriterion;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class IsGreaterThanCriterionTest extends ConstellioTest {

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

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(numberValue12);

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo("numberMetadata:{12 TO *}");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(textValue);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:{\"text value\" TO *} AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(date.toDate().getTime()));

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(date);

		assertThat(criterion.getSolrQuery(dateMetadata))
				.isEqualTo("dateTimeMetadata:{" + date + "Z TO *} AND (*:* -(dateTimeMetadata:\"4242-06-06T06:42:42.666Z\")) ");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(anInteger());

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(aString());

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(anInteger());

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(aString());

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(aDateTime());

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(aString());

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {

		String value = "value with spaces";

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:{\"value with spaces\" TO *} AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		String value = "value*with*asterisks*";

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:{\"value*with*asterisks*\" TO *} AND (*:* -(textMetadata:\"__NULL__\"))");
	}

	@Test
	public void givenValueContainsSlashsWhenGettingSolrQueryThenAsterisksEscaped() {

		String value = "value/with/slashs";

		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value\\/with\\/slashs* OR textMetadata:value\\/with\\/slashs");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		String value = "value?with?wildcards?";

		IsGreaterThanCriterion criterion = new IsGreaterThanCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:{\"value?with?wildcards?\" TO *} AND (*:* -(textMetadata:\"__NULL__\"))");
	}
}
