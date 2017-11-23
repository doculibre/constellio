package com.constellio.app.modules.rm.ui.pages.containers;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.STORAGES;
import static com.constellio.model.services.search.query.ReturnedMetadatasFilter.idVersionSchemaTitlePath;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.returnAll;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.container.fields.ContainerStorageSpaceLookupField;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerPresenter;
import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.trees.LinkableRecordTreeNodesDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

/**
 * Created by Constellio on 2017-01-10.
 */
public class AddEditContainerAcceptanceTest extends ConstellioTest {
	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	@Mock AddEditContainerViewImpl view;
	SessionContext sessionContext;
	AddEditContainerPresenter presenter;
	RMSchemasRecordsServices rm;
	@Mock ContainerStorageSpaceLookupField lookupField;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		getDataLayerFactory().getDataLayerLogger().monitor("idx_rfc_00000000001");

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		sessionContext = FakeSessionContext.adminInCollection(zeCollection);

		when(view.getConstellioFactories()).thenReturn(getConstellioFactories());
		when(view.getCollection()).thenReturn(zeCollection);
		when(view.getSessionContext()).thenReturn(sessionContext);
		doNothing().when(view).reloadWithContainer(any(RecordVO.class));

		presenter = new AddEditContainerPresenter(view);
		lookupField = spy(new ContainerStorageSpaceLookupField(records.containerTypeId_boite22x22, 100D, presenter));
	}

	@Test
	public void givenNoValidStorageSpaceThenSuggestionReturnNull()
			throws Exception {
		setAllStorageSpaceWithNoCapacity();
		waitForBatchProcess();
		recordServices.add(buildDefaultContainer());
		presenter.forParams("containerTest");
		lookupField.suggestedButtonClicked();

		verify(lookupField, never()).setFieldValue(any(Object.class));
	}

	@Test
	public void givenValidStorageSpaceThenSuggestionReturnFirst()
			throws Exception {
		setAllStorageSpaceWithNoCapacity();
		waitForBatchProcess();
		recordServices.add(buildDefaultStorageSpace());
		recordServices.add(buildDefaultContainer());
		presenter.forParams("containerTest");
		lookupField.suggestedButtonClicked();

		ArgumentCaptor<Object> lookupFieldCaptor = ArgumentCaptor.forClass(Object.class);
		verify(lookupField).setFieldValue(lookupFieldCaptor.capture());
		verify(lookupField, times(1)).setFieldValue(any(Object.class));
		assertThat(lookupFieldCaptor.getValue()).isEqualTo("storageTest");
	}

	@Test
	public void givenMultipleModeWithInsufficientStorageSpaceThenErrorIsThrownAndNoContainerCreated()
			throws RecordServicesException {
		recordServices.add(buildDefaultStorageSpace());
		presenter.forParams("/multiple");
		try {
			presenter.createMultipleContainer(buildDefaultContainer().setStorageSpace("storageTest").getWrappedRecord(), 10);
			fail("No exception thrown");
		} catch (Exception e) {

		} finally {
			long numberOfContainerCreated = getModelLayerFactory().newSearchServices()
					.getResultsCount(from(rm.containerRecord.schemaType())
							.where(rm.containerRecord.storageSpace()).isEqualTo("storageTest"));

			assertThat(numberOfContainerCreated).isEqualTo(0);
		}
	}

	@Test
	public void givenMultipleModeWithSufficientStorageSpaceThenMultipleContainersCreated()
			throws RecordServicesException {
		recordServices.add(buildDefaultStorageSpace());
		presenter.forParams("/multiple");
		presenter.createMultipleContainer(
				buildDefaultContainer().setStorageSpace("storageTest").setCapacity(10).getWrappedRecord(), 10);
		long numberOfContainerCreated = getModelLayerFactory().newSearchServices()
				.getResultsCount(from(rm.containerRecord.schemaType())
						.where(rm.containerRecord.storageSpace()).isEqualTo("storageTest"));

		assertThat(numberOfContainerCreated).isEqualTo(10);
	}

	@Test
	public void givenTreeWithConditionParentAreOnlySelectableIfTheyAlsoCorrespondToCriterias()
			throws Exception {
		StorageSpace parent = buildDefaultStorageSpace();
		recordServices.execute(transactionWithRecalculateAggregateMetadata(parent));
		StorageSpace child = buildDefaultChildStorageSpace();
		recordServices.execute(transactionWithRecalculateAggregateMetadata(child));
		recordServices.recalculate(parent);
		recordServices.execute(transactionWithRecalculateAggregateMetadata(parent));

		waitForBatchProcess();
		TaxonomiesSearchServices taxonomiesSearchServices = getModelLayerFactory().newTaxonomiesSearchService();

		LinkableRecordTreeNodesDataProvider container = ContainerStorageSpaceLookupField
				.getDataProvider(STORAGES, records.containerTypeId_boite22x22, null);
		Taxonomy taxonomy = getModelLayerFactory().getTaxonomiesManager().getTaxonomyFor(zeCollection, StorageSpace.SCHEMA_TYPE);

		TaxonomiesSearchFilter filter = container.getFilter();
		TaxonomiesSearchOptions options = buildDefaultOption(filter);

		assertThat(taxonomiesSearchServices.isLinkable(child.getWrappedRecord(), taxonomy, options)).isTrue();
		assertThat(taxonomiesSearchServices.isLinkable(parent.getWrappedRecord(), taxonomy, options)).isFalse();
	}

	private Transaction transactionWithRecalculateAggregateMetadata(StorageSpace storageSpace) {
		Transaction tx = new Transaction();
		tx.add(storageSpace);
		tx.getRecordUpdateOptions().setUpdateAggregatedMetadatas(true);

		return tx;
	}

	private TaxonomiesSearchOptions buildDefaultOption(TaxonomiesSearchFilter filter) {
		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(0, 100, StatusFilter.ACTIVES)
				.setFastContinueInfos(null)
				.setReturnedMetadatasFilter(idVersionSchemaTitlePath().withIncludedMetadata(Schemas.CODE)
						.withIncludedMetadata(Schemas.DESCRIPTION_TEXT).withIncludedMetadata(Schemas.DESCRIPTION_STRING));
		options.setHasChildrenFlagCalculated(TaxonomiesSearchOptions.HasChildrenFlagCalculated.ALWAYS);
		options.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(false);
		options.setFilter(filter);
		return options;
	}

	public ContainerRecord buildDefaultContainer() {
		return rm.newContainerRecordWithId("containerTest").setType(records.containerTypeId_boite22x22)
				.setTemporaryIdentifier("containerTestTemporary").setCapacity(100)
				.setAdministrativeUnits(asList(records.unitId_10))
				.setDecommissioningType(DecommissioningType.DEPOSIT);
	}

	public StorageSpace buildDefaultStorageSpace() {
		return rm.newStorageSpaceWithId("storageTest").setCode("storageTest").setTitle("storageTest").setCapacity(150);
	}

	public StorageSpace buildDefaultChildStorageSpace() {
		return rm.newStorageSpaceWithId("storageTestChild").setCode("storageTestChild").setParentStorageSpace("storageTest")
				.setTitle("storageTestChild").setCapacity(150);
	}

	private void setAllStorageSpaceWithNoCapacity() {
		List<StorageSpace> storageSpaces = rm.searchStorageSpaces(returnAll());
		Transaction transaction = new Transaction();
		for (StorageSpace storageSpace : storageSpaces) {
			Record wrappedRecord = storageSpace.setCapacity(0).getWrappedRecord();
			recordServices.recalculate(wrappedRecord);
			transaction.addUpdate(wrappedRecord);
		}
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}
}
