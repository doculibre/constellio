package com.constellio.app.modules.rm.model;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ContainerRecordAcceptanceTest extends ConstellioTest {

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

	public ContainerRecord buildContainerRecord() {
		String type = searchServices.searchRecordIds(from(rm.containerRecordTypeSchemaType()).returnAll()).get(0);
		ContainerRecord containerRecord = rm.newContainerRecord().setTitle("storageTest");
		containerRecord.setIdentifier(containerRecord.getId()).setType(type);
		return containerRecord;
	}

	//OK
	@Test
	public void givenContainerRecordsHasTotalCapacityGreaterThanParentThenException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(9L));

		try {
			recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(2L));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(1L));
	}

	//OK
	@Test
	public void givenContainerRecordsHasTotalCapacityGreaterThanParentWhenModifyingThenException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(8L));

		ContainerRecord containerRecord = buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(1L);
		recordServices.add(containerRecord);

		recordServices.update(containerRecord.setCapacity(2L));

		try {
			recordServices.update(containerRecord.setCapacity(3L));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

	}

	//OK
	@Test
	public void givenContainerRecordsHaveCapacityLesserThanParentButGreaterWithEnteredLinearSizeThenException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L).setLinearSizeEntered(2L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(7L));

		try {
			recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(2L));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(1L));
	}

	//OK
	@Test
	public void givenContainerRecordsHaveCapacityLesserThanParentButGreaterWithEnteredLinearSizeWhenModifyingThenException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L).setLinearSizeEntered(2L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(6L));

		ContainerRecord containerRecord = buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(1L);
		recordServices.add(containerRecord);

		recordServices.update(containerRecord.setCapacity(2L));

		try {
			recordServices.update(containerRecord.setCapacity(3L));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}
	}

	//OK
	@Test
	public void whenMovingAContainerRecordInAParentWithInsufficientAvailableCapacityThenException()
			throws Exception {

		StorageSpace parentStorageSpace1 = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace1);

		StorageSpace parentStorageSpace2 = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace2);

		ContainerRecord child1 = buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(4L);
		ContainerRecord child2 = buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(4L);
		ContainerRecord child3 = buildContainerRecord().setStorageSpace(parentStorageSpace2).setCapacity(4L);

		recordServices.add(child1);
		recordServices.add(child2);
		recordServices.add(child3);

		recordServices.update(child1.setStorageSpace(parentStorageSpace2));

		try {
			recordServices.update(child2.setStorageSpace(parentStorageSpace2));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}
	}

	//TODO Rien si le parent n'a pas de capacity
	//TODO Rien si l'enfant n'a pas de capacity
}
