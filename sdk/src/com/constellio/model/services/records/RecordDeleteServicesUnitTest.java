package com.constellio.model.services.records;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentModificationsBuilder;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException.RecordDeleteServicesRuntimeException_CannotDeleteRecordWithUserFromOtherCollection;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotRestoreRecord;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;

public class RecordDeleteServicesUnitTest extends ConstellioTest {

	@Mock Record theRecord, zeParent, thePrincipalConcept, aSubPrincipalConcept, aRecordInThePrincipalConcept,
			aRecordInTheSubPrincipalConcept, aRecordInTheRecordHierarchy, anotherRecordInTheRecordHierarchy;
	@Mock RecordDTO theRecordDTO, aRecordInTheRecordHierarchyRecordDTO, anotherRecordInTheRecordHierarchyRecordDTO;

	@Mock RecordDao recordDao;
	@Mock SearchServices searchServices;

	@Mock User user;

	@Mock RecordServicesImpl recordServices;

	@Mock ContentManager contentManager;

	@Mock AuthorizationsServices authorizationsServices;

	RecordDeleteServices recordDeleteServices;

	@Mock Taxonomy principalTaxonomy;

	@Mock TaxonomiesManager taxonomiesManager;

	@Mock MetadataSchemasManager metadataSchemasManager;

	@Mock RecordUtils recordUtils;

	@Mock MetadataSchemaTypes types;
	@Mock MetadataSchemaType type1, type2;
	@Mock Metadata type1Reference1, type1Reference2, type2Reference1;

	@Mock Record firstRecord;
	@Mock Record secondRecord;
	@Mock Record thirdRecord;

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock ContentModificationsBuilder contentModificationsBuilder;
	ModelLayerExtensions extensions = new ModelLayerExtensions();

	List<String> idsOfRecordsWithReferences = Arrays.asList("1", "2", "3");

	@Before
	public void setUp()
			throws Exception {

		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);
		when(modelLayerFactory.newRecordServices()).thenReturn(recordServices);
		when(modelLayerFactory.newAuthorizationsServices()).thenReturn(authorizationsServices);
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(modelLayerFactory.getContentManager()).thenReturn(contentManager);
		when(modelLayerFactory.getExtensions()).thenReturn(extensions);

		recordDeleteServices = spy(new RecordDeleteServices(recordDao, modelLayerFactory));

		doReturn(Arrays.asList(theRecord, aRecordInTheRecordHierarchy, anotherRecordInTheRecordHierarchy))
				.when(recordDeleteServices)
				.getAllRecordsInHierarchy(eq(theRecord));

		doReturn(Arrays.asList(aSubPrincipalConcept, aRecordInThePrincipalConcept, aRecordInTheSubPrincipalConcept))
				.when(recordDeleteServices).getAllRecordsInHierarchy(eq(thePrincipalConcept));

		doReturn(Arrays.asList(aSubPrincipalConcept))
				.when(recordDeleteServices).getAllPrincipalConceptsRecordsInHierarchy(thePrincipalConcept, principalTaxonomy);

		when(user.getCollection()).thenReturn(zeCollection);
		when(theRecord.getCollection()).thenReturn(zeCollection);
		when(theRecord.getSchemaCode()).thenReturn("folder_default");
		when(aRecordInTheRecordHierarchy.getCollection()).thenReturn(zeCollection);
		when(aRecordInTheRecordHierarchy.getSchemaCode()).thenReturn("folder_default");
		when(anotherRecordInTheRecordHierarchy.getCollection()).thenReturn(zeCollection);
		when(anotherRecordInTheRecordHierarchy.getSchemaCode()).thenReturn("folder_default");

		when(thePrincipalConcept.getCollection()).thenReturn(zeCollection);
		when(thePrincipalConcept.getSchemaCode()).thenReturn("type_Default");
		when(aSubPrincipalConcept.getCollection()).thenReturn(zeCollection);
		when(aSubPrincipalConcept.getSchemaCode()).thenReturn("type_Default");
		when(aRecordInThePrincipalConcept.getCollection()).thenReturn(zeCollection);
		when(aRecordInThePrincipalConcept.getSchemaCode()).thenReturn("folder_default");
		when(aRecordInTheSubPrincipalConcept.getCollection()).thenReturn(zeCollection);
		when(aRecordInTheSubPrincipalConcept.getSchemaCode()).thenReturn("folder_default");

		when(taxonomiesManager.getPrincipalTaxonomy(zeCollection)).thenReturn(principalTaxonomy);
		when(principalTaxonomy.getSchemaTypes()).thenReturn(Arrays.asList("type2", "type"));

		doReturn(recordUtils).when(recordDeleteServices).newRecordUtils();
		when(recordUtils
				.toRecordDTOList(Arrays.asList(theRecord, aRecordInTheRecordHierarchy, anotherRecordInTheRecordHierarchy)))
				.thenReturn(Arrays.asList(theRecordDTO, aRecordInTheRecordHierarchyRecordDTO,
						anotherRecordInTheRecordHierarchyRecordDTO));

		when(metadataSchemasManager.getSchemaTypes(zeCollection)).thenReturn(types);
		when(types.getSchemaTypes()).thenReturn(Arrays.asList(type1, type2));
		when(types.getSchemaType("folder")).thenReturn(type1);
		when(types.getSchemaType("type")).thenReturn(type1);
		when(types.getSchemaType("type2")).thenReturn(type2);
		when(type1.getAllNonParentReferences()).thenReturn(Arrays.asList(type1Reference1, type1Reference2));
		when(type2.getAllNonParentReferences()).thenReturn(Arrays.asList(type2Reference1));
		when(type1.hasSecurity()).thenReturn(true);
		when(type2.hasSecurity()).thenReturn(true);

		when(firstRecord.getId()).thenReturn("firstRecord");
		when(secondRecord.getId()).thenReturn("secondRecord");
		when(thirdRecord.getId()).thenReturn("thirdRecord");

		doReturn(contentModificationsBuilder).when(recordDeleteServices).newContentModificationsBuilder(zeCollection);
	}

	@Test
	public void whenRestoringThenSetLogicallyDeletedStatusToAllRecordInHierarchyAndExecuteTransaction()
			throws Exception {

		doReturn(true).when(recordDeleteServices).isRestorable(theRecord, user);

		ArgumentCaptor<Transaction> transaction = ArgumentCaptor.forClass(Transaction.class);

		recordDeleteServices.restore(theRecord, user);

		verify(theRecord).set(Schemas.LOGICALLY_DELETED_STATUS, false);
		verify(aRecordInTheRecordHierarchy).set(Schemas.LOGICALLY_DELETED_STATUS, false);
		verify(anotherRecordInTheRecordHierarchy).set(Schemas.LOGICALLY_DELETED_STATUS, false);
		verify(recordServices).execute(transaction.capture());

		assertThat(transaction.getValue().getRecords())
				.containsOnly(theRecord, aRecordInTheRecordHierarchy, anotherRecordInTheRecordHierarchy);
	}

	@Test
	public void whenLogicallyDeletingThenSetLogicallyDeletedStatusToAllRecordInHierarchyAndExecuteTransaction()
			throws Exception {

		doReturn(true).when(recordDeleteServices).isLogicallyDeletable(theRecord, user);

		ArgumentCaptor<Transaction> transaction = ArgumentCaptor.forClass(Transaction.class);

		recordDeleteServices.logicallyDelete(theRecord, user);

		verify(theRecord).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(aRecordInTheRecordHierarchy).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(anotherRecordInTheRecordHierarchy).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(recordServices).execute(transaction.capture());

		assertThat(transaction.getValue().getRecords())
				.containsOnly(theRecord, aRecordInTheRecordHierarchy, anotherRecordInTheRecordHierarchy);
	}

	@Test
	public void whenLogicallyDeletingPrincipalConceptIncludingHierarchyThenSetLogicallyDeletedStatusToAllRecordInHierarchyAndExecuteTransaction()
			throws Exception {

		doReturn(true).when(recordDeleteServices).isPrincipalConceptLogicallyDeletableIncludingContent(thePrincipalConcept, user);

		ArgumentCaptor<Transaction> transaction = ArgumentCaptor.forClass(Transaction.class);

		recordDeleteServices.logicallyDeletePrincipalConceptIncludingRecords(thePrincipalConcept, user);

		verify(thePrincipalConcept).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(aSubPrincipalConcept).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(aRecordInThePrincipalConcept).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(aRecordInTheSubPrincipalConcept).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(recordServices).execute(transaction.capture());

		assertThat(transaction.getValue().getRecords()).containsOnly(thePrincipalConcept, aSubPrincipalConcept,
				aRecordInThePrincipalConcept, aRecordInTheSubPrincipalConcept);
	}

	@Test
	public void whenLogicallyDeletingPrincipalConceptExcludingHierarchyThenSetLogicallyDeletedStatusToAllConceptInHierarchyAndExecuteTransaction()
			throws Exception {

		doReturn(true).when(recordDeleteServices).isPrincipalConceptLogicallyDeletableExcludingContent(thePrincipalConcept, user);

		ArgumentCaptor<Transaction> transaction = ArgumentCaptor.forClass(Transaction.class);

		recordDeleteServices.logicallyDeletePrincipalConceptExcludingRecords(thePrincipalConcept, user);

		verify(thePrincipalConcept).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(aSubPrincipalConcept).set(Schemas.LOGICALLY_DELETED_STATUS, true);
		verify(recordServices).execute(transaction.capture());

		assertThat(transaction.getValue().getRecords()).containsOnly(thePrincipalConcept, aSubPrincipalConcept);
	}

	@Test
	public void whenPhysicallyDeletingThenGetRecordInHierarchyAndDeleteThemInATransaction()
			throws Exception {

		RecordDeleteOptions options = new RecordDeleteOptions();
		doNothing().when(recordDeleteServices).deleteContents(anyList());
		doReturn(true).when(recordDeleteServices).isPhysicallyDeletable(theRecord, user, options);
		ArgumentCaptor<TransactionDTO> transactionDTO = ArgumentCaptor.forClass(TransactionDTO.class);

		recordDeleteServices.physicallyDelete(theRecord, user, options);
		verify(recordDao).execute(transactionDTO.capture());
		verify(recordDeleteServices)
				.deleteContents(Arrays.asList(theRecord, aRecordInTheRecordHierarchy, anotherRecordInTheRecordHierarchy));
		assertThat(transactionDTO.getValue().getDeletedRecords())
				.containsOnly(theRecordDTO, aRecordInTheRecordHierarchyRecordDTO, anotherRecordInTheRecordHierarchyRecordDTO);
	}

	@Test
	public void whenDeleteContentsThenFindAllPotentiallyDeletableVersionsAndMarkThemForPotentialDelete()
			throws Exception {

		List<Record> deletedRecords = Arrays.asList(theRecord, aRecordInTheRecordHierarchy, anotherRecordInTheRecordHierarchy);
		List<String> deletedVersionHashes = Arrays.asList("hash1", "hash2", "hash3");
		when(contentModificationsBuilder.buildForDeletedRecords(deletedRecords)).thenReturn(deletedVersionHashes);

		recordDeleteServices.deleteContents(deletedRecords);

		verify(contentManager).silentlyMarkForDeletionIfNotReferenced("hash1");
		verify(contentManager).silentlyMarkForDeletionIfNotReferenced("hash2");
		verify(contentManager).silentlyMarkForDeletionIfNotReferenced("hash3");

	}

	@Test
	public void givenNotLogicallyDeletableWhenLogicallyDeletingThenThrowException()
			throws Exception {

		doReturn(false).when(recordDeleteServices).isLogicallyDeletable(theRecord, user);

		try {
			recordDeleteServices.logicallyDelete(theRecord, user);
			fail("RecordServicesRuntimeException_CannotLogicallyDeleteRecord expected");
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
			//OK
		}
		verify(recordServices, never()).execute(any(Transaction.class));
	}

	@Test
	public void givenNotLogicallyDeletableWhenLogicallyDeletingPrincipalConceptExcludingContentThenThrowException()
			throws Exception {

		doReturn(false).when(recordDeleteServices)
				.isPrincipalConceptLogicallyDeletableExcludingContent(thePrincipalConcept, user);

		try {
			recordDeleteServices.logicallyDeletePrincipalConceptExcludingRecords(thePrincipalConcept, user);
			fail("RecordServicesRuntimeException_CannotLogicallyDeleteRecord expected");
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
			//OK
		}
		verify(recordServices, never()).execute(any(Transaction.class));
	}

	@Test
	public void givenNotLogicallyDeletableWhenLogicallyDeletingPrincipalConceptIncludingContentThenThrowException()
			throws Exception {

		doReturn(false).when(recordDeleteServices).isPrincipalConceptLogicallyDeletableIncludingContent(theRecord, user);

		try {
			recordDeleteServices.logicallyDeletePrincipalConceptIncludingRecords(theRecord, user);
			fail("RecordServicesRuntimeException_CannotLogicallyDeleteRecord expected");
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
			//OK
		}
		verify(recordServices, never()).execute(any(Transaction.class));
	}

	@Test
	public void givenNotRestorableWhenRestoringThenThrowException()
			throws Exception {

		doReturn(false).when(recordDeleteServices).isRestorable(theRecord, user);

		try {
			recordDeleteServices.restore(theRecord, user);
			fail("RecordServicesRuntimeException_CannotLogicallyDeleteRecord expected");
		} catch (RecordServicesRuntimeException_CannotRestoreRecord e) {
			//OK
		}
		verify(recordServices, never()).execute(any(Transaction.class));
	}

	@Test
	public void givenNotPhysicallyDeletableWhenPhysicallyDeletingThenThrowException()
			throws Exception {

		doReturn(false).when(recordDeleteServices).isPhysicallyDeletable(theRecord, user);

		try {
			recordDeleteServices.physicallyDelete(theRecord, user);
			fail("RecordServicesRuntimeException_CannotLogicallyDeleteRecord expected");
		} catch (RecordServicesRuntimeException_CannotPhysicallyDeleteRecord e) {
			//OK
		}
		verify(recordServices, never()).execute(any(Transaction.class));
	}

	@Test
	public void givenNoDeleteAccessOnAllRecordsOfHierarchyThenNotRestorable()
			throws Exception {
		when(authorizationsServices.hasDeletePermissionOnHierarchy(user, theRecord)).thenReturn(false);

		assertThat(recordDeleteServices.isRestorable(theRecord, user)).isFalse();

	}

	@Test
	public void givenDeleteAccessOnAllRecordsOfHierarchyThenRestorable()
			throws Exception {
		when(authorizationsServices.hasRestaurationPermissionOnHierarchy(user, theRecord)).thenReturn(true);

		assertThat(recordDeleteServices.isRestorable(theRecord, user)).isTrue();

	}

	@Test
	public void givenNoDeleteAccessOnAllRecordsOfHierarchyThenNotLogicallyDeletable()
			throws Exception {
		when(authorizationsServices.hasDeletePermissionOnHierarchy(user, theRecord)).thenReturn(false);

		assertThat(recordDeleteServices.isLogicallyDeletable(theRecord, user)).isFalse();

	}

	@Test
	public void givenDeleteAccessOnAllRecordsOfHierarchyThenLogicallyDeletable()
			throws Exception {
		when(authorizationsServices.hasDeletePermissionOnHierarchy(user, theRecord)).thenReturn(true);

		assertThat(recordDeleteServices.isLogicallyDeletable(theRecord, user)).isTrue();

	}

	@Test
	public void givenRecordNotLogicallyDeletedThenNotPysicallyDeletable()
			throws Exception {
		when(theRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);
		when(authorizationsServices.hasRestaurationPermissionOnHierarchy(user, theRecord)).thenReturn(true);
		doReturn(true).when(recordDeleteServices).containsNoActiveRecords(theRecord);
		doReturn(false).when(recordDeleteServices).isReferencedByOtherRecords(theRecord);

		assertThat(recordDeleteServices.isPhysicallyDeletable(theRecord, user)).isFalse();

	}

	@Test
	public void givenNoDeleteAccessOnAllLogicallyDeletedRecordsOfHierarchyThenNotPysicallyDeletable()
			throws Exception {
		when(theRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		when(authorizationsServices.hasRestaurationPermissionOnHierarchy(user, theRecord)).thenReturn(false);
		doReturn(true).when(recordDeleteServices).containsNoActiveRecords(theRecord);
		doReturn(false).when(recordDeleteServices).isReferencedByOtherRecords(theRecord);

		assertThat(recordDeleteServices.isPhysicallyDeletable(theRecord, user)).isFalse();

	}

	@Test
	public void givenReferencesToRecordInHierarchyThenNotPhysicallyDeletable()
			throws Exception {
		when(theRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		when(authorizationsServices.hasRestaurationPermissionOnHierarchy(user, theRecord)).thenReturn(true);
		doReturn(true).when(recordDeleteServices).containsNoActiveRecords(theRecord);
		doReturn(true).when(recordDeleteServices).isReferencedByOtherRecords(theRecord);

		assertThat(recordDeleteServices.isPhysicallyDeletable(theRecord, user)).isFalse();

	}

	@Test
	public void givenNotAllRecordsInHierarchyLogicallyDeletedThenCannotPhysicallyDeleteIt()
			throws Exception {
		when(theRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		when(authorizationsServices.hasRestaurationPermissionOnHierarchy(user, theRecord)).thenReturn(true);
		doReturn(false).when(recordDeleteServices).containsNoActiveRecords(theRecord);
		doReturn(false).when(recordDeleteServices).isReferencedByOtherRecords(theRecord);

		assertThat(recordDeleteServices.isPhysicallyDeletable(theRecord, user)).isFalse();

	}

	@Test
	public void givenNoDeleteAccessOnAllPrincipalConceptsAndRecordsThenPrincipalConceptNotLogicallyDeletableIncludingHierarchy()
			throws Exception {
		when(authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(user, theRecord, true,
				metadataSchemasManager)).thenReturn(false);

		assertThat(recordDeleteServices.isPrincipalConceptLogicallyDeletableIncludingContent(theRecord, user)).isFalse();

	}

	@Test
	public void givenDeleteAccessOnAllPrincipalConceptsAndRecordsThenPrincipalConceptLogicallyDeletableIncludingHierarchy()
			throws Exception {
		when(authorizationsServices
				.hasDeletePermissionOnPrincipalConceptHierarchy(user, theRecord, true, metadataSchemasManager)).thenReturn(true);

		assertThat(recordDeleteServices.isPrincipalConceptLogicallyDeletableIncludingContent(theRecord, user)).isTrue();

	}

	@Test
	public void givenNoDeleteAccessOnAllPrincipalConceptsButNotOnRecordsThenPrincipalConceptNotLogicallyDeletableExcludingHierarchy()
			throws Exception {
		when(authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(user, theRecord, false,
				metadataSchemasManager)).thenReturn(false);

		assertThat(recordDeleteServices.isPrincipalConceptLogicallyDeletableExcludingContent(theRecord, user)).isFalse();

	}

	@Test
	public void givenDeleteAccessOnAllPrincipalConceptsButNotOnRecordsThenPrincipalConceptLogicallyDeletableExcludingHierarchy()
			throws Exception {
		when(authorizationsServices.hasDeletePermissionOnPrincipalConceptHierarchy(user, theRecord, false,
				metadataSchemasManager)).thenReturn(true);

		assertThat(recordDeleteServices.isPrincipalConceptLogicallyDeletableExcludingContent(theRecord, user)).isTrue();

	}

	@Test
	public void givenLogicallyDeletedRecordWithoutParentThenRestorable()
			throws Exception {

		when(authorizationsServices.hasRestaurationPermissionOnHierarchy(user, theRecord)).thenReturn(true);
		when(theRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		when(theRecord.getParentId()).thenReturn(null);

		assertThat(recordDeleteServices.isRestorable(theRecord, user)).isTrue();
	}

	@Test
	public void givenLogicallyDeletedRecordWithActiveParentThenRestorable()
			throws Exception {

		when(authorizationsServices.hasRestaurationPermissionOnHierarchy(user, theRecord)).thenReturn(true);
		when(theRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		when(theRecord.getParentId()).thenReturn("zeParent");
		when(recordServices.getDocumentById("zeParent")).thenReturn(zeParent);
		when(zeParent.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);

		assertThat(recordDeleteServices.isRestorable(theRecord, user)).isTrue();
	}

	@Test
	public void givenLogicallyDeletedRecordWithLogicallyDeletedParentThenNotRestorable()
			throws Exception {

		when(authorizationsServices.hasRestaurationPermissionOnHierarchy(user, theRecord)).thenReturn(true);
		when(theRecord.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);
		when(theRecord.getParentId()).thenReturn("zeParent");
		when(recordServices.getDocumentById("zeParent")).thenReturn(zeParent);
		when(zeParent.get(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);

		assertThat(recordDeleteServices.isRestorable(theRecord, user)).isFalse();
	}

	@Test
	public void givenRecordsWith2SubRecordsInHierachyWithReferencesThenGetAllRecordsReferencing()
			throws Exception {

		doReturn(idsOfRecordsWithReferences).when(recordDeleteServices).getRecordsInHierarchyWithDependency(theRecord);

		doReturn(Arrays.asList(firstRecord, secondRecord)).when(recordDeleteServices)
				.getRecordsInTypeWithReferenceTo(user, idsOfRecordsWithReferences, type1,
						Arrays.asList(type1Reference1, type1Reference2));
		doReturn(Arrays.asList(thirdRecord)).when(recordDeleteServices).getRecordsInTypeWithReferenceTo(user,
				idsOfRecordsWithReferences, type2, Arrays.asList(type2Reference1));

		List<Record> records = recordDeleteServices.getVisibleRecordsWithReferenceToRecordInHierarchy(theRecord, user);
		assertThat(records).containsExactly(firstRecord, secondRecord, thirdRecord);

	}

	@Test(expected = RecordDeleteServicesRuntimeException_CannotDeleteRecordWithUserFromOtherCollection.class)
	public void whenCallIsLogicallyDeleteWithUserAndRecordOfDifferentCollectionsThenThrowException() {
		when(user.getCollection()).thenReturn("aCollection");
		when(theRecord.getCollection()).thenReturn("anotherCollection");

		recordDeleteServices.isLogicallyDeletable(theRecord, user);
	}

	@Test(expected = RecordDeleteServicesRuntimeException_CannotDeleteRecordWithUserFromOtherCollection.class)
	public void whenCallIsPhysicallyDeleteWithUserAndRecordOfDifferentCollectionsThenThrowException() {
		when(user.getCollection()).thenReturn("aCollection");
		when(theRecord.getCollection()).thenReturn("anotherCollection");

		recordDeleteServices.isPhysicallyDeletable(theRecord, user);
	}

	@Test(expected = RecordDeleteServicesRuntimeException_CannotDeleteRecordWithUserFromOtherCollection.class)
	public void whenCallIsRestorableWithUserAndRecordOfDifferentCollectionsThenThrowException() {
		when(user.getCollection()).thenReturn("aCollection");
		when(theRecord.getCollection()).thenReturn("anotherCollection");

		recordDeleteServices.isRestorable(theRecord, user);
	}
}
