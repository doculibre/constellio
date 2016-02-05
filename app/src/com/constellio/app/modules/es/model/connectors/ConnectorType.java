package com.constellio.app.modules.es.model.connectors;

import java.util.List;
import java.util.Map;

import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorType extends RecordWrapper implements SchemaLinkingType {

	public static final String SCHEMA_TYPE = "connectorType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";

	public static final String CONNECTOR_CLASS_NAME = "connectorClassName";

	public static final String LINKED_SCHEMA = "linkedSchema";

	public static final String CODE_HTTP = "http";

	public static final String CODE_SMB = "smb";

	public static final String CODE_LDAP = "ldap";
	public static final String CODE_EXCHANGE = "exchange";

	public static final String CODE_SHAREPOINT = "sharepoint";

	public static final String DEFAULT_AVAILABLE_FIELDS = "defaultAvailableFields";

	public ConnectorType(Record record, MetadataSchemaTypes types) {
		super(record, types, "connectorType");
	}

	public String getCode() {
		return get(CODE);
	}

	public ConnectorType setCode(String code) {
		this.set(CODE, code);
		return this;
	}

	@Override
	public ConnectorType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getConnectorClassName() {
		return get(CONNECTOR_CLASS_NAME);
	}

	public ConnectorType setConnectorClassName(String connectorClassName) {
		this.set(CONNECTOR_CLASS_NAME, connectorClassName);
		return this;
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public ConnectorType setLinkedSchema(String connectorSchema) {
		set(LINKED_SCHEMA, connectorSchema);
		return this;
	}

	public List<ConnectorField> getDefaultAvailableConnectorFields() {
		return get(DEFAULT_AVAILABLE_FIELDS);
	}

	public ConnectorType setDefaultAvailableConnectorFields(List<ConnectorField> connectorFields) {
		set(DEFAULT_AVAILABLE_FIELDS, connectorFields);
		return this;
	}

	//TODO DELETE
	public Map<String, String> getDefaultAvailableProperties() {
		return null;
	}
}
