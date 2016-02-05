package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsOldLikeCriterion;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.sdk.tests.ConstellioTest;

public class IsOldLikeCriterionTest extends ConstellioTest {

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

		LocalDateTime now = TimeProvider.getLocalDateTime();
		givenTimeIs(now);
		IsOldLikeCriterion criterion = new IsOldLikeCriterion(1.0, MeasuringUnitTime.DAYS);
		LocalDateTime ldt = now.minusDays(1);

		assertThat(criterion.getSolrQuery(dateMetadata))
				.isEqualTo("dateTimeMetadata:\"" + ldt + "Z\"");
	}

	@Test
	public void givenDateMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsOldLikeCriterion criterion = new IsOldLikeCriterion(numberValue1, MeasuringUnitTime.DAYS);

		assertThat(criterion.isValidFor(numberMetadata)).isFalse();
	}

	@Test
	public void givenContentMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsOldLikeCriterion criterion = new IsOldLikeCriterion(numberValue1, MeasuringUnitTime.DAYS);

		assertThat(criterion.isValidFor(dateMetadata)).isTrue();
	}

	//
}
