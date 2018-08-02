package com.constellio.app.ui.pages.management.taxonomy;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
		when(sessionContext.getCurrentLocale()).thenReturn(Locale.FRENCH);

		presenter = new AddEditTaxonomyConceptPresenter(view);
	}

	@Test
	public void givenExistingRecordIdParameterWhenGettingRecordVOThenExpectedObjectReturned() {
		presenter.forElementInTaxonomy(
				"EDIT/" + RMTaxonomies.CLASSIFICATION_PLAN + "/" + Category.DEFAULT_SCHEMA + "/" + records.categoryId_X);

		RecordVO record = presenter.getRecordVO();

		assertThat(record.getId()).isEqualTo(records.categoryId_X);
		assertThat(record.getSchema().getCode()).isEqualTo(records.getCategory_X().getSchema().getCode());
		assertThat(record.<String>get(Category.CODE)).isEqualTo(records.getCategory_X().getCode());
		assertThat(record.<String>get(Category.DESCRIPTION)).isEqualTo(records.getCategory_X().getDescription());
	}

	// SaveButtonClicked doesn't work when called directly, don't know why
	// @Test
	public void givenNewRecordVOContainsValuesWhenSavingThenRecordAdded()
			throws InterruptedException {
		presenter.forElementInTaxonomy("ADD/" + RMTaxonomies.CLASSIFICATION_PLAN + "/" + Category.DEFAULT_SCHEMA);

		RecordVO newRecord = presenter.getRecordVO();

		newRecord.set(Category.CODE, "NEW");
		newRecord.set(Category.DESCRIPTION, "This is a new category");

		presenter.saveButtonClicked(newRecord, false);

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

		presenter.saveButtonClicked(newRecord, false);

		Category record = new Category(recordServices.getDocumentById(newRecord.getId()),
				getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection));

		assertThat(record.getDescription()).isEqualTo("This is a changed description");
	}

}
