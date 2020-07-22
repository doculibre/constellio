package com.constellio.model.services.users;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserCredentialsManagerRuntimeException.UserCredentialsManagerRuntimeException_CannotExecuteTransaction;
import org.joda.time.LocalDateTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

	public UserCredential create(String username, String firstName, String lastName, String email, String serviceKey,
								 boolean systemAdmin, List<String> globalGroups, List<String> collections,
								 Map<String, LocalDateTime> tokens,
								 UserCredentialStatus status, String domain, List<String> msExchDelegateListBL,
								 String dn) {
		return ((UserCredential) valueOrDefault(getUserCredential(username), schemas.newCredential()))
				.setUsername(cleanUsername(username))
				.setFirstName(firstName)
				.setLastName(lastName)
				.setEmail(email)
				.setServiceKey(serviceKey)
				.setSystemAdmin(systemAdmin)
				.setGlobalGroups(globalGroups)
				.setCollections(collections)
				.setAccessTokens(tokens)
				.setStatus(status)
				.setDomain(domain)
				.setMsExchDelegateListBL(msExchDelegateListBL)
				.setDn(dn);
	}

	public UserCredential create(String username, String firstName, String lastName, String email,
								 List<String> personalEmails,
								 String serviceKey,
								 boolean systemAdmin, List<String> globalGroups, List<String> collections,
								 Map<String, LocalDateTime> tokens,
								 UserCredentialStatus status, String domain, List<String> msExchDelegateListBL,
								 String dn) {
		return ((UserCredential) valueOrDefault(getUserCredential(username), schemas.newCredential()))
				.setUsername(cleanUsername(username))
				.setFirstName(firstName)
				.setLastName(lastName)
				.setEmail(email)
				.setPersonalEmails(personalEmails)
				.setServiceKey(serviceKey)
				.setSystemAdmin(systemAdmin)
				.setGlobalGroups(globalGroups)
				.setCollections(collections)
				.setAccessTokens(tokens)
				.setStatus(status)
				.setDomain(domain)
				.setMsExchDelegateListBL(msExchDelegateListBL)
				.setDn(dn);
	}

	public UserCredential create(String username, String firstName, String lastName, String email,
								 List<String> personalEmails,
								 String serviceKey, boolean systemAdmin, List<String> globalGroups,
								 List<String> collections,
								 Map<String, LocalDateTime> tokens, UserCredentialStatus status, String domain,
								 List<String> msExchDelegateListBL,
								 String dn, String jobTitle, String phone, String fax, String address) {
		return ((UserCredential) valueOrDefault(getUserCredential(username), schemas.newCredential()))
				.setUsername(cleanUsername(username))
				.setFirstName(firstName)
				.setLastName(lastName)
				.setEmail(email)
				.setPersonalEmails(personalEmails)
				.setServiceKey(serviceKey)
				.setSystemAdmin(systemAdmin)
				.setGlobalGroups(globalGroups)
				.setCollections(collections)
				.setAccessTokens(tokens)
				.setStatus(status)
				.setDomain(domain)
				.setMsExchDelegateListBL(msExchDelegateListBL)
				.setDn(dn)
				.setJobTitle(jobTitle)
				.setAddress(address)
				.setPhone(phone)
				.setFax(fax);
	}

	public UserCredential create(String username, String firstName, String lastName, String email, String serviceKey,
								 boolean systemAdmin, List<String> globalGroups, List<String> collections,
								 Map<String, LocalDateTime> tokens,
								 UserCredentialStatus status) {
		return create(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections, tokens, status,
				null, null, null);
	}

	public UserCredential create(String username, String firstName, String lastName, String email,
								 List<String> globalGroups,
								 List<String> collections, UserCredentialStatus status, String domain,
								 List<String> msExchDelegateListBL, String dn) {
		return create(username, firstName, lastName, email, null, false, globalGroups, collections,
				Collections.<String, LocalDateTime>emptyMap(), status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential create(String username, String firstName, String lastName, String email,
								 List<String> globalGroups,
								 List<String> collections, UserCredentialStatus status) {
		return create(username, firstName, lastName, email, null, false, globalGroups, collections,
				Collections.<String, LocalDateTime>emptyMap(), status, null, null, null);
	}

	public void addUpdate(UserCredential userCredential) {
		try {
			modelLayerFactory.newRecordServices().add((UserCredential) userCredential);
		} catch (RecordServicesException e) {
			throw new UserCredentialsManagerRuntimeException_CannotExecuteTransaction(e);
		}
	}

	public UserCredential getUserCredential(String username) {

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

	public UserCredential getAzureUserCredential(String azureUsername) {

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

	public LogicalSearchQuery getUserCredentialsQuery() {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).returnAll()).sortAsc(Schemas.TITLE);
	}

	public LogicalSearchQuery getUserCredentialsWithAgreedToPolicyQuery() {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.credentialSchemaType().getAllMetadatas().getMetadataWithLocalCode(UserCredential.HAS_AGREED_TO_PRIVACY_POLICY)).isTrue());
	}

	public LogicalSearchQuery getUserCredentialsWithReadLastAlert() {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.credentialSchemaType()
				.getAllMetadatas().getMetadataWithLocalCode(UserCredential.HAS_READ_LAST_ALERT)).isTrue());
	}

	public List<UserCredential> getUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getUserCredentialsQuery()));
	}

	public LogicalSearchQuery getActiveUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.ACTIVE);
	}

	public List<UserCredential> getActiveUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getActiveUserCredentialsQuery()));
	}

	public LogicalSearchQuery getSuspendedUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.SUSPENDED);
	}

	public List<UserCredential> getSuspendedUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getSuspendedUserCredentialsQuery()));
	}

	public LogicalSearchQuery getPendingApprovalUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.PENDING);
	}

	public List<UserCredential> getPendingApprovalUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getPendingApprovalUserCredentialsQuery()));
	}

	public LogicalSearchQuery getDeletedUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.DELETED);
	}

	public List<UserCredential> getDeletedUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getDeletedUserCredentialsQuery()));
	}

	public LogicalSearchQuery getUserCredentialsInGlobalGroupQuery(String group) {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.credentialGroups()).isEqualTo(group))
				.sortAsc(Schemas.TITLE);
	}

	public List<UserCredential> getUserCredentialsInGlobalGroup(String group) {
		return schemas.wrapCredentials(searchServices.search(getUserCredentialsInGlobalGroupQuery(group)));
	}

	public LogicalSearchQuery getUserCredentialsInCollectionQuery(String collection) {
		return new LogicalSearchQuery(
				from(schemas.credentialSchemaType()).where(schemas.credentialCollections()).isEqualTo(collection))
				.sortAsc(Schemas.TITLE);
	}

	public void removeCollection(final String collection) {
		try {
			new ActionExecutorInBatch(searchServices, "Remove collection in user credentials records", 100) {

				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {
					Transaction transaction = new Transaction();
					transaction.getRecordUpdateOptions().setOptimisticLockingResolution(EXCEPTION);
					for (Record record : records) {
						transaction.add((UserCredential) schemas.wrapCredential(record).removeCollection(collection));
					}

					modelLayerFactory.newRecordServices().execute(transaction);

				}
			}.execute(getUserCredentialsInCollectionQuery(collection));
		} catch (Exception e) {
			throw new UserCredentialsManagerRuntimeException_CannotExecuteTransaction(e);
		}
	}

	public void removeToken(String token) {
		UserCredential credential = getUserCredentialByToken(token);
		if (credential != null) {
			addUpdate(credential.removeAccessToken(token));
		}
	}

	public void removeUserCredentialFromCollection(UserCredential userCredential, String collection) {
		addUpdate(userCredential.removeCollection(collection));
	}

	public void removeGroup(String group) {
		Transaction transaction = new Transaction();
		for (Record record : searchServices.search(getUserCredentialsInGlobalGroupQuery(group))) {
			transaction.add((UserCredential) schemas.wrapCredential(record).removeGlobalGroup(group));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new UserCredentialsManagerRuntimeException_CannotExecuteTransaction(e);
		}
	}

	public UserCredential getUserCredentialByServiceKey(String serviceKey) {
		String encryptedKey = modelLayerFactory.newEncryptionServices().encrypt(serviceKey);
		Record record = searchServices.searchSingleResult(
				from(schemas.credentialSchemaType()).where(schemas.credentialServiceKey()).isEqualTo(encryptedKey));
		return record != null ? schemas.wrapCredential(record) : null;
	}

	public UserCredential getUserCredentialByDN(String dn) {
		Record record = recordServices.getRecordByMetadata(schemas.credentialDN(), dn);
		return record != null ? schemas.wrapUserCredential(record) : null;
	}

	public String getUsernameByServiceKey(String serviceKey) {
		UserCredential credential = getUserCredentialByServiceKey(serviceKey);
		return credential != null ? credential.getUsername() : null;
	}

	public UserCredential getUserCredentialByToken(String token) {
		String encryptedToken = modelLayerFactory.newEncryptionServices().encrypt(token);
		Record record = searchServices.searchSingleResult(
				from(schemas.credentialSchemaType()).where(schemas.credentialTokenKeys()).isEqualTo(encryptedToken));
		return record != null ? schemas.wrapCredential(record) : null;
	}

	public String getServiceKeyByToken(String token) {
		UserCredential credential = getUserCredentialByToken(token);
		return credential != null ? credential.getServiceKey() : null;
	}

	public void removeTimedOutTokens() {
		LocalDateTime now = TimeProvider.getLocalDateTime();
		Transaction transaction = new Transaction();
		for (Record record : searchServices.search(getUserCredentialsWithExpiredTokensQuery(now))) {
			UserCredential credential = schemas.wrapCredential(record);
			Map<String, LocalDateTime> validTokens = new HashMap<>();
			for (Entry<String, LocalDateTime> token : credential.getAccessTokens().entrySet()) {
				LocalDateTime expiration = token.getValue();
				if (expiration.isAfter(now)) {
					validTokens.put(token.getKey(), token.getValue());
				}
			}
			transaction.add((UserCredential) credential.setAccessTokens(validTokens));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new UserCredentialsManagerRuntimeException_CannotExecuteTransaction(e);
		}
	}

	public LogicalSearchQuery getUserCredentialsWithExpiredTokensQuery(LocalDateTime now) {
		return new LogicalSearchQuery(
				from(schemas.credentialSchemaType()).where(schemas.credentialTokenExpirations()).isLessOrEqualThan(now));
	}

	public void rewrite() {
		// Nothing to be done
	}

	private LogicalSearchQuery getQueryFilteredByStatus(UserCredentialStatus status) {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.credentialStatus()).isEqualTo(status))
				.sortAsc(Schemas.TITLE);
	}
}
