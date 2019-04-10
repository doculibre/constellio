package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.List;

public class RMTask extends Task {
	public static final String ADMINISTRATIVE_UNIT = "administrativeUnit";
	public static final String LINKED_FOLDERS = "linkedFolders";
	public static final String LINKED_DOCUMENTS = "linkedDocuments";
	public static final String LINKED_CONTAINERS = "linkedContainers";
	public static final String CREATED_AUTHORIZATIONS = "createdAuthorizations";

	public RMTask(Record record, MetadataSchemaTypes types) {
		super(record, types);
	}

	public RMTask(Task task) {
		super(task.getWrappedRecord(), task.getMetadataSchemaTypes());
	}

	public String getAdministrativeUnit() {
		return get(ADMINISTRATIVE_UNIT);
	}

	public RMTask setAdministrativeUnit(String administrativeUnitId) {
		set(ADMINISTRATIVE_UNIT, administrativeUnitId);
		return this;
	}

	public RMTask setAdministrativeUnit(Record administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public RMTask setAdministrativeUnit(AdministrativeUnit administrativeUnit) {
		set(ADMINISTRATIVE_UNIT, administrativeUnit);
		return this;
	}

	public List<String> getLinkedFolders() {
		return getList(LINKED_FOLDERS);
	}

	@Override
	public RMTask setLinkedFolders(List<?> linkedFolders) {
		set(LINKED_FOLDERS, linkedFolders);
		return this;
	}

	public List<String> getLinkedDocuments() {
		return getList(LINKED_DOCUMENTS);
	}

	@Override
	public RMTask setLinkedDocuments(List<?> linkedDocuments) {
		set(LINKED_DOCUMENTS, linkedDocuments);
		return this;
	}

	public List<String> getLinkedContainers() {
		return getList(LINKED_CONTAINERS);
	}

	@Override
	public RMTask setLinkedContainers(List<?> linkedContainers) {
		set(LINKED_CONTAINERS, linkedContainers);
		return this;
	}

	public RMTask set(Metadata metadata, Object value) {
		super.set(metadata, value);
		return this;
	}

	public List<String> getCreatedAuthorizations() {
		return getList(CREATED_AUTHORIZATIONS);
	}

	public Task setCreatedAuthorizations(List<String> authorizationIds) {
		set(CREATED_AUTHORIZATIONS, authorizationIds);
		return this;
	}

	public Task addCreatedAuthorizations(List<String> authorizationIds) {
		List<String> list = new ArrayList<>(this.<String>getList(CREATED_AUTHORIZATIONS));
		for (String authorizationId : authorizationIds) {
			if (!list.contains(authorizationId)) {
				list.add(authorizationId);
				set(CREATED_AUTHORIZATIONS, list);
			}
		}
		return this;
	}

	public Task removeCreatedAuthorizations(List<String> authorizationIds) {
		List<String> list = new ArrayList<>(this.<String>getList(CREATED_AUTHORIZATIONS));
		list.removeAll(authorizationIds);
		set(CREATED_AUTHORIZATIONS, list);
		return this;
	}
}
