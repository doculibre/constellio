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
import com.constellio.model.services.search.query.logical.criteria.IsNotContainingElementsCriterion;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;

public class IsNotContainingElementsCriterionTest extends ConstellioTest {

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

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(numberValue12));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo("(*:* -(numberMetadata:\"12\"))");
	}

	@Test
	public void givenThreeValuesWhenGettingSolrQueryThenQueryIsCorrect() {

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(numberValue12,
				numberValue100, numberValueNegative1));

		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo(
				"(*:* -(numberMetadata:\"12\" AND numberMetadata:\"100\" AND numberMetadata:\"-1\"))");
	}

	@Test
	public void givenTextWhenGettingSolrQueryThenQueryIsCorrect() {

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(textValue, textValue2));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo(
				"(*:* -(textMetadata:\"text\\ value\" AND textMetadata:\"text\\ value2\"))");
	}

	@Test
	public void givenDateWhenGettingSolrQueryThenQueryIsCorrect() {

		int offsetMillis = -1 * (DateTimeZone.getDefault().getOffset(date.toDate().getTime()));
		int offsetMillis2 = -1 * (DateTimeZone.getDefault().getOffset(date2.toDate().getTime()));

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(date, date2));

		assertThat(criterion.getSolrQuery(dateMetadata))
				.isEqualTo(
						"(*:* -(dateTimeMetadata:\"" + date + "Z\" AND dateTimeMetadata:\""
								+ date2 + "Z\"))");
	}

	@Test
	public void givenNumberMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(anInteger()));

		assertThat(criterion.isValidFor(numberMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(aString()));

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataSingleValueMetadataWhenCheckingIsValidForThenReturnFalse() {

		when(textMetadata.isMultivalue()).thenReturn(false);

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(aString()));

		assertThat(criterion.isValidFor(textMetadata)).isFalse();
	}

	@Test
	public void givenReferenceMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(anInteger()));

		assertThat(criterion.isValidFor(referenceMetadata)).isTrue();
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(aDateTime()));

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(aString()));

		assertThat(criterion.isValidFor(contentMetadata)).isTrue();
	}

	@Test
	public void givenRecordWhenGettingSolrQueryThenQueryIsCorrect() {

		Record record1 = new TestRecord("code", "zeCollection", "zeId1");
		Record record2 = new TestRecord("code", "zeCollection", "zeId2");

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(record1, record2));

		assertThat(criterion.getSolrQuery(referenceMetadata))
				.isEqualTo("(*:* -(referenceMetadata:\"zeId1\" AND referenceMetadata:\"zeId2\"))");
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value with spaces";

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("(*:* -(textMetadata:\"value\\ with\\ spaces\"))");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value*with*asterisks*";

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("(*:* -(textMetadata:\"value\\*with\\*asterisks\\*\"))");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenQueryIsCorrect() {

		String value = "value?with?wildcards?";

		IsNotContainingElementsCriterion criterion = new IsNotContainingElementsCriterion(Arrays.asList(value));

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("(*:* -(textMetadata:\"value\\?with\\?wildcards\\?\"))");
	}

}
