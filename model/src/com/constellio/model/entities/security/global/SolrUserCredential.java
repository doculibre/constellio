package com.constellio.model.entities.security.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class SolrUserCredential extends RecordWrapper implements UserCredential {
	public static final String SCHEMA_TYPE = "userCredential";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String USERNAME = "username";
	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME = "lastname";
	public static final String EMAIL = "email";
	public static final String PERSONAL_EMAILS = "personalEmails";
	public static final String SERVICE_KEY = "serviceKey";
	public static final String TOKEN_KEYS = "tokenKeys";
	public static final String TOKEN_EXPIRATIONS = "tokenExpirations";
	public static final String SYSTEM_ADMIN = "systemAdmin";
	public static final String COLLECTIONS = "collections";
	public static final String GLOBAL_GROUPS = "globalGroups";
	public static final String STATUS = "status";
	public static final String DOMAIN = "domain";
	public static final String MS_EXCHANGE_DELEGATE_LIST = "msExchangeDelegateList";
	public static final String DN = "dn";
	public static final String PHONE = "phone";
	public static final String FAX = "fax";
	public static final String JOB_TITLE = "jobTitle";
	public static final String ADDRESS = "address";
	public static final String AGENT_STATUS = "agentStatus";

	public SolrUserCredential(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	@Override
	public String getUsername() {
		return get(USERNAME);
	}

	public SolrUserCredential setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	@Override
	public String getFirstName() {
		return get(FIRST_NAME);
	}

	public SolrUserCredential setFirstName(String firstName) {
		set(FIRST_NAME, firstName);
		return this;
	}

	@Override
	public String getLastName() {
		return get(LAST_NAME);
	}

	public SolrUserCredential setLastName(String lastName) {
		set(LAST_NAME, lastName);
		return this;
	}

	@Override
	public String getEmail() {
		return get(EMAIL);
	}

	@Override
	public List<String> getPersonalEmails() {
		return get(PERSONAL_EMAILS);
	}

	public SolrUserCredential setEmail(String email) {
		set(EMAIL, email);
		return this;
	}

	public SolrUserCredential setPersonalEmails(List<String> personalEmails) {
		set(PERSONAL_EMAILS, personalEmails);
		return this;
	}

	@Override
	public String getServiceKey() {
		return get(SERVICE_KEY);
	}

	public SolrUserCredential setServiceKey(String serviceKey) {
		set(SERVICE_KEY, serviceKey);
		return this;
	}

	@Override
	public Map<String, LocalDateTime> getAccessTokens() {
		HashMap<String, LocalDateTime> result = new HashMap<>();
		Iterator<LocalDateTime> expirations = getTokenExpirations().iterator();
		for (String token : getTokenKeys()) {
			result.put(token, expirations.next());
		}
		return result;
	}

	public SolrUserCredential setAccessTokens(Map<String, LocalDateTime> tokens) {
		if (tokens != null) {
			List<String> keys = new ArrayList<>(tokens.size());
			List<LocalDateTime> expirations = new ArrayList<>(tokens.size());
			for (Entry<String, LocalDateTime> token : tokens.entrySet()) {
				keys.add(token.getKey());
				expirations.add(token.getValue());
			}
			set(TOKEN_KEYS, keys);
			set(TOKEN_EXPIRATIONS, expirations);
		}
		return this;
	}

	@Override
	public List<String> getTokenKeys() {
		return getList(TOKEN_KEYS);
	}

	public List<LocalDateTime> getTokenExpirations() {
		return getList(TOKEN_EXPIRATIONS);
	}

	@Override
	public boolean isSystemAdmin() {
		return get(SYSTEM_ADMIN);
	}

	public SolrUserCredential setSystemAdmin(boolean systemAdmin) {
		set(SYSTEM_ADMIN, systemAdmin);
		return this;
	}

	@Override
	public List<String> getCollections() {
		return getList(COLLECTIONS);
	}

	public SolrUserCredential setCollections(List<String> collections) {
		set(COLLECTIONS, collections);
		return this;
	}

	@Override
	public List<String> getGlobalGroups() {
		return getList(GLOBAL_GROUPS);
	}

	public SolrUserCredential setGlobalGroups(List<String> globalGroups) {
		set(GLOBAL_GROUPS, globalGroups);
		return this;
	}

	@Override
	public UserCredentialStatus getStatus() {
		return get(STATUS);
	}

	public SolrUserCredential setStatus(UserCredentialStatus status) {
		set(STATUS, status);
		return this;
	}

	@Override
	public String getDomain() {
		return get(DOMAIN);
	}

	public SolrUserCredential setDomain(String domain) {
		set(DOMAIN, domain);
		return this;
	}

	@Override
	public List<String> getMsExchDelegateListBL() {
		return getList(MS_EXCHANGE_DELEGATE_LIST);
	}

	public SolrUserCredential setMsExchDelegateListBL(List<String> delegateList) {
		set(MS_EXCHANGE_DELEGATE_LIST, delegateList);
		return this;
	}

	@Override
	public String getDn() {
		return get(DN);
	}

	public SolrUserCredential setDn(String dn) {
		set(DN, dn);
		return this;
	}

	@Override
	public String getJobTitle() {
		return get(JOB_TITLE);
	}

	@Override
	public String getPhone() {
		return get(PHONE);
	}

	@Override
	public String getFax() {
		return get(FAX);
	}

	@Override
	public String getAddress() {
		return get(ADDRESS);
	}

	@Override
	public UserCredential withCollections(List<String> collections) {
		return setCollections(collections);
	}

	@Override
	public UserCredential withRemovedCollection(String collection) {
		List<String> collections = new ArrayList<>(getCollections());
		collections.remove(collection);
		return setCollections(collections);
	}

	@Override
	public UserCredential withNewGlobalGroup(String newGroup) {
		List<String> groups = new ArrayList<>(getGlobalGroups());
		groups.add(newGroup);
		return setGlobalGroups(groups);
	}

	@Override
	public UserCredential withRemovedGlobalGroup(String removedGroup) {
		List<String> groups = new ArrayList<>(getGlobalGroups());
		groups.remove(removedGroup);
		return setGlobalGroups(groups);
	}

	@Override
	public UserCredential withGlobalGroups(List<String> globalGroups) {
		return setGlobalGroups(globalGroups);
	}

	@Override
	public UserCredential withFirstName(String firstName) {
		return setFirstName(firstName);
	}

	@Override
	public UserCredential withLastName(String lastName) {
		return setLastName(lastName);
	}

	@Override
	public UserCredential withEmail(String email) {
		return setEmail(email);
	}

	@Override
	public UserCredential withPersonalEmails(List<String> personalEmails) {
		return setPersonalEmails(personalEmails);
	}

	@Override
	public UserCredential withStatus(UserCredentialStatus status) {
		return setStatus(status);
	}

	@Override
	public UserCredential withAccessToken(String token, LocalDateTime dateTime) {
		Map<String, LocalDateTime> tokens = getAccessTokens();
		tokens.put(token, dateTime);
		return setAccessTokens(tokens);
	}

	@Override
	public UserCredential withRemovedToken(String token) {
		Map<String, LocalDateTime> tokens = getAccessTokens();
		tokens.remove(token);
		return setAccessTokens(tokens);
	}

	@Override
	public UserCredential withAccessTokens(Map<String, LocalDateTime> tokens) {
		return setAccessTokens(tokens);
	}

	@Override
	public UserCredential withNewCollection(String collection) {
		List<String> collections = new ArrayList<>(getCollections());
		if (!collections.contains(collection)) {
			collections.add(collection);
		}
		return setCollections(collections);
	}

	@Override
	public UserCredential withSystemAdminPermission() {
		return setSystemAdmin(true);
	}

	@Override
	public UserCredential withServiceKey(String serviceKey) {
		return setServiceKey(serviceKey);
	}

	@Override
	public UserCredential withMsExchDelegateListBL(List<String> msExchDelegateListBL) {
		return setMsExchDelegateListBL(msExchDelegateListBL);
	}

	@Override
	public UserCredential withDN(String dn) {
		return setDn(dn);
	}

	@Override
	public UserCredential withPhone(String phone) {
		set(PHONE, phone);
		return this;
	}

	@Override
	public UserCredential withJobTitle(String jobTitle) {
		set(JOB_TITLE, jobTitle);
		return this;
	}

	@Override
	public UserCredential withFax(String fax) {
		set(FAX, fax);
		return this;
	}

	@Override
	public UserCredential withAddress(String address) {
		set(ADDRESS, address);
		return this;
	}

	public AgentStatus getAgentStatus() {
		return getEnumWithDefaultValue(AGENT_STATUS, AgentStatus.DISABLED);
	}

	public SolrUserCredential setAgentStatus(AgentStatus agentStatus) {
		return set(AGENT_STATUS, agentStatus);
	}

}
