package com.constellio.model.services.taxonomies;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.batch.controller.BatchProcessController;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.DocumentSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.FolderSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1FirstSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy1SecondSchemaType;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2CustomSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.Taxonomy2DefaultSchema;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.TaxonomyRecords;
import com.constellio.sdk.tests.setups.TwoTaxonomiesContainingFolderAndDocumentsSetup.UserSchema;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ConceptNodesTaxonomiesSearchServicesAcceptanceTest extends ConstellioTest {

	String TAXO1 = "taxo1";
	String TAXO2 = "taxo2";

	TwoTaxonomiesContainingFolderAndDocumentsSetup schemas = new TwoTaxonomiesContainingFolderAndDocumentsSetup(zeCollection);
	Taxonomy1FirstSchemaType taxonomy1FirstSchema = schemas.new Taxonomy1FirstSchemaType();
	Taxonomy1SecondSchemaType taxonomy1SecondSchema = schemas.new Taxonomy1SecondSchemaType();
	Taxonomy2DefaultSchema taxonomy2DefaultSchema = schemas.new Taxonomy2DefaultSchema();
	Taxonomy2CustomSchema taxonomy2CustomSchema = schemas.new Taxonomy2CustomSchema();
	FolderSchema folderSchema = schemas.new FolderSchema();
	DocumentSchema documentSchema = schemas.new DocumentSchema();
	UserServices userServices;
	AuthorizationsServices authorizationsServices;
	BatchProcessesManager batchProcessesManager;
	BatchProcessController batchProcessController;
	RolesManager roleManager;
	TaxonomiesManager taxonomiesManager;
	TaxonomyRecords records;
	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	ConceptNodesTaxonomySearchServices services;
	TaxonomiesSearchOptions options;
	Record folder;
	Record subFolder;
	Record document;
	UserSchema userSchema;
	String ZE_ROLE = "zeRoleCode";
	User bob;
	User chuck;

	@Before
	public void setUp()
			throws Exception {
		userServices = getModelLayerFactory().newUserServices();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		roleManager = getModelLayerFactory().getRolesManager();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		options = new TaxonomiesSearchOptions();
		options.setReturnedMetadatasFilter(ReturnedMetadatasFilter.all());

		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		batchProcessesManager.initialize();

		defineSchemasManager().using(schemas);

		userSchema = schemas.getUserSchema();
		Transaction transaction = new Transaction();
		bob = wrapUser(addUserRecord(transaction, "bob", null));
		recordServices.execute(transaction);

		transaction = new Transaction();
		chuck = wrapUser(addUserRecord(transaction, "chuck", null));
		recordServices.execute(transaction);

		for (Taxonomy taxonomy : schemas.getTaxonomies()) {
			getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy, schemasManager);
		}
		taxonomiesManager.setPrincipalTaxonomy(schemas.getTaxo1(), schemasManager);

		records = schemas.givenTaxonomyRecords(recordServices);

		MetadataSchemaTypesBuilder types = schemasManager.modify("zeCollection");

		services = new ConceptNodesTaxonomySearchServices(getModelLayerFactory());

	}

	private void givenFoldersAndDocuments()
			throws RecordServicesException {
		folder = new TestRecord(folderSchema, "folder");
		folder.set(folderSchema.taxonomy1(), records.taxo1_firstTypeItem2_secondTypeItem2);
		folder.set(folderSchema.taxonomy2(), asList(records.taxo2_defaultSchemaItem2_customSchemaItem1,
				records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2));

		subFolder = new TestRecord(folderSchema, "subFolder");
		subFolder.set(folderSchema.parent(), folder);
		subFolder.set(folderSchema.taxonomy2(), asList(records.taxo2_defaultSchemaItem2_customSchemaItem2));

		document = new TestRecord(documentSchema, "document");
		document.set(documentSchema.parent(), folder);
		recordServices.execute(new Transaction(folder, subFolder, document));
	}

	@Test
	public void whenGetRootRecordsThenReturnOnlyRecordsOfGivenTaxonomy()
			throws Exception {
		givenFoldersAndDocuments();

		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);
		List<Record> taxonomy2RootRecords = services.getRootConcept(records.taxo2_defaultSchemaItem1.getCollection(), "taxo2",
				options);

		assertThat(taxonomy1RootRecords).extracting("id")
				.containsOnly(records.taxo1_firstTypeItem1.getId(), records.taxo1_firstTypeItem2.getId());
		assertThat(taxonomy2RootRecords).extracting("id")
				.containsOnly(records.taxo2_defaultSchemaItem1.getId(), records.taxo2_defaultSchemaItem2.getId());

	}

	@Test
	public void whenGetChildrenOfTaxonomyRecordsThenReturnOnlyChildrenRecords()
			throws Exception {
		givenFoldersAndDocuments();

		List<Record> taxonomy1FirstTypeItem2RecordChildren = services.getChildConcept(records.taxo1_firstTypeItem2,
				options, false);
		List<Record> taxonomy1FirstTypeItem2SecondItem2RecordChildren = services.getChildConcept(
				records.taxo1_firstTypeItem2_secondTypeItem2, options, false);
		List<Record> taxonomy2RecordChildren = services.getChildConcept(records.taxo2_defaultSchemaItem2_defaultSchemaItem2,
				options, false);

		assertThat(taxonomy1FirstTypeItem2RecordChildren).extracting("id").containsOnly(
				records.taxo1_firstTypeItem2_firstTypeItem1.getId(), records.taxo1_firstTypeItem2_firstTypeItem2.getId(),
				records.taxo1_firstTypeItem2_secondTypeItem1.getId(), records.taxo1_firstTypeItem2_secondTypeItem2.getId());
		assertThat(taxonomy1FirstTypeItem2SecondItem2RecordChildren).extracting("id").containsOnly(
				records.taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem1.getId(),
				records.taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem2.getId(), folder.getId());
		assertThat(taxonomy2RecordChildren).extracting("id").containsOnly(
				records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1.getId(),
				records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2.getId());

	}

	@Test
	public void whenGetParentOfTaxonomyRecordThenObtainValidParentId()
			throws Exception {

		assertThat(records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2.getParentId())
				.isEqualTo(records.taxo1_firstTypeItem2_firstTypeItem2.getId());

		assertThat(records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2_secondTypeItem1.getParentId())
				.isEqualTo(records.taxo1_firstTypeItem2_firstTypeItem2_secondTypeItem2.getId());

		assertThat(records.taxo2_defaultSchemaItem2_defaultSchemaItem2.getParentId())
				.isEqualTo(records.taxo2_defaultSchemaItem2.getId());

		assertThat(records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1.getParentId())
				.isEqualTo(records.taxo2_defaultSchemaItem2_defaultSchemaItem2.getId());

	}

	@Test
	public void whenGetChildrenOfNonTaxonomyRecordsThenReturnChildrenUsingChildOfMetadata()
			throws Exception {
		givenFoldersAndDocuments();
		List<Record> folderRecordChildren = services.getChildConcept(folder, options, false);
		List<Record> subFolderRecordChildren = services.getChildConcept(subFolder, options, false);
		List<Record> documentRecordChildren = services.getChildConcept(document, options, false);

		assertThat(services.getChildConcept(folder, options, true)).isEmpty();
		assertThat(services.getChildConcept(subFolder, options, true)).isEmpty();
		assertThat(services.getChildConcept(document, options, true)).isEmpty();

		assertThat(folderRecordChildren).extracting("id").containsOnly(subFolder.getId(), document.getId());
		assertThat(subFolderRecordChildren).isEmpty();
		assertThat(documentRecordChildren).isEmpty();

	}

	//
	@Test
	public void givenDeletedRecordWhenGetRootRecordsWithDefaultSearchOptionsThenReturnOnlyNotDeletedRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);

		assertThat(taxonomy1RootRecords).extracting("id").containsOnly(records.taxo1_firstTypeItem2.getId());
	}

	@Test
	public void givenDeletedRecordWhenGetRootRecordsWithActivesSearchOptionsThenReturnOnlyNotDeletedRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(StatusFilter.ACTIVES);
		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);

		assertThat(taxonomy1RootRecords).usingElementComparatorOnFields("id").containsOnly(records.taxo1_firstTypeItem2);

	}

	@Test
	public void givenDeletedRecordWhenGetRootRecordsWithDeletedSearchOptionsThenReturnOnlyDeletedRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(StatusFilter.DELETED);
		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);

		assertThat(taxonomy1RootRecords).usingElementComparatorOnFields("id").containsOnly(records.taxo1_firstTypeItem1);

	}

	@Test
	public void givenDeletedRecordWhenGetRootRecordsWithAllSearchOptionsThenReturnTwoRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(StatusFilter.ALL);
		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);

		assertThat(taxonomy1RootRecords).usingElementComparatorOnFields("id").containsOnly(records.taxo1_firstTypeItem1,
				records.taxo1_firstTypeItem2);

	}

	@Test
	public void givenDeletedRecordWhenGetRootRecordsWithStartRowAndAllSearchOptionsThenReturnOnlyOneRecord()
			throws Exception {
		givenFoldersAndDocuments();
		options = new TaxonomiesSearchOptions(10, 1, StatusFilter.ALL);
		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);

		assertThat(taxonomy1RootRecords).usingElementComparatorOnFields("id").containsOnly(records.taxo1_firstTypeItem2);

	}

	@Test
	public void givenDeletedRecordWhenGetRootRecordsWithNumberOfRowsAndAllSearchOptionsThenReturnOnlyOneRecord()
			throws Exception {
		givenFoldersAndDocuments();
		options = new TaxonomiesSearchOptions(1, 0, StatusFilter.ALL);
		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);

		assertThat(taxonomy1RootRecords).usingElementComparatorOnFields("id").containsOnly(records.taxo1_firstTypeItem1);

	}

	@Test
	public void givenDeletedRecordsWhenGetChildrenOfTaxonomyRecordsWithDefaultSearchOptionsThenReturnOnlyNotDeletedChildrenRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		List<Record> taxonomy1FirstTypeItem2RecordChildren = services.getChildConcept(records.taxo1_firstTypeItem2,
				options);

		assertThat(taxonomy1FirstTypeItem2RecordChildren).usingElementComparatorOnFields("id")
				.containsOnly(records.taxo1_firstTypeItem2_firstTypeItem1,
						records.taxo1_firstTypeItem2_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem2);
	}

	@Test
	public void givenDeletedRecordsWhenGetChildrenOfTaxonomyRecordsWithAllSearchOptionsThenReturnAllChildrenRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(StatusFilter.ALL);
		List<Record> taxonomy1FirstTypeItem2RecordChildren = services.getChildConcept(records.taxo1_firstTypeItem2,
				options);

		assertThat(taxonomy1FirstTypeItem2RecordChildren).usingElementComparatorOnFields("id")
				.containsOnly(records.taxo1_firstTypeItem2_firstTypeItem1,
						recordToDelete, records.taxo1_firstTypeItem2_firstTypeItem2,
						records.taxo1_firstTypeItem2_secondTypeItem2);
	}

	@Test
	public void givenDeletedRecordsWhenGetChildrenOfTaxonomyRecordsWithDeletedSearchOptionsThenReturnOnlyDeletedChildrenRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(StatusFilter.DELETED);
		List<Record> taxonomy1FirstTypeItem2RecordChildren = services.getChildConcept(records.taxo1_firstTypeItem2,
				options);

		assertThat(taxonomy1FirstTypeItem2RecordChildren).usingElementComparatorOnFields("id")
				.usingElementComparatorOnFields("id").containsOnly(recordToDelete);
	}

	@Test
	public void givenDeletedRecordsWhenGetChildrenOfTaxonomyRecordsWithStartRowAndAllSearchOptionsThenReturnThreeChildrenRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);

		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(10, 0, StatusFilter.ALL);
		List<Record> taxonomy1FirstTypeItem2RecordChildren = services.getChildConcept(records.taxo1_firstTypeItem2,
				options);

		assertThat(taxonomy1FirstTypeItem2RecordChildren).hasSize(4);
		assertThat(taxonomy1FirstTypeItem2RecordChildren)
				.usingElementComparatorOnFields("id").containsOnly(records.taxo1_firstTypeItem2_firstTypeItem2,
				records.taxo1_firstTypeItem2_firstTypeItem1,
				records.taxo1_firstTypeItem2_secondTypeItem1, recordToDelete);
	}

	@Test
	public void givenDeletedRecordsWhenGetChildrenOfTaxonomyRecordsWithNumberOfRowsAndAllSearchOptionsThenReturnTwo()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(2, 0, StatusFilter.ALL);
		List<Record> taxonomy1FirstTypeItem2RecordChildren = services.getChildConcept(records.taxo1_firstTypeItem2,
				options);

		assertThat(taxonomy1FirstTypeItem2RecordChildren).hasSize(2);
	}

	@Test
	public void givenDeletedSubFolderWhenGetChildrenOfNonTaxonomyRecordsWithDefaultSearchOptionsThenReturnOnlyDocument()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = subFolder;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		assertThat(services.getChildConcept(folder, options, true)).isEmpty();

		List<Record> folderRecordChildren = services.getChildConcept(folder, options, false);

		assertThat(folderRecordChildren).extracting("id").containsOnly(document.getId());

	}

	@Test
	public void givenDeletedSubFolderWhenGetChildrenOfNonTaxonomyRecordsWithAllSearchOptionsThenReturnSubFolderAndDocument()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = subFolder;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options.setIncludeStatus(StatusFilter.ALL);

		assertThat(services.getChildConcept(folder, options, true)).isEmpty();

		List<Record> folderRecordChildren = services.getChildConcept(folder, options, false);
		assertThat(folderRecordChildren).extracting("id").containsOnly(subFolder.getId(), document.getId());

	}

	@Test
	public void givenDeletedSubFolderWhenGetChildrenOfNonTaxonomyRecordsWithDeletedSearchOptionsThenReturnOnlySubFolder()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = subFolder;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.<Boolean>get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options.setIncludeStatus(StatusFilter.DELETED);
		List<Record> folderRecordChildren = services.getChildConcept(folder, options, false);

		assertThat(folderRecordChildren).extracting("id").containsOnly(subFolder.getId());

	}

	// ------------------------------------------------

	private void givenAuthorizationsToChuck(Record record)
			throws RolesManagerRuntimeException, InterruptedException {
		List<String> roles = asList(Role.READ, Role.WRITE, Role.DELETE);
		addAuthorizationWithoutDetaching(roles, asList(chuck.getId()), asList(record.getId()));
		waitForBatchProcess();
	}

	private Record addUserRecord(Transaction transaction, String id, List<Record> groups) {
		Record record = new TestRecord(userSchema, id);
		record.set(userSchema.username(), id);
		record.set(userSchema.groups(), groups);
		transaction.addUpdate(record);
		return record;
	}

	private Authorization addAuthorizationWithoutDetaching(List<String> roles, List<String> grantedToPrincipals,
														   List<String> grantedOnRecords) {
		String id = authorizationsServices.add(authorizationInCollection(zeCollection).giving(roles)
				.forPrincipalsIds(grantedToPrincipals).on(grantedOnRecords.get(0)));
		recordServices.refresh(chuck);
		return authorizationsServices.getAuthorization(zeCollection, id);
	}

	private List<String> recordIdsWithAuthorizations(List<TaxonomySearchRecord> results) {
		List<String> ids = new ArrayList<>();
		for (TaxonomySearchRecord record : results) {
			if (record.isLinkable()) {
				ids.add(record.getRecord().getId());
			}
		}
		return ids;
	}

	private List<String> recordIdsWithoutAuthorizations(List<TaxonomySearchRecord> results) {
		List<String> ids = new ArrayList<>();
		for (TaxonomySearchRecord record : results) {
			if (!record.isLinkable()) {
				ids.add(record.getRecord().getId());
			}
		}
		return ids;
	}
}
