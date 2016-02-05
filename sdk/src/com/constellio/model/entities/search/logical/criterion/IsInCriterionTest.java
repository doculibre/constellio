package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsInCriterion;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;

public class IsInCriterionTest extends ConstellioTest {

	@Mock Metadata textMetadata;
	@Mock Metadata referenceMetadata;
	@Mock Metadata numberMetadata;
	@Mock Metadata booleanMetadata;
	@Mock Metadata dateMetadata;
	@Mock Metadata contentMetadata;

	LocalDateTime date = new LocalDateTime(2001, 9, 15, 9, 40);
	LocalDateTime date2 = new LocalDateTime(2003, 10, 20, 10, 50);
	int intValue1 = 1;
	int intValue20 = 20;
	int intValue100 = 100;
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

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(intValue1, intValue20, intValue100));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"numberMetadata:\"1\" OR numberMetadata:\"20\" OR numberMetadata:\"100\"");
	}

	@Test
	public void whenGettingSolrQueryWithLongsThenQueryIsCorrect() {
		long longValue1 = 1l;
		long longValue20 = 20l;
		long longValue100 = 100l;

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(longValue1, longValue20, longValue100));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"numberMetadata:\"1\" OR numberMetadata:\"20\" OR numberMetadata:\"100\"");
	}

	@Test
	public void whenGettingSolrQueryWithFloatsThenQueryIsCorrect() {
		float floatValue1 = 1.0f;
		float floatValue20 = 20f;
		float floatValue100 = 100f;

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(floatValue1, floatValue20, floatValue100));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"numberMetadata:\"1.0\" OR numberMetadata:\"20.0\" OR numberMetadata:\"100.0\"");
	}

	@Test
	public void whenGettingSolrQueryWithRecordsThenQueryIsCorrect() {
		Record record1 = new TestRecord("a", "zeCollection", "id1");
		Record record2 = new TestRecord("a", "zeCollection", "id2");

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(record1, record2));

		assertThat(criterion.getSolrQuery(referenceMetadata)).isEqualTo(
				"referenceMetadata:\"id1\" OR referenceMetadata:\"id2\"");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(atextValue, zetextValue));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:\"a\\ text\\ value\" OR textMetadata:\"ze\\ text\\ value\"");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(date.toDate().getTime()));
		int offsetMillis2 = -1 * (DateTimeZone.getDefault().getOffset(date2.toDate().getTime()));

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(date, date2));

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo(
				"dateTimeMetadata:\"" + date + "Z\" OR dateTimeMetadata:\"" + date2 + "Z\"");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(anInteger(), anInteger()));

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsInCriterion criterion = new IsInCriterion(Arrays.asList(aString(), aString()));

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsInCriterion criterion = new IsInCriterion(Arrays.asList(anInteger(), anInteger()));

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsInCriterion criterion = new IsInCriterion(Arrays.asList(aString(), aString()));

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsInCriterion criterion = new IsInCriterion(Arrays.asList(aDateTime(), aDateTime()));

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsInCriterion criterion = new IsInCriterion(Arrays.asList(aString(), aString()));

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {
		String value = "value*with*asterisks*";
		String value2 = "value with spaces";

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(value, value2));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:\"value\\*with\\*asterisks\\*\" OR textMetadata:\"value\\ with\\ spaces\"");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {
		String value = "value?with?wildcards";
		String value2 = "value with spaces";

		IsInCriterion criterion = new IsInCriterion(Arrays.asList(value, value2));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"textMetadata:\"value\\?with\\?wildcards\" OR textMetadata:\"value\\ with\\ spaces\"");
	}
}
