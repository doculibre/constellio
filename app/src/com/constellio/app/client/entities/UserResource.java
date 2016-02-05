package com.constellio.app.client.entities;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.constellio.model.entities.security.global.UserCredentialStatus;

@XmlRootElement
public class UserResource {

	private String username;

	private String firstName;

	private String lastName;

	private String email;

	private String serviceKey;

	private Map<String, String> tokens;

	private boolean systemAdmin;

	private List<String> globalGroups;

	private List<String> collections;

	private UserCredentialStatus status = UserCredentialStatus.ACTIVE;

	private String domain;

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

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public Map<String, String> getTokens() {
		return tokens;
	}

	public void setTokens(Map<String, String> tokens) {
		this.tokens = tokens;
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

	public List<String> getCollections() {
		return collections;
	}

	public void setCollections(List<String> collections) {
		this.collections = collections;
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
