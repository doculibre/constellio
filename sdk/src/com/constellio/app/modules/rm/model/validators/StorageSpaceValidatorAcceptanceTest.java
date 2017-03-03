package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

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
        recordServices.add(buildChildStorageSpaces().setCapacity(5L).setContainerType(asList(rm.getContainerRecordType(records.containerTypeId_boite22x22))));
    }

    @Test
    public void givenNoContainerTypeInChildThenNoErrorIsThrown()
            throws RecordServicesException {

        recordServices.add(buildParentStorageSpaces().setCapacity(10L).setContainerType(asList(rm.getContainerRecordType(records.containerTypeId_boite22x22))));
        recordServices.add(buildChildStorageSpaces().setCapacity(5L));
    }

    @Test
    public void givenChildContainerTypeContainedInParentContainerTypeThenNoErrorIsThrown()
            throws RecordServicesException {

        ContainerRecordType containerRecordType = buildDefaultContainerType();
        recordServices.add(containerRecordType);
        recordServices.add(buildParentStorageSpaces().setCapacity(10L).setContainerType(asList(rm.getContainerRecordType(records.containerTypeId_boite22x22), containerRecordType)));
        recordServices.add(buildChildStorageSpaces().setCapacity(5L).setContainerType(asList(containerRecordType)));
    }

    @Test
    public void givenValidationExceptionThenParamsAreOK()
            throws RecordServicesException {

        ContainerRecordType containerRecordType = buildDefaultContainerType();
        recordServices.add(containerRecordType);

        recordServices.add(buildParentStorageSpaces().setCapacity(10L).setContainerType(asList(rm.getContainerRecordType(records.containerTypeId_boite22x22))));
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
            assertThat(TestUtils.frenchMessages(e.getErrors()).get(0)).isEqualTo("La capacité du sous-emplacement (20 cm) doit être plus petite ou égale à celle du parent (10 cm)");

            params = e.getErrors().getValidationErrors().get(1).getParameters();
            assertThat(params).containsOnly(
                    entry("schemaCode", "storageSpace_default"),
                    entry("containerType", "[containerTypeTest]"),
                    entry("parentContainerType", "[boite22x22]"));
            assertThat(TestUtils.frenchMessages(e.getErrors()).get(1)).isEqualTo("L''emplacement parent ne peut que contenir les contenants de type [boite22x22]. [containerTypeTest] est invalide");
        }
    }

    public StorageSpace buildParentStorageSpaces() {
        return rm.newStorageSpaceWithId("storageSpaceParent").setCode("PARENT").setTitle("Parent");
    }

    public StorageSpace buildChildStorageSpaces() {
        return rm.newStorageSpaceWithId("storageSpaceChild").setCode("CHILD").setTitle("Child").setParentStorageSpace("storageSpaceParent");
    }

    public ContainerRecordType buildDefaultContainerType() {
        return rm.newContainerRecordTypeWithId("containerTypeTest").setTitle("containerTypeTest").setCode("containerTypeTest");
    }
}
