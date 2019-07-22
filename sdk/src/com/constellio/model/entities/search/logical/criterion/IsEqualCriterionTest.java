package com.constellio.model.entities.search.logical.criterion;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsEqualCriterion;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class IsEqualCriterionTest extends ConstellioTest {

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

		IsEqualCriterion criterion = new IsEqualCriterion(numberValue12);

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo("numberMetadata:\"12\"");
	}

	@Test
	public void givenRecordWhenGettingSolrQueryThenQueryIsCorrect() {
		Record record = new TestRecord("code", "zeCollection", "zeId");

		IsEqualCriterion criterion = new IsEqualCriterion(record);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:\"zeId\"");
	}

	@Test
	public void givenNullWhenGettingSolrQueryThenQueryIsCorrect() {
		IsEqualCriterion criterion = new IsEqualCriterion(null);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"(*:* -textMetadata:*)");
	}

	@Test
	public void givenBooleanMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		IsEqualCriterion criterion = new IsEqualCriterion(booleanTrueValue);

		assertThat(criterion.getSolrQuery(booleanMetadata)).isEqualTo("booleanMetadata:\"__TRUE__\"");

		criterion = new IsEqualCriterion(booleanFalseValue);

		assertThat(criterion.getSolrQuery(booleanMetadata)).isEqualTo("booleanMetadata:\"__FALSE__\"");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsEqualCriterion criterion = new IsEqualCriterion(textValue);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:\"text\\ value\"");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		IsEqualCriterion criterion = new IsEqualCriterion(date);

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo("dateTimeMetadata:\"" + date + "Z\"");
	}

	@Test
	public void givenDateWithSummerTimeWhenGettingSolrQueryThenQueryIsCorrect() {

		LocalDateTime date = new LocalDateTime(2015, 07, 01, 22, 50);

		IsEqualCriterion criterion = new IsEqualCriterion(date);

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo("dateTimeMetadata:\"" + date + "Z\"");
	}

	@Test
	public void givenDateWithOutSummerTimeWhenGettingSolrQueryThenQueryIsCorrect() {

		LocalDateTime date = new LocalDateTime(2014, 11, 04, 22, 50);

		IsEqualCriterion criterion = new IsEqualCriterion(date);

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo("dateTimeMetadata:\"" + date + "Z\"");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsEqualCriterion criterion = new IsEqualCriterion(anInteger());

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsEqualCriterion criterion = new IsEqualCriterion(aString());

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsEqualCriterion criterion = new IsEqualCriterion(anInteger());

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsEqualCriterion criterion = new IsEqualCriterion(aString());

		assertThat(criterion.isValidFor(booleanMetadata)).isTrue();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsEqualCriterion criterion = new IsEqualCriterion(aDateTime());

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsEqualCriterion criterion = new IsEqualCriterion(aString());

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {

		String value = "value with spaces";

		IsEqualCriterion criterion = new IsEqualCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:\"value\\ with\\ spaces\"");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		String value = "value*with*asterisks*";

		IsEqualCriterion criterion = new IsEqualCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:\"value\\*with\\*asterisks\\*\"");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		String value = "value?with?wildcards?";

		IsEqualCriterion criterion = new IsEqualCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:\"value\\?with\\?wildcards\\?\"");
	}

}
