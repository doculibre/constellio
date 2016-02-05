package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsFalseCriterion;
import com.constellio.sdk.tests.ConstellioTest;

/**
 * Created by maxime on 7/30/14.
 */
public class IsFalseCriterionTest extends ConstellioTest {
	@Mock Metadata textMetadata;
	@Mock Metadata booleanMetadata;
	private IsFalseCriterion falsecriterion;

	@Before
	public void setUp() {
		willReturn("firstText").given(textMetadata).getCode();
		willReturn("boolean").given(booleanMetadata).getCode();
		willReturn("enabled_s").given(booleanMetadata).getDataStoreCode();
		willReturn(MetadataValueType.BOOLEAN).given(booleanMetadata).getType();
		willReturn(MetadataValueType.STRING).given(textMetadata).getType();

		falsecriterion = new IsFalseCriterion();
	}

	@Test
	public void givenBooleanMetadataWhenIsValidForThenReturnTrue() {
		assertThat(falsecriterion.isValidFor(booleanMetadata)).isTrue();
	}

	@Test
	public void givenTextMetadataWhenIsValidForThenReturnFalse() {
		assertThat(falsecriterion.isValidFor(textMetadata)).isFalse();
	}

	@Test
	public void givenBooleanMetadataWhenGetSolrQueryThenValidSolrQuery() {
		String expectedQuery = "enabled_s:__FALSE__";
		String query = falsecriterion.getSolrQuery(booleanMetadata);
		assertThat(query).isEqualTo(expectedQuery);
	}
}
