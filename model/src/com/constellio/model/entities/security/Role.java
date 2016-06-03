package com.constellio.model.entities.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Role {

	private final String code;

	private final String title;

	private final String collection;

	private final List<String> operationPermissions;

	public static final String READ = "READ";

	public static final Role READ_ROLE = new Role(READ);

	public static final String WRITE = "WRITE";

	public static final Role WRITE_ROLE = new Role(WRITE);

	public static final String DELETE = "DELETE";

	public static final Role DELETE_ROLE = new Role(DELETE);

	public Role(String collection, String code, String title, List<String> operationPermissions) {
		this.collection = collection;
		this.code = code;
		this.title = title;
		this.operationPermissions = Collections.unmodifiableList(operationPermissions);
	}

	private Role(String code) {
		this.collection = null;
		this.code = code;
		this.title = code;
		this.operationPermissions = Collections.emptyList();
		//this.contentPermissions = Arrays.asList(contentPermissions);
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getOperationPermissions() {
		return operationPermissions;
	}

	public String getCollection() {
		return collection;
	}

	public boolean isContentPermissionRole() {
		return operationPermissions.isEmpty();
	}

	public boolean hasOperationPermission(String wantedOperationPermission) {
		return operationPermissions.contains(wantedOperationPermission);
	}

	public Role withTitle(String title) {
		return new Role(collection, code, title, operationPermissions);
	}

	public Role withPermissions(List<String> operationPermissions) {
		return new Role(collection, code, title, operationPermissions);
	}

	public Role withNewPermissions(List<String> operationPermissions) {
		List<String> allPermissions = new ArrayList<>(this.operationPermissions);
		allPermissions.addAll(operationPermissions);
		return new Role(collection, code, title, allPermissions);
	}
}
