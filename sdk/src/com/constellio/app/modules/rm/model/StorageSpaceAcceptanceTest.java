package com.constellio.app.modules.rm.model;

import static com.constellio.data.utils.LangUtils.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.services.taxonomies.*;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

import java.util.List;

public class StorageSpaceAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	RMSchemasRecordsServices rm;

	RecordServices recordServices;

	SearchServices searchServices;

	Users users = new Users();

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		users = users.setUp(getAppLayerFactory().getModelLayerFactory().newUserServices());
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
	}

	public StorageSpace buildStorageSpace() {
		StorageSpace storageSpace = rm.newStorageSpace().setTitle("storageTest");
		storageSpace.setCode(storageSpace.getId());
		return storageSpace;
	}

	//OK
	@Test
	public void givenStorageSpacesHasTotalCapacityGreaterThanParentThenException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(9L));

		try {
			recordServices.add(buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(2L));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		recordServices.add(buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(1L));
	}

	//OK
	@Test
	public void givenStorageSpaceHasTotalCapacityGreaterThanParentWhenModifyingThenException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(8L));

		StorageSpace storageSpace = buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(1L);
		recordServices.add(storageSpace);

		recordServices.update(storageSpace.setCapacity(2L));

		try {
			recordServices.update(storageSpace.setCapacity(3L));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

	}

	//OK
	@Test
	public void givenStorageSpacesHaveCapacityLesserThanParentButGreaterWithEnteredLinearSizeThenNoException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L).setLinearSizeEntered(2L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(7L));

		recordServices.add(buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(2L));

		recordServices.add(buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(1L));
	}

	//OK
	@Test
	public void givenStorageSpacesHaveCapacityLesserThanParentButGreaterWithEnteredLinearSizeWhenModifyingThenException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L).setLinearSizeEntered(2L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(6L));

		StorageSpace storageSpace = buildStorageSpace().setParentStorageSpace(parentStorageSpace).setCapacity(1L);
		recordServices.add(storageSpace);

		recordServices.update(storageSpace.setCapacity(2L));

		recordServices.update(storageSpace.setCapacity(3L));
	}

	//OK
	@Test
	public void whenMovingAStorageSpaceInAParentWithInsufficientAvailableCapacityThenException()
			throws Exception {

		StorageSpace parentStorageSpace1 = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace1);

		StorageSpace parentStorageSpace2 = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace2);

		StorageSpace child1 = buildStorageSpace().setParentStorageSpace(parentStorageSpace1).setCapacity(4L);
		StorageSpace child2 = buildStorageSpace().setParentStorageSpace(parentStorageSpace1).setCapacity(4L);
		StorageSpace child3 = buildStorageSpace().setParentStorageSpace(parentStorageSpace2).setCapacity(4L);

		recordServices.add(child1);
		recordServices.add(child2);
		recordServices.add(child3);

		recordServices.update(child1.setParentStorageSpace(parentStorageSpace2));

		try {
			recordServices.update(child2.setParentStorageSpace(parentStorageSpace2));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}
	}

	//TODO Rien si le parent n'a pas de capacity
	//TODO Rien si l'enfant n'a pas de capacity

	//OK
	@Test
	public void whenSavingAContainerRecordWithoutCapacityThenNoException()
			throws Exception {

		StorageSpace parentStorageSpace1 = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace1);

		StorageSpace parentStorageSpace2 = buildStorageSpace();
		recordServices.add(parentStorageSpace2);

		StorageSpace child1 = buildStorageSpace().setParentStorageSpace(parentStorageSpace1);
		StorageSpace child2 = buildStorageSpace().setParentStorageSpace(parentStorageSpace1);
		StorageSpace child3 = buildStorageSpace().setParentStorageSpace(parentStorageSpace1);
		StorageSpace child4 = buildStorageSpace().setParentStorageSpace(parentStorageSpace2).setCapacity(5);

		recordServices.add(child1);
		recordServices.add(child2);
		recordServices.add(child3);
		recordServices.add(child4);

		recordServices.update(child1.setDescription("test"));
		recordServices.update(child4.setDescription("test"));
	}

	@Test
	public void givenStorageSpaceIsNotValidAndHasNoValidChildThenDoNotShowInTree() throws RecordServicesException {
		StorageSpace parentStorageSpace = buildStorageSpace();
		recordServices.add(parentStorageSpace);
		StorageSpace child = buildStorageSpace().setParentStorageSpace(parentStorageSpace);
		recordServices.add(child);
		StorageSpace childChild = buildStorageSpace().setParentStorageSpace(child).setCapacity(10);
		recordServices.add(childChild);
		recordServices.recalculate(child);
		recordServices.update(child);
		recordServices.recalculate(parentStorageSpace);
		recordServices.update(parentStorageSpace);

		TaxonomiesSearchServices taxonomiesSearchServices = getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService();
		TaxonomiesSearchFilter taxonomiesSearchFilter = new TaxonomiesSearchFilter();
		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(params.getRecord().getCollection(),
						ConstellioFactories.getInstance().getAppLayerFactory());

				StorageSpace storageSpace = rm.wrapStorageSpace(params.getRecord());

				return canStorageSpaceContainContainer(storageSpace, 20D);
			}
		});
		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions().setFilter(new TaxonomiesSearchFilter());
		List<TaxonomySearchRecord> visibleChild = taxonomiesSearchServices.getLinkableChildConcept(users.adminIn(zeCollection), parentStorageSpace.getWrappedRecord(), "plan", StorageSpace.SCHEMA_TYPE, taxonomiesSearchOptions);
		assertThat(visibleChild).isEmpty();

		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(params.getRecord().getCollection(),
						ConstellioFactories.getInstance().getAppLayerFactory());

				StorageSpace storageSpace = rm.wrapStorageSpace(params.getRecord());

				return canStorageSpaceContainContainer(storageSpace, 5D);
			}
		});
		taxonomiesSearchOptions = new TaxonomiesSearchOptions().setFilter(new TaxonomiesSearchFilter());
		visibleChild = taxonomiesSearchServices.getLinkableChildConcept(users.adminIn(zeCollection), parentStorageSpace.getWrappedRecord(), "plan", StorageSpace.SCHEMA_TYPE, taxonomiesSearchOptions);
		assertThat(visibleChild).hasSize(1);
	}

	public static boolean canStorageSpaceContainContainer(StorageSpace storageSpace, Double containerCapacity) {
		Double numberOfChild = storageSpace.getNumberOfChild();
		boolean hasNoChildren = numberOfChild == null || isEqual(0.0, numberOfChild);
		boolean enoughAvailableSize = storageSpace.getAvailableSize() == null
				|| storageSpace.getAvailableSize() > (containerCapacity == null ? 0.0 : containerCapacity);

		return hasNoChildren && enoughAvailableSize;
	}
}
