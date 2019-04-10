package com.constellio.model.entities.search.logical.condition;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderContext;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

/**
 * Created by maxime on 7/31/14.
 */
public class CompositeLogicalSearchConditionTest extends ConstellioTest {

	@Mock MetadataSchema schema;
	@Mock Metadata booleanMetadata;
	@Mock Metadata firstTextMetadata;
	private List<LogicalSearchCondition> validConditions;
	private SolrQueryBuilderContext params = new SolrQueryBuilderContext(false, new ArrayList<>(), null, null, null, null);

	@Before
	public void setUp() {
		willReturn("firstText").given(firstTextMetadata).getDataStoreCode();
		willReturn(MetadataValueType.BOOLEAN).given(booleanMetadata).getType();
		willReturn(MetadataValueType.STRING).given(firstTextMetadata).getType();

		validConditions = new ArrayList<>();

		LogicalSearchCondition startsWith = where(firstTextMetadata).isStartingWithText("chuck");
		LogicalSearchCondition endsWith = where(firstTextMetadata).isEndingWithText("noris");
		LogicalSearchCondition contains = where(firstTextMetadata).isContainingText("lechat");

		validConditions.add(startsWith);
		validConditions.add(contains);
		validConditions.add(endsWith);
	}

	@Test
	public void givenStartsWithAndEndsWithConditionThenReturnValidSolrString() {
		CompositeLogicalSearchCondition andSearchCondition = new CompositeLogicalSearchCondition(new SchemaFilters(schema),
				LogicalOperator.AND, validConditions);
		assertThat(andSearchCondition.getSolrQuery(params)).isEqualTo(
				"( ( firstText:chuck* ) AND ( firstText:*lechat* ) AND ( firstText:*noris ) )");
	}

	@Test
	public void givenStartsWithOrEndsWithConditionThenReturnValidSolrString() {
		CompositeLogicalSearchCondition andSearchCondition = new CompositeLogicalSearchCondition(new SchemaFilters(schema),
				LogicalOperator.OR, validConditions);
		assertThat(andSearchCondition.getSolrQuery(params)).isEqualTo(
				"( ( firstText:chuck* ) OR ( firstText:*lechat* ) OR ( firstText:*noris ) )");
	}
}
