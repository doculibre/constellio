package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsContainingElementsCriterion;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;

public class IsContainingElementsCriterionTest extends ConstellioTest {

	@Mock Metadata textMetadata;
	@Mock Metadata textMetadata1;
	@Mock Metadata referenceMetadata;
	@Mock Metadata numberMetadata;
	@Mock Metadata booleanMetadata;
	@Mock Metadata dateTimeMetadata;
	@Mock Metadata dateMetadata;
	@Mock Metadata contentMetadata;

	LocalDateTime dateTime = new LocalDateTime(2000, 10, 20, 10, 50);
	LocalDateTime dateTime2 = new LocalDateTime(2002, 10, 20, 22, 50);
	LocalDate date = new LocalDate(2000, 10, 20);
	LocalDate date2 = new LocalDate(2002, 10, 20);
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

		when(dateTimeMetadata.getDataStoreCode()).thenReturn("dateTimeMetadata");
		when(dateTimeMetadata.getType()).thenReturn(MetadataValueType.DATE_TIME);
		when(dateTimeMetadata.isMultivalue()).thenReturn(true);

		when(dateMetadata.getDataStoreCode()).thenReturn("dateMetadata");
		when(dateMetadata.getType()).thenReturn(MetadataValueType.DATE);
		when(dateMetadata.isMultivalue()).thenReturn(true);

		when(contentMetadata.getDataStoreCode()).thenReturn("contentMetadata");
		when(contentMetadata.getType()).thenReturn(MetadataValueType.CONTENT);
		when(contentMetadata.isMultivalue()).thenReturn(true);

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(dateTime.toDate().getTime()));
		int offsetMillis2 = -1 * (DateTimeZone.getDefault().getOffset(dateTime2.toDate().getTime()));
	}

	@Test
	public void givenOneValueWhenGettingSolrQueryThenQueryIsCorrect() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(numberValue12));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo("numberMetadata:\"12\"");
	}

	@Test
	public void givenThreeValuesWhenGettingSolrQueryThenQueryIsCorrect() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(numberValue12, numberValue100,
				numberValueNegative1));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"numberMetadata:\"12\" AND numberMetadata:\"100\" AND numberMetadata:\"\\-1\"");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(textValue, textValue2));

		assertThat(criterion.getSolrQuery(textMetadata))
				.isEqualTo("textMetadata:\"text\\ value\" AND textMetadata:\"text\\ value2\"");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {
		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(date, date2));

		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo(
				"dateMetadata:\"" + date.toLocalDateTime(LocalTime.MIDNIGHT) + "Z\" AND dateMetadata:\"" + date2
						.toLocalDateTime(LocalTime.MIDNIGHT) + "Z\"");
	}

	@Test
	public void givenDateTimeWhenGettingSolrQueryThenQueryIsCorrect() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(dateTime, dateTime2));

		assertThat(criterion.getSolrQuery(dateTimeMetadata)).isEqualTo(
				"dateTimeMetadata:\"" + dateTime + "Z\" AND dateTimeMetadata:\"" + dateTime2
						+ "Z\"");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(anInteger()));

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(aString()));

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataSingleValueMetadataWhenCheckingIsValidForThenReturnFalse() {

		when(textMetadata.isMultivalue()).thenReturn(false);

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(aString()));

		assertThat(criterion.isValidFor(textMetadata)).isFalse();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(anInteger()));

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenDateTimeMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(aDateTime()));

		assertThat(criterion.isValidFor(dateTimeMetadata)).isTrue();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(aDate()));

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(aString()));

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value with spaces";

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:\"value\\ with\\ spaces\"");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value*with*asterisks*";

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:\"value\\*with\\*asterisks\\*\"");
	}

	@Test
	public void givenRecordWhenGettingSolrQueryThenQueryIsCorrect() {

		Record record1 = new TestRecord("code", "zeCollection", "zeId1");
		Record record2 = new TestRecord("code", "zeCollection", "zeId2");

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(record1, record2));

		assertThat(criterion.getSolrQuery(referenceMetadata))
				.isEqualTo("referenceMetadata:\"zeId1\" AND referenceMetadata:\"zeId2\"");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value?with?wildcards?";

		IsContainingElementsCriterion criterion = new IsContainingElementsCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:\"value\\?with\\?wildcards\\?\"");
	}
}
