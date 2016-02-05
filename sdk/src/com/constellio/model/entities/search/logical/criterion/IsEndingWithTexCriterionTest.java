package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsEndingWithTextCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsEndingWithTexCriterionTest extends ConstellioTest {

	@Mock Metadata textMetadata;
	@Mock Metadata booleanMetadata;

	String value = "value";

	@Before
	public void setUp()
			throws Exception {
		when(textMetadata.getDataStoreCode()).thenReturn("textMetadata");
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);

		when(booleanMetadata.getDataStoreCode()).thenReturn("booleanMetadata");
		when(booleanMetadata.getType()).thenReturn(MetadataValueType.BOOLEAN);
	}

	@Test
	public void whenGettingSolrQueryThenQueryIsCorrect() {

		IsEndingWithTextCriterion criterion = new IsEndingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:*value");
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {

		value = "value with spaces";

		IsEndingWithTextCriterion criterion = new IsEndingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:*value\\ with\\ spaces");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		value = "value*with*asterisks";

		IsEndingWithTextCriterion criterion = new IsEndingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:*value\\*with\\*asterisks");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		value = "value?with?wildcards";

		IsEndingWithTextCriterion criterion = new IsEndingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:*value\\?with\\?wildcards");
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsEndingWithTextCriterion criterion = new IsEndingWithTextCriterion(value);

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenWrongTypeMetadataWhenCheckingIsValidForThenReturnFalse() {

		IsEndingWithTextCriterion criterion = new IsEndingWithTextCriterion(value);

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}
}
