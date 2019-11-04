package com.constellio.model.services.records;

import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionValidationEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordDeleteServicesRuntimeException_CannotTotallyDeleteSchemaType;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords;
import com.constellio.model.services.records.RecordLogicalDeleteOptions.LogicallyDeleteTaxonomyRecordsBehavior;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotRestoreRecord;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.FolderSchema;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.Records;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;
import static com.constellio.model.services.records.RecordPhysicalDeleteOptions.PhysicalDeleteTaxonomyRecordsBehavior.PHYSICALLY_DELETE_THEM;
import static com.constellio.model.services.records.RecordPhysicalDeleteOptions.PhysicalDeleteTaxonomyRecordsBehavior.PHYSICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY;
import static com.constellio.sdk.tests.TestUtils.*;
import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class RecordsDeleteAcceptTest extends ConstellioTest {

	RecordDeleteServices recordDeleteServices;
	SecurityAcceptanceTestSetup schemas = new SecurityAcceptanceTestSetup(zeCollection);
	FolderSchema folderSchema = schemas.new FolderSchema();
	MetadataSchemasManager schemasManager;
	SearchServices searchServices;
	RecordServicesImpl recordServices;
	TaxonomiesManager taxonomiesManager;
	CollectionsListManager collectionsListManager;
	AuthorizationsServices authorizationsServices;

	RecordPhysicalDeleteOptions withMostReferencesRemoved;
	RecordPhysicalDeleteOptions withMostReferencesRemovedPhysicallyDeletingRecords;
	RecordPhysicalDeleteOptions withMostReferencesRemovedPhysicallyDeletingRecordsIfPrincipalTaxonomy;
	RecordLogicalDeleteOptions logicallyDeletingRecordsIfPrincipalTaxonomy, logicallyDeletingRecords, keepingRecords;

	Records records;
	Users users = new Users();
	RolesManager roleManager;

	User bob, userWithDeletePermission;
	Record valueListItem1, valueListItem2, valueListItem3, rootUnclassifiedItem, childUnclassifiedItem;

	boolean required = true;
	boolean notRequired = false;

	private ModelLayerCollectionExtensions extensions;

	@Before
	public void setUp()
			throws Exception {

		withMostReferencesRemoved = new RecordPhysicalDeleteOptions().setMostReferencesToNull(true);
		withMostReferencesRemovedPhysicallyDeletingRecords = new RecordPhysicalDeleteOptions().setMostReferencesToNull(true)
				.setBehaviorForRecordsAttachedToTaxonomy(PHYSICALLY_DELETE_THEM);
		withMostReferencesRemovedPhysicallyDeletingRecordsIfPrincipalTaxonomy = new RecordPhysicalDeleteOptions()
				.setMostReferencesToNull(true)
				.setBehaviorForRecordsAttachedToTaxonomy(PHYSICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY);
		logicallyDeletingRecordsIfPrincipalTaxonomy = new RecordLogicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(
				LogicallyDeleteTaxonomyRecordsBehavior.LOGICALLY_DELETE_THEM_ONLY_IF_PRINCIPAL_TAXONOMY);

		logicallyDeletingRecords = new RecordLogicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(
				LogicallyDeleteTaxonomyRecordsBehavior.LOGICALLY_DELETE_THEM);

		keepingRecords = new RecordLogicalDeleteOptions();

		customSystemPreparation(new CustomSystemPreparation() {
			@Override
			public void prepare() {
				givenCachedCollection(zeCollection).withAllTestUsers();
				givenCachedCollection("anotherCollection").withAllTestUsers();

				setupServices();

				defineSchemasManager().using(schemas.with(unsecuredAndUnclassifiedValueListSchemaAndUnclassifiedSecuredSchema()));
				taxonomiesManager.addTaxonomy(schemas.getTaxonomy1(), schemasManager);
				taxonomiesManager.addTaxonomy(schemas.getTaxonomy2(), schemasManager);
				taxonomiesManager.setPrincipalTaxonomy(schemas.getTaxonomy1(), schemasManager);
				records = schemas.new Records(recordServices);
				records.setup();

				userWithDeletePermission = users.chuckNorrisIn(zeCollection);
				userWithDeletePermission.setCollectionDeleteAccess(true);
				try {
					recordServices.update(userWithDeletePermission.getWrappedRecord());
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void initializeFromCache() {
				setupServices();
				schemas.refresh(schemasManager);
				records = schemas.new Records(recordServices);
			}

			private void setupServices() {
				users.setUp(getModelLayerFactory().newUserServices());
				recordServices = spy(getModelLayerFactory().newCachelessRecordServices());
				taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
				searchServices = getModelLayerFactory().newSearchServices();
				authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
				schemasManager = getModelLayerFactory().getMetadataSchemasManager();
				roleManager = getModelLayerFactory().getRolesManager();
				collectionsListManager = getModelLayerFactory().getCollectionsListManager();
				recordDeleteServices = spy(recordServices.newRecordDeleteServices());

				doReturn(recordDeleteServices).when(recordServices).newRecordDeleteServices();
			}
		});

		bob = users.bobIn(zeCollection);
		userWithDeletePermission = users.chuckNorrisIn(zeCollection);

		extensions = getModelLayerFactory().getExtensions().forCollection(zeCollection);

		MetadataSchema valueListItemSchema = schemas.getSchema("valueList_default");
		MetadataSchema securedUnclassifiedSchema = schemas.getSchema("securedUnclassified_default");

		valueListItem1 = recordServices.newRecordWithSchema(valueListItemSchema, "valuelListItem").set(TITLE, "Ze item");
		valueListItem2 = recordServices.newRecordWithSchema(valueListItemSchema, "value2ListItem").set(TITLE, "Ze item 2");
		valueListItem3 = recordServices.newRecordWithSchema(valueListItemSchema, "value3ListItem").set(TITLE, "Ze item 3");

		rootUnclassifiedItem = recordServices.newRecordWithSchema(securedUnclassifiedSchema, "rootUnclassifiedItem")
				.set(TITLE, "rootUnclassifiedItem");

		childUnclassifiedItem = recordServices.newRecordWithSchema(securedUnclassifiedSchema, "childUnclassifiedItem")
				.set(TITLE, "childUnclassifiedItem").set(securedUnclassifiedSchema.get("parent"), rootUnclassifiedItem);

		recordServices.execute(
				new Transaction(valueListItem1, valueListItem2, valueListItem3, rootUnclassifiedItem, childUnclassifiedItem));
	}

	private void givenValueListSchemaHasSecurity() {
		MetadataSchema valueListItemSchema = schemas.getSchema("valueList_default");

		MetadataSchemaTypesBuilder typesBuilder = getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection);
		typesBuilder.getSchemaType("valueList").setSecurity(true);
		try {
			getModelLayerFactory().getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	@Test
	public void givenRecordReferencedByOtherRecordsIn()
			throws Exception {

		givenMultiValueLinkToOtherFolderMetadatasIs(required);
		given(records.folder2()).hasAReferenceTo(records.folder1(), records.folder4());
		given(records.folder3()).hasAReferenceTo(records.folder1());

		givenSingleValueLinkToOtherFolderMetadatasIs(required);
		given(records.folder2()).hasASingleValueReferenceTo(records.folder1());
		given(records.folder3()).hasASingleValueReferenceTo(records.folder4());

		given(userWithDeletePermission).logicallyDelete(records.folder1());

		try {
			when(userWithDeletePermission).physicallyDelete(records.folder1(), withMostReferencesRemoved);
			fail("Physically delete should have failed");
		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords e) {
			assertThat(e.getRecordsIdsWithUnremovableReferences()).containsOnly(
					records.folder2().getId(), records.folder3().getId());
		}
		assertThatRecord(records.folder2())
				.hasMetadataValue(folderSchema.linkToOtherFolder(), records.folder1().getId())
				.hasMetadataValue(folderSchema.linkToOtherFolders(),
						asList(records.folder1().getId(), records.folder4().getId()));

		assertThatRecord(records.folder3())
				.hasMetadataValue(folderSchema.linkToOtherFolders(), asList(records.folder1().getId()));

		givenMultiValueLinkToOtherFolderMetadatasIs(notRequired);
		try {
			when(userWithDeletePermission).physicallyDelete(records.folder1(), withMostReferencesRemoved);
			fail("Physically delete should have failed");
		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords e) {
			assertThat(e.getRecordsIdsWithUnremovableReferences()).containsOnly(
					records.folder2().getId());
		}
		assertThatRecord(records.folder2())
				.hasMetadataValue(folderSchema.linkToOtherFolder(), records.folder1().getId())
				.hasMetadataValue(folderSchema.linkToOtherFolders(),
						asList(records.folder1().getId(), records.folder4().getId()));

		assertThatRecord(records.folder3())
				.hasNoMetadataValue(folderSchema.linkToOtherFolders());

		givenSingleValueLinkToOtherFolderMetadatasIs(notRequired);
		when(userWithDeletePermission).physicallyDelete(records.folder1(), withMostReferencesRemoved);

		assertThatRecord(records.folder2())
				.hasNoMetadataValue(folderSchema.linkToOtherFolder())
				.hasMetadataValue(folderSchema.linkToOtherFolders(), asList(records.folder4().getId()));

	}

	@Test
	public void givenNotPhysicallyDeletableByExtensionThenNotPhysicallyDeletable()
			throws Exception {
		extensions.recordExtensions.add(new RecordExtension() {

			@Override
			public ValidationErrors validatePhysicallyDeletable(RecordPhysicalDeletionValidationEvent params) {
				ValidationErrors validationErrors = new ValidationErrors();
				validationErrors.add(RecordsDeleteAcceptTest.class, "Not physically deletable");
				return validationErrors;
			}
		});
		given(bob).logicallyDelete(valueListItem1);

		assertThat(valueListItem1).isNot(physicallyDeletableBy(bob));
		assertThat(valueListItem1).isNot(physicallyDeletableBy(bob, withMostReferencesRemoved));
	}

	@Test
	public void givenPhysicallyDeletableByExtensionThenPhysicallyDeletable()
			throws Exception {
		extensions.recordExtensions.add(new RecordExtension() {
			@Override
			public ValidationErrors validatePhysicallyDeletable(RecordPhysicalDeletionValidationEvent params) {
				return new ValidationErrors();
			}
		});
		given(bob).logicallyDelete(valueListItem1);

		assertThat(valueListItem1).is(physicallyDeletableBy(bob));
		assertThat(valueListItem1).is(physicallyDeletableBy(bob, withMostReferencesRemoved));
	}

	@Test
	public void givenNotLogicallyDeletableByExtensionThenNotLogicallyDeletable()
			throws Exception {
		extensions.recordExtensions.add(new RecordExtension() {
			@Override
			public ValidationErrors validateLogicallyDeletable(RecordLogicalDeletionValidationEvent params) {
				ValidationErrors validationErrors = new ValidationErrors();
				validationErrors.add(RecordsDeleteAcceptTest.class, "Record is not logically deleted");
				return validationErrors;
			}

		});
		assertThat(valueListItem1).isNot(logicallyDeletableBy(bob));
	}

	@Test
	public void givenLogicallyDeletableByExtensionThenLogicallyDeletable()
			throws Exception {
		extensions.recordExtensions.add(new RecordExtension() {
			@Override
			public ValidationErrors validateLogicallyDeletable(RecordLogicalDeletionValidationEvent params) {
				return new ValidationErrors();
			}

		});
		assertThat(valueListItem1).is(logicallyDeletableBy(bob));
	}

	@Test
	public void givenNotReferencedValueListItemWithoutSecurityThenLogicallyDeletableByAnybody()
			throws Exception {
		assertThat(valueListItem1).is(logicallyDeletableBy(bob));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(bob));
		assertThat(valueListItem1).is(notPhysicallyDeletableBy(bob));
		assertThat(valueListItem1).is(notPhysicallyDeletableBy(bob, withMostReferencesRemoved));

		when(bob).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(logicallyDeleted());

	}

	@Test
	public void givenNotReferencedValueListItemWithoutSecurityThenRestorableByAnybody()
			throws Exception {
		given(bob).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(restorableBy(bob));

		when(bob).restore(valueListItem1);
		assertThat(valueListItem1).isNot(logicallyDeleted());

	}

	@Test
	public void givenNotReferencedValueListItemWithoutSecurityThenPhysicallyDeletableByAnybody()
			throws Exception {
		given(bob).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(physicallyDeletableBy(bob));
		assertThat(valueListItem1).is(physicallyDeletableBy(bob, withMostReferencesRemoved));

		when(bob).physicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(physicallyDeleted());

	}

	@Test
	public void givenNotReferencedValueListItemWithSecurityThenLogicallyDeletableByGodAndUserWithCollectionDelete()
			throws Exception {
		givenValueListSchemaHasSecurity();
		assertThat(valueListItem1).is(notLogicallyDeletableBy(bob));
		assertThat(valueListItem1).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(valueListItem1).is(notLogicallyThenPhysicallyDeletableBy(bob, withMostReferencesRemoved));
		assertThat(valueListItem1).is(logicallyDeletableBy(User.GOD));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(User.GOD));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(User.GOD, withMostReferencesRemoved));
		assertThat(valueListItem1).is(logicallyDeletableBy(userWithDeletePermission));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));

		when(User.GOD).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(logicallyDeleted());

	}

	@Test
	public void givenNotReferencedValueListItemWithSecurityThenRestorableByGodAndUserWithCollectionDelete()
			throws Exception {
		givenValueListSchemaHasSecurity();
		given(userWithDeletePermission).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(notRestorableBy(bob));
		assertThat(valueListItem1).is(restorableBy(User.GOD));
		assertThat(valueListItem1).is(restorableBy(userWithDeletePermission));

		when(userWithDeletePermission).restore(valueListItem1);
		assertThat(valueListItem1).isNot(logicallyDeleted());

	}

	@Test
	public void givenNotReferencedValueListItemWithSecurityThenPhysicallyDeletableByGodAndUserWithCollectionDelete()
			throws Exception {
		givenValueListSchemaHasSecurity();
		assertThat(valueListItem1).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(User.GOD));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));

		given(User.GOD).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(notPhysicallyDeletableBy(bob));
		assertThat(valueListItem1).is(physicallyDeletableBy(User.GOD));
		assertThat(valueListItem1).is(physicallyDeletableBy(userWithDeletePermission));

		when(User.GOD).physicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(physicallyDeleted());

	}

	@Test
	public void givenReferencedValueListItemThenLogicallyDeletable()
			throws Exception {
		given(records.folder3()).hasAValueListReferenceTo(valueListItem1);
		assertThat(valueListItem1).is(logicallyDeletableBy(bob));
		assertThat(valueListItem1).is(notLogicallyThenPhysicallyDeletableBy(bob));

		when(bob).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(logicallyDeleted());

	}

	@Test
	public void givenRecordIsDefaultValueThenNotLogicallyDeletable()
			throws Exception {

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(records.folder3().getSchemaCode()).get("valueListRef")
						.setDefaultValue(asList(valueListItem1.getId()));
			}
		});
		assertThat(valueListItem1).isNot(logicallyDeletableBy(userWithDeletePermission));
		assertThat(valueListItem1).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));

	}

	@Test
	public void givenReferencedValueListItemThenRestorable()
			throws Exception {
		given(records.folder3()).hasAValueListReferenceTo(valueListItem1);
		given(userWithDeletePermission).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(restorableBy(bob));

		when(bob).restore(valueListItem1);
		assertThat(valueListItem1).isNot(logicallyDeleted());

	}

	@Test
	public void givenReferencedValueListItemThenNotPhysicallyDeletable()
			throws Exception {
		given(records.folder3()).hasAValueListReferenceTo(valueListItem1);
		given(bob).logicallyDelete(valueListItem1);
		assertThat(valueListItem1).is(notPhysicallyDeletableBy(bob));
		assertThat(valueListItem1).is(notPhysicallyDeletableBy(User.GOD));
		assertThat(valueListItem1).is(notPhysicallyDeletableBy(userWithDeletePermission));

	}

	@Test
	public void givenReferencedValueListItemInMultivalueMetadataThenPhysicallyDeletableWithRefRemovedOption()
			throws Exception {
		given(records.folder3()).hasAValueListReferenceTo(valueListItem1, valueListItem2, valueListItem3);
		given(records.folder4()).hasAValueListReferenceTo(valueListItem2, valueListItem1, valueListItem3);
		given(records.folder1()).hasAValueListReferenceTo(valueListItem2, valueListItem3, valueListItem1);
		assertThat(valueListItem1).is(logicallyDeletableBy(bob));
		assertThat(valueListItem1).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(bob, withMostReferencesRemoved));

		Metadata metadata = folderSchema.instance().getMetadata("valueListRef");
		when(bob).logicallyDelete(valueListItem1);
		when(bob).physicallyDelete(valueListItem1, withMostReferencesRemoved);
		assertThat(valueListItem1).is(physicallyDeleted());
		assertThat(records.folder3().getList(metadata)).containsExactly("value2ListItem", "value3ListItem");
		assertThat(records.folder4().getList(metadata)).containsExactly("value2ListItem", "value3ListItem");
		assertThat(records.folder1().getList(metadata)).containsExactly("value2ListItem", "value3ListItem");

	}

	@Test
	public void givenReferencedValueListItemInSingleValueMetadataThenPhysicallyDeletableWithRefRemovedOption()
			throws Exception {
		given(records.folder3()).hasASingleValueListReferenceTo(valueListItem1);
		given(records.folder4()).hasASingleValueListReferenceTo(valueListItem1);
		given(records.folder1()).hasASingleValueListReferenceTo(valueListItem2);
		assertThat(valueListItem1).is(logicallyDeletableBy(bob));
		assertThat(valueListItem1).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(valueListItem1).is(logicallyThenPhysicallyDeletableBy(bob, withMostReferencesRemoved));

		Metadata metadata = folderSchema.instance().getMetadata("valueListSingleRef");
		when(bob).logicallyDelete(valueListItem1);
		when(bob).physicallyDelete(valueListItem1, withMostReferencesRemoved);
		assertThat(valueListItem1).is(physicallyDeleted());
		assertThat(records.folder3().<String>get(metadata)).isNull();
		assertThat(records.folder4().<String>get(metadata)).isNull();
		assertThat(records.folder1().<String>get(metadata)).isEqualTo("value2ListItem");

	}

	@Test
	public void givenUserWithCollectionDeleteAccessWhenDeletingASecuredUnclassifiedRecordThenAllHierarchyDeleted()
			throws Exception {
		assertThat(rootUnclassifiedItem).is(logicallyDeletableBy(userWithDeletePermission));
		assertThat(rootUnclassifiedItem).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(rootUnclassifiedItem)
				.is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));
		assertThat(rootUnclassifiedItem).is(notPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(rootUnclassifiedItem).is(notPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));
		assertThat(childUnclassifiedItem).is(logicallyDeletableBy(userWithDeletePermission));
		assertThat(childUnclassifiedItem).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(childUnclassifiedItem).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission,
				withMostReferencesRemoved));
		assertThat(childUnclassifiedItem).is(notPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(childUnclassifiedItem).is(notPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));

		when(userWithDeletePermission).logicallyDelete(rootUnclassifiedItem);
		when(userWithDeletePermission).physicallyDelete(rootUnclassifiedItem);

		assertThat(rootUnclassifiedItem).is(physicallyDeleted());
		assertThat(childUnclassifiedItem).is(physicallyDeleted());

	}

	@Test
	public void givenUserDoesntHaveFullDeleteAccessOnASecuredUnclassifiedRecordThenCannotLogicallyDelete()
			throws Exception {
		assertThat(rootUnclassifiedItem).is(notLogicallyDeletableBy(bob));
		assertThat(rootUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(rootUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(bob, withMostReferencesRemoved));
		assertThat(rootUnclassifiedItem).is(notPhysicallyDeletableBy(bob));
		assertThat(rootUnclassifiedItem).is(notPhysicallyDeletableBy(bob, withMostReferencesRemoved));

		assertThat(childUnclassifiedItem).is(notLogicallyDeletableBy(bob));
		assertThat(childUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(childUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(bob, withMostReferencesRemoved));
		assertThat(childUnclassifiedItem).is(notPhysicallyDeletableBy(bob));
		assertThat(childUnclassifiedItem).is(notPhysicallyDeletableBy(bob, withMostReferencesRemoved));

	}

	@Test
	public void givenUserDoesntHaveFullDeleteAccessOnASecuredUnclassifiedLogicallyDeletedRecordThenCannotPhysicallyDeleteOrRestoreIt()
			throws Exception {

		given(userWithDeletePermission).logicallyDelete(rootUnclassifiedItem);

		assertThat(rootUnclassifiedItem).is(notLogicallyDeletableBy(bob));
		assertThat(rootUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(rootUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(bob, withMostReferencesRemoved));
		assertThat(rootUnclassifiedItem).is(notPhysicallyDeletableBy(bob));
		assertThat(rootUnclassifiedItem).is(notPhysicallyDeletableBy(bob, withMostReferencesRemoved));

		assertThat(childUnclassifiedItem).is(notLogicallyDeletableBy(bob));
		assertThat(childUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(childUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(bob, withMostReferencesRemoved));
		assertThat(childUnclassifiedItem).is(notPhysicallyDeletableBy(bob));
		assertThat(childUnclassifiedItem).is(notPhysicallyDeletableBy(bob, withMostReferencesRemoved));

	}

	@Test
	public void givenUserHasFullDeleteAccessOnASecuredUnclassifiedRecordWhichAreReferencedThenCanDeleteLogicallyButNotPhysically()
			throws Exception {
		given(records.folder3()).hasAReferenceToUnclassifiedSecuredRecord(childUnclassifiedItem);

		assertThat(rootUnclassifiedItem).is(logicallyDeletableBy(userWithDeletePermission));
		assertThat(rootUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(rootUnclassifiedItem)
				.is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));
		assertThat(rootUnclassifiedItem).is(notPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(rootUnclassifiedItem).is(notPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));

		assertThat(childUnclassifiedItem).is(logicallyDeletableBy(userWithDeletePermission));
		assertThat(childUnclassifiedItem).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(childUnclassifiedItem).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission,
				withMostReferencesRemoved));
		assertThat(childUnclassifiedItem).is(notPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(childUnclassifiedItem).is(notPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));

	}

	@Test
	public void givenUserHasDeletePermissionToActiveRecordAndItsHierarchyThenCanDeleteLogicallyButNotPhysically()
			throws Exception {
		given(bob).hasDeletePermissionOn(records.folder4());

		assertThat(records.folder4()).is(logicallyDeletableBy(bob));
		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.folder4()).is(notPhysicallyDeletableBy(bob));
	}

	@Test
	public void givenGodUserThenCanDeleteLogicallyButNotPhysically()
			throws Exception {
		assertThat(records.folder4()).is(logicallyDeletableBy(User.GOD));
		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(User.GOD));
		assertThat(records.folder4()).is(notPhysicallyDeletableBy(User.GOD));
	}

	@Test
	public void givenUserHasDeletePermissionToActiveRecordAndItsHierarchyWhenDeletingLogicallyThenAllHierarchyLogicallyDeleted()
			throws Exception {
		given(bob).hasDeletePermissionOn(records.folder4());

		when(bob).logicallyDelete(records.folder4());

		assertThat(records.inFolder4Hierarchy()).are(logicallyDeleted());
	}

	@Test
	public void givenGodUserWhenDeletingLogicallyThenAllHierarchyLogicallyDeleted()
			throws Exception {
		when(User.GOD).logicallyDelete(records.folder4());

		assertThat(records.inFolder4Hierarchy()).are(logicallyDeleted());
	}

	@Test
	public void givenUserHasDeletePermissionToActiveRecordButNotToAnElementInItsHierarchyThenCannotDeleteItLogically()
			throws Exception {
		given(bob).hasDeletePermissionOn(records.folder4());
		given(bob).hasRemovedDeletePermissionOn(records.folder4_2_doc1());

		assertThat(records.folder4()).is(notLogicallyDeletableBy(bob));
		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(bob));
	}

	@Test
	public void givenARecordWithoutPathThenAUserWithCollectionDeleteAccessCanLogicallyDeleteIt()
			throws Exception {
		Record folder = recordServices.newRecordWithSchema(folderSchema.instance());
		recordServices.add(folder.set(TITLE, "title"));

		when(userWithDeletePermission).logicallyDelete(folder);

		assertThat(folder).is(logicallyDeleted());

	}

	@Test
	public void givenARecordWithoutPathThenATypicalUserCannotLogicallyDeleteIt()
			throws Exception {
		Record folder = recordServices.newRecordWithSchema(folderSchema.instance());
		recordServices.add(folder.set(TITLE, "title"));

		assertThat(records.folder4()).is(notLogicallyDeletableBy(bob));
		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(bob));

	}

	@Test
	public void givenALogicallyDeletedRecordWithoutPathThenAUserWithCollectionDeleteAccessCanRestoreIt()
			throws Exception {
		Record folder = recordServices.newRecordWithSchema(folderSchema.instance());
		recordServices.add(folder.set(TITLE, "title"));
		given(userWithDeletePermission).logicallyDelete(folder);

		when(userWithDeletePermission).restore(folder);

		assertThat(folder).isNot(logicallyDeleted());

	}

	@Test
	public void givenALogicallyDeletedRecordWithoutPathThenATypicalUserCannotRestoreIt()
			throws Exception {
		Record folder = recordServices.newRecordWithSchema(folderSchema.instance());
		recordServices.add(folder.set(TITLE, "title"));
		given(userWithDeletePermission).logicallyDelete(folder);

		assertThat(records.folder4()).is(notRestorableBy(bob));

	}

	@Test
	public void givenALogicallyDeletedRecordWithoutPathThenAUserWithCollectionDeleteAccessCanPhysicallyDeleteIt()
			throws Exception {
		Record folder = recordServices.newRecordWithSchema(folderSchema.instance());
		recordServices.add(folder.set(TITLE, "title"));
		assertThat(folder).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(folder);

		when(userWithDeletePermission).physicallyDelete(folder);

		assertThat(folder).is(physicallyDeleted());

	}

	@Test
	public void givenALogicallyDeletedRecordWithoutPathThenATypicalUserCannotPhysicallyDeleteIt()
			throws Exception {
		Record folder = recordServices.newRecordWithSchema(folderSchema.instance());
		recordServices.add(folder.set(TITLE, "title"));
		given(userWithDeletePermission).logicallyDelete(folder);

		assertThat(records.folder4()).is(notPhysicallyDeletableBy(bob));

	}

	@Test
	public void givenARecordIsReferencedInAnotherRecordWhenAUserDeleteTheRecordThenWholeHierarchyLogicallyDeleted()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4());

		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));
		when(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.inFolder4Hierarchy()).are(logicallyDeleted());

	}

	@Test
	public void givenARecordIsReferencedInAnotherRecordWhenGodUserDeleteTheRecordThenWholeHierarchyLogicallyDeleted()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4());

		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		when(User.GOD).logicallyDelete(records.folder4());

		assertThat(records.inFolder4Hierarchy()).are(logicallyDeleted());

	}

	@Test
	public void givenARecordIsReferencedInAnotherRecordWhenAUserDeleteTheRecordParentThenWholeHierarchyLogicallyDeleted()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());

		when(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.inFolder4Hierarchy()).are(logicallyDeleted());
	}

	@Test
	public void givenARecordIsReferencedInAnotherRecordWhenGodUserDeleteTheRecordParentThenWholeHierarchyLogicallyDeleted()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());

		when(User.GOD).logicallyDelete(records.folder4());

		assertThat(records.inFolder4Hierarchy()).are(logicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordWhenRestoringItThenAllItsHierarchyRestored()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(userWithDeletePermission).restore(records.folder4());

		assertThat(records.inFolder4Hierarchy()).areNot(logicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordWhenRestoringItWithGodUserThenAllItsHierarchyRestored()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(User.GOD).restore(records.folder4());

		assertThat(records.inFolder4Hierarchy()).areNot(logicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordThenASubRecordInItsHierarchyIsNotRestorableAlone()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4_2()).is(notRestorableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordThenASubRecordInItsHierarchyIsNotRestorableAloneEvenWithGodUser()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4_2()).is(notRestorableBy(User.GOD));
	}

	@Test
	public void givenARecordIsDeletedLogicallyAndASubRecordRestoredWhenRestoringTheRecordThenAllRestored()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(userWithDeletePermission).restore(records.folder4());

		assertThat(records.inFolder4Hierarchy()).areNot(logicallyDeleted());

	}

	@Test
	public void givenALogicallyDeletedRecordAndAUserWithoutSufficientDeletePermissionThenCannotRestoreIt()
			throws Exception {
		given(bob).hasDeletePermissionOn(records.folder4());
		given(bob).hasRemovedDeletePermissionOn(records.folder4_2_doc1());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(restorableBy(userWithDeletePermission));
		assertThat(records.folder4()).is(notRestorableBy(bob));
	}

	@Test
	public void givenALogicallyDeletedRecordWhenAUserWithDeletePermissionPhysicallyDeleteItThenAllDeleted()
			throws Exception {
		List<Record> recordsInFolder4Hierarchy = records.inFolder4Hierarchy();
		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(userWithDeletePermission).physicallyDelete(records.folder4());

		assertThat(recordsInFolder4Hierarchy).are(physicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordWhenGodUserWithDeletePermissionPhysicallyDeleteItThenAllDeleted()
			throws Exception {
		List<Record> recordsInFolder4Hierarchy = records.inFolder4Hierarchy();
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(User.GOD).physicallyDelete(records.folder4());

		assertThat(recordsInFolder4Hierarchy).are(physicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordAndAUserWithDeletePermissionToTheRecordButNotToASubRecordThenCannotPhysicallyDeleteIt()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());
		given(bob).hasDeletePermissionOn(records.folder4());
		given(bob).hasRemovedDeletePermissionOn(records.folder4_2());

		assertThat(records.folder4()).is(notPhysicallyDeletableBy(bob));
		assertThat(records.folder4()).is(notPhysicallyDeletableBy(bob, withMostReferencesRemoved));
		assertThat(records.inFolder4Hierarchy()).areNot(physicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordThenDeletedAndNotReferenceable()
			throws Exception {

		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(logicallyDeleted());
		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4()).isNot(referencable());
	}

	@Test
	public void givenALogicallyDeletedRecordWhenUpdatingThenStillDeletedAndNotReferenceable()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		given(bob).modify(records.folder4());

		assertThat(records.folder4()).is(logicallyDeleted());
		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4()).isNot(referencable());
	}

	@Test
	public void givenALogicallyDeletedRecordThenAUserWithoutDeletePermissionCannotPhysicallyDeleteIt()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(logicallyDeleted());
		assertThat(records.folder4()).is(notPhysicallyDeletableBy(bob));
		assertThat(records.inFolder4Hierarchy()).areNot(physicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordAndAUserWithoutDeletePermissionThenCannotPhysicallyDeleteIt()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(notPhysicallyDeletableBy(bob));
		assertThat(records.inFolder4Hierarchy()).areNot(physicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordOfSchemaWithoutSecurityAndAUserWithoutDeletePermissionThenCanPhysicallyDeleteIt()
			throws Exception {

		List<Record> recordsInFolder4Hierarchy = records.inFolder4Hierarchy();
		givenSecurityDisabledInFolderSchemaType();
		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(physicallyDeletableBy(bob));
		when(bob).physicallyDelete(records.folder4());

		assertThat(recordsInFolder4Hierarchy).are(physicallyDeleted());
	}

	@Test
	public void givenRecordOfSchemaWithoutSecurityAndAUserWithoutDeletePermissionThenCanPhysicallyDeleteIt()
			throws Exception {

		givenSecurityDisabledInFolderSchemaType();

		assertThat(records.folder4()).is(logicallyDeletableBy(bob));
		when(bob).logicallyDelete(records.folder4());

		assertThat(records.inFolder4Hierarchy()).are(logicallyDeleted());
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByAnotherRecordThenCannotPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());

		assertThat(records.folder4_2()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4_2()).is(notPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordInHierarchyIsReferencedByMultivalueMetadataAnotherRecordThenCannotPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());

		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(notPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordInHierarchyIsReferencedBySingleValueMetadataAnotherRecordThenCannotPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasASingleValueReferenceTo(records.folder4_2());

		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(notPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByMultivalueMetadataInAnotherRecordAndTheReferenceIsRemovedThenCanPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasAReferenceTo();

		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(physicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedBySinglevalueMetadataInAnotherRecordAndTheReferenceIsRemovedThenCanPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasASingleValueReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasASingleValueReferenceTo(null);

		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(physicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByMultivalueMetadataInAnotherRecordAndTheReferenceIsRemovedAndRecordRestoredThenCanLogicallyThenPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasAReferenceTo();
		given(userWithDeletePermission).restore(records.folder4());

		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedBySinglevalueMetadataInAnotherRecordAndTheReferenceIsRemovedAndRecordRestoredThenCanLogicallyThenPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasASingleValueReferenceTo(records.folder4_2());
		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasASingleValueReferenceTo(null);
		given(userWithDeletePermission).restore(records.folder4());

		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByAnotherRecordAndTheReferenceIsReplacedThenCanPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasAReferenceTo(records.folder3());

		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(physicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByAnotherRecordAndTheReferenceIsReplacedAndRecordRestoredThenCanLogicallyThenPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasAReferenceTo(records.folder3());
		given(userWithDeletePermission).restore(records.folder4());

		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByAnotherRecordAndANewReferenceIsAddedKeepingThePreviousThenCannotPhysicallyDeleteIt()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasAReferenceTo(records.folder4_2(), records.folder3());

		assertThat(records.folder4()).is(notPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(notPhysicallyDeletableBy(userWithDeletePermission));

		given(userWithDeletePermission).restore(records.folder4());
		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByMultivalueMetadataInAnotherRecordAndIsMovedInAnOtherFolderThenPreviousParentFolderIsNowPhysicallyDeletable()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder4_2()).hasNewParent(records.folder3());

		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(notPhysicallyDeletableBy(userWithDeletePermission));

		when(userWithDeletePermission).logicallyDelete(records.folder3());
		assertThat(records.folder3()).is(notPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedBySingleValueMetadataInAnotherRecordAndIsMovedInAnOtherFolderThenPreviousParentFolderIsNowPhysicallyDeletable()
			throws Exception {
		given(records.folder2()).hasASingleValueReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder4_2()).hasNewParent(records.folder3());

		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4_2()).is(notPhysicallyDeletableBy(userWithDeletePermission));

		when(userWithDeletePermission).logicallyDelete(records.folder3());
		assertThat(records.folder3()).is(notPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByThreeRecordsWhenAllReferencingRecordsDeletedThenRecordIsPhysicallyDeletable()
			throws Exception {
		given(records.folder1()).hasAReferenceTo(records.folder4());
		given(records.folder2()).hasAReferenceTo(records.folder4());
		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());
		assertThat(records.folder4()).is(notPhysicallyDeletableBy(userWithDeletePermission));

		when(userWithDeletePermission).logicallyDelete(records.folder1());
		when(userWithDeletePermission).physicallyDelete(records.folder1());
		assertThat(records.folder4()).is(notPhysicallyDeletableBy(userWithDeletePermission));

		when(userWithDeletePermission).logicallyDelete(records.folder2());
		when(userWithDeletePermission).physicallyDelete(records.folder2());

		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));

		given(userWithDeletePermission).restore(records.folder4());
		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void givenALogicallyDeletedRecordIsReferencedByThreeRecordsWhenPhysicallyDeletingWithRefRemovedThenRefRemoved()
			throws Exception {
		given(records.folder1()).hasAReferenceTo(records.folder4());
		given(records.folder2()).hasAReferenceTo(records.folder4());
		Record folder4 = records.folder4();
		assertThat(records.folder4()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission, withMostReferencesRemoved));
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(userWithDeletePermission).physicallyDelete(records.folder4(), withMostReferencesRemoved);
		assertThat(folder4).is(physicallyDeleted());
		assertThat(records.folder1().getList(folderSchema.linkToOtherFolders())).isEmpty();
		assertThat(records.folder2().getList(folderSchema.linkToOtherFolders())).isEmpty();

	}

	@Test
	public void givenRecordIsReferencedBySiblingRecordThenParentRecordIsPhysicallyDeletable()
			throws Exception {
		given(records.folder4_1()).hasAReferenceTo(records.folder4_2());
		assertThat(records.folder4()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.folder4());
		assertThat(records.folder4()).is(physicallyDeletableBy(userWithDeletePermission));
	}

	@Test
	public void whenAPrincipalConceptIsLogicallyDeletedIncludingRecordsByGodThenAllConceptsSubConceptsAndRecordsLogicallyDeleted()
			throws Exception {
		when(User.GOD).logicallyDelete(records.taxo1_category2(), logicallyDeletingRecords);

		assertThat(records.taxo1_category2()).is(logicallyDeleted());
		assertThat(records.taxo1_category2_1()).is(logicallyDeleted());
		assertThat(records.folder3()).is(logicallyDeleted());
		assertThat(records.inFolder4Hierarchy()).are(logicallyDeleted());

	}

	@Test
	public void givenAUserHasNoDeleteAccessOnCategoriesThenCannotLogicallyDeleteIt()
			throws Exception {

		assertThat(records.taxo1_category2()).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.taxo1_category2_1()).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.taxo1_category2()).is(notLogicallyDeletableExcludingRecordsBy(bob));
		assertThat(records.taxo1_category2_1()).is(notLogicallyDeletableExcludingRecordsBy(bob));

	}

	@Test
	public void givenAUserHasDeleteAccessOnCategoriesAndSubCategoriesButNotOnRecordsThenCanOnlyDeletePrincipalConceptExcludingRecords()
			throws Exception {
		given(bob).hasDeletePermissionOn(records.taxo1_category2());
		given(bob).hasRemovedDeletePermissionOn(records.folder3());

		assertThat(records.taxo1_category2()).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.taxo1_category2_1()).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.taxo1_category2()).is(logicallyDeletableExcludingRecordsBy(bob));
		assertThat(records.taxo1_category2_1()).is(logicallyDeletableExcludingRecordsBy(bob));

	}

	@Test
	public void givenLogicallyDeletedPrincipalConceptExcludingRecordsThenEvenGodUserCannotPhysicallyDeleteThePrincipalConcept()
			throws Exception {
		given(User.GOD).logicallyDelete(records.taxo1_category2());

		assertThat(records.taxo1_category2()).is(notPhysicallyDeletableBy(User.GOD));
		assertThat(records.taxo1_category2_1()).is(notPhysicallyDeletableBy(User.GOD));

	}

	@Test
	public void givenAUserHasDeleteAccessOnCategoriesSubCategoriesAndRecordsThenCanLogicallyDeletePrincipalConceptIncludingAndExcludingRecordsButNotPhysically()
			throws Exception {
		given(bob).hasDeletePermissionOn(records.taxo1_category2());

		assertThat(records.taxo1_category2()).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.taxo1_category2_1()).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.taxo1_category2()).is(logicallyDeletableExcludingRecordsBy(bob));
		assertThat(records.taxo1_category2_1()).is(logicallyDeletableExcludingRecordsBy(bob));
	}

	@Test
	public void givenAUserHasDeleteAccessOnACategoryButNotOnASubCategoryThenCannotLogicallyDeleteIt()
			throws Exception {
		given(bob).hasDeletePermissionOn(records.taxo1_category2());
		given(bob).hasRemovedDeletePermissionOn(records.taxo1_category2_1());

		assertThat(records.taxo1_category2()).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.taxo1_category2_1()).is(notLogicallyThenPhysicallyDeletableBy(bob));
		assertThat(records.taxo1_category2()).is(notLogicallyDeletableExcludingRecordsBy(bob));
		assertThat(records.taxo1_category2_1()).is(notLogicallyDeletableExcludingRecordsBy(bob));
	}

	@Test
	public void givenLogicallyDeletedPrincipalConceptAndAllItsHierarchyWhenRestoringThePrincipalConceptThenAllHierarchyRestored()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.taxo1_category2(), logicallyDeletingRecordsIfPrincipalTaxonomy);

		when(userWithDeletePermission).restore(records.taxo1_category2());

		assertThat(records.taxo1_category2()).isNot(logicallyDeleted());
		assertThat(records.taxo1_category2_1()).isNot(logicallyDeleted());
		assertThat(records.folder3()).isNot(logicallyDeleted());
		assertThat(records.inFolder4Hierarchy()).areNot(logicallyDeleted());
	}

	@Test
	public void givenLogicallyDeletedPrincipalConceptAndAllItsHierarchyThenAPrincipalSubConceptInALogicallyDeleteConceptIsNotRestorable()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.taxo1_category2(), logicallyDeletingRecordsIfPrincipalTaxonomy);

		assertThat(records.taxo1_category2_1()).is(notRestorableBy(userWithDeletePermission));
	}

	@Test
	public void givenLogicallyDeletedPrincipalConceptAndAllItsHierarchyThenCannotDeleteConceptUntilRecordsAreDeleted()
			throws Exception {
		Record taxo1_category2 = records.taxo1_category2();
		Record taxo1_category2_1 = records.taxo1_category2_1();
		Record folder3 = records.folder3();
		List<Record> recordsInFolder4Hierarchy = records.inFolder4Hierarchy();

		assertThat(records.taxo1_category2()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.taxo1_category2_1()).is(notLogicallyThenPhysicallyDeletableBy(userWithDeletePermission));

		when(userWithDeletePermission).logicallyDelete(records.folder3());
		when(userWithDeletePermission).logicallyDelete(records.folder4());
		when(userWithDeletePermission).physicallyDelete(records.folder3());
		when(userWithDeletePermission).physicallyDelete(records.folder4());
		assertThat(folder3).is(physicallyDeleted());
		assertThat(recordsInFolder4Hierarchy).are(physicallyDeleted());

		assertThat(records.taxo1_category2()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		assertThat(records.taxo1_category2_1()).is(logicallyThenPhysicallyDeletableBy(userWithDeletePermission));
		given(userWithDeletePermission).logicallyDelete(records.taxo1_category2(), logicallyDeletingRecordsIfPrincipalTaxonomy);

		when(userWithDeletePermission)
				.physicallyDelete(records.taxo1_category2(), withMostReferencesRemovedPhysicallyDeletingRecords);

		assertThat(taxo1_category2).is(physicallyDeleted());
		assertThat(taxo1_category2_1).is(physicallyDeleted());

	}

	@Test
	public void givenLogicallyDeletedPrincipalConceptAndAllItsHierarchyWhenDeletingAPrincipalSubConceptThenAllHierarchyIsPhysicallyDeletedExceptPrincipalRootConcept()
			throws Exception {

		Record taxo1_category2 = records.taxo1_category2();
		Record taxo1_category2_1 = records.taxo1_category2_1();
		Record folder3 = records.folder3();
		List<Record> recordsInFolder4Hierarchy = records.inFolder4Hierarchy();

		given(userWithDeletePermission).logicallyDelete(records.taxo1_category2(), logicallyDeletingRecordsIfPrincipalTaxonomy);

		when(userWithDeletePermission).physicallyDelete(records.taxo1_category2_1(),
				withMostReferencesRemovedPhysicallyDeletingRecords);

		assertThat(taxo1_category2).isNot(physicallyDeleted());
		assertThat(taxo1_category2_1).is(physicallyDeleted());
		assertThat(folder3).is(physicallyDeleted());
		assertThat(recordsInFolder4Hierarchy).areNot(physicallyDeleted());
	}

	@Test
	public void whenAPrincipalConceptIsLogicallyDeletedExcludingRecordsThenOnlyConceptsSubConceptsDeleted()
			throws Exception {
		when(userWithDeletePermission).logicallyDelete(records.taxo1_category2(), keepingRecords);

		assertThat(records.taxo1_category2()).is(logicallyDeleted());
		assertThat(records.taxo1_category2_1()).is(logicallyDeleted());
		assertThat(records.folder3()).isNot(logicallyDeleted());
		assertThat(records.inFolder4Hierarchy()).areNot(logicallyDeleted());
	}

	@Test
	public void whenLogicallyDeletingANonPrincipalConceptIncludingRecordsIfPrincipalThenOnlyLogicallyDeleteConceptsInHierarchy()
			throws Exception {

		when(userWithDeletePermission).logicallyDelete(records.taxo2_unit1(), logicallyDeletingRecordsIfPrincipalTaxonomy);

		assertThat(records.taxo2_unit1()).is(logicallyDeleted());
		assertThat(records.taxo2_unit1_1()).is(logicallyDeleted());
		assertThat(records.folder1()).isNot(logicallyDeleted());
		assertThat(records.folder1_doc1()).isNot(logicallyDeleted());
		assertThat(records.folder2()).isNot(logicallyDeleted());

	}

	@Test
	public void whenLogicallyDeletingANonPrincipalConceptIncludingRecordsThenAllLogicallyDeleted()
			throws Exception {

		when(userWithDeletePermission).logicallyDelete(records.taxo2_unit1(), logicallyDeletingRecords);

		assertThat(records.taxo2_unit1()).is(logicallyDeleted());
		assertThat(records.taxo2_unit1_1()).is(logicallyDeleted());
		assertThat(records.folder1()).is(logicallyDeleted());
		assertThat(records.folder1_doc1()).is(logicallyDeleted());
		assertThat(records.folder2()).is(logicallyDeleted());

	}

	@Test
	public void givenANonPrincipalConceptAndAllHisRecordsAreLogicallyDeletingWhenPhysicallyDeleteRecordsThenOnlyDeleteConceptsInHierarchy()
			throws Exception {

		Record taxo2_unit1 = records.taxo2_unit1();
		Record taxo2_unit1_1 = records.taxo2_unit1_1();
		Record folder1 = records.folder1();
		Record folder1_doc1 = records.folder1_doc1();
		Record folder2 = records.folder2();

		when(userWithDeletePermission).logicallyDelete(records.taxo2_unit1());
		when(userWithDeletePermission).logicallyDelete(records.folder1());
		when(userWithDeletePermission).logicallyDelete(records.folder2());
		when(userWithDeletePermission).logicallyDelete(records.folder3());
		when(userWithDeletePermission).logicallyDelete(records.folder4());
		when(userWithDeletePermission).logicallyDelete(records.folder5());

		when(userWithDeletePermission)
				.physicallyDelete(records.taxo2_unit1(), withMostReferencesRemovedPhysicallyDeletingRecords);

		assertThat(taxo2_unit1).is(physicallyDeleted());
		assertThat(taxo2_unit1_1).is(physicallyDeleted());
		assertThat(folder1).is(physicallyDeleted());
		assertThat(folder1_doc1).is(physicallyDeleted());
		assertThat(folder2).is(physicallyDeleted());

	}

	@Test
	public void givenANonPrincipalConceptAndAllHisRecordsAreLogicallyDeletingWhenPhysicallyDeleteRecordsIfPrincipalTaxonomyThenOnlyDeleteConceptsInHierarchy()
			throws Exception {

		Record taxo2_unit1 = records.taxo2_unit1();
		Record taxo2_unit1_1 = records.taxo2_unit1_1();

		when(userWithDeletePermission).logicallyDelete(records.taxo2_unit1());
		when(userWithDeletePermission).logicallyDelete(records.folder1());
		when(userWithDeletePermission).logicallyDelete(records.folder2());
		when(userWithDeletePermission).logicallyDelete(records.folder3());
		when(userWithDeletePermission).logicallyDelete(records.folder4());
		when(userWithDeletePermission).logicallyDelete(records.folder5());

		when(userWithDeletePermission)
				.physicallyDelete(records.taxo2_unit1(), withMostReferencesRemovedPhysicallyDeletingRecordsIfPrincipalTaxonomy);

		assertThat(taxo2_unit1).is(physicallyDeleted());
		assertThat(taxo2_unit1_1).is(physicallyDeleted());
		assertThat(records.folder1()).isNot(physicallyDeleted());
		assertThat(records.folder1_doc1()).isNot(physicallyDeleted());
		assertThat(records.folder2()).isNot(physicallyDeleted());

	}

	@Test
	public void givenANonPrincipalConceptIsLogicallyDeletingWhenPhysicallyDeleteThenOnlyDeleteConceptsInHierarchy()
			throws Exception {

		Record taxo2_unit1 = records.taxo2_unit1();
		Record taxo2_unit1_1 = records.taxo2_unit1_1();
		given(userWithDeletePermission).logicallyDelete(records.taxo2_unit1());

		when(userWithDeletePermission).physicallyDelete(records.taxo2_unit1(), withMostReferencesRemoved);

		assertThat(taxo2_unit1).is(physicallyDeleted());
		assertThat(taxo2_unit1_1).is(physicallyDeleted());
		assertThat(records.folder1()).isNot(logicallyDeleted());
		assertThat(records.folder1()).isNot(physicallyDeleted());
		assertThat(records.folder1_doc1()).isNot(logicallyDeleted());
		assertThat(records.folder1_doc1()).isNot(physicallyDeleted());
		assertThat(records.folder2()).isNot(logicallyDeleted());
		assertThat(records.folder2()).isNot(physicallyDeleted());

	}

	@Test
	public void givenARecordReferencingALogicallyDeletedRecordWhenAddingASecondReferenceThenSecondReferenceAdded()
			throws Exception {
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasAReferenceTo(records.folder4_2(), records.folder3());

		recordServices.refresh(records.folder2());
		assertThat((List) records.folder2().get(folderSchema.linkToOtherFolders())).hasSize(2);
	}

	@Test(expected = Exception.class)
	public void givenARecordWhenAddingAReferenceToALogicallyDeletedRecordThenException()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasAReferenceTo(records.folder4());

	}

	@Test(expected = Exception.class)
	public void givenARecordWhenAddingAReferenceToALogicallyDeletedSubRecordThenException()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		when(records.folder2()).hasAReferenceTo(records.folder4_2());

	}

	@Test
	public void givenARecordWhenAddingAReferenceToARestoredLogicallyDeletedRecordThenOK()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());
		given(userWithDeletePermission).restore(records.folder4());

		when(records.folder2()).hasAReferenceTo(records.folder4());

	}

	@Test
	public void givenARecordWhenAddingAReferenceToARestoredLogicallyDeletedSubRecordThenOK()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder4());
		given(userWithDeletePermission).restore(records.folder4());

		when(records.folder2()).hasAReferenceTo(records.folder4_2());

	}

	@Test
	public void givenARecordWithoutReferencesWhenGetListOfReferencesThenEmpty()
			throws Exception {

		assertThat(records.folder4()).is(notReferenced());
		assertThat(records.folder4()).is(seenByUserToBeReferencedByNoRecords(userWithDeletePermission));

	}

	@Test
	public void whenLogicallyDeletingANonPrincipalConceptThenOnlyLogicallyDeleteConceptsInHierarchy()
			throws Exception {

		when(userWithDeletePermission).logicallyDelete(records.taxo2_unit1());

		assertThat(records.taxo2_unit1()).is(logicallyDeleted());
		assertThat(records.taxo2_unit1_1()).is(logicallyDeleted());
		assertThat(records.folder1()).isNot(logicallyDeleted());
		assertThat(records.folder2()).isNot(logicallyDeleted());

	}

	@Test
	public void givenARecordWith2ReferencesWhenGetListOfReferencesThen2Records()
			throws Exception {

		given(records.folder3()).hasAReferenceTo(records.folder4());
		given(records.folder3()).hasAReferenceTo(records.folder4_2());
		given(records.folder2()).hasAReferenceTo(records.folder4_2());

		assertThat(records.folder4()).is(referenced());
		assertThat(records.folder4())
				.is(seenByUserToBeReferencedByRecords(userWithDeletePermission, records.folder2(), records.folder3()));

	}

	@Test
	public void givenARecordWith2ReferencesWhenGetListOfReferencesWithUserOnlySeeing1RecordThen1Record()
			throws Exception {

		given(records.folder3()).hasAReferenceTo(records.folder4());
		given(records.folder3()).hasAReferenceTo(records.folder4_2());
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(bob).hasReadPermissionOn(records.folder3());

		assertThat(records.folder4()).is(referenced());
		assertThat(records.folder4()).is(seenByUserToBeReferencedByRecords(bob, records.folder3()));

	}

	@Test
	public void givenARecordWith2ReferencesWhenGetListOfReferencesWithUserSeeingNoRecordThen0RecordButStillConsideredAsReferenced()
			throws Exception {

		given(records.folder3()).hasAReferenceTo(records.folder4());
		given(records.folder3()).hasAReferenceTo(records.folder4_2());
		given(records.folder2()).hasAReferenceTo(records.folder4_2());

		assertThat(records.folder4()).is(referenced());
		assertThat(records.folder4()).is(seenByUserToBeReferencedByNoRecords(bob));

	}

	@Test
	public void givenALogicallyDeletedRecordWithoutReferencesWhenGetListOfReferencesThenEmpty()
			throws Exception {

		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(notReferenced());
		assertThat(records.folder4()).is(seenByUserToBeReferencedByNoRecords(userWithDeletePermission));

	}

	@Test
	public void givenALogicallyDeletedRecordWith2ReferencesWhenGetListOfReferencesThen2Records()
			throws Exception {

		given(records.folder3()).hasAReferenceTo(records.folder4());
		given(records.folder3()).hasAReferenceTo(records.folder4_2());
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		recordServices.refresh(records.folder4());
		recordServices.isReferencedByOtherRecords(records.folder4());

		assertThat(records.folder4()).is(referenced());
		assertThat(records.folder4())
				.is(seenByUserToBeReferencedByRecords(userWithDeletePermission, records.folder2(), records.folder3()));

	}

	@Test
	public void givenALogicallyDeletedRecordWith2ReferencesWhenGetListOfReferencesWithUserOnlySeeing1RecordThen1Record()
			throws Exception {

		given(records.folder3()).hasAReferenceTo(records.folder4());
		given(records.folder3()).hasAReferenceTo(records.folder4_2());
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(bob).hasReadPermissionOn(records.folder3());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(referenced());
		assertThat(records.folder4()).is(seenByUserToBeReferencedByRecords(bob, records.folder3()));

	}

	@BenchmarkHistoryChart(maxRuns = 20, labelWith = LabelType.RUN_ID)
	@Test
	public void givenALogicallyDeletedRecordWith2ReferencesWhenGetListOfReferencesWithUserSeeingNoRecordThen0RecordButStillConsideredAsReferenced()
			throws Exception {

		given(records.folder3()).hasAReferenceTo(records.folder4());
		given(records.folder3()).hasAReferenceTo(records.folder4_2());
		given(records.folder2()).hasAReferenceTo(records.folder4_2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		assertThat(records.folder4()).is(referenced());
		assertThat(records.folder4()).is(seenByUserToBeReferencedByRecords(bob));

	}

	@Test
	public void givenActiveRestoredAndLogicallyDeletedRecordsWhenSearchingRecordsUsingTheStatusFilterThenObtainCorrectResults()
			throws Exception {
		given(userWithDeletePermission).logicallyDelete(records.folder2());
		given(userWithDeletePermission).restore(records.folder2());
		given(userWithDeletePermission).logicallyDelete(records.folder4());

		// Search only root folders (Folder1, 2, 3 and 4)
		LogicalSearchQuery query = new LogicalSearchQuery(LogicalSearchQueryOperators.from(folderSchema.instance())
				.where(folderSchema.taxonomy1()).isNotNull());

		assertThat(searchServices.searchRecordIds(query))
				.containsOnly(idsArray(records.folder1(), records.folder2(), records.folder3(), records.folder4()));
		assertThat(searchServices.searchRecordIds(query.filteredByStatus(StatusFilter.ALL))).containsOnly(
				idsArray(records.folder1(), records.folder2(), records.folder3(), records.folder4()));
		assertThat(searchServices.searchRecordIds(query.filteredByStatus(StatusFilter.DELETED)))
				.containsOnly(idsArray(records.folder4()));
		assertThat(searchServices.searchRecordIds(query.filteredByStatus(StatusFilter.ACTIVES))).containsOnly(
				idsArray(records.folder1(), records.folder2(), records.folder3()));
	}

	@Test(expected = RecordDeleteServicesRuntimeException_CannotTotallyDeleteSchemaType.class)
	public void givenTypeNotSupportingTotalDeleteWhenTotallyDeletingRecordsThenExceptionThrown()
			throws Exception {

		MetadataSchemaType typeNotTotallyDeletable = schemas.get("valueList");
		recordDeleteServices.totallyDeleteSchemaTypeRecords(typeNotTotallyDeletable);
	}

	@Test
	public void givenTypeSupportingTotalDeleteWhenTotallyDeletingRecordsThenAllDeleted()
			throws Exception {
		MetadataSchemaType typeTotallyDeletable = schemas.get("typeSupportingRawDelete");
		Record record1 = new TestRecord(typeTotallyDeletable.getDefaultSchema(), "totalRecord1").set(TITLE, "1");
		Record record2 = new TestRecord(typeTotallyDeletable.getDefaultSchema(), "totalRecord2").set(TITLE, "2");

		recordServices.execute(new Transaction(record1, record2));

		assertThatRecord(record1).isNot(physicallyDeleted());
		assertThatRecord(record2).isNot(physicallyDeleted());
		assertThatRecord(records.folder2()).isNot(physicallyDeleted());

		recordDeleteServices.totallyDeleteSchemaTypeRecords(typeTotallyDeletable);

		assertThatRecord(record1).is(physicallyDeleted());
		assertThatRecord(record2).is(physicallyDeleted());
		assertThatRecord(records.folder2()).isNot(physicallyDeleted());
	}

	@Test
	public void givenInvalidRecordTryingToDeleteItWithOptions() {
		try {
			final MetadataSchemaType testRecordSchema = schemas.get("typeSupportingRawDelete");
			final Record record = new TestRecord(testRecordSchema.getDefaultSchema(), "testRecord").set(TITLE, "test");
			Transaction transaction = new Transaction();
			transaction.add(record);
			recordServices.execute(transaction);

			getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {

				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					types.getDefaultSchema(record.getTypeCode()).get(Schemas.TITLE).addValidator(InvalidMetadataValidator.class);
				}
			});

			recordServices.logicallyDelete(record, User.GOD, new RecordLogicalDeleteOptions().disableValidatorChecking());
		} catch (Exception e) {
			fail("Should not throw any exception");
		}
	}

	// -------------------------------------------------------------

	private Condition<? super Record> logicallyDeletableBy(final User user) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				return recordServices.validateLogicallyDeletable(record, user).isEmpty();
			}
		}.describedAs("logically deletable by " + user);
	}

	private Condition<? super Record> logicallyThenPhysicallyDeletableBy(final User user) {
		return logicallyThenPhysicallyDeletableBy(user, new RecordPhysicalDeleteOptions());
	}

	private Condition<? super Record> logicallyThenPhysicallyDeletableBy(final User user,
																		 final RecordPhysicalDeleteOptions options) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				return recordServices.validateLogicallyThenPhysicallyDeletable(record, user, options).isEmpty();
			}
		}.describedAs("logically then physically deletable by " + user);
	}

	private Condition<? super Record> notLogicallyThenPhysicallyDeletableBy(final User user) {
		return notLogicallyThenPhysicallyDeletableBy(user, new RecordPhysicalDeleteOptions());

	}

	private Condition<? super Record> notLogicallyThenPhysicallyDeletableBy(final User user,
																			final RecordPhysicalDeleteOptions options) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				boolean deletable = recordServices.validateLogicallyThenPhysicallyDeletable(record, user).isEmpty();
				assertThat(deletable).describedAs("validateLogicallyThenPhysicallyDeletable").isFalse();

				boolean logicallyDeleted = false;
				try {
					recordServices.logicallyDelete(record, user);
					logicallyDeleted = true;
					recordServices.physicallyDelete(record, user, options);

					return false;
				} catch (Exception e) {
					//OK
					if (logicallyDeleted) {
						recordServices.restore(record, user);
					}

					return true;
				}

			}
		}.describedAs("not logically then physically deletable by " + user);
	}

	private Condition<? super Record> notLogicallyDeletableBy(final User user) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {

				if (recordServices.validateLogicallyDeletable(record, user).isEmpty()) {
					return false;
				} else {

					try {
						// It's not logically deletable... Fine. But let's try to deleteLogically it anyway
						recordServices.logicallyDelete(record, user);
						// An exception should be thrown, the condition fail
						return false;

					} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
						return true;
					}

				}
			}
		}.describedAs("not logically deletable by " + user);
	}

	private Condition<? super Record> logicallyDeletableExcludingRecordsBy(final User user) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				return recordServices.isPrincipalConceptLogicallyDeletableExcludingContent(record, user);
			}
		}.describedAs("principal concept excluding records logically deletable by " + user);
	}

	private Condition<? super Record> notLogicallyDeletableExcludingRecordsBy(final User user) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {

				if (recordServices.isPrincipalConceptLogicallyDeletableExcludingContent(record, user)) {
					return false;
				} else {

					try {
						// It's not logically deletable... Fine. But let's try to deleteLogically it anyway
						recordServices.logicallyDelete(record, user);
						// An exception should be thrown, the condition fail
						return false;

					} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
						return true;
					}

				}
			}
		}.describedAs("principal concept excluding records not logically deletable by " + user);
	}

	private Condition<? super Record> referencable() {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record value) {
				Record record = new TestRecord(folderSchema);
				record.set(folderSchema.linkToOtherFolders(), asList(value));
				try {
					recordServices.add(record);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		};
	}

	private Condition<? super Record> physicallyDeletableBy(final User user) {
		return physicallyDeletableBy(user, new RecordPhysicalDeleteOptions());
	}

	private Condition<? super Record> physicallyDeletableBy(final User user,
															final RecordPhysicalDeleteOptions options) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				return recordServices.validatePhysicallyDeletable(record, user, options).isEmpty();
			}
		}.describedAs("physically deletable by " + user);
	}

	private Condition<? super Record> notPhysicallyDeletableBy(final User user) {
		return notPhysicallyDeletableBy(user, new RecordPhysicalDeleteOptions());
	}

	private Condition<? super Record> notPhysicallyDeletableBy(final User user,
															   final RecordPhysicalDeleteOptions options) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {

				if (recordServices.validatePhysicallyDeletable(record, user, options).isEmpty()) {
					return false;
				} else {

					try {
						// It's not physically deletable... Fine. But let's try to deleteLogically it anyway

						recordServices.physicallyDelete(record, user);
						// An exception should be thrown, the condition fail
						return false;

					} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord e) {
						return true;
					}

				}
			}
		}.describedAs("not physically deletable by " + user);
	}

	private Condition<? super Record> restorableBy(final User user) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				return recordServices.isRestorable(record, user);
			}
		}.describedAs("restorable by " + user);
	}

	private Condition<? super Record> notRestorableBy(final User user) {

		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {

				if (recordServices.isRestorable(record, user)) {
					return false;
				} else {

					try {
						// It's not restorable... Fine. But let's try to restore it anyway

						recordServices.restore(record, user);
						// An exception should be thrown, the condition fail
						return false;

					} catch (RecordServicesRuntimeException_CannotRestoreRecord e) {
						return true;
					}

				}
			}
		}.describedAs("not restorable by " + user);
	}

	private Condition<? super Record> logicallyDeleted() {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				try {
					Record refreshedRecord = recordServices.getDocumentById(record.getId());
					return Boolean.TRUE.equals(refreshedRecord.get(Schemas.LOGICALLY_DELETED_STATUS));
				} catch (NoSuchRecordWithId e) {
					return false;
				}
			}
		}.describedAs("logicallyDeleted");
	}

	private Condition<? super Record> physicallyDeleted() {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				try {
					recordServices.getDocumentById(record.getId());
					return false;
				} catch (NoSuchRecordWithId e) {
					return true;
				}
			}
		}.describedAs("physicallyDeleted");
	}

	private UserPreparation given(User user) {
		return new UserPreparation(user);
	}

	private RecordPreparation given(Record record) {
		return new RecordPreparation(record);
	}

	private UserPreparation when(User user) {
		return new UserPreparation(user);
	}

	private RecordPreparation when(Record record) {
		return new RecordPreparation(record);
	}

	private Condition<? super Record> notReferenced() {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				recordServices.refresh(record);
				return !recordServices.isReferencedByOtherRecords(record);
			}
		}.describedAs("not referenced");
	}

	private Condition<? super Record> referenced() {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				recordServices.refresh(record);
				return recordServices.isReferencedByOtherRecords(record);
			}
		}.describedAs("referenced");
	}

	private Condition<? super Record> seenByUserToBeReferencedByNoRecords(User user) {
		return seenByUserToBeReferencedByRecords(user);
	}

	private Condition<? super Record> seenByUserToBeReferencedByRecords(final User user, final Record... records) {
		return new Condition<Record>() {
			@Override
			public boolean matches(Record record) {
				recordServices.refresh(record);

				List<String> foundReferences = recordsIds(recordServices.getVisibleRecordsWithReferenceTo(record, user));
				assertThat(foundReferences).containsOnly(idsArray(records));
				return true;
			}
		}.describedAs("seen by user to be referenced by records");
	}

	private void givenSecurityDisabledInFolderSchemaType() {
		MetadataSchemaTypesBuilder typesBuilder = getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection);

		typesBuilder.getSchemaType(folderSchema.type().getCode()).setSecurity(false);

		try {
			getModelLayerFactory().getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	private class UserPreparation {

		private User user;

		private UserPreparation(User user) {
			this.user = user;
		}

		public void physicallyDelete(Record record) {

			recordServices.physicallyDelete(record, user);
		}

		public void physicallyDelete(Record record, RecordPhysicalDeleteOptions options) {
			recordServices.physicallyDelete(record, user, options);
		}

		public void logicallyDelete(Record record) {
			recordServices.logicallyDelete(record, user);
		}

		public void logicallyDelete(Record record, RecordLogicalDeleteOptions options) {
			recordServices.logicallyDelete(record, user, options);
		}

		public void restore(Record record) {
			recordServices.restore(record, user);
		}

		public void hasDeletePermissionOn(Record record)
				throws InterruptedException {
			recordServices.refresh(record);
			authorizationsServices.add(authorizationForUsers(user).on(record).givingReadDeleteAccess());
			waitForBatchProcess();
		}

		public void hasReadPermissionOn(Record record)
				throws InterruptedException {
			recordServices.refresh(record);
			authorizationsServices.add(authorizationForUsers(user).on(record).givingReadAccess());
			waitForBatchProcess();
		}

		public void hasRemovedDeletePermissionOn(Record record)
				throws InterruptedException {
			recordServices.refresh(record);
			String authorizationDetailId = authorizationsServices.getRecordAuthorizations(record).get(0).getId();
			authorizationsServices.execute(modifyAuthorizationOnRecord(authorizationDetailId, record).removingItOnRecord());
			waitForBatchProcess();
		}

		public void modify(Record record) {
			recordServices.refresh(record);
			record.set(TITLE, record.get(TITLE) + " modified");
			try {
				recordServices.update(record);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class RecordPreparation {

		private Record record;

		private RecordPreparation(Record record) {
			this.record = record;
		}

		public void hasASingleValueListReferenceTo(Record anotherRecord) {

			record.set(folderSchema.instance().getMetadata("valueListSingleRef"), anotherRecord);
			try {
				recordServices.update(record);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}

		public void hasAValueListReferenceTo(Record... anotherRecords) {

			record.set(folderSchema.instance().getMetadata("valueListRef"), asList(anotherRecords));
			try {
				recordServices.update(record);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}

		public void hasAReferenceTo(Record... anotherRecords) {

			record.set(folderSchema.linkToOtherFolders(), asList(anotherRecords));
			try {
				recordServices.update(record);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}

		public void hasASingleValueReferenceTo(Record anotherRecord) {

			record.set(folderSchema.linkToOtherFolder(), anotherRecord);
			try {
				recordServices.update(record);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}

		public void hasNewParent(Record record)
				throws InterruptedException {
			recordServices.refresh(this.record);
			this.record.set(folderSchema.parent(), record);
			try {
				recordServices.update(this.record);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			waitForBatchProcess();
		}

		public void hasAReferenceToUnclassifiedSecuredRecord(Record referencedRecord)
				throws InterruptedException {
			this.record.set(folderSchema.instance().get("securedUnclassified"), referencedRecord);
			try {
				recordServices.update(this.record);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
			waitForBatchProcess();
		}
	}

	private MetadataSchemaTypesConfigurator unsecuredAndUnclassifiedValueListSchemaAndUnclassifiedSecuredSchema() {
		return new MetadataSchemaTypesConfigurator() {

			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder aValueListType = schemaTypes.createNewSchemaType("valueList");
				aValueListType.setSecurity(false);

				MetadataSchemaTypeBuilder securedUnclassifiedSchemaType = schemaTypes.createNewSchemaType("securedUnclassified");
				securedUnclassifiedSchemaType.setSecurity(true);
				securedUnclassifiedSchemaType.getDefaultSchema().create("parent")
						.defineChildOfRelationshipToType(securedUnclassifiedSchemaType);

				MetadataSchemaBuilder folderSchema = schemaTypes.getSchemaType("folder").getDefaultSchema();
				folderSchema.create("valueListRef").defineReferencesTo(aValueListType).setMultivalue(true);
				folderSchema.create("valueListSingleRef").defineReferencesTo(aValueListType).setMultivalue(false);
				folderSchema.create("securedUnclassified").defineReferencesTo(securedUnclassifiedSchemaType);

				MetadataSchemaTypeBuilder typeSupportingRawDelete = schemaTypes.createNewSchemaType("typeSupportingRawDelete")
						.setInTransactionLog(false);
			}
		};
	}

	private void givenMultiValueLinkToOtherFolderMetadatasIs(final boolean value) {
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(schemas.folderSchema.linkToOtherFolders().getCode()).setDefaultRequirement(value);
			}
		});
	}

	private void givenSingleValueLinkToOtherFolderMetadatasIs(final boolean value) {
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(schemas.folderSchema.linkToOtherFolder().getCode()).setDefaultRequirement(value);
			}
		});
	}

	public static class InvalidMetadataValidator implements RecordMetadataValidator<String> {
		@Override
		public void validate(Metadata metadata, String value, ConfigProvider configProvider,
							 ValidationErrors validationErrors) {
			if (value.equals("test")) {
				validationErrors.add(getClass(), "test");
			}
		}
	}
}
