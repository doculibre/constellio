package com.constellio.model.entities.security.global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

public class XmlUserCredential implements UserCredential {
	private final String username;
	private final String firstName;
	private final String lastName;
	private final String email;
	private final String personalEmails;
	private final String serviceKey;
	private final Map<String, LocalDateTime> tokensMap;
	private final boolean systemAdmin;
	private final List<String> globalGroups;
	private final List<String> collections;
	private final String title;
	private final UserCredentialStatus status;
	private final String domain;
	private final List<String> msExchDelegateListBL;
	private final String dn;

	public XmlUserCredential(String username, String firstName, String lastName, String email, List<String> globalGroups,
			List<String> collections, UserCredentialStatus status) {
		this(username, firstName, lastName, email, globalGroups, collections, status, "", null, null);
	}

	public XmlUserCredential(String username, String firstName, String lastName, String email, List<String> globalGroups,
			List<String> collections, UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.title = firstName + " " + lastName;
		this.email = email;
		this.personalEmails = null;
		this.serviceKey = null;
		this.tokensMap = new HashMap<>();
		this.systemAdmin = false;
		this.globalGroups = Collections.unmodifiableList(globalGroups);
		this.collections = Collections.unmodifiableList(collections);
		this.status = status;
		this.domain = domain;
		this.msExchDelegateListBL = msExchDelegateListBL;
		this.dn = dn;
	}

	public XmlUserCredential(String username, String firstName, String lastName, String email, String serviceKey,
			boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
			UserCredentialStatus status) {
		this(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections, tokens, status, "", null,
				null);
	}

	public XmlUserCredential(String username, String firstName, String lastName, String email, String serviceKey,
			boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
			UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.title = firstName + " " + lastName;
		this.email = email;
		this.personalEmails = null;
		this.serviceKey = serviceKey;
		this.systemAdmin = systemAdmin;
		this.globalGroups = Collections.unmodifiableList(globalGroups);
		this.collections = Collections.unmodifiableList(collections);
		this.tokensMap = tokens;
		this.status = status;
		this.domain = domain;
		this.msExchDelegateListBL = msExchDelegateListBL;
		this.dn = dn;
	}

	public XmlUserCredential(String username, String firstName, String lastName, String email, String personalEmails, String serviceKey,
							 boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
							 UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.title = firstName + " " + lastName;
		this.email = email;
		this.personalEmails = personalEmails;
		this.serviceKey = serviceKey;
		this.systemAdmin = systemAdmin;
		this.globalGroups = Collections.unmodifiableList(globalGroups);
		this.collections = Collections.unmodifiableList(collections);
		this.tokensMap = tokens;
		this.status = status;
		this.domain = domain;
		this.msExchDelegateListBL = msExchDelegateListBL;
		this.dn = dn;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public String getPersonalEmails() {
		return personalEmails;
	}

	@Override
	public String getServiceKey() {
		return serviceKey;
	}

	@Override
	public Map<String, LocalDateTime> getAccessTokens() {
		return tokensMap;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public List<String> getTokenKeys() {
		List<String> tokens = new ArrayList<>();
		for (Map.Entry<String, LocalDateTime> token : tokensMap.entrySet()) {
			tokens.add(token.getKey());
		}
		return tokens;
	}

	@Override
	public boolean isSystemAdmin() {
		return systemAdmin;
	}

	@Override
	public List<String> getCollections() {
		return collections;
	}

	@Override
	public List<String> getGlobalGroups() {
		return globalGroups;
	}

	@Override
	public UserCredentialStatus getStatus() {
		return status;
	}

	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public List<String> getMsExchDelegateListBL() {
		return msExchDelegateListBL;
	}

	@Override
	public UserCredential withCollections(List<String> collections) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withRemovedCollection(String collection) {
		List<String> newCollections = new ArrayList<>(collections);
		collections.remove(collection);
		return withCollections(newCollections);
	}

	@Override
	public UserCredential withNewGlobalGroup(String newGroup) {

		List<String> groups = new ArrayList<>(this.globalGroups);
		groups.add(newGroup);

		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, groups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withRemovedGlobalGroup(String removedGroup) {

		List<String> groups = new ArrayList<>(this.globalGroups);
		groups.remove(removedGroup);

		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, groups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withGlobalGroups(List<String> globalGroups) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withFirstName(String firstName) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withLastName(String lastName) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withEmail(String email) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withPersonalEmails(String personalEmails) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withStatus(UserCredentialStatus status) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withAccessToken(String token, LocalDateTime dateTime) {
		Map<String, LocalDateTime> tokens = new HashMap<>();
		tokens.put(token, dateTime);
		return withAccessTokens(tokens);
	}

	@Override
	public UserCredential withRemovedToken(String key) {
		Map<String, LocalDateTime> allTokens = new HashMap<>();
		allTokens.putAll(this.getAccessTokens());
		allTokens.remove(key);
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				allTokens, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withAccessTokens(Map<String, LocalDateTime> tokens) {
		Map<String, LocalDateTime> allTokens = new HashMap<>();
		allTokens.putAll(this.getAccessTokens());
		while (allTokens.size() >= 50) {

			String olderToken = null;
			LocalDateTime dateTime = null;
			for (Map.Entry<String, LocalDateTime> token : allTokens.entrySet()) {
				if (dateTime == null || dateTime.isAfter(token.getValue())) {
					olderToken = token.getKey();
					dateTime = token.getValue();
				}
			}
			allTokens.remove(olderToken);

		}
		for (Map.Entry<String, LocalDateTime> token : tokens.entrySet()) {
			allTokens.put(token.getKey(), token.getValue());
		}

		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				allTokens, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return username;
	}

	@Override
	public UserCredential withNewCollection(String collection) {
		List<String> collections = new ArrayList<>();
		collections.addAll(this.collections);
		if (!collections.contains(collection)) {
			collections.add(collection);
			return withCollections(collections);
		} else {
			return this;
		}
	}

	@Override
	public UserCredential withSystemAdminPermission() {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, true, globalGroups, collections, tokensMap,
				status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withServiceKey(String serviceKey) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withMsExchDelegateListBL(List<String> msExchDelegateListBL) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	@Override
	public UserCredential withDN(String dn) {
		return new XmlUserCredential(username, firstName, lastName, email, personalEmails, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL,
				dn);
	}

	@Override
	public String getDn() {
		return dn;
	}
}
