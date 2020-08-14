package com.constellio.model.entities.search.logical.criterion;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsStartingWithTextCriterion;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class IsStartingWithTextCriterionTest extends ConstellioTest {

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
		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value* OR textMetadata:value");
	}

	@Test
	public void givenValueContainsSpacesWhenGettingSolrQueryThenSpacesEscaped() {
		value = "value with spaces";

		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value\\ with\\ spaces* OR textMetadata:value\\ with\\ spaces");
	}

	@Test
	public void givenValueContainsAsterisksWhenGettingSolrQueryThenAsterisksEscaped() {
		value = "value*with*asterisks";

		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value\\*with\\*asterisks* OR textMetadata:value\\*with\\*asterisks");
	}

	@Test
	public void givenValueContainsWildCardsWhenGettingSolrQueryThenWildCardsEscaped() {
		value = "value?with?wildcards";

		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("textMetadata:value\\?with\\?wildcards* OR textMetadata:value\\?with\\?wildcards");
	}

	@Test
	public void givenTextMetadataWhenCheckingIsValidForThenReturnTrue() {
		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.isValidFor(textMetadata)).isTrue();
	}

	@Test
	public void givenWrongTypeMetadataWhenCheckingIsValidForThenReturnFalse() {
		IsStartingWithTextCriterion criterion = new IsStartingWithTextCriterion(value);

		assertThat(criterion.isValidFor(booleanMetadata)).isFalse();
	}
}
