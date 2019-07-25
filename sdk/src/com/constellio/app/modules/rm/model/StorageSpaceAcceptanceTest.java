package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.LinkableConceptFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.app.modules.rm.constants.RMTaxonomies.STORAGES;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

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
	public void whenSavingAContainerRecordWithCustomSchemaThenAggregatedMetadatasStillWork()
			throws Exception {
		getAppLayerFactory().getModelLayerFactory().getMetadataSchemasManager()
				.modify(zeCollection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						types.getSchemaType(ContainerRecord.SCHEMA_TYPE).createCustomSchema("ZeSchema");
					}
				});

		ContainerRecordType zeContainerType = rm.newContainerRecordTypeWithId("zeContainerType");
		zeContainerType.setTitle("zeContainerType");
		zeContainerType.setCode("BOITE");
		zeContainerType.setLinkedSchema(ContainerRecord.SCHEMA_TYPE + "_" + "ZeSchema");
		recordServices.add(zeContainerType);

		StorageSpace parentStorageSpace1 = buildStorageSpace().setCapacity(10L);
		recordServices.add(parentStorageSpace1);

		assertThat(parentStorageSpace1.getNumberOfContainers()).isEqualTo(0);

		ContainerRecord zeContainer = rm.newContainerRecordWithId("zeContainer");
		zeContainer.changeSchemaTo("ZeSchema");
		zeContainer.setTemporaryIdentifier("Ze temp identifier");
		zeContainer.setDescription("Ze description");
		zeContainer.setStorageSpace(parentStorageSpace1);
		zeContainer.setDecommissioningType(DecommissioningType.DEPOSIT);
		zeContainer.setAdministrativeUnits(asList(records.unitId_10a));
		zeContainer.setType("zeContainerType");
		recordServices.add(zeContainer);

		waitForBatchProcess();
		assertThat(rm.getStorageSpace(parentStorageSpace1.getId()).getNumberOfContainers()).isEqualTo(1);
	}

	@Test
	public void givenStorageSpaceIsNotValidAndHasNoValidChildThenDoNotShowInTree()
			throws Exception {
		cleanStorageSpaces();
		StorageSpace ancestor = buildStorageSpace();
		StorageSpace parent = buildStorageSpace().setParentStorageSpace(ancestor);
		StorageSpace child = buildStorageSpace().setParentStorageSpace(parent).setCapacity(10);

		Transaction transaction = new Transaction();
		transaction.addAll(ancestor, parent, child);
		recordServices.execute(transaction);
		waitForBatchProcess();

		TaxonomiesSearchFilter taxonomiesSearchFilter = new TaxonomiesSearchFilter();
		taxonomiesSearchFilter.setLinkableConceptsFilter(new LinkableConceptFilter() {
			@Override
			public boolean isLinkable(LinkableConceptFilterParams params) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

				StorageSpace storageSpace = rm.wrapStorageSpace(params.getRecord());

				return canStorageSpaceContainContainer(storageSpace, 20D);
			}
		});
		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions().setFilter(taxonomiesSearchFilter);

		List<TaxonomySearchRecord> visibleChild = getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService()
				.getLinkableRootConcept(users.adminIn(zeCollection), zeCollection, STORAGES, StorageSpace.SCHEMA_TYPE,
						taxonomiesSearchOptions);
		assertThat(visibleChild).isEmpty();

		getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService()
				.getLinkableChildConcept(users.adminIn(zeCollection), ancestor.getWrappedRecord(), STORAGES,
						StorageSpace.SCHEMA_TYPE, taxonomiesSearchOptions);
		assertThat(visibleChild).isEmpty();

		child.setCapacity(30);
		recordServices.update(child);

		visibleChild = getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService()
				.getLinkableRootConcept(users.adminIn(zeCollection), zeCollection, STORAGES, StorageSpace.SCHEMA_TYPE,
						taxonomiesSearchOptions);
		assertThat(visibleChild).hasSize(1);

		visibleChild = getAppLayerFactory().getModelLayerFactory().newTaxonomiesSearchService()
				.getLinkableChildConcept(users.adminIn(zeCollection), ancestor.getWrappedRecord(), STORAGES,
						StorageSpace.SCHEMA_TYPE, taxonomiesSearchOptions);
		assertThat(visibleChild).hasSize(1);
	}

	@Test
	public void givenAddAndRemoveChildThenNumberOfChildIsUpdated()
			throws Exception {
		StorageSpace parentStorageSpace = buildStorageSpace();
		StorageSpace childStorageSpace = buildStorageSpace().setParentStorageSpace(parentStorageSpace);
		Transaction transaction = new Transaction();
		transaction.addAll(parentStorageSpace, childStorageSpace);
		recordServices.execute(transaction);

		waitForBatchProcess();
		assertThat(rm.getStorageSpace(parentStorageSpace.getId()).getNumberOfChild()).isEqualTo(1);

		recordServices.update(childStorageSpace.setParentStorageSpace((String) null));
		waitForBatchProcess();
		assertThat(rm.getStorageSpace(parentStorageSpace.getId()).getNumberOfChild()).isEqualTo(0);
	}

	@Test
	public void givenAddChildAndDeleteItThenNumberOfChildIsUpdated()
			throws Exception {
		StorageSpace parentStorageSpace = buildStorageSpace();
		StorageSpace childStorageSpace = buildStorageSpace().setParentStorageSpace(parentStorageSpace);
		Transaction transaction = new Transaction();
		transaction.addAll(parentStorageSpace, childStorageSpace);
		recordServices.execute(transaction);

		waitForBatchProcess();
		assertThat(rm.getStorageSpace(parentStorageSpace.getId()).getNumberOfChild()).isEqualTo(1);

		recordServices.physicallyDeleteNoMatterTheStatus(childStorageSpace.getWrappedRecord(), User.GOD,
				new RecordPhysicalDeleteOptions());
		waitForBatchProcess();
		assertThat(rm.getStorageSpace(parentStorageSpace.getId()).getNumberOfChild()).isEqualTo(0);
	}

	@Test
	public void givenChildStorageSpaceWithContainersThenDoesNotIncrementNumberOfChild()
			throws Exception {
		StorageSpace parentStorageSpace = buildStorageSpace();
		StorageSpace childStorageSpace = buildStorageSpace().setParentStorageSpace(parentStorageSpace);
		ContainerRecord container1 = rm.newContainerRecord().setType(records.containerTypeId_boite22x22).setAdministrativeUnits(asList(records.unitId_10a))
				.setDecommissioningType(DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE).setTemporaryIdentifier("containerTest").setStorageSpace(childStorageSpace.getId());

		Transaction transaction = new Transaction();
		transaction.addAll(parentStorageSpace, childStorageSpace, container1);
		recordServices.execute(transaction);

		waitForBatchProcess();
		assertThat(rm.getStorageSpace(parentStorageSpace.getId()).getNumberOfChild()).isEqualTo(1);
		assertThat(rm.getStorageSpace(childStorageSpace.getId()).getNumberOfChild()).isEqualTo(0);

		reindex();
		waitForBatchProcess();
		assertThat(rm.getStorageSpace(parentStorageSpace.getId()).getNumberOfChild()).isEqualTo(1);
		assertThat(rm.getStorageSpace(childStorageSpace.getId()).getNumberOfChild()).isEqualTo(0);
	}


	public static boolean canStorageSpaceContainContainer(StorageSpace storageSpace, Double containerCapacity) {
		return storageSpace.getTitle().equals("storageTest") && storageSpace.getCapacity() != null
			   && storageSpace.getCapacity() > containerCapacity;
	}

	public void cleanStorageSpaces() {
		RecordDao recordDao = getAppLayerFactory().getModelLayerFactory().getDataLayerFactory().newRecordDao();
		TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams();
		modifiableSolrParams.set("q", "schema_s:storageSpace*");
		transaction = transaction.withDeletedByQueries(modifiableSolrParams);
		try {
			recordDao.execute(transaction);
		} catch (RecordDaoException.OptimisticLocking optimisticLocking) {
			optimisticLocking.printStackTrace();
		}
		getModelLayerFactory().getRecordsCaches().getCache(zeCollection)
				.invalidateVolatileReloadPermanent(asList("storageSpace"));
	}
}
