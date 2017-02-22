package com.constellio.app.modules.rm.wrappers;

import java.util.List;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class StorageSpace extends RecordWrapper {

	public static final String SCHEMA_TYPE = "storageSpace";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CAPACITY = "capacity";

	public static final String CODE = "code";

	public static final String DECOMMISSIONING_TYPE = "decommissioningType";

	public static final String DESCRIPTION = "description";

	public static final String PARENT_STORAGE_SPACE = "parentStorageSpace";

	public static final String TYPE = "type";

	public static final String COMMENTS = "comments";

	public static final String LINEAR_SIZE_ENTERED = "linearSizeEntered";

	public static final String LINEAR_SIZE = "linearSize";

	public static final String LINEAR_SIZE_SUM = "linearSizeSum";

	public static final String AVAILABLE_SIZE = "availableSize";

	public static final String CONTAINER_TYPE = "containerType";

	public static final String NUMBER_OF_CHILD = "numberOfChild";

	public StorageSpace(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public StorageSpace setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public StorageSpace setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public StorageSpace setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public Long getCapacity() {
		return get(CAPACITY) == null ? null : ((Double) get(CAPACITY)).longValue();
	}

	public StorageSpace setCapacity(int capacity) {
		set(CAPACITY, new Long(capacity));
		return this;
	}

	public StorageSpace setCapacity(Long capacity) {
		set(CAPACITY, capacity);
		return this;
	}

	public DecommissioningType getDecommissioningType() {
		return get(DECOMMISSIONING_TYPE);
	}

	public StorageSpace setDecommissioningType(DecommissioningType type) {
		set(DECOMMISSIONING_TYPE, type);
		return this;
	}

	public String getParentStorageSpace() {
		return get(PARENT_STORAGE_SPACE);
	}

	public StorageSpace setParentStorageSpace(String storageSpace) {
		set(PARENT_STORAGE_SPACE, storageSpace);
		return this;
	}

	public StorageSpace setParentStorageSpace(StorageSpace storageSpace) {
		set(PARENT_STORAGE_SPACE, storageSpace);
		return this;
	}

	public StorageSpace setParentStorageSpace(Record storageSpace) {
		set(PARENT_STORAGE_SPACE, storageSpace);
		return this;
	}

	public String getType() {
		return get(TYPE);
	}

	public StorageSpace setType(StorageSpaceType type) {
		set(TYPE, type);
		return this;
	}

	public StorageSpace setType(Record type) {
		set(TYPE, type);
		return this;
	}

	public StorageSpace setType(String type) {
		set(TYPE, type);
		return this;
	}

	public List<Comment> getComments() {
		return getList(COMMENTS);
	}

	public StorageSpace setComments(List<Comment> comments) {
		set(COMMENTS, comments);
		return this;
	}

	public Double getLinearSizeEntered() {
		return get(LINEAR_SIZE_ENTERED);
	}

	public StorageSpace setLinearSizeEntered(double linearSizeEntered) {
		set(LINEAR_SIZE_ENTERED, linearSizeEntered);
		return this;
	}

	public Double getLinearSizeSum() {
		return get(LINEAR_SIZE_SUM);
	}

	public Double getLinearSize() {
		return get(LINEAR_SIZE);
	}

	public Double getAvailableSize() {
		return get(AVAILABLE_SIZE);
	}

	public StorageSpace setContainerType(List<?> containerRecordType) {
		set(CONTAINER_TYPE, containerRecordType);
		return this;
	}

	public List<String> getContainerType() {
		return get(CONTAINER_TYPE);
	}

	public Double getNumberOfChild() {
		return get(NUMBER_OF_CHILD);
	}
}
