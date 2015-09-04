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
package com.constellio.app.ui.pages.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.setups.Users;

public class SimpleSearchPresenterAcceptanceTest extends ConstellioTest {

	String retentionRuleFacetId = "retentionRuleFacetId";
	String archivisticStatusFacetId = "archivisticStatusFacetId";
	String typeFacetId = "typeFacetId";

	LocalDateTime threeYearsAgo = new LocalDateTime().minusYears(3);

	LogicalSearchQuery allFolders;
	LogicalSearchQuery allFoldersAndDocuments;

	RMSchemasRecordsServices rm;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	SearchServices searchServices;
	SearchPresenterService searchPresenterService;

	@Mock SimpleSearchView view;
	SimpleSearchPresenter simpleSearchPresenter;

	long allFolderDocumentsContainersCount;

	long allFolderDocumentsContainersCountWithRetentionRule1;

	long allActiveFolderDocumentsContainersCountWithRetentionRule1;

	long foldersCount, documentsCount, containersCount;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getSessionContext()).thenReturn(FakeSessionContext.gandalfInCollection(zeCollection));
		simpleSearchPresenter = new SimpleSearchPresenter(view);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();

		allFolders = new LogicalSearchQuery(from(rm.folderSchemaType()).returnAll());
		allFoldersAndDocuments = new LogicalSearchQuery(from(asList(rm.folderSchemaType(), rm.documentSchemaType())).returnAll());

		clearExistingFacets();

		recordServices.add(rm.newFacetQuery(typeFacetId).setOrder(0).setTitle("Type")
				.withQuery("schema_s:folder*", "Dossiers")
				.withQuery("schema_s:document*", "Documents")
				.withQuery("schema_s:containerRecord*", "Contenants"));

		recordServices.add(rm.newFacetField(retentionRuleFacetId).setOrder(1).setTitle("Règles de conservations")
				.setFieldDataStoreCode("retentionRuleId_s"));
		recordServices.add(rm.newFacetField(archivisticStatusFacetId).setOrder(2).setTitle("Archivistic status")
				.setFieldDataStoreCode("archivisticStatus_s"));

		allFolderDocumentsContainersCount = searchServices.getResultsCount(
				from(asList(rm.folderSchemaType(), rm.documentSchemaType(), rm.containerRecordSchemaType())).returnAll());

		allFolderDocumentsContainersCountWithRetentionRule1 = searchServices.getResultsCount(
				from(asList(rm.folderSchemaType(), rm.documentSchemaType(), rm.containerRecordSchemaType()))
						.where(rm.folderRetentionRule()).isEqualTo(records.ruleId_1));

		allActiveFolderDocumentsContainersCountWithRetentionRule1 = searchServices.getResultsCount(
				from(asList(rm.folderSchemaType(), rm.documentSchemaType(), rm.containerRecordSchemaType()))
						.where(rm.folderRetentionRule()).isEqualTo(records.ruleId_1)
						.andWhere(rm.folderArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE));

		foldersCount = searchServices.getResultsCount(from(rm.folderSchemaType()).returnAll());
		documentsCount = searchServices.getResultsCount(from(rm.documentSchemaType()).returnAll());
		containersCount = searchServices.getResultsCount(from(rm.containerRecordSchemaType()).returnAll());
	}

	@Test
	public void givenSelectedFieldFacetThenAppliedToSearchResults()
			throws Exception {

		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCount);

		simpleSearchPresenter.facetValueSelected(retentionRuleFacetId, records.ruleId_1);
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCountWithRetentionRule1);

		simpleSearchPresenter.facetValueSelected(archivisticStatusFacetId, FolderStatus.ACTIVE.getCode());
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allActiveFolderDocumentsContainersCountWithRetentionRule1);

		simpleSearchPresenter.facetValueDeselected(archivisticStatusFacetId, FolderStatus.ACTIVE.getCode());
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(allFolderDocumentsContainersCountWithRetentionRule1);
	}

	@Test
	public void givenSelectedQueryFacetThenAppliedToSearchResults()
			throws Exception {

		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(foldersCount + documentsCount + containersCount);

		simpleSearchPresenter.facetValueSelected(typeFacetId, "schema_s:folder*");
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(foldersCount);

		simpleSearchPresenter.facetValueSelected(typeFacetId, "schema_s:document*");
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(foldersCount + documentsCount);

		simpleSearchPresenter.facetValueDeselected(typeFacetId, "schema_s:folder*");
		assertThat(searchServices.getResultsCount(simpleSearchPresenter.getSearchQuery()))
				.isEqualTo(documentsCount);
	}

	// ---------------------------------------

	private void clearExistingFacets() {
		for (Record facetRecord : searchServices.search(new LogicalSearchQuery(from(rm.facetSchemaType()).returnAll()))) {
			recordServices.logicallyDelete(facetRecord, User.GOD);
			recordServices.physicallyDelete(facetRecord, User.GOD);
		}
	}
}
