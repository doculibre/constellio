package com.constellio.model.services.users;

import static com.constellio.data.utils.LangUtils.valueOrDefault;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDateTime;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.SystemCollectionListener;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class SolrUserCredentialsManager implements UserCredentialsManager, SystemCollectionListener {
	private final ModelLayerFactory modelLayerFactory;
	private final SearchServices searchServices;
	private final SchemasRecordsServices schemas;

	public SolrUserCredentialsManager(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		modelLayerFactory.addSystemCollectionListener(this);
		searchServices = modelLayerFactory.newSearchServices();
		schemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	@Override
	public UserCredential create(String username, String firstName, String lastName, String email, String serviceKey,
			boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
			UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn) {
		return ((SolrUserCredential) valueOrDefault(getUserCredential(username), schemas.newCredential()))
				.setUsername(username)
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

	@Override
	public UserCredential create(String username, String firstName, String lastName, String email, String serviceKey,
			boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
			UserCredentialStatus status) {
		return create(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections, tokens, status,
				null, null, null);
	}

	@Override
	public UserCredential create(String username, String firstName, String lastName, String email, List<String> globalGroups,
			List<String> collections, UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn) {
		return create(username, firstName, lastName, email, null, false, globalGroups, collections,
				Collections.<String, LocalDateTime>emptyMap(), status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential create(String username, String firstName, String lastName, String email, List<String> globalGroups,
			List<String> collections, UserCredentialStatus status) {
		return create(username, firstName, lastName, email, null, false, globalGroups, collections,
				Collections.<String, LocalDateTime>emptyMap(), status, null, null, null);
	}

	@Override
	public void addUpdate(UserCredential userCredential) {
		try {
			modelLayerFactory.newRecordServices().add((SolrUserCredential) userCredential);
		} catch (RecordServicesException e) {
			// TODO: Exception
			e.printStackTrace();
		}
	}

	@Override
	public UserCredential getUserCredential(String username) {
		Record record = searchServices.searchSingleResult(
				from(schemas.credentialSchemaType()).where(schemas.credentialUsername()).isEqualTo(username));
		return record != null ? schemas.wrapCredential(record) : null;
	}

	public LogicalSearchQuery getUserCredentialsQuery() {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).returnAll()).sortAsc(schemas.credentialUsername());
	}

	@Override
	public List<UserCredential> getUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getUserCredentialsQuery()));
	}

	public LogicalSearchQuery getActiveUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.ACTIVE);
	}

	@Override
	public List<UserCredential> getActiveUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getActiveUserCredentialsQuery()));
	}

	public LogicalSearchQuery getSuspendedUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.SUSPENDED);
	}

	@Override
	public List<UserCredential> getSuspendedUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getSuspendedUserCredentialsQuery()));
	}

	public LogicalSearchQuery getPendingApprovalUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.PENDING);
	}

	@Override
	public List<UserCredential> getPendingApprovalUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getPendingApprovalUserCredentialsQuery()));
	}

	public LogicalSearchQuery getDeletedUserCredentialsQuery() {
		return getQueryFilteredByStatus(UserCredentialStatus.DELETED);
	}

	@Override
	public List<UserCredential> getDeletedUserCredentials() {
		return schemas.wrapCredentials(searchServices.search(getDeletedUserCredentialsQuery()));
	}

	public LogicalSearchQuery getUserCredentialsInGlobalGroupQuery(String group) {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.credentialGroups()).isEqualTo(group))
				.sortAsc(schemas.credentialUsername());
	}

	@Override
	public List<UserCredential> getUserCredentialsInGlobalGroup(String group) {
		return schemas.wrapCredentials(searchServices.search(getUserCredentialsInGlobalGroupQuery(group)));
	}

	public LogicalSearchQuery getUserCredentialsInCollectionQuery(String collection) {
		return new LogicalSearchQuery(
				from(schemas.credentialSchemaType()).where(schemas.credentialCollections()).isEqualTo(collection))
				.sortAsc(schemas.credentialUsername());
	}

	@Override
	public void removeCollection(String collection) {
		Transaction transaction = new Transaction();
		for (Record record : searchServices.search(getUserCredentialsInCollectionQuery(collection))) {
			transaction.add((SolrUserCredential) schemas.wrapCredential(record).withRemovedCollection(collection));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			// TODO: Exception
			e.printStackTrace();
		}
	}

	@Override
	public void removeToken(String token) {
		UserCredential credential = getUserCredentialByToken(token);
		if (credential != null) {
			addUpdate(credential.withRemovedToken(token));
		}
	}

	@Override
	public void removeUserCredentialFromCollection(UserCredential userCredential, String collection) {
		addUpdate(userCredential.withRemovedCollection(collection));
	}

	@Override
	public void removeGroup(String group) {
		Transaction transaction = new Transaction();
		for (Record record : searchServices.search(getUserCredentialsInGlobalGroupQuery(group))) {
			transaction.add((SolrUserCredential) schemas.wrapCredential(record).withRemovedGlobalGroup(group));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			// TODO: Exception
			e.printStackTrace();
		}
	}

	public UserCredential getUserCredentialByServiceKey(String serviceKey) {
		Record record = searchServices.searchSingleResult(
				from(schemas.credentialSchemaType()).where(schemas.credentialServiceKey()).isEqualTo(serviceKey));
		return record != null ? schemas.wrapCredential(record) : null;
	}

	@Override
	public String getUsernameByServiceKey(String serviceKey) {
		UserCredential credential = getUserCredentialByServiceKey(serviceKey);
		return credential != null ? credential.getUsername() : null;
	}

	public UserCredential getUserCredentialByToken(String token) {
		Record record = searchServices.searchSingleResult(
				from(schemas.credentialSchemaType()).where(schemas.credentialTokenKeys()).isEqualTo(token));
		return record != null ? schemas.wrapCredential(record) : null;
	}

	@Override
	public String getServiceKeyByToken(String token) {
		UserCredential credential = getUserCredentialByToken(token);
		return credential != null ? credential.getServiceKey() : null;
	}

	@Override
	public void removedTimedOutTokens() {
		LocalDateTime now = TimeProvider.getLocalDateTime();
		Transaction transaction = new Transaction();
		for (Record record : searchServices.search(getUserCredentialsWithExpiredTokensQuery(now))) {
			UserCredential credential = schemas.wrapCredential(record);
			Map<String, LocalDateTime> validTokens = new HashMap<>();
			for (Entry<String, LocalDateTime> token : credential.getAccessTokens().entrySet()) {
				if (token.getValue().isBefore(now)) {
					validTokens.put(token.getKey(), token.getValue());
				}
			}
			transaction.add((SolrUserCredential) credential.withAccessTokens(validTokens));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			// TODO: Exception
			e.printStackTrace();
		}
	}

	public LogicalSearchQuery getUserCredentialsWithExpiredTokensQuery(LocalDateTime now) {
		return new LogicalSearchQuery(
				from(schemas.credentialSchemaType()).where(schemas.credentialTokenExpirations()).isGreaterThan(now));
	}

	@Override
	public void rewrite() {
		// Nothing to be done
	}

	@Override
	public void initialize() {
		// Nothing to be done
	}

	@Override
	public void close() {
		// Nothing to be done
	}

	@Override
	public void systemCollectionCreated() {
		MetadataSchemasManager manager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypesBuilder builder = manager.modify(Collection.SYSTEM_COLLECTION);
		createUserCredentialSchema(builder);
		try {
			manager.saveUpdateSchemaTypes(builder);
		} catch (OptimisticLocking e) {
			systemCollectionCreated();
		}
	}

	private void createUserCredentialSchema(MetadataSchemaTypesBuilder builder) {
		MetadataSchemaBuilder credentials = builder.createNewSchemaType(SolrUserCredential.SCHEMA_TYPE).getDefaultSchema();

		credentials.createUndeletable(SolrUserCredential.USERNAME).setType(MetadataValueType.STRING)
				.setDefaultRequirement(true).setUniqueValue(true).setUnmodifiable(true);
		credentials.createUndeletable(SolrUserCredential.FIRST_NAME).setType(MetadataValueType.STRING);
		credentials.createUndeletable(SolrUserCredential.LAST_NAME).setType(MetadataValueType.STRING);
		credentials.createUndeletable(SolrUserCredential.EMAIL).setType(MetadataValueType.STRING)
				.setUniqueValue(true).addValidator(EmailValidator.class);
		credentials.createUndeletable(SolrUserCredential.SERVICE_KEY).setType(MetadataValueType.STRING).setEncrypted(true);
		credentials.createUndeletable(SolrUserCredential.TOKEN_KEYS).setType(MetadataValueType.STRING).setMultivalue(true)
				.setEncrypted(true);
		credentials.createUndeletable(SolrUserCredential.TOKEN_EXPIRATIONS).setType(MetadataValueType.DATE_TIME)
				.setMultivalue(true);
		credentials.createUndeletable(SolrUserCredential.SYSTEM_ADMIN).setType(MetadataValueType.BOOLEAN)
				.setDefaultRequirement(true).setDefaultValue(false);
		credentials.createUndeletable(SolrUserCredential.COLLECTIONS).setType(MetadataValueType.STRING).setMultivalue(true);
		credentials.createUndeletable(SolrUserCredential.GLOBAL_GROUPS).setType(MetadataValueType.STRING).setMultivalue(true);
		credentials.createUndeletable(SolrUserCredential.STATUS).defineAsEnum(UserCredentialStatus.class)
				.setDefaultRequirement(true);
		credentials.createUndeletable(SolrUserCredential.DOMAIN).setType(MetadataValueType.STRING);
		credentials.createUndeletable(SolrUserCredential.MS_EXCHANGE_DELEGATE_LIST).setType(MetadataValueType.STRING)
				.setMultivalue(true);
		credentials.createUndeletable(SolrUserCredential.DN).setType(MetadataValueType.STRING);
	}

	private LogicalSearchQuery getQueryFilteredByStatus(UserCredentialStatus status) {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.credentialStatus()).isEqualTo(status))
				.sortAsc(schemas.credentialUsername());
	}
}
