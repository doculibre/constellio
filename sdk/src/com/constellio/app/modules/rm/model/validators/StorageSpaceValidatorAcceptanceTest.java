package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Created by Constellio on 2017-01-04.
 */
public class StorageSpaceValidatorAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);

	RMSchemasRecordsServices rm;

	RecordServices recordServices;

	SearchServices searchServices;

	@Before
	public void setUp() {
		//DataLayerLogger.logAllTransactions = true;

		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records)
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenEnableOrDisableStorageSpaceTitleCalculatorConfigThenTitleIsOk()
			throws RecordServicesException {
		recordServices.add(buildParentStorageSpaces().setCapacity(10L));
		recordServices.add(buildChildStorageSpaces().setCapacity(20L));
		assertThat(rm.getStorageSpace("storageSpaceParent").getTitle()).isEqualTo("Parent");
		assertThat(rm.getStorageSpace("storageSpaceChild").getTitle()).isEqualTo("Child");

		givenConfig(RMConfigs.STORAGE_SPACE_TITLE_CALCULATOR_ENABLED, true);
		reindexIfRequired();
		assertThat(rm.getStorageSpace("storageSpaceParent").getTitle()).isEqualTo("PARENT");
		assertThat(rm.getStorageSpace("storageSpaceChild").getTitle()).isEqualTo("PARENT-CHILD");

		givenConfig(RMConfigs.STORAGE_SPACE_TITLE_CALCULATOR_ENABLED, false);
		reindexIfRequired();
		assertThat(rm.getStorageSpace("storageSpaceParent").getTitle()).isEqualTo("PARENT");
		assertThat(rm.getStorageSpace("storageSpaceChild").getTitle()).isEqualTo("PARENT-CHILD");
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenChildStorageSpaceWithCapacityGreaterThanParentThenErrorIsThrown()
			throws RecordServicesException {

		recordServices.add(buildParentStorageSpaces().setCapacity(10L));
		recordServices.add(buildChildStorageSpaces().setCapacity(20L));
	}

	@Test
	public void givenChildStorageSpaceWithCapacityEqualToParentThenNoErrorIsThrown()
			throws RecordServicesException {

		recordServices.add(buildParentStorageSpaces().setCapacity(10L));
		recordServices.add(buildChildStorageSpaces().setCapacity(10L));
	}

	@Test
	public void givenChildStorageSpaceWithCapacityLesserThanParentThenNoErrorIsThrown()
			throws RecordServicesException {

		recordServices.add(buildParentStorageSpaces().setCapacity(10L));
		recordServices.add(buildChildStorageSpaces().setCapacity(5L));
	}

	@Test
	public void givenNoContainerTypeInParentThenNoErrorIsThrown()
			throws RecordServicesException {

		recordServices.add(buildParentStorageSpaces().setCapacity(10L));
		recordServices.add(buildChildStorageSpaces().setCapacity(5L)
				.setContainerType(asList(rm.getContainerRecordType(records.containerTypeId_boite22x22))));
	}

	@Test
	public void givenNoContainerTypeInChildThenNoErrorIsThrown()
			throws RecordServicesException {

		recordServices.add(buildParentStorageSpaces().setCapacity(10L)
				.setContainerType(asList(rm.getContainerRecordType(records.containerTypeId_boite22x22))));
		recordServices.add(buildChildStorageSpaces().setCapacity(5L));
	}

	@Test
	public void givenChildContainerTypeContainedInParentContainerTypeThenNoErrorIsThrown()
			throws RecordServicesException {

		ContainerRecordType containerRecordType = buildDefaultContainerType();
		recordServices.add(containerRecordType);
		recordServices.add(buildParentStorageSpaces().setCapacity(10L)
				.setContainerType(asList(rm.getContainerRecordType(records.containerTypeId_boite22x22), containerRecordType)));
		recordServices.add(buildChildStorageSpaces().setCapacity(5L).setContainerType(asList(containerRecordType)));
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenContainerIsMultivalueAndStorageSpaceHasMoreThanOneContainerThenErrorIsThrown()
			throws RecordServicesException, InterruptedException {
		givenConfig(RMConfigs.IS_CONTAINER_MULTIVALUE, true);
		recordServices.add(buildDefaultContainerType());
		StorageSpace storageSpace = buildParentStorageSpaces();
		recordServices.add(storageSpace.setCapacity(10L));
		recordServices.add(buildDefaultContainer("firstContainer").setStorageSpace("storageSpaceParent"));
		recordServices.add(buildDefaultContainer("secondContainer").setStorageSpace("storageSpaceParent"));
		recordServices.recalculate(storageSpace);
		recordServices.update(storageSpace.setTitle("test"));
	}

	@Test
	public void givenContainerIsMultivalueThenOnlyOneContainerPerStorageSpace()
			throws RecordServicesException {
		givenConfig(RMConfigs.IS_CONTAINER_MULTIVALUE, true);
		recordServices.add(buildDefaultContainerType());
		StorageSpace storageSpace = buildParentStorageSpaces();
		recordServices.add(storageSpace.setCapacity(10L));
		recordServices.add(buildDefaultContainer("firstContainer").setStorageSpace("storageSpaceParent")
				.setAdministrativeUnits(asList(records.unitId_10a))
				.setDecommissioningType(DecommissioningType.DEPOSIT));
		recordServices.recalculate(storageSpace);
		recordServices.update(storageSpace);
		try {
			recordServices.add(buildDefaultContainer("secondContainer").setStorageSpace("storageSpaceParent"));
			fail("Should have thrown an exception when adding second container to storage space");
		} catch (Exception e) {
		}
	}

	@Test
	public void givenContainerIsSingleValueThenNoErrorIsThrownForMultipleContainers()
			throws RecordServicesException {
		recordServices.add(buildDefaultContainerType());
		recordServices.add(buildParentStorageSpaces().setCapacity(10L));
		recordServices.add(buildDefaultContainer("firstContainer").setStorageSpace("storageSpaceParent")
				.setAdministrativeUnits(asList(records.unitId_10a))
				.setDecommissioningType(DecommissioningType.DEPOSIT));
		recordServices.add(buildDefaultContainer("secondContainer").setStorageSpace("storageSpaceParent")
				.setAdministrativeUnits(asList(records.unitId_10a))
				.setDecommissioningType(DecommissioningType.DEPOSIT));
	}

	@Test
	public void givenValidationExceptionThenParamsAreOK()
			throws RecordServicesException {

		ContainerRecordType containerRecordType = buildDefaultContainerType();
		recordServices.add(containerRecordType);
		recordServices.add(buildDefaultContainerType("secondContainerType"));

		recordServices.add(buildParentStorageSpaces().setCapacity(10L)
				.setContainerType(asList(rm.getContainerRecordType("secondContainerType"))));
		try {
			recordServices.add(buildChildStorageSpaces().setCapacity(20L).setContainerType(asList(containerRecordType)));
			fail("No exception was thrown");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(2);
			Map<String, Object> params = e.getErrors().getValidationErrors().get(0).getParameters();
			assertThat(params).containsOnly(
					entry("schemaCode", "storageSpace_default"),
					entry("parentCapacity", "10"),
					entry("capacity", "20"));
			assertThat(TestUtils.frenchMessages(e.getErrors()).get(0)).isEqualTo(
					"La capacité du sous-emplacement (20 cm) doit être plus petite ou égale à celle du parent (10 cm)");

			params = e.getErrors().getValidationErrors().get(1).getParameters();
			assertThat(params).containsOnly(
					entry("schemaCode", "storageSpace_default"),
					entry("containerType", "[containerTypeTest]"),
					entry("parentContainerType", "[secondContainerType]"));
			assertThat(TestUtils.frenchMessages(e.getErrors()).get(1)).isEqualTo(
					"L'emplacement parent ne peut que contenir les contenants de type [secondContainerType]. [containerTypeTest] est invalide");
		}
	}

	public StorageSpace buildParentStorageSpaces() {
		return rm.newStorageSpaceWithId("storageSpaceParent").setCode("PARENT").setTitle("Parent");
	}

	public StorageSpace buildChildStorageSpaces() {
		return rm.newStorageSpaceWithId("storageSpaceChild").setCode("CHILD").setTitle("Child")
				.setParentStorageSpace("storageSpaceParent");
	}

	public ContainerRecord buildDefaultContainer(String id) {
		return rm.newContainerRecordWithId(id).setType("containerTypeTest")
				.setTemporaryIdentifier("containerTestTemporary");
	}

	public ContainerRecordType buildDefaultContainerType() {
		return rm.newContainerRecordTypeWithId("containerTypeTest").setTitle("containerTypeTest").setCode("containerTypeTest");
	}

	public ContainerRecordType buildDefaultContainerType(String id) {
		return rm.newContainerRecordTypeWithId(id).setTitle(id).setCode(id);
	}

	private void removeAllContainers() {
		List<ContainerRecord> containerRecords = rm.searchContainerRecords(LogicalSearchQueryOperators.returnAll());
		for (ContainerRecord containerRecord : containerRecords) {
			recordServices.physicallyDeleteNoMatterTheStatus(containerRecord.getWrappedRecord(), User.GOD,
					new RecordPhysicalDeleteOptions());
		}
	}
}
