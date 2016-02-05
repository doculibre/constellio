package com.constellio.model.entities.search.logical.criterion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.criteria.IsNotNullCriterion;
import com.constellio.sdk.tests.ConstellioTest;

public class IsNotNullCriterionTest extends ConstellioTest {

	IsNotNullCriterion criterion;

	@Mock Metadata textMetadata, booleanMetadata, dateMetadata, numberMetadata;

	@Before
	public void setUp()
			throws Exception {
		criterion = new IsNotNullCriterion();

		when(textMetadata.getDataStoreCode()).thenReturn("textMetadata_s");
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);

		when(booleanMetadata.getDataStoreCode()).thenReturn("booleanMetadata_b");
		when(booleanMetadata.getType()).thenReturn(MetadataValueType.BOOLEAN);

		when(dateMetadata.getDataStoreCode()).thenReturn("dateMetadata_dt");
		when(dateMetadata.getType()).thenReturn(MetadataValueType.DATE_TIME);

		when(numberMetadata.getDataStoreCode()).thenReturn("numberMetadata_d");
		when(numberMetadata.getType()).thenReturn(MetadataValueType.NUMBER);
	}

	@Test
	public void givenTextMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		assertThat(criterion.getSolrQuery(textMetadata)).isEqualTo("(textMetadata_s:*)");
	}

	@Test
	public void givenBooleanMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		assertThat(criterion.getSolrQuery(booleanMetadata)).isEqualTo("(booleanMetadata_b:*)");
	}

	@Test
	public void givenDateMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		assertThat(criterion.getSolrQuery(dateMetadata)).isEqualTo("(dateMetadata_dt:*)");
	}

	@Test
	public void givenNumberMetadataWhenGettingSolrQueryThenQueryIsCorrect() {
		assertThat(criterion.getSolrQuery(numberMetadata)).isEqualTo("(numberMetadata_d:*)");
	}
}
