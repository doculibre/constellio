package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsFalseOrNullCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsFalseOrNullCriterionTest extends ConstellioTest {
	@Mock Metadata textMetadata;
	@Mock Metadata booleanMetadata;
	private IsFalseOrNullCriterion criterion;

	@Before
	public void setUp() {
		willReturn("firstText").given(textMetadata).getCode();
		willReturn("boolean").given(booleanMetadata).getCode();
		willReturn("enabled_s").given(booleanMetadata).getDataStoreCode();
		willReturn(MetadataValueType.BOOLEAN).given(booleanMetadata).getType();
		willReturn(MetadataValueType.STRING).given(textMetadata).getType();

		criterion = new IsFalseOrNullCriterion();
	}

	@Test
	public void givenBooleanMetadataWhenIsValidForThenReturnTrue() {
		assertThat(criterion.isValidFor(booleanMetadata)).isTrue();
	}

	@Test
	public void givenBooleanMetadataWhenIsValidForThenReturnFalse() {
		assertThat(criterion.isValidFor(textMetadata)).isFalse();
	}

	@Test
	public void givenTextMetadataWhenIsValidForThenReturnFalse() {
		assertThat(criterion.isValidFor(textMetadata)).isFalse();
	}

	@Test
	public void givenBooleanMetadataWhenGetSolrQueryThenValidSolrQuery() {
		String expectedQuery = "(*:* -enabled_s:__TRUE__)";
		String query = criterion.getSolrQuery(booleanMetadata);
		assertThat(query).isEqualTo(expectedQuery);
	}
}
