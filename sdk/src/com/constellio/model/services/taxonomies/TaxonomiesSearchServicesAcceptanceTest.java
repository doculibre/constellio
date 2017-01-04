package com.constellio.model.services.taxonomies;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;
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

public class TaxonomiesSearchServicesAcceptanceTest extends ConstellioTest {

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
	TaxonomiesSearchServices services;
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

		services = getModelLayerFactory().newTaxonomiesSearchService();

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
		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);

		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);
		List<Record> taxonomy2RootRecords = services.getRootConcept(records.taxo2_defaultSchemaItem1.getCollection(), "taxo2",
				options);

		assertThat(taxonomy1RootRecords).containsOnly(records.taxo1_firstTypeItem1, records.taxo1_firstTypeItem2);
		assertThat(taxonomy2RootRecords).containsOnly(records.taxo2_defaultSchemaItem1, records.taxo2_defaultSchemaItem2);

	}

	@Test
	public void whenGetChildrenOfTaxonomyRecordsThenReturnOnlyChildrenRecords()
			throws Exception {
		givenFoldersAndDocuments();
		List<Record> taxonomy1FirstTypeItem2RecordChildren = services.getChildConcept(records.taxo1_firstTypeItem2,
				options);
		List<Record> taxonomy1FirstTypeItem2SecondItem2RecordChildren = services.getChildConcept(
				records.taxo1_firstTypeItem2_secondTypeItem2, options);
		List<Record> taxonomy2RecordChildren = services.getChildConcept(records.taxo2_defaultSchemaItem2_defaultSchemaItem2,
				options);

		assertThat(taxonomy1FirstTypeItem2RecordChildren).containsOnly(records.taxo1_firstTypeItem2_firstTypeItem1,
				records.taxo1_firstTypeItem2_firstTypeItem2, records.taxo1_firstTypeItem2_secondTypeItem1,
				records.taxo1_firstTypeItem2_secondTypeItem2);
		assertThat(taxonomy1FirstTypeItem2SecondItem2RecordChildren).containsOnly(
				records.taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem1,
				records.taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem2, folder);
		assertThat(taxonomy2RecordChildren).containsOnly(
				records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1,
				records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2);

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
		List<Record> folderRecordChildren = services.getChildConcept(folder, options);
		List<Record> subFolderRecordChildren = services.getChildConcept(subFolder, options);
		List<Record> documentRecordChildren = services.getChildConcept(document, options);

		assertThat(folderRecordChildren).containsOnly(subFolder, document);
		assertThat(subFolderRecordChildren).isEmpty();
		assertThat(documentRecordChildren).isEmpty();

	}

	@Test
	public void whenCheckingIfHasNonTaxonomyRecordsThenWorkWithAllLevels()
			throws Exception {
		givenFoldersAndDocuments();
		assertThat(services.findNonTaxonomyRecordsInStructure(records.taxo1_firstTypeItem1, options)).isFalse();
		assertThat(services.findNonTaxonomyRecordsInStructure(records.taxo1_firstTypeItem2, options)).isTrue();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(records.taxo1_firstTypeItem2_firstTypeItem1, options))
				.isFalse();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(records.taxo1_firstTypeItem2_firstTypeItem2, options))
				.isFalse();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(records.taxo1_firstTypeItem2_secondTypeItem1, options))
				.isFalse();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(records.taxo1_firstTypeItem2_secondTypeItem2, options))
				.isTrue();

		assertThat(services.findNonTaxonomyRecordsInStructure(records.taxo2_defaultSchemaItem1, options))
				.isFalse();
		assertThat(services.findNonTaxonomyRecordsInStructure(records.taxo2_defaultSchemaItem2, options))
				.isTrue();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(records.taxo2_defaultSchemaItem2_customSchemaItem1,
						options)).isTrue();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(records.taxo2_defaultSchemaItem2_customSchemaItem2,
						options)).isTrue();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(records.taxo2_defaultSchemaItem2_defaultSchemaItem1,
						options)).isFalse();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(records.taxo2_defaultSchemaItem2_defaultSchemaItem2,
						options)).isTrue();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(
						records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1, options))
				.isFalse();
		assertThat(
				services.findNonTaxonomyRecordsInStructure(
						records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2, options))
				.isTrue();

	}

	@Test
	public void whenGettingVisibleChildThenOnlyVisibleConceptReturned()
			throws Exception {
		givenFoldersAndDocuments();

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()),
				asList(records.taxo1_firstTypeItem2_secondTypeItem2.getId(), folder.getId()));

		waitForBatchProcess();
		bob = wrapUser(bob.getWrappedRecord());

		assertThat(services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem1, options)).isEmpty();

		List<TaxonomySearchRecord> visibleChildConcept = services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2,
				options);
		assertThat(visibleChildConcept).hasSize(1);
		assertThat(visibleChildConcept.get(0).getId()).isEqualTo(records.taxo1_firstTypeItem2_secondTypeItem2.getId());

		assertThat(services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2_firstTypeItem1, options))
				.isEmpty();
		assertThat(services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2_firstTypeItem2, options))
				.isEmpty();
		assertThat(services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2_secondTypeItem1, options))
				.isEmpty();

		visibleChildConcept = services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2_secondTypeItem2,
				options);
		assertThat(visibleChildConcept).hasSize(1);
		assertThat(visibleChildConcept.get(0).getId()).isEqualTo(folder.getId());

		assertThat(services.getVisibleChildConcept(bob, TAXO2, records.taxo2_defaultSchemaItem1, options)).isEmpty();

		assertThat(services.getVisibleChildConcept(bob, TAXO2, records.taxo2_defaultSchemaItem2, options)).containsOnly(
				new TaxonomySearchRecord(records.taxo2_defaultSchemaItem2_customSchemaItem1, false, true),
				new TaxonomySearchRecord(records.taxo2_defaultSchemaItem2_customSchemaItem2, false, true),
				new TaxonomySearchRecord(records.taxo2_defaultSchemaItem2_defaultSchemaItem2, false, true));

		visibleChildConcept = services.getVisibleChildConcept(bob, TAXO2, records.taxo2_defaultSchemaItem2_customSchemaItem1,
				options);
		assertThat(visibleChildConcept).hasSize(1);
		assertThat(visibleChildConcept.get(0).getId()).isEqualTo(folder.getId());

		assertThat(
				services.getVisibleChildConcept(bob, TAXO2, records.taxo2_defaultSchemaItem2_customSchemaItem2,
						options)).hasSize(1);
		assertThat(
				services.getVisibleChildConcept(bob, TAXO2, records.taxo2_defaultSchemaItem2_defaultSchemaItem1,
						options)).isEmpty();

		assertThat(services.getVisibleChildConcept(bob, TAXO2, records.taxo2_defaultSchemaItem2_defaultSchemaItem2,
				options)).containsOnly(
				new TaxonomySearchRecord(records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2, false, true));

		assertThat(
				services.getVisibleChildConcept(bob, TAXO2, records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem1,
						options)).isEmpty();

		visibleChildConcept = services.getVisibleChildConcept(bob, TAXO2,
				records.taxo2_defaultSchemaItem2_defaultSchemaItem2_customSchemaItem2, options);
		assertThat(visibleChildConcept).hasSize(1);
		assertThat(visibleChildConcept.get(0).getId()).isEqualTo(folder.getId());
	}

	@Test
	public void whenGettingVisibleChildWithOnlySubFolderAuthorizationThenRightVisibleConceptReturned()
			throws Exception {
		givenFoldersAndDocuments();

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()), asList(subFolder.getId()));

		waitForBatchProcess();
		bob = wrapUser(bob.getWrappedRecord());

		assertThat(services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem1, options)).isEmpty();

		List<TaxonomySearchRecord> visibleChildConcept = services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2,
				options);
		assertThat(visibleChildConcept).hasSize(1);
		assertThat(visibleChildConcept.get(0).getId()).isEqualTo(records.taxo1_firstTypeItem2_secondTypeItem2.getId());
	}

	@Test
	public void whenGettingVisibleChildWithUserThenOnlyVisibleConceptReturned()
			throws Exception {
		givenFoldersAndDocuments();

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()),
				asList(records.taxo1_firstTypeItem2_secondTypeItem2.getId()));

		waitForBatchProcess();

		bob = wrapUser(bob.getWrappedRecord());

		List<TaxonomySearchRecord> visibleChildConcept = services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2,
				options);
		assertThat(visibleChildConcept).hasSize(1);
		assertThat(visibleChildConcept.get(0).getId()).isEqualTo(records.taxo1_firstTypeItem2_secondTypeItem2.getId());
	}

	@Test
	public void whenGettingLinkableChildWithUserThenOnlyVisibleConceptReturned()
			throws Exception {

		folder = new TestRecord(folderSchema, "folder");
		folder.set(folderSchema.taxonomy1(), records.taxo1_firstTypeItem2_secondTypeItem1);
		recordServices.add(folder);

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()),
				asList(records.taxo1_firstTypeItem2_secondTypeItem2.getId(), folder.getId()));

		waitForBatchProcess();

		bob = wrapUser(bob.getWrappedRecord());

		List<TaxonomySearchRecord> results = services
				.getLinkableRootConcept(bob, zeCollection, TAXO1, taxonomy1SecondSchema.type().getCode(), options);
		assertThat(recordIdsWithAuthorizations(results)).isEmpty();
		assertThat(recordIdsWithoutAuthorizations(results)).containsOnly(records.taxo1_firstTypeItem2.getId());

		results = services
				.getLinkableChildConcept(bob, records.taxo1_firstTypeItem2, TAXO1, taxonomy1SecondSchema.type().getCode(),
						options);
		assertThat(recordIdsWithAuthorizations(results)).containsOnly(records.taxo1_firstTypeItem2_secondTypeItem2.getId());
		assertThat(recordIdsWithoutAuthorizations(results)).isEmpty();

		results = services.getLinkableChildConcept(bob, records.taxo1_firstTypeItem2_secondTypeItem2, TAXO1,
				taxonomy1SecondSchema.type().getCode(), options);
		assertThat(recordIdsWithAuthorizations(results))
				.containsOnly(records.taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem1.getId(),
						records.taxo1_firstTypeItem2_secondTypeItem2_secondTypeItem2.getId());
		assertThat(recordIdsWithoutAuthorizations(results)).isEmpty();

		results = services
				.getLinkableRootConcept(bob, zeCollection, TAXO2, taxonomy2DefaultSchema.type().getCode(), options);
		assertThat(recordIdsWithAuthorizations(results))
				.containsOnly(records.taxo2_defaultSchemaItem1.getId(), records.taxo2_defaultSchemaItem2.getId());
		assertThat(recordIdsWithoutAuthorizations(results)).isEmpty();

		results = services
				.getLinkableChildConcept(bob, records.taxo2_defaultSchemaItem2, TAXO2, taxonomy2DefaultSchema.type().getCode(),
						options);
		assertThat(recordIdsWithAuthorizations(results))
				.containsOnly(records.taxo2_defaultSchemaItem2_customSchemaItem1.getId(),
						records.taxo2_defaultSchemaItem2_customSchemaItem2.getId(),
						records.taxo2_defaultSchemaItem2_defaultSchemaItem1.getId(),
						records.taxo2_defaultSchemaItem2_defaultSchemaItem2.getId());
		assertThat(recordIdsWithoutAuthorizations(results)).isEmpty();

	}

	@Test
	public void whenGetVisibleRootRecordsWithAuthorizationThenReturnOnlyRecordsOfGivenTaxonomy()
			throws Exception {
		givenFoldersAndDocuments();

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()),
				asList(records.taxo1_firstTypeItem2.getId(), folder.getId()));

		waitForBatchProcess();

		bob = wrapUser(bob.getWrappedRecord());

		List<TaxonomySearchRecord> taxonomy1RootRecords = services
				.getVisibleRootConcept(bob, bob.getCollection(), "taxo1", options);
		List<TaxonomySearchRecord> taxonomy2RootRecords = services
				.getVisibleRootConcept(bob, bob.getCollection(), "taxo2", options);

		assertThat(taxonomy1RootRecords).hasSize(1);
		assertThat(taxonomy1RootRecords.get(0).getId()).isEqualTo(records.taxo1_firstTypeItem2.getId());

		assertThat(taxonomy2RootRecords).hasSize(1);
		assertThat(taxonomy2RootRecords.get(0).getId()).isEqualTo(records.taxo2_defaultSchemaItem2.getId());

	}

	@Test
	public void whenGetVisibleRecordsWithAuthorizationOnFolderThenReturnOnlyRecordsOfGivenTaxonomy()
			throws Exception {
		givenFoldersAndDocuments();

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()), asList(folder.getId()));

		waitForBatchProcess();

		bob = wrapUser(bob.getWrappedRecord());

		List<TaxonomySearchRecord> taxonomy1RootRecords = services
				.getVisibleRootConcept(bob, bob.getCollection(), "taxo1", options);

		assertThat(taxonomy1RootRecords).hasSize(1);
		assertThat(taxonomy1RootRecords.get(0).getId()).isEqualTo(records.taxo1_firstTypeItem2.getId());

	}

	@Test
	public void whenGetVisibleRootRecordsWithNotAuthorizationThenReturnEmpty()
			throws Exception {
		givenFoldersAndDocuments();

		bob = wrapUser(bob.getWrappedRecord());

		List<TaxonomySearchRecord> taxonomy1RootRecords = services
				.getVisibleRootConcept(bob, bob.getCollection(), "taxo1", options);
		List<TaxonomySearchRecord> taxonomy2RootRecords = services
				.getVisibleRootConcept(bob, bob.getCollection(), "taxo2", options);

		assertThat(taxonomy1RootRecords).hasSize(0);
		assertThat(taxonomy2RootRecords).hasSize(0);
	}

	//
	@Test
	public void givenDeletedRecordWhenGetRootRecordsWithDefaultSearchOptionsThenReturnOnlyNotDeletedRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		List<Record> taxonomy1RootRecords = services.getRootConcept(records.taxo1_firstTypeItem1.getCollection(), "taxo1",
				options);

		assertThat(taxonomy1RootRecords).containsOnly(records.taxo1_firstTypeItem2);
	}

	@Test
	public void givenDeletedRecordWhenGetRootRecordsWithActivesSearchOptionsThenReturnOnlyNotDeletedRecords()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem1;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

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
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

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
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

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
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

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
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

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
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

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
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

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
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(2, 0, StatusFilter.ALL);
		List<Record> taxonomy1FirstTypeItem2RecordChildren = services.getChildConcept(records.taxo1_firstTypeItem2,
				options);

		assertThat(taxonomy1FirstTypeItem2RecordChildren).hasSize(2);
	}

	@Test
	public void givenDeletedRecordWhenCheckingIfHasNonTaxonomyRecordsWithDefaultRecordsSearchOptionsThenFalse()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions();
		assertThat(services.getVisibleChildConcept(chuck, TAXO1, records.taxo1_firstTypeItem2, options)).isEmpty();
	}

	@Test
	public void givenDeletedRecordWhenCheckingIfHasNonTaxonomyRecordsWithDeletedRecordsSearchOptionsThenTrue()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(StatusFilter.ALL);
		assertThat(services.getVisibleChildConcept(chuck, TAXO1, records.taxo1_firstTypeItem2, options)).isNotEmpty();
	}

	@Test
	public void givenDeletedRecordWhenCheckingIfHasNonTaxonomyRecordsWithActiveRecordsSearchOptionsThenFalse()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options = new TaxonomiesSearchOptions(StatusFilter.ACTIVES);
		assertThat(services.getVisibleChildConcept(chuck, TAXO1, records.taxo1_firstTypeItem2, options)).isEmpty();
	}

	@Test
	public void givenDeletedSubFolderWhenGetChildrenOfNonTaxonomyRecordsWithDefaultSearchOptionsThenReturnOnlyDocument()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = subFolder;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		List<Record> folderRecordChildren = services.getChildConcept(folder, options);

		assertThat(folderRecordChildren).containsOnly(document);

	}

	@Test
	public void givenDeletedSubFolderWhenGetChildrenOfNonTaxonomyRecordsWithAllSearchOptionsThenReturnSubFolderAndDocument()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = subFolder;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options.setIncludeStatus(StatusFilter.ALL);
		List<Record> folderRecordChildren = services.getChildConcept(folder, options);

		assertThat(folderRecordChildren).containsOnly(subFolder, document);

	}

	@Test
	public void givenDeletedSubFolderWhenGetChildrenOfNonTaxonomyRecordsWithDeletedSearchOptionsThenReturnOnlySubFolder()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = subFolder;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		options.setIncludeStatus(StatusFilter.DELETED);
		List<Record> folderRecordChildren = services.getChildConcept(folder, options);

		assertThat(folderRecordChildren).containsOnly(subFolder);

	}

	@Test
	public void givenDeletedRecordwhenGettingVisibleChildWithDefaultSearchOptionsThenNothingIsReturned()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()),
				asList(records.taxo1_firstTypeItem2_secondTypeItem2.getId(), folder.getId()));

		waitForBatchProcess();
		bob = wrapUser(bob.getWrappedRecord());

		List<TaxonomySearchRecord> visibleChildConcept = services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2,
				options);
		assertThat(visibleChildConcept).hasSize(0);
	}

	@Test
	public void givenDeletedRecordwhenGettingVisibleChildWithAllSearchOptionsThenOnlyVisibleConceptReturned()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2_secondTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()),
				asList(records.taxo1_firstTypeItem2_secondTypeItem2.getId(), folder.getId()));

		waitForBatchProcess();
		bob = wrapUser(bob.getWrappedRecord());

		options.setIncludeStatus(StatusFilter.ALL);

		List<TaxonomySearchRecord> visibleChildConcept = services.getVisibleChildConcept(bob, TAXO1, records.taxo1_firstTypeItem2,
				options);
		assertThat(visibleChildConcept).hasSize(1);
		assertThat(visibleChildConcept.get(0).getId()).isEqualTo(records.taxo1_firstTypeItem2_secondTypeItem2.getId());
	}

	@Test
	public void givenDeleteRecordWhenGetVisibleRootRecordsWithAuthorizationAndDefaultSearchOptionsThenReturnOnlyRecordsOfGivenTaxonomy()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()),
				asList(records.taxo1_firstTypeItem2.getId(), folder.getId()));

		waitForBatchProcess();

		bob = wrapUser(bob.getWrappedRecord());

		List<TaxonomySearchRecord> taxonomy1RootRecords = services
				.getVisibleRootConcept(bob, bob.getCollection(), "taxo1", options);

		assertThat(taxonomy1RootRecords).hasSize(0);
	}

	@Test
	public void givenDeleteRecordWhenGetVisibleRootRecordsWithAuthorizationAndAllSearchOptionsThenReturnOnlyRecordsOfGivenTaxonomy()
			throws Exception {
		givenFoldersAndDocuments();

		Record recordToDelete = records.taxo1_firstTypeItem2;
		givenAuthorizationsToChuck(recordToDelete);
		recordServices.logicallyDelete(recordToDelete, chuck);
		assertThat(recordToDelete.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);

		addAuthorizationWithoutDetaching(asList(Role.READ), asList(bob.getId()),
				asList(records.taxo1_firstTypeItem2.getId(), folder.getId()));

		waitForBatchProcess();

		bob = wrapUser(bob.getWrappedRecord());

		options.setIncludeStatus(StatusFilter.ALL);
		List<TaxonomySearchRecord> taxonomy1RootRecords = services
				.getVisibleRootConcept(bob, bob.getCollection(), "taxo1", options);

		assertThat(taxonomy1RootRecords).hasSize(1);
		assertThat(taxonomy1RootRecords.get(0).getId()).isEqualTo(records.taxo1_firstTypeItem2.getId());
	}

	// ------------------------------------------------

	private void givenAuthorizationsToChuck(Record record)
			throws RolesManagerRuntimeException, InterruptedException {
		List<String> roles = asList(Role.WRITE, Role.DELETE);
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
		AuthorizationDetails details = AuthorizationDetails.create(aString(), roles, zeCollection);

		Authorization authorization = new Authorization(details, grantedToPrincipals, grantedOnRecords);

		authorizationsServices.add(authorization, null);
		return authorization;
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
