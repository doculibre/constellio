package com.constellio.model.services.users;

import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserCredentialsManagerRuntimeException.UserCredentialsManagerRuntimeException_CannotExecuteTransaction;

import java.util.List;

import static com.constellio.data.dao.dto.records.OptimisticLockingResolution.EXCEPTION;
import static com.constellio.data.utils.LangUtils.valueOrDefault;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.users.UserUtils.cleanUsername;

public class SolrUserCredentialsManager {
	private final ModelLayerFactory modelLayerFactory;
	private final SearchServices searchServices;
	private final RecordServices recordServices;
	private final SchemasRecordsServices schemas;

	public SolrUserCredentialsManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();
		schemas = SchemasRecordsServices.usingMainModelLayerFactory(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	UserCredential addEdit(String username) {
		return (valueOrDefault(getUserCredential(username), schemas.newCredential()))._setUsername(cleanUsername(username));
	}


	void addUpdate(UserCredential userCredential) {
		try {
			modelLayerFactory.newRecordServices().add(userCredential);
		} catch (RecordServicesException e) {
			throw new UserCredentialsManagerRuntimeException_CannotExecuteTransaction(e);
		}
	}

	UserCredential getUserCredential(String username) {

		if (username == null) {
			return null;
		}

		Record record = modelLayerFactory.newRecordServices()
				.getRecordByMetadata(schemas.credentialUsername(), username);

		if (record == null) {

			String cleanedUsername = cleanUsername(username);
			if (!cleanedUsername.equals(username)) {
				record = modelLayerFactory.newRecordServices()
						.getRecordByMetadata(schemas.credentialUsername(), cleanUsername(username));
			}
		}
		return record != null ? schemas.wrapCredential(record) : null;
	}

	UserCredential getAzureUserCredential(String azureUsername) {

		if (azureUsername == null) {
			return null;
		}

		Record record = modelLayerFactory.newRecordServices()
				.getRecordByMetadata(schemas.credentialAzureUsername(), azureUsername);

		if (record == null) {

			String cleanedUsername = cleanUsername(azureUsername);
			if (!cleanedUsername.equals(azureUsername)) {
				record = modelLayerFactory.newRecordServices()
						.getRecordByMetadata(schemas.credentialAzureUsername(), cleanUsername(azureUsername));
			}
		}
		return record != null ? schemas.wrapCredential(record) : null;
	}

	private LogicalSearchQuery getUserCredentialsQuery() {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).returnAll()).sortAsc(Schemas.TITLE);
	}

	List<UserCredential> getUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getUserCredentialsQuery()));
	}

	LogicalSearchQuery getActiveUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.ACTIVE);
	}

	List<UserCredential> getActiveUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getActiveUserCredentialsQuery()));
	}

	LogicalSearchQuery getSuspendedUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.SUSPENDED);
	}

	List<UserCredential> getSuspendedUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getSuspendedUserCredentialsQuery()));
	}

	LogicalSearchQuery getPendingApprovalUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.PENDING);
	}

	List<UserCredential> getPendingApprovalUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getPendingApprovalUserCredentialsQuery()));
	}

	LogicalSearchQuery getDeletedUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.DISABLED);
	}

	List<UserCredential> getDeletedUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getDeletedUserCredentialsQuery()));
	}

	LogicalSearchQuery getUserCredentialsInGlobalGroupQuery(String group) {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.credentialGroups()).isEqualTo(group))
				.sortAsc(Schemas.TITLE);
	}

	List<UserCredential> getUserCredentialsInGlobalGroup(String group) {
		return schemas.wrapCredentials(searchServices.search(getUserCredentialsInGlobalGroupQuery(group)));
	}

	LogicalSearchQuery getUserCredentialsInCollectionQuery(String collection) {
		return new LogicalSearchQuery(
				from(schemas.credentialSchemaType()).where(schemas.credentialCollections()).isEqualTo(collection))
				.sortAsc(Schemas.TITLE);
	}

	void removeCollection(final String collection) {
		try {
			new ActionExecutorInBatch(searchServices, "Remove collection in user credentials records", 100) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					Transaction transaction = new Transaction();
					transaction.getRecordUpdateOptions().setOptimisticLockingResolution(EXCEPTION);
					for (Record record : records) {
						transaction.add(schemas.wrapCredential(record).removeCollection(collection));
					}

					modelLayerFactory.newRecordServices().execute(transaction);

				}
			}.execute(getUserCredentialsInCollectionQuery(collection));
		} catch (Exception e) {
			throw new UserCredentialsManagerRuntimeException_CannotExecuteTransaction(e);
		}
	}

	void removeToken(String token) {
		UserCredential credential = getUserCredentialByToken(token);
		if (credential != null) {
			addUpdate(credential.removeAccessToken(token));
		}
	}

	void removeUserCredentialFromCollection(UserCredential userCredential, String collection) {
		addUpdate(userCredential.removeCollection(collection));
	}

	void removeGroup(String group) {
		Transaction transaction = new Transaction();
		for (Record record : searchServices.search(getUserCredentialsInGlobalGroupQuery(group))) {
			transaction.add(schemas.wrapCredential(record).removeGlobalGroup(group));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new UserCredentialsManagerRuntimeException_CannotExecuteTransaction(e);
		}
	}

	UserCredential getUserCredentialByServiceKey(String serviceKey) {
		String encryptedKey = modelLayerFactory.newEncryptionServices().encrypt(serviceKey);
		Record record = searchServices.searchSingleResult(
				from(schemas.credentialSchemaType()).where(schemas.credentialServiceKey()).isEqualTo(encryptedKey));
		return record != null ? schemas.wrapCredential(record) : null;
	}

	UserCredential getUserCredentialByDN(String dn) {
		Record record = recordServices.getRecordByMetadata(schemas.credentialDN(), dn);
		return record != null ? schemas.wrapUserCredential(record) : null;
	}

	LogicalSearchQuery getUserCredentialNotSynced() {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.syncStatus()).is(UserSyncMode.NOT_SYNCED))
				.sortAsc(Schemas.TITLE);
	}

	String getUsernameByServiceKey(String serviceKey) {
		UserCredential credential = getUserCredentialByServiceKey(serviceKey);
		return credential != null ? credential.getUsername() : null;
	}

	UserCredential getUserCredentialByToken(String token) {
		String encryptedToken = modelLayerFactory.newEncryptionServices().encrypt(token);
		Record record = searchServices.searchSingleResult(
				from(schemas.credentialSchemaType()).where(schemas.credentialTokenKeys()).isEqualTo(encryptedToken));
		return record != null ? schemas.wrapCredential(record) : null;
	}

	String getServiceKeyByToken(String token) {
		UserCredential credential = getUserCredentialByToken(token);
		return credential != null ? credential.getServiceKey() : null;
	}

	void removeTimedOutTokens() {

	}



	private LogicalSearchQuery getQueryFilteredByStatus(UserCredentialStatus status) {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.credentialStatus()).isEqualTo(status))
				.sortAsc(Schemas.TITLE);
	}
}
