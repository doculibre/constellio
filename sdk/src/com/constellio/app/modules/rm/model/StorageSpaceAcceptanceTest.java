package com.constellio.app.modules.rm.model;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class StorageSpaceAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	RMSchemasRecordsServices rm;

	RecordServices recordServices;

	SearchServices searchServices;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

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
}
