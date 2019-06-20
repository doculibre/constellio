package com.constellio.model.entities.security.global;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Arrays.asList;

public class UserCredential extends RecordWrapper {
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
	public static final String HAS_AGREED_TO_PRIVACY_POLICY = "hasAgreedToPrivacyPolicy";
	public static final String DO_NOT_RECEIVE_EMAILS = "doNotReceiveEmails";
	public static final String HAS_SEEN_MESSAGE = "hasSeenMessage";

	public UserCredential(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public UserCredential setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	public String getFirstName() {
		return get(FIRST_NAME);
	}

	public UserCredential setFirstName(String firstName) {
		set(FIRST_NAME, firstName);
		return this;
	}

	public String getLastName() {
		return get(LAST_NAME);
	}

	public UserCredential setLastName(String lastName) {
		set(LAST_NAME, lastName);
		return this;
	}

	public String getEmail() {
		return get(EMAIL);
	}

	public List<String> getPersonalEmails() {
		return get(PERSONAL_EMAILS);
	}

	public UserCredential setEmail(String email) {
		set(EMAIL, email);
		return this;
	}

	public UserCredential setPersonalEmails(List<String> personalEmails) {
		set(PERSONAL_EMAILS, personalEmails);
		return this;
	}

	public String getServiceKey() {
		return get(SERVICE_KEY);
	}

	public UserCredential setServiceKey(String serviceKey) {
		set(SERVICE_KEY, serviceKey);
		return this;
	}

	public Map<String, LocalDateTime> getAccessTokens() {
		HashMap<String, LocalDateTime> result = new HashMap<>();
		Iterator<LocalDateTime> expirations = getTokenExpirations().iterator();
		for (String token : getTokenKeys()) {
			result.put(token, expirations.next());
		}
		return result;
	}

	public UserCredential setAccessTokens(Map<String, LocalDateTime> tokens) {
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

	public List<String> getTokenKeys() {
		return getList(TOKEN_KEYS);
	}

	public List<LocalDateTime> getTokenExpirations() {
		return getList(TOKEN_EXPIRATIONS);
	}

	public boolean isSystemAdmin() {
		return get(SYSTEM_ADMIN);
	}

	public UserCredential setSystemAdmin(boolean systemAdmin) {
		set(SYSTEM_ADMIN, systemAdmin);
		return this;
	}

	public List<String> getCollections() {
		return getList(COLLECTIONS);
	}

	public UserCredential setCollections(List<String> collections) {
		set(COLLECTIONS, collections);
		return this;
	}

	public UserCredential setCollections(String... collections) {
		set(COLLECTIONS, asList(collections));
		return this;
	}

	public List<String> getGlobalGroups() {
		return getList(GLOBAL_GROUPS);
	}

	public UserCredential setGlobalGroups(List<String> globalGroups) {
		set(GLOBAL_GROUPS, globalGroups);
		return this;
	}

	public UserCredentialStatus getStatus() {
		return get(STATUS);
	}

	public UserCredential setStatus(UserCredentialStatus status) {
		set(STATUS, status);
		return this;
	}

	public String getDomain() {
		return get(DOMAIN);
	}

	public UserCredential setDomain(String domain) {
		set(DOMAIN, domain);
		return this;
	}

	public List<String> getMsExchDelegateListBL() {
		return getList(MS_EXCHANGE_DELEGATE_LIST);
	}

	public UserCredential setMsExchDelegateListBL(List<String> delegateList) {
		set(MS_EXCHANGE_DELEGATE_LIST, delegateList);
		return this;
	}

	public String getDn() {
		return get(DN);
	}

	public boolean isActiveUser() {
		return getStatus() == UserCredentialStatus.ACTIVE || getStatus() == null;
	}

	public UserCredential setDn(String dn) {
		set(DN, dn);
		return this;
	}

	public String getJobTitle() {
		return get(JOB_TITLE);
	}

	public String getPhone() {
		return get(PHONE);
	}

	public String getFax() {
		return get(FAX);
	}

	public String getAddress() {
		return get(ADDRESS);
	}

	public UserCredential removeCollection(String collection) {
		List<String> collections = new ArrayList<>(getCollections());
		collections.remove(collection);
		return setCollections(collections);
	}

	public UserCredential addGlobalGroup(String newGroup) {
		List<String> groups = new ArrayList<>(getGlobalGroups());
		groups.add(newGroup);
		return setGlobalGroups(groups);
	}

	public UserCredential removeGlobalGroup(String removedGroup) {
		List<String> groups = new ArrayList<>(getGlobalGroups());
		groups.remove(removedGroup);
		return setGlobalGroups(groups);
	}

	public UserCredential addAccessToken(String token, LocalDateTime dateTime) {
		Map<String, LocalDateTime> tokens = getAccessTokens();
		tokens.put(token, dateTime);
		return setAccessTokens(tokens);
	}

	public UserCredential removeAccessToken(String token) {
		Map<String, LocalDateTime> tokens = getAccessTokens();
		tokens.remove(token);
		return setAccessTokens(tokens);
	}


	public UserCredential addCollection(String collection) {
		List<String> collections = new ArrayList<>(getCollections());
		if (!collections.contains(collection)) {
			collections.add(collection);
		}
		return setCollections(collections);
	}

	public UserCredential setSystemAdminEnabled() {
		return setSystemAdmin(true);
	}

	public UserCredential setDN(String dn) {
		return setDn(dn);
	}

	public UserCredential setPhone(String phone) {
		set(PHONE, phone);
		return this;
	}

	public UserCredential setJobTitle(String jobTitle) {
		set(JOB_TITLE, jobTitle);
		return this;
	}

	public UserCredential setFax(String fax) {
		set(FAX, fax);
		return this;
	}

	public UserCredential setAddress(String address) {
		set(ADDRESS, address);
		return this;
	}

	public AgentStatus getAgentStatus() {
		return getEnumWithDefaultValue(AGENT_STATUS, AgentStatus.DISABLED);
	}

	public UserCredential setAgentStatus(AgentStatus agentStatus) {
		return set(AGENT_STATUS, agentStatus);
	}

	public boolean hasAgreedToPrivacyPolicy() {
		return Boolean.TRUE.equals(get(HAS_AGREED_TO_PRIVACY_POLICY));
	}
	public boolean hasSeenMessage() {
		return Boolean.TRUE.equals(get(HAS_SEEN_MESSAGE));
	}

	public UserCredential setAgreedPrivacyPolicy(Boolean hasAgreedToPrivacyPolicy) {
		set(HAS_AGREED_TO_PRIVACY_POLICY, hasAgreedToPrivacyPolicy);
		return this;
	}

	public boolean isNotReceivingEmails() {
		return Boolean.TRUE.equals(get(DO_NOT_RECEIVE_EMAILS));
	}
}
