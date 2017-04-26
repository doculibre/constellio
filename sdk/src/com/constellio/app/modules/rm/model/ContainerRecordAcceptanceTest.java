package com.constellio.app.modules.rm.model;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.records.Transaction;
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
	public void givenContainerRecordsHaveCapacityLesserThanParentButGreaterWithEnteredLinearSizeThenNoException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L).setLinearSizeEntered(2L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(7L));

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(2L));

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(1L));
	}

	//OK
	@Test
	public void givenContainerRecordsHaveCapacityLesserThanParentButGreaterWithEnteredLinearSizeWhenModifyingThenNoException()
			throws Exception {

		StorageSpace parentStorageSpace = buildStorageSpace().setCapacity(10L).setLinearSizeEntered(2L);
		recordServices.add(parentStorageSpace);

		recordServices.add(buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(6L));

		ContainerRecord containerRecord = buildContainerRecord().setStorageSpace(parentStorageSpace).setCapacity(1L);
		recordServices.add(containerRecord);

		recordServices.update(containerRecord.setCapacity(2L));

		recordServices.update(containerRecord.setCapacity(3L));
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

	//OK
	@Test
	public void whenSavingAContainerRecordWithoutCapacityThenNoException()
			throws Exception {

		StorageSpace parentStorageSpace1 = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace1);

		StorageSpace parentStorageSpace2 = buildStorageSpace();
		recordServices.add(parentStorageSpace2);

		ContainerRecord child1 = buildContainerRecord().setStorageSpace(parentStorageSpace1);
		ContainerRecord child2 = buildContainerRecord().setStorageSpace(parentStorageSpace1);
		ContainerRecord child3 = buildContainerRecord().setStorageSpace(parentStorageSpace1);
		ContainerRecord child4 = buildContainerRecord().setStorageSpace(parentStorageSpace2).setCapacity(5);

		recordServices.add(child1);
		recordServices.add(child2);
		recordServices.add(child3);
		recordServices.add(child4);

		recordServices.update(child1.setDescription("test"));
		recordServices.update(child4.setDescription("test"));
	}

	//OK
	@Test
	public void whenAddingMultipleContainerRecordsContainerWithCapacityHigherThanStorageSpaceThenException()
			throws Exception {

		StorageSpace parentStorageSpace1 = buildStorageSpace().setCapacity(20L);
		StorageSpace parentStorageSpace2 = buildStorageSpace().setCapacity(20L);
		StorageSpace parentStorageSpace3 = buildStorageSpace().setCapacity(20L);
		recordServices.add(parentStorageSpace1);
		recordServices.add(parentStorageSpace2);
		recordServices.add(parentStorageSpace3);

		Transaction tx = new Transaction();
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(8));

		try {
			recordServices.execute(tx);
			fail("Error");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(1);
		}

		tx = new Transaction();
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace2).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace2).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace2).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace3).setCapacity(8));

		try {
			recordServices.execute(tx);
			fail("Error");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(2);
		}

		tx = new Transaction();
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(8));
		tx.add(buildContainerRecord().setStorageSpace(parentStorageSpace1).setCapacity(2));

		recordServices.execute(tx);

	}
}
