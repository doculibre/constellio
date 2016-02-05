package com.constellio.model.entities.security.global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

public class UserCredential {

	private final String username;

	private final String firstName;

	private final String lastName;

	private final String email;

	private final String serviceKey;

	private final Map<String, LocalDateTime> tokensMap;

	private final boolean systemAdmin;

	private final List<String> globalGroups;

	private final List<String> collections;

	private final String title;

	private final UserCredentialStatus status;

	private final String domain;

	private final List<String> msExchDelegateListBL;

	private String dn;

	public UserCredential(String username, String firstName, String lastName, String email, List<String> globalGroups,
			List<String> collections, UserCredentialStatus status) {
		this(username, firstName, lastName, email, globalGroups, collections, status, "", null, null);

	}

	public UserCredential(String username, String firstName, String lastName, String email, List<String> globalGroups,
			List<String> collections, UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.title = firstName + " " + lastName;
		this.email = email;
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

	public UserCredential(String username, String firstName, String lastName, String email, String serviceKey,
			boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
			UserCredentialStatus status) {
		this(username, firstName, lastName, email, serviceKey,
				systemAdmin, globalGroups, collections, tokens,
				status, "", null, null);
	}

	public UserCredential(String username, String firstName, String lastName, String email, String serviceKey,
			boolean systemAdmin, List<String> globalGroups, List<String> collections, Map<String, LocalDateTime> tokens,
			UserCredentialStatus status, String domain, List<String> msExchDelegateListBL, String dn) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.title = firstName + " " + lastName;
		this.email = email;
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

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public Map<String, LocalDateTime> getTokens() {
		return tokensMap;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getTokensKeys() {
		List<String> tokens = new ArrayList<>();
		for (Map.Entry<String, LocalDateTime> token : tokensMap.entrySet()) {
			tokens.add(token.getKey());
		}
		return tokens;
	}

	public boolean isSystemAdmin() {
		return systemAdmin;
	}

	public List<String> getCollections() {
		return collections;
	}

	public List<String> getGlobalGroups() {
		return globalGroups;
	}

	public UserCredentialStatus getStatus() {
		return status;
	}

	public String getDomain() {
		return domain;
	}

	public List<String> getMsExchDelegateListBL() {
		return msExchDelegateListBL;
	}

	public UserCredential withCollections(List<String> collections) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withNewGlobalGroup(String newGroup) {

		List<String> groups = new ArrayList<>(this.globalGroups);
		groups.add(newGroup);

		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, groups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withRemovedGlobalGroup(String removedGroup) {

		List<String> groups = new ArrayList<>(this.globalGroups);
		groups.remove(removedGroup);

		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, groups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withGlobalGroups(List<String> globalGroups) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withFirstName(String firstName) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withLastName(String lastName) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withEmail(String email) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withStatus(UserCredentialStatus status) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withToken(String token, LocalDateTime dateTime) {
		Map<String, LocalDateTime> tokens = new HashMap<>();
		tokens.put(token, dateTime);
		return withTokens(tokens);
	}

	public UserCredential withRemovedToken(String key) {
		Map<String, LocalDateTime> allTokens = new HashMap<>();
		allTokens.putAll(this.getTokens());
		allTokens.remove(key);
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				allTokens, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withTokens(Map<String, LocalDateTime> tokens) {
		Map<String, LocalDateTime> allTokens = new HashMap<>();
		allTokens.putAll(this.getTokens());
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

		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
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

	public UserCredential withSystemAdminPermission() {
		return new UserCredential(username, firstName, lastName, email, serviceKey, true, globalGroups, collections, tokensMap,
				status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withNewServiceKey() {
		String serviceKey = UUID.randomUUID().toString();
		return withServiceKey(serviceKey);
	}

	public UserCredential withServiceKey(String serviceKey) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withMsExchDelegateListBL(List<String> msExchDelegateListBL) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL, dn);
	}

	public UserCredential withDN(String dn) {
		return new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups, collections,
				tokensMap, status, domain, msExchDelegateListBL,
				dn);
	}

	public String getDn() {
		return dn;
	}

	private UserCredential setDn(String dn) {
		this.dn = dn;
		return this;
	}
}
