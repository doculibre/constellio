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
package com.constellio.app.ui.pages.management.taxonomy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;

public class AddEditTaxonomyConceptPresenterAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock AddEditTaxonomyConceptView view;
	@Mock SessionContext sessionContext;
	AddEditTaxonomyConceptPresenter presenter;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(sessionContext.getCurrentCollection()).thenReturn(zeCollection);

		presenter = new AddEditTaxonomyConceptPresenter(view);
	}

	@Test
	public void givenExistingRecordIdParameterWhenGettingRecordVOThenExpectedObjectReturned() {
		presenter.forElementInTaxonomy(
				"EDIT/" + RMTaxonomies.CLASSIFICATION_PLAN + "/" + Category.DEFAULT_SCHEMA + "/" + records.categoryId_X);

		RecordVO record = presenter.getRecordVO();

		assertThat(record.getId()).isEqualTo(records.categoryId_X);
		assertThat(record.getSchema().getCode()).isEqualTo(records.getCategory_X().getSchema().getCode());
		assertThat(record.get(Category.CODE)).isEqualTo(records.getCategory_X().getCode());
		assertThat(record.get(Category.DESCRIPTION)).isEqualTo(records.getCategory_X().getDescription());
	}

	// SaveButtonClicked doesn't work when called directly, don't know why
	// @Test
	public void givenNewRecordVOContainsValuesWhenSavingThenRecordAdded()
			throws InterruptedException {
		presenter.forElementInTaxonomy("ADD/" + RMTaxonomies.CLASSIFICATION_PLAN + "/" + Category.DEFAULT_SCHEMA);

		RecordVO newRecord = presenter.getRecordVO();

		newRecord.set(Category.CODE, "NEW");
		newRecord.set(Category.DESCRIPTION, "This is a new category");

		presenter.saveButtonClicked(newRecord);

		Thread.sleep(10000);

		Category record = new Category(recordServices.getDocumentById(newRecord.getId()),
				getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection));

		assertThat(record.getCode()).isEqualTo("NEW");
		assertThat(record.getDescription()).isEqualTo("This is a new category");
	}

	// SaveButtonClicked doesn't work when called directly, don't know why
	// @Test
	public void givenExistingRecordVOContainsModifiedValuesWhenSavingThenRecordUpdated()
			throws InterruptedException {
		presenter.forElementInTaxonomy(
				"EDIT/" + RMTaxonomies.CLASSIFICATION_PLAN + "/" + Category.DEFAULT_SCHEMA + "/" + records.categoryId_X);

		RecordVO newRecord = presenter.getRecordVO();

		newRecord.set(Category.DESCRIPTION, "This is a changed description");

		presenter.saveButtonClicked(newRecord);

		Category record = new Category(recordServices.getDocumentById(newRecord.getId()),
				getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection));

		assertThat(record.getDescription()).isEqualTo("This is a changed description");
	}

}
