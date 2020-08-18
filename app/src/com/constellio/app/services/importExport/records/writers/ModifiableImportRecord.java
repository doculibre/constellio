package com.constellio.app.services.importExport.records.writers;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.Schemas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ModifiableImportRecord {

	private String collection;
	private String schemaType;
	private String schema;
	private String previousSystemId;
	private Map<String, Object> fields = new HashMap<>();

	public ModifiableImportRecord() {

	}

	public ModifiableImportRecord(String collection, String schemaType, String previousSystemId) {
		this(collection, schemaType, previousSystemId, null);
	}

	public ModifiableImportRecord(String collection, String schemaType, String previousSystemId, String schema) {
		if (collection == null) {
			throw new RuntimeException("Collection is required");
		}
		if (schemaType == null) {
			throw new RuntimeException("Schema type is required");
		}
		if (previousSystemId == null) {
			throw new RuntimeException("Previous system id is required");
		}
		this.collection = collection;
		this.schemaType = schemaType;
		this.previousSystemId = previousSystemId;
		this.schema = schema;
	}

	public ModifiableImportRecord setCollection(String collection) {
		this.collection = collection;
		return this;
	}

	public ModifiableImportRecord setSchemaType(String schemaType) {
		this.schemaType = schemaType;
		return this;
	}

	public ModifiableImportRecord setPreviousSystemId(String previousSystemId) {
		this.previousSystemId = previousSystemId;
		return this;
	}

	public ModifiableImportRecord setFields(Map<String, Object> fields) {
		this.fields = fields;
		return this;
	}

	public ModifiableImportRecord code(Object value) {
		return addField(Schemas.CODE.getLocalCode(), value);
	}

	public ModifiableImportRecord title(Object value) {
		return addField(Schemas.TITLE.getLocalCode(), value);
	}

	public ModifiableImportRecord with(String metadataName, Object value) {
		return addField(metadataName, value);
	}

	public ModifiableImportRecord addField(String metadataName, Object value, Locale locale) {
		if (metadataName == null) {
			throw new RuntimeException("Metadata name is required");
		}

		if (value instanceof EnumWithSmallCode) {
			this.fields.put(metadataName, ((EnumWithSmallCode) value).getCode());
		} else if (isMultivaluedEnum(value)) {
			List<EnumWithSmallCode> enumList = (List<EnumWithSmallCode>) value;
			this.fields.put(metadataName, enumList.stream().map(v -> v.getCode()).collect(Collectors.toList()));

		} else if (Boolean.TRUE.equals(value)) {
			this.fields.put(metadataName, "true");

		} else if (Boolean.FALSE.equals(value)) {
			this.fields.put(metadataName, "false");

		} else if (value instanceof Number) {
			this.fields.put(metadataName, "" + value);

		} else {
			this.fields.put(metadataName, value == null ? "" : value);
		}

		return this;
	}

	public ModifiableImportRecord addField(String metadataName, Object value) {
		return addField(metadataName, value, null);
	}

	public String getCollection() {
		return collection;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public String getPreviousSystemId() {
		return previousSystemId;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public <T> T get(String key) {
		return (T) fields.get(key);
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public ModifiableImportRecord duplicateWithId(String newId) {
		ModifiableImportRecord copy = new ModifiableImportRecord(collection, schemaType, newId, schema);
		for (Map.Entry<String, Object> entry : fields.entrySet()) {
			if (copy != null) {
				if (entry.getValue() instanceof List) {
					copy.addField(entry.getKey(), new ArrayList((List) entry.getValue()));

				} else if (entry.getValue() instanceof Map) {
					copy.addField(entry.getKey(), new HashMap<>((Map) entry.getValue()));

				} else {
					copy.addField(entry.getKey(), entry.getValue());
				}

			}
		}
		return copy;
	}

	public boolean hasAnyField(String... fields) {
		for (String field : fields) {
			if (getFields().containsKey(field)) {
				return true;
			}
		}
		return false;
	}

	private boolean isMultivaluedEnum(Object value) {
		if (value instanceof List) {
			List valuesList = (List) value;
			return valuesList.size() >= 1 && valuesList.get(0) instanceof EnumWithSmallCode;
		}
		return false;
	}
}
