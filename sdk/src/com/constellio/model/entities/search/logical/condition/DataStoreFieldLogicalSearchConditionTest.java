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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.sdk.tests.ConstellioTest;

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
		assertThat(andSearchCondition.getSolrQuery()).isEqualTo("( firstText:edouard* AND secondText:edouard* )");
	}

	@Test
	public void givenMetadatasAndOrOperatorStartsWithConditionThenReturnValidSolrString() {
		DataStoreFieldLogicalSearchCondition andSearchCondition = new DataStoreFieldLogicalSearchCondition(
				new SchemaFilters(schemaType), metadatas, LogicalOperator.OR, startsWith);
		assertThat(andSearchCondition.getSolrQuery()).isEqualTo("( firstText:edouard* OR secondText:edouard* )");
	}
}
