package com.constellio.app.modules.rm.model.calculators.storageSpace;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Constellio on 2016-12-19.
 */

public class StorageSpaceSingleContainerAvailableSizeCalculatorAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	StorageSpaceSingleContainerAvailableSizeCalculator calculator;

	RMSchemasRecordsServices rm;

	RecordServices recordServices;

	SearchServices searchServices;

	@Mock CalculatorParameters parameters;

	@Before
	public void setUp() {
		givenBackgroundThreadsEnabled();
		calculator = spy(new StorageSpaceSingleContainerAvailableSizeCalculator());
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records)
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
	}

	@Test
	public void givenParametersThenCalculatorReturnsGoodValue() {
		when(parameters.get(calculator.numberOfContainersParam)).thenReturn(new Double(0));
		when(parameters.get(calculator.capacityParam)).thenReturn(new Double(10));
		assertThat(calculator.calculate(parameters)).isEqualTo(10);

		when(parameters.get(calculator.numberOfContainersParam)).thenReturn(new Double(0));
		when(parameters.get(calculator.capacityParam)).thenReturn(null);
		assertThat(calculator.calculate(parameters)).isEqualTo(null);

		when(parameters.get(calculator.numberOfContainersParam)).thenReturn(null);
		when(parameters.get(calculator.capacityParam)).thenReturn(new Double(10));
		assertThat(calculator.calculate(parameters)).isEqualTo(10);

		when(parameters.get(calculator.numberOfContainersParam)).thenReturn(null);
		when(parameters.get(calculator.capacityParam)).thenReturn(null);
		assertThat(calculator.calculate(parameters)).isEqualTo(null);

		when(parameters.get(calculator.numberOfContainersParam)).thenReturn(new Double(1));
		when(parameters.get(calculator.capacityParam)).thenReturn(null);
		assertThat(calculator.calculate(parameters)).isEqualTo(0);
	}

	@Test
	public void givenContainerIsMultivalueAndStorageSpaceHasNoContainerThenAvailableSpaceIsEqualToCapacity()
			throws RecordServicesException {

		givenConfig(RMConfigs.IS_CONTAINER_MULTIVALUE, true);

		StorageSpace storageSpace = buildDefaultStorageSpace().setCapacity(new Long(10)).setLinearSizeEntered(6);
		recordServices.add(storageSpace);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		Record record = searchServices
				.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
		assertThat(rm.wrapStorageSpace(record).getCapacity()).isEqualTo(new Long(10));
		assertThat(rm.wrapStorageSpace(record).getAvailableSize()).isEqualTo(new Double(10));
	}

	@Test
	public void givenContainerIsMultivalueAndStorageSpaceHasAContainerThenAvailableSpaceIsZero()
			throws RecordServicesException {
		givenConfig(RMConfigs.IS_CONTAINER_MULTIVALUE, true);

		StorageSpace storageSpace = buildDefaultStorageSpace().setCapacity(new Long(10)).setLinearSizeEntered(6);
		recordServices.add(storageSpace);
		recordServices.add(buildDefaultContainerType());
		recordServices.add(buildDefaultContainer("containerTest").setStorageSpace("storageTest"));

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		Record record = searchServices
				.searchSingleResult(from(rm.storageSpace.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("storageTest"));
		assertThat(rm.wrapStorageSpace(record).getCapacity()).isEqualTo(new Long(10));
		assertThat(rm.wrapStorageSpace(record).getAvailableSize()).isEqualTo(new Double(0));
	}

	public StorageSpace buildDefaultStorageSpace() {
		return rm.newStorageSpaceWithId("storageTest").setCode("TEST").setTitle("storageTest");
	}

	public ContainerRecord buildDefaultContainer(String id) {
		return rm.newContainerRecordWithId(id).setType("containerTypeTest")
				.setTemporaryIdentifier("containerTestTemporary");
	}

	public ContainerRecordType buildDefaultContainerType() {
		return rm.newContainerRecordTypeWithId("containerTypeTest").setTitle("containerTypeTest").setCode("containerTypeTest");
	}
}
