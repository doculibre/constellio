package com.constellio.model.entities.search.logical.criterion;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderParams;
import org.mockito.Mock;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.when;

/**
 * Created by maxime on 8/11/14.
 */
public class NotCriterionTest extends com.constellio.sdk.tests.ConstellioTest {

	@Mock MetadataSchemaType schemaType;
	@Mock MetadataSchema schema;
	@Mock Metadata booleanMetadata;
	@Mock Metadata firstTextMetadata;
	private java.util.List<LogicalSearchCondition> firstConditions;
	private java.util.List<LogicalSearchCondition> secondConditions;

	private SolrQueryBuilderParams params = new SolrQueryBuilderParams(false, null, null);

	@org.junit.Before
	public void setUp() {
		willReturn("firstText").given(firstTextMetadata).getDataStoreCode();
		willReturn(com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN).given(booleanMetadata).getType();
		willReturn(com.constellio.model.entities.schemas.MetadataValueType.STRING).given(firstTextMetadata).getType();
		when(firstTextMetadata.getCollection()).thenReturn(zeCollection);
		when(booleanMetadata.getCollection()).thenReturn(zeCollection);

		firstConditions = new java.util.ArrayList<>();

		LogicalSearchCondition endsWith = where(firstTextMetadata).isEndingWithText("noris");
		LogicalSearchCondition notStarting = where(firstTextMetadata).isNot(startingWithText("chuck"));

		firstConditions.add(notStarting);
		firstConditions.add(endsWith);

		secondConditions = new java.util.ArrayList<>();

		LogicalSearchCondition notStartingAndEnding = where(firstTextMetadata).isNot(startingWithText("chuck")).and(
				asList(not(endingWithText("lechat"))));

		secondConditions.add(notStartingAndEnding);
	}

	@org.junit.Test
	public void givenStartsWithAndEndsWithConditionThenReturnValidSolrString() {
		CompositeLogicalSearchCondition andSearchCondition = new CompositeLogicalSearchCondition(new SchemaFilters(schemaType),
				LogicalOperator.AND, firstConditions);

		assertThat(andSearchCondition.getSolrQuery(params))
				.isEqualTo("( ( (*:* -(firstText:chuck*) ) ) AND ( firstText:*noris ) )");
	}

	@org.junit.Test
	public void givenStartsWithConditionThenReturnValidSolrString() {
		CompositeLogicalSearchCondition andSearchCondition = new CompositeLogicalSearchCondition(new SchemaFilters(schemaType),
				LogicalOperator.AND, secondConditions);

		assertThat(andSearchCondition.getSolrQuery(params)).isEqualTo(
				"( ( ( (*:* -(firstText:chuck*) ) AND (*:* -(firstText:*lechat) ) ) ) )");
	}
}
