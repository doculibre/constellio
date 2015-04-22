/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.entities.search.logical.criterion;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.endingWithText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.not;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.startingWithText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;

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

		assertThat(andSearchCondition.getSolrQuery()).isEqualTo("( ( (*:* -(firstText:chuck*) ) ) AND ( firstText:*noris ) )");
	}

	@org.junit.Test
	public void givenStartsWithConditionThenReturnValidSolrString() {
		CompositeLogicalSearchCondition andSearchCondition = new CompositeLogicalSearchCondition(new SchemaFilters(schemaType),
				LogicalOperator.AND, secondConditions);

		assertThat(andSearchCondition.getSolrQuery()).isEqualTo(
				"( ( ( (*:* -(firstText:chuck*) ) AND (*:* -(firstText:*lechat) ) ) ) )");
	}
}
