package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.security.global.UserCredentialStatus;

@SuppressWarnings("serial")
public class UserCredentialVO implements Serializable {

	String username;

	String firstName;

	String lastName;

	String email;

	String personalEmails;

	String serviceKey;

	Map<String, LocalDateTime> tokensMap;

	boolean systemAdmin;

	List<String> globalGroups;

	Set<String> collections;

	String password;

	String confirmPassword;

	UserCredentialStatus status;

	String domain;

	public UserCredentialVO() {
		this.status = UserCredentialStatus.ACTIVE;
	}

	public UserCredentialVO(String username, String firstName, String lastName, String email, String personalEmails,
			String serviceKey, boolean systemAdmin, List<String> globalGroups, Set<String> collections,
			Map<String, LocalDateTime> tokens, String password, String confirmPassword, UserCredentialStatus status) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.personalEmails = personalEmails;
		this.serviceKey = serviceKey;
		this.systemAdmin = systemAdmin;
		this.globalGroups = Collections.unmodifiableList(globalGroups);
		this.collections = collections;
		this.tokensMap = tokens;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPersonalEmails() {
		return personalEmails;
	}

	public void setPersonalEmails(String personalEmails) {
		this.personalEmails = personalEmails;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public Map<String, LocalDateTime> getTokensMap() {
		return tokensMap;
	}

	public void setTokensMap(Map<String, LocalDateTime> tokensMap) {
		this.tokensMap = tokensMap;
	}

	public boolean isSystemAdmin() {
		return systemAdmin;
	}

	public void setSystemAdmin(boolean systemAdmin) {
		this.systemAdmin = systemAdmin;
	}

	public List<String> getGlobalGroups() {
		return globalGroups;
	}

	public void setGlobalGroups(List<String> globalGroups) {
		this.globalGroups = globalGroups;
	}

	public Set<String> getCollections() {
		return collections;
	}

	public String getStringCollections() {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		Iterator iterator = collections.iterator();
		while (iterator.hasNext()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(iterator.next());
		}
		return sb.toString();
	}

	public void setCollections(Set<String> collections) {
		this.collections = collections;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public UserCredentialStatus getStatus() {
		return status;
	}

	public void setStatus(UserCredentialStatus status) {
		this.status = status;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
