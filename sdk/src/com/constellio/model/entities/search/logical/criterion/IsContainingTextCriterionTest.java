package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsContainingTextCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsContainingTextCriterionTest extends ConstellioTest {

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

		IsContainingTextCriterion criterion = new IsContainingTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:*value*");
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {

		value = "value with spaces";

		IsContainingTextCriterion criterion = new IsContainingTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:*value\\ with\\ spaces*");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {

		value = "value*with*asterisks";

		IsContainingTextCriterion criterion = new IsContainingTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:*value\\*with\\*asterisks*");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {

		value = "value?with?wildcards";

		IsContainingTextCriterion criterion = new IsContainingTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:*value\\?with\\?wildcards*");
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {

		IsContainingTextCriterion criterion = new IsContainingTextCriterion(value);

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenWrongTypeMetadataWhenCheckingIsValidForThenReturnFalse() {

		IsContainingTextCriterion criterion = new IsContainingTextCriterion(value);

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}
}
