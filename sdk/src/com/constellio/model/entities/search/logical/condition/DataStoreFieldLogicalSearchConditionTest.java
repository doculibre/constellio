package com.constellio.model.entities.search.logical.condition;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderContext;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

/**
 * Created by maxime on 7/31/14.
 */
public class DataStoreFieldLogicalSearchConditionTest extends ConstellioTest {

	@Mock MetadataSchemaType schemaType;
	@Mock MetadataSchema schema;
	@Mock Metadata secondTextMetadata;
	@Mock Metadata firstTextMetadata;
	private LogicalSearchValueCondition startsWith;
	private List<Metadata> metadatas;
	private SolrQueryBuilderContext params = new SolrQueryBuilderContext(false, new ArrayList<>(), null, null, null, null);

	@Before
	public void setUp() {
		willReturn("firstText").given(firstTextMetadata).getDataStoreCode();
		willReturn("secondText").given(secondTextMetadata).getDataStoreCode();
		willReturn(MetadataValueType.STRING).given(secondTextMetadata).getType();
		willReturn(MetadataValueType.STRING).given(firstTextMetadata).getType();
		metadatas = new ArrayList<>();
		metadatas.add(firstTextMetadata);
		metadatas.add(secondTextMetadata);

		startsWith = LogicalSearchQueryOperators.startingWithText("edouard");
	}

	@Test
	public void givenMetadatasAndAndOperatorStartsWithConditionThenReturnValidSolrString() {
		DataStoreFieldLogicalSearchCondition andSearchCondition = new DataStoreFieldLogicalSearchCondition(
				new SchemaFilters(schemaType), metadatas, LogicalOperator.AND, startsWith);
		assertThat(andSearchCondition.getSolrQuery(params)).isEqualTo("( firstText:edouard* OR firstText:edouard AND secondText:edouard* OR secondText:edouard )");
	}

	@Test
	public void givenMetadatasAndOrOperatorStartsWithConditionThenReturnValidSolrString() {
		DataStoreFieldLogicalSearchCondition andSearchCondition = new DataStoreFieldLogicalSearchCondition(
				new SchemaFilters(schemaType), metadatas, LogicalOperator.OR, startsWith);
		assertThat(andSearchCondition.getSolrQuery(params)).isEqualTo("( firstText:edouard* OR firstText:edouard OR secondText:edouard* OR secondText:edouard )");
	}
}
