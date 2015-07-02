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
package com.constellio.app.ui.pages.search.criteria;

import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.AND;
import static com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator.OR;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class ConditionBuilderAcceptTest extends ConstellioTest {

	SearchServices searchServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	ConditionBuilder folderConditionBuilder;

	@Test
	public void test()
			throws Exception {

		// A && ( B || C)
		CriteriaBuilder builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.ADMINISTRATIVE_UNIT).isEqualTo(records.unitId_10).booleanOperator(AND);
		builder.addCriterion("title").isContainingText("Écureuil").withLeftParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("ouille").withRightParens();
		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A45, records.folder_A49);

		// A && ( B || (C & D) || E)
		builder = newFolderCriteriaBuilderAsAdmin();
		builder.addCriterion(Folder.ADMINISTRATIVE_UNIT).isEqualTo(records.unitId_10).booleanOperator(AND);
		builder.addCriterion("title").isContainingText("Écureuil").withLeftParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("Chauve").withLeftParens().booleanOperator(AND);
		builder.addCriterion("title").isContainingText("souris").withRightParens().booleanOperator(OR);
		builder.addCriterion("title").isContainingText("ouille").withRightParens();
		assertThat(recordIdsOfFolderCriteria(builder)).containsOnly(records.folder_A45, records.folder_A49, records.folder_A17);

	}

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		searchServices = getModelLayerFactory().newSearchServices();

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		folderConditionBuilder = new ConditionBuilder(rm.folderSchemaType());
	}

	private List<String> recordIdsOfFolderCriteria(CriteriaBuilder criteriaBuilder)
			throws Exception {
		LogicalSearchCondition condition = folderConditionBuilder.build(criteriaBuilder.build());
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		return searchServices.searchRecordIds(query);
	}

	private CriteriaBuilder newFolderCriteriaBuilderAsAdmin() {
		SessionContext sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		return new CriteriaBuilder(rm.folderSchemaType(), sessionContext);
	}
}
