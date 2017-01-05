package com.constellio.model.entities.security.global;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.model.entities.records.wrappers.User;

public class AuthorizationAddRequest {

	private String id;
	private String collection;
	private List<String> principals;
	private String target;
	private LocalDate start, end;
	private User executedBy;

	private AuthorizationAddRequest() {
	}

	public static AuthorizationAddRequest authorizationAddRequestOnRecord(String target) {
		AuthorizationAddRequest request = new AuthorizationAddRequest();
		request.target = target;
		return request;
	}

	public String getId() {
		return id;
	}

	public AuthorizationAddRequest setId(String id) {
		this.id = id;
		return this;
	}

	public String getCollection() {
		return collection;
	}

	public AuthorizationAddRequest setCollection(String collection) {
		this.collection = collection;
		return this;
	}

	public List<String> getPrincipals() {
		return principals;
	}

	public AuthorizationAddRequest setPrincipals(List<String> principals) {
		this.principals = principals;
		return this;
	}

	public String getTarget() {
		return target;
	}

	public LocalDate getStart() {
		return start;
	}

	public AuthorizationAddRequest setStart(LocalDate start) {
		this.start = start;
		return this;
	}

	public LocalDate getEnd() {
		return end;
	}

	public AuthorizationAddRequest setEnd(LocalDate end) {
		this.end = end;
		return this;
	}

	public User getExecutedBy() {
		return executedBy;
	}

	public AuthorizationAddRequest setExecutedBy(User executedBy) {
		this.executedBy = executedBy;
		return this;
	}
}
