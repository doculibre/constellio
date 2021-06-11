package com.constellio.model.entities.security.global;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	public static final String SYNC_MODE = "syncMode";
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
	public static final String ENABLE_FACETS_APPLY_BUTTON = "enableFacetsApplyButton";
	public static final String HAS_READ_LAST_ALERT = "hasReadLastAlert";
	public static final String ELECTRONIC_SIGNATURE = "electronicSignature";
	public static final String ELECTRONIC_INITIALS = "electronicInitials";
	public static final String AZURE_USERNAME = "azureUsername";

	public static final String LICENSE_NOTIFICATION_VIEW_DATE = "licenseNotificationViewDate";
	public static final String NEW_VERSIONS_NOTIFICATION_VIEW_DATE = "newVersionsNotificationViewDate";
	public static final String LTS_END_OF_LIFE_NOTIFICATION_VIEW_DATE = "ltsEndOfLifeNotificationViewDate";
	public static final String NOT_A_LTS_NOTIFICATION_VIEW_DATE = "notALtsNotificationViewDate";

	public static final String TEAMS_FAVORITES_DISPLAY_ORDER = "teamsFavoritesDisplayOrder";
	public static final String TEAMS_HIDDEN_FAVORITES = "teamsHiddenFavorites";

	public static final String HAS_SEEN_LATEST_MESSAGE_AT_LOGIN = "hasSeenLatestMessageAtLogin";

	public UserCredential(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public UserCredential _setUsername(String username) {
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

	public String getAzureUsername() {
		return get(AZURE_USERNAME);
	}

	public UserCredential setAzureUsername(String azureUsername) {
		set(AZURE_USERNAME, azureUsername);
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
		return Boolean.TRUE.equals(get(SYSTEM_ADMIN));
	}

	public UserCredential setSystemAdmin(Boolean systemAdmin) {
		set(SYSTEM_ADMIN, Boolean.TRUE.equals(systemAdmin));
		return this;
	}

	public UserSyncMode getSyncMode() {
		return get(SYNC_MODE);
	}

	public UserCredential setSyncMode(UserSyncMode syncMode) {
		set(SYNC_MODE, syncMode);
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

	public UserCredential setAgreedPrivacyPolicy(Boolean hasAgreedToPrivacyPolicy) {
		set(HAS_AGREED_TO_PRIVACY_POLICY, hasAgreedToPrivacyPolicy);
		return this;
	}

	public boolean hasReadLastAlert() {
		return Boolean.TRUE.equals(get(HAS_READ_LAST_ALERT));
	}

	public UserCredential setReadLastAlert(Boolean hasReadLastAlert) {
		set(HAS_READ_LAST_ALERT, hasReadLastAlert);
		return this;
	}

	public LocalDate getLicenseNotificationViewDate() {
		return get(LICENSE_NOTIFICATION_VIEW_DATE);
	}

	public UserCredential setLicenseNotificationViewDate(LocalDate date) {
		set(LICENSE_NOTIFICATION_VIEW_DATE, date);
		return this;
	}

	public LocalDate getNewVersionsNotificationViewDate() {
		return get(NEW_VERSIONS_NOTIFICATION_VIEW_DATE);
	}

	public UserCredential setNewVersionsNotificationViewDate(LocalDate date) {
		set(NEW_VERSIONS_NOTIFICATION_VIEW_DATE, date);
		return this;
	}

	public LocalDate getLtsEndOfLifeNotificationViewDate() {
		return get(LTS_END_OF_LIFE_NOTIFICATION_VIEW_DATE);
	}

	public UserCredential setLtsEndOfLifeNotificationViewDate(LocalDate date) {
		set(LTS_END_OF_LIFE_NOTIFICATION_VIEW_DATE, date);
		return this;
	}

	public LocalDate getNotALtsNotificationViewDate() {
		return get(NOT_A_LTS_NOTIFICATION_VIEW_DATE);
	}

	public UserCredential setNotALtsNotificationViewDate(LocalDate date) {
		set(NOT_A_LTS_NOTIFICATION_VIEW_DATE, date);
		return this;
	}

	public UserCredential setNotReceivingEmails(Boolean value) {
		set(DO_NOT_RECEIVE_EMAILS, value);
		return this;
	}


	public UserCredential setApplyFacetsEnabled(Boolean value) {
		set(ENABLE_FACETS_APPLY_BUTTON, value);
		return this;
	}

	public boolean isNotReceivingEmails() {
		return Boolean.TRUE.equals(get(DO_NOT_RECEIVE_EMAILS));
	}

	public boolean isApplyFacetsEnabled() {
		return Boolean.TRUE.equals(ENABLE_FACETS_APPLY_BUTTON);
	}

	public Content getElectronicSignature() {
		return get(ELECTRONIC_SIGNATURE);
	}

	public UserCredential setElectronicSignature(Content content) {
		set(ELECTRONIC_SIGNATURE, content);
		return this;
	}

	public Content getElectronicInitials() {
		return get(ELECTRONIC_INITIALS);
	}

	public UserCredential setElectronicInitials(Content content) {
		set(ELECTRONIC_INITIALS, content);
		return this;
	}

	public boolean hasSeenLatestMessageAtLogin() {
		return Boolean.TRUE.equals(get(HAS_SEEN_LATEST_MESSAGE_AT_LOGIN));
	}

	public UserCredential setSeenLatestMessageAtLogin(Boolean seenLatestMessageAtLogin) {
		set(HAS_SEEN_LATEST_MESSAGE_AT_LOGIN, seenLatestMessageAtLogin);
		return this;
	}
}
