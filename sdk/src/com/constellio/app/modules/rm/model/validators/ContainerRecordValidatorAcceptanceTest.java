package com.constellio.app.modules.rm.model.validators;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;

/**
 * Created by Constellio on 2017-01-04.
 */
public class ContainerRecordValidatorAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);

	RMSchemasRecordsServices rm;

	RecordServices recordServices;

	SearchServices searchServices;

	@Before
	public void setUp() {
		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
						.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenContainerWithLinearSizeGreaterThanCapacityThenErrorIsThrown()
			throws RecordServicesException {

		ContainerRecord containerRecord = buildDefaultContainer().setCapacity(new Double(10)).setLinearSizeEntered(20.0);
		recordServices.add(containerRecord);
	}

	@Test
	public void givenContainerWithLinearSizeEqualToCapacityThenNoErrorIsThrown()
			throws RecordServicesException {

		ContainerRecord containerRecord = buildDefaultContainer().setCapacity(new Double(10)).setLinearSizeEntered(10.0);
		recordServices.add(containerRecord);

		getModelLayerFactory().getBatchProcessesManager().waitUntilAllFinished();
		Record record = searchServices
				.searchSingleResult(from(rm.containerRecord.schemaType()).where(Schemas.IDENTIFIER).isEqualTo("containerTest"));
		assertThat(rm.wrapContainerRecord(record).getLinearSizeEntered()).isEqualTo(10);
		assertThat(rm.wrapContainerRecord(record).getLinearSize()).isEqualTo(new Double(10));
		assertThat(rm.wrapContainerRecord(record).getCapacity()).isEqualTo(new Double(10));
		assertThat(rm.wrapContainerRecord(record).getAvailableSize()).isEqualTo(new Double(0));
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenContainerWithStorageSpaceThatCantContainTheContainerTypeThenErrorIsThrown()
			throws RecordServicesException {

		ContainerRecordType containerType = buildDefaultContainerType();
		recordServices.add(containerType);

		StorageSpace storageSpace = buildDefaultStorageSpace().setContainerType(asList(containerType));
		recordServices.add(storageSpace);

		ContainerRecord containerRecord = buildDefaultContainer().setStorageSpace(storageSpace);
		recordServices.add(containerRecord);
	}

	@Test
	public void givenValidationExceptionThenParamsAreOK()
			throws RecordServicesException {

		ContainerRecordType containerType = buildDefaultContainerType();
		recordServices.add(containerType);

		StorageSpace storageSpace = buildDefaultStorageSpace().setContainerType(asList(containerType));
		recordServices.add(storageSpace);

		ContainerRecord containerRecord = buildDefaultContainer().setCapacity(new Double(10)).setLinearSizeEntered(10.0);
		recordServices.add(containerRecord);
		addFoldersLinkedToContainer(containerRecord.getId());
		containerRecord.setLinearSizeEntered(20.0).setStorageSpace(storageSpace);
		try {
			recordServices.add(containerRecord);
			fail("No exception was thrown");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(2);
			Map<String, Object> params = e.getErrors().getValidationErrors().get(0).getParameters();
			assertThat(params).containsOnly(entry("linearSize", "20.0"),
					entry("linearSizeEntered", "20.0"),
					entry("schemaCode", "containerRecord_default"),
					entry("linearSizeSum", "6.0"),
					entry("capacity", "10.0"));

			params = e.getErrors().getValidationErrors().get(1).getParameters();
			assertThat(params).containsOnly(entry("schemaCode", "containerRecord_default"),
					entry("storageSpace", "storageTest"));

			assertThat(TestUtils.frenchMessages(e.getErrors())).containsOnly(
					"La capacité (10.0 cm) doit être plus grande ou égale à la longueur linéaire (20.0 cm)",
					"L'emplacement storageTest ne peut pas contenir ce type de contenant");
		}
	}

	public ContainerRecord buildDefaultContainer() {
		return rm.newContainerRecordWithId("containerTest").setType(records.containerTypeId_boite22x22)
				.setTemporaryIdentifier("containerTestTemporary");
	}

	public ContainerRecordType buildDefaultContainerType() {
		return rm.newContainerRecordTypeWithId("containerTypeTest").setTitle("containerTypeTest").setCode("containerTypeTest");
	}

	public StorageSpace buildDefaultStorageSpace() {
		return rm.newStorageSpaceWithId("storageTest").setCode("storageTest").setTitle("storageTest");
	}

	public void addFoldersLinkedToContainer(String containerID)
			throws RecordServicesException {

		recordServices
				.add(rm.newFolderWithId("parentFolder").setTitle("title").setLinearSize(new Double(2)).setContainer(containerID)
						.setAdministrativeUnitEntered(records.unitId_10).setCategoryEntered(records.categoryId_X)
						.setRetentionRuleEntered(records.ruleId_1).setMediumTypes(records.PA).setOpenDate(new LocalDate())
				);
		recordServices.add(rm.newFolder().setTitle("title").setLinearSize(new Double(2)).setContainer(containerID)
				.setAdministrativeUnitEntered(records.unitId_10).setCategoryEntered(records.categoryId_X)
				.setRetentionRuleEntered(records.ruleId_1).setMediumTypes(records.PA).setOpenDate(new LocalDate())
		);
		recordServices.add(rm.newFolder().setTitle("title").setLinearSize(new Double(2)).setContainer(containerID)
				.setParentFolder("parentFolder")
				.setAdministrativeUnitEntered(records.unitId_10).setCategoryEntered(records.categoryId_X)
				.setRetentionRuleEntered(records.ruleId_1).setMediumTypes(records.PA).setOpenDate(new LocalDate())
		);
		recordServices.add(rm.newFolder().setTitle("title").setLinearSize(new Double(2)).setParentFolder("parentFolder")
				.setAdministrativeUnitEntered(records.unitId_10).setCategoryEntered(records.categoryId_X)
				.setRetentionRuleEntered(records.ruleId_1).setMediumTypes(records.PA).setOpenDate(new LocalDate())
		);
	}
}
