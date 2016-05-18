package com.constellio.app.modules.rm.wrappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Cart extends RecordWrapper {
	public static final String SCHEMA_TYPE = "cart";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String FOLDERS = "folders";
	public static final String DOCUMENTS = "documents";
	public static final String CONTAINERS = "containers";
	public static final String OWNER = "owner";

	public Cart(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getOwner() {
		return get(OWNER);
	}

	public Cart setOwner(String owner) {
		set(OWNER, owner);
		return this;
	}

	public Cart setOwner(User owner) {
		set(OWNER, owner);
		return this;
	}

	// TODO When implementing cart sharing, must add a list of 'shared with users'

	public List<String> getFolders() {
		return getList(FOLDERS);
	}

	public Cart setFolders(List<String> folders) {
		set(FOLDERS, folders);
		return this;
	}

	public Cart addFolders(List<String> folders) {
		return addWithoutDuplicates(FOLDERS, folders);
	}

	public Cart removeFolder(String id) {
		return removeFrom(FOLDERS, id);
	}

	public List<String> getDocuments() {
		return getList(DOCUMENTS);
	}

	public Cart setDocuments(List<String> documents) {
		set(DOCUMENTS, documents);
		return this;
	}

	public Cart addDocuments(List<String> documents) {
		return addWithoutDuplicates(DOCUMENTS, documents);
	}

	public Cart removeDocument(String id) {
		return removeFrom(DOCUMENTS, id);
	}

	public List<String> getContainers() {
		return getList(CONTAINERS);
	}

	public Cart setContainers(List<String> containers) {
		set(CONTAINERS, containers);
		return this;
	}

	public Cart addContainers(List<String> containers) {
		return addWithoutDuplicates(CONTAINERS, containers);
	}

	public Cart removeContainer(String id) {
		return removeFrom(CONTAINERS, id);
	}

	public List<String> getAllItems() {
		List<String> result = new ArrayList<>();
		result.addAll(getFolders());
		result.addAll(getDocuments());
		result.addAll(getContainers());
		return result;
	}

	public boolean isEmpty() {
		return getFolders().isEmpty() && getDocuments().isEmpty() && getContainers().isEmpty();
	}

	public Cart empty() {
		setFolders(new ArrayList<String>());
		setDocuments(new ArrayList<String>());
		setContainers(new ArrayList<String>());
		return this;
	}

	private Cart addWithoutDuplicates(String metadata, List<String> items) {
		Set<String> result = new HashSet<>(this.<String>getList(metadata));
		result.addAll(items);
		set(metadata, new ArrayList<>(result));
		return this;
	}

	private Cart removeFrom(String metadata, String id) {
		List<String> result = new ArrayList<>(this.<String>getList(metadata));
		result.remove(id);
		set(metadata, result);
		return this;
	}
}
