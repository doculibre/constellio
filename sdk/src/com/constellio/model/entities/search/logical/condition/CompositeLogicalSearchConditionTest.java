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
package com.constellio.model.entities.search.logical.condition;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.sdk.tests.ConstellioTest;

/**
 * Created by maxime on 7/31/14.
 */
public class CompositeLogicalSearchConditionTest extends ConstellioTest {

	@Mock MetadataSchema schema;
	@Mock Metadata booleanMetadata;
	@Mock Metadata firstTextMetadata;
	private List<LogicalSearchCondition> validConditions;

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
		assertThat(andSearchCondition.getSolrQuery()).isEqualTo(
				"( ( firstText:chuck* ) AND ( firstText:*lechat* ) AND ( firstText:*noris ) )");
	}

	@Test
	public void givenStartsWithOrEndsWithConditionThenReturnValidSolrString() {
		CompositeLogicalSearchCondition andSearchCondition = new CompositeLogicalSearchCondition(new SchemaFilters(schema),
				LogicalOperator.OR, validConditions);
		assertThat(andSearchCondition.getSolrQuery()).isEqualTo(
				"( ( firstText:chuck* ) OR ( firstText:*lechat* ) OR ( firstText:*noris ) )");
	}
}
