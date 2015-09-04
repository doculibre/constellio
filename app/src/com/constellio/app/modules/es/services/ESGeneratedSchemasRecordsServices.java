/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ESGeneratedSchemasRecordsServices extends SchemasRecordsServices {

	AppLayerFactory appLayerFactory;

	public ESGeneratedSchemasRecordsServices(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory.getModelLayerFactory());
		this.appLayerFactory = appLayerFactory;
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/



public ConnectorHttpDocument wrapConnectorHttpDocument(Record record) {
	return record == null ? null : new ConnectorHttpDocument(record, getTypes());
}

public List<ConnectorHttpDocument> wrapConnectorHttpDocuments(List<Record> records) {
	List<ConnectorHttpDocument> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorHttpDocument(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorHttpDocument> searchConnectorHttpDocuments(LogicalSearchQuery query) {
	return wrapConnectorHttpDocuments(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorHttpDocument> searchConnectorHttpDocuments(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorHttpDocument.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorHttpDocuments(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorHttpDocument getConnectorHttpDocument(String id) {
	return wrapConnectorHttpDocument(get(id));
}

public List<ConnectorHttpDocument> getConnectorHttpDocuments(List<String> ids) {
	return wrapConnectorHttpDocuments(get(ids));
}

public ConnectorHttpDocument getConnectorHttpDocumentWithLegacyId(String legacyId) {
	return wrapConnectorHttpDocument(getByLegacyId(connectorHttpDocument.schemaType(),  legacyId));
}

public ConnectorHttpDocument newConnectorHttpDocument() {
	return wrapConnectorHttpDocument(create(connectorHttpDocument.schema()));
}

public ConnectorHttpDocument newConnectorHttpDocumentWithId(String id) {
	return wrapConnectorHttpDocument(create(connectorHttpDocument.schema(), id));
}

public final SchemaTypeShortcuts_connectorHttpDocument_default connectorHttpDocument
	 = new SchemaTypeShortcuts_connectorHttpDocument_default("connectorHttpDocument_default");
public class SchemaTypeShortcuts_connectorHttpDocument_default extends SchemaTypeShortcuts {
	protected SchemaTypeShortcuts_connectorHttpDocument_default(String schemaCode) {
		super(schemaCode);
}

	public Metadata baseURI() {
		return metadata("baseURI");
	}

	public Metadata connector() {
		return metadata("connector");
	}

	public Metadata connectorType() {
		return metadata("connectorType");
	}

	public Metadata fetched() {
		return metadata("fetched");
	}

	public Metadata parsedContent() {
		return metadata("parsedContent");
	}

	public Metadata url() {
		return metadata("url");
	}
}
public ConnectorInstance wrapConnectorInstance(Record record) {
	return record == null ? null : new ConnectorInstance(record, getTypes());
}

public List<ConnectorInstance> wrapConnectorInstances(List<Record> records) {
	List<ConnectorInstance> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorInstance(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorInstance> searchConnectorInstances(LogicalSearchQuery query) {
	return wrapConnectorInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorInstance> searchConnectorInstances(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorInstance.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorInstance getConnectorInstance(String id) {
	return wrapConnectorInstance(get(id));
}

public List<ConnectorInstance> getConnectorInstances(List<String> ids) {
	return wrapConnectorInstances(get(ids));
}

public ConnectorInstance getConnectorInstanceWithCode(String code) {
	return wrapConnectorInstance(getByCode(connectorInstance.schemaType(), code));
}

public ConnectorInstance getConnectorInstanceWithLegacyId(String legacyId) {
	return wrapConnectorInstance(getByLegacyId(connectorInstance.schemaType(),  legacyId));
}

public ConnectorInstance newConnectorInstance() {
	return wrapConnectorInstance(create(connectorInstance.schema()));
}

public ConnectorInstance newConnectorInstanceWithId(String id) {
	return wrapConnectorInstance(create(connectorInstance.schema(), id));
}

public final SchemaTypeShortcuts_connectorInstance_default connectorInstance
	 = new SchemaTypeShortcuts_connectorInstance_default("connectorInstance_default");
public class SchemaTypeShortcuts_connectorInstance_default extends SchemaTypeShortcuts {
	protected SchemaTypeShortcuts_connectorInstance_default(String schemaCode) {
		super(schemaCode);
}

	public Metadata code() {
		return metadata("code");
	}

	public Metadata connectorType() {
		return metadata("connectorType");
	}

	public Metadata enabled() {
		return metadata("enabled");
	}

	public Metadata propertiesMapping() {
		return metadata("propertiesMapping");
	}

	public Metadata traversalCode() {
		return metadata("traversalCode");
	}
}
public ConnectorHttpInstance wrapConnectorHttpInstance(Record record) {
	return record == null ? null : new ConnectorHttpInstance(record, getTypes());
}

public List<ConnectorHttpInstance> wrapConnectorHttpInstances(List<Record> records) {
	List<ConnectorHttpInstance> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorHttpInstance(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorHttpInstance> searchConnectorHttpInstances(LogicalSearchQuery query) {
	return wrapConnectorHttpInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorHttpInstance> searchConnectorHttpInstances(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorInstance.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorHttpInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorHttpInstance getConnectorHttpInstance(String id) {
	return wrapConnectorHttpInstance(get(id));
}

public List<ConnectorHttpInstance> getConnectorHttpInstances(List<String> ids) {
	return wrapConnectorHttpInstances(get(ids));
}

public ConnectorHttpInstance getConnectorHttpInstanceWithCode(String code) {
	return wrapConnectorHttpInstance(getByCode(connectorInstance.schemaType(), code));
}

public ConnectorHttpInstance getConnectorHttpInstanceWithLegacyId(String legacyId) {
	return wrapConnectorHttpInstance(getByLegacyId(connectorInstance.schemaType(),  legacyId));
}

public ConnectorHttpInstance newConnectorHttpInstance() {
	return wrapConnectorHttpInstance(create(connectorInstance_http.schema()));
}

public ConnectorHttpInstance newConnectorHttpInstanceWithId(String id) {
	return wrapConnectorHttpInstance(create(connectorInstance_http.schema(), id));
}

public final SchemaTypeShortcuts_connectorInstance_http connectorInstance_http
	 = new SchemaTypeShortcuts_connectorInstance_http("connectorInstance_http");
public class SchemaTypeShortcuts_connectorInstance_http extends SchemaTypeShortcuts_connectorInstance_default {
	protected SchemaTypeShortcuts_connectorInstance_http(String schemaCode) {
		super(schemaCode);
}

	public Metadata onDemands() {
		return metadata("onDemands");
	}

	public Metadata seeds() {
		return metadata("seeds");
	}
}
public ConnectorSmbInstance wrapConnectorSmbInstance(Record record) {
	return record == null ? null : new ConnectorSmbInstance(record, getTypes());
}

public List<ConnectorSmbInstance> wrapConnectorSmbInstances(List<Record> records) {
	List<ConnectorSmbInstance> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorSmbInstance(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorSmbInstance> searchConnectorSmbInstances(LogicalSearchQuery query) {
	return wrapConnectorSmbInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorSmbInstance> searchConnectorSmbInstances(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorInstance.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorSmbInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorSmbInstance getConnectorSmbInstance(String id) {
	return wrapConnectorSmbInstance(get(id));
}

public List<ConnectorSmbInstance> getConnectorSmbInstances(List<String> ids) {
	return wrapConnectorSmbInstances(get(ids));
}

public ConnectorSmbInstance getConnectorSmbInstanceWithCode(String code) {
	return wrapConnectorSmbInstance(getByCode(connectorInstance.schemaType(), code));
}

public ConnectorSmbInstance getConnectorSmbInstanceWithLegacyId(String legacyId) {
	return wrapConnectorSmbInstance(getByLegacyId(connectorInstance.schemaType(),  legacyId));
}

public ConnectorSmbInstance newConnectorSmbInstance() {
	return wrapConnectorSmbInstance(create(connectorInstance_smb.schema()));
}

public ConnectorSmbInstance newConnectorSmbInstanceWithId(String id) {
	return wrapConnectorSmbInstance(create(connectorInstance_smb.schema(), id));
}

public final SchemaTypeShortcuts_connectorInstance_smb connectorInstance_smb
	 = new SchemaTypeShortcuts_connectorInstance_smb("connectorInstance_smb");
public class SchemaTypeShortcuts_connectorInstance_smb extends SchemaTypeShortcuts_connectorInstance_default {
	protected SchemaTypeShortcuts_connectorInstance_smb(String schemaCode) {
		super(schemaCode);
}

	public Metadata domain() {
		return metadata("domain");
	}

	public Metadata exclusions() {
		return metadata("exclusions");
	}

	public Metadata inclusions() {
		return metadata("inclusions");
	}

	public Metadata password() {
		return metadata("password");
	}

	public Metadata smbSeeds() {
		return metadata("smbSeeds");
	}

	public Metadata username() {
		return metadata("username");
	}
}
public ConnectorSmbDocument wrapConnectorSmbDocument(Record record) {
	return record == null ? null : new ConnectorSmbDocument(record, getTypes());
}

public List<ConnectorSmbDocument> wrapConnectorSmbDocuments(List<Record> records) {
	List<ConnectorSmbDocument> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorSmbDocument(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorSmbDocument> searchConnectorSmbDocuments(LogicalSearchQuery query) {
	return wrapConnectorSmbDocuments(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorSmbDocument> searchConnectorSmbDocuments(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorSmbDocument.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorSmbDocuments(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorSmbDocument getConnectorSmbDocument(String id) {
	return wrapConnectorSmbDocument(get(id));
}

public List<ConnectorSmbDocument> getConnectorSmbDocuments(List<String> ids) {
	return wrapConnectorSmbDocuments(get(ids));
}

public ConnectorSmbDocument getConnectorSmbDocumentWithLegacyId(String legacyId) {
	return wrapConnectorSmbDocument(getByLegacyId(connectorSmbDocument.schemaType(),  legacyId));
}

public ConnectorSmbDocument newConnectorSmbDocument() {
	return wrapConnectorSmbDocument(create(connectorSmbDocument.schema()));
}

public ConnectorSmbDocument newConnectorSmbDocumentWithId(String id) {
	return wrapConnectorSmbDocument(create(connectorSmbDocument.schema(), id));
}

public final SchemaTypeShortcuts_connectorSmbDocument_default connectorSmbDocument
	 = new SchemaTypeShortcuts_connectorSmbDocument_default("connectorSmbDocument_default");
public class SchemaTypeShortcuts_connectorSmbDocument_default extends SchemaTypeShortcuts {
	protected SchemaTypeShortcuts_connectorSmbDocument_default(String schemaCode) {
		super(schemaCode);
}

	public Metadata connector() {
		return metadata("connector");
	}

	public Metadata connectorType() {
		return metadata("connectorType");
	}

	public Metadata extension() {
		return metadata("extension");
	}

	public Metadata language() {
		return metadata("language");
	}

	public Metadata lastFetched() {
		return metadata("lastFetched");
	}

	public Metadata lastFetchedStatus() {
		return metadata("lastFetchedStatus");
	}

	public Metadata lastModified() {
		return metadata("lastModified");
	}

	public Metadata parent() {
		return metadata("parent");
	}

	public Metadata parsedContent() {
		return metadata("parsedContent");
	}

	public Metadata permissionsHash() {
		return metadata("permissionsHash");
	}

	public Metadata size() {
		return metadata("size");
	}

	public Metadata url() {
		return metadata("url");
	}
}
public ConnectorSmbFolder wrapConnectorSmbFolder(Record record) {
	return record == null ? null : new ConnectorSmbFolder(record, getTypes());
}

public List<ConnectorSmbFolder> wrapConnectorSmbFolders(List<Record> records) {
	List<ConnectorSmbFolder> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorSmbFolder(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorSmbFolder> searchConnectorSmbFolders(LogicalSearchQuery query) {
	return wrapConnectorSmbFolders(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorSmbFolder> searchConnectorSmbFolders(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorSmbFolder.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorSmbFolders(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorSmbFolder getConnectorSmbFolder(String id) {
	return wrapConnectorSmbFolder(get(id));
}

public List<ConnectorSmbFolder> getConnectorSmbFolders(List<String> ids) {
	return wrapConnectorSmbFolders(get(ids));
}

public ConnectorSmbFolder getConnectorSmbFolderWithLegacyId(String legacyId) {
	return wrapConnectorSmbFolder(getByLegacyId(connectorSmbFolder.schemaType(),  legacyId));
}

public ConnectorSmbFolder newConnectorSmbFolder() {
	return wrapConnectorSmbFolder(create(connectorSmbFolder.schema()));
}

public ConnectorSmbFolder newConnectorSmbFolderWithId(String id) {
	return wrapConnectorSmbFolder(create(connectorSmbFolder.schema(), id));
}

public final SchemaTypeShortcuts_connectorSmbFolder_default connectorSmbFolder
	 = new SchemaTypeShortcuts_connectorSmbFolder_default("connectorSmbFolder_default");
public class SchemaTypeShortcuts_connectorSmbFolder_default extends SchemaTypeShortcuts {
	protected SchemaTypeShortcuts_connectorSmbFolder_default(String schemaCode) {
		super(schemaCode);
}

	public Metadata connector() {
		return metadata("connector");
	}

	public Metadata connectorType() {
		return metadata("connectorType");
	}

	public Metadata lastFetched() {
		return metadata("lastFetched");
	}

	public Metadata lastFetchedStatus() {
		return metadata("lastFetchedStatus");
	}

	public Metadata parent() {
		return metadata("parent");
	}

	public Metadata url() {
		return metadata("url");
	}
}
public ConnectorType wrapConnectorType(Record record) {
	return record == null ? null : new ConnectorType(record, getTypes());
}

public List<ConnectorType> wrapConnectorTypes(List<Record> records) {
	List<ConnectorType> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorType(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorType> searchConnectorTypes(LogicalSearchQuery query) {
	return wrapConnectorTypes(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorType> searchConnectorTypes(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorType.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorTypes(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorType getConnectorType(String id) {
	return wrapConnectorType(get(id));
}

public List<ConnectorType> getConnectorTypes(List<String> ids) {
	return wrapConnectorTypes(get(ids));
}

public ConnectorType getConnectorTypeWithCode(String code) {
	return wrapConnectorType(getByCode(connectorType.schemaType(), code));
}

public ConnectorType getConnectorTypeWithLegacyId(String legacyId) {
	return wrapConnectorType(getByLegacyId(connectorType.schemaType(),  legacyId));
}

public ConnectorType newConnectorType() {
	return wrapConnectorType(create(connectorType.schema()));
}

public ConnectorType newConnectorTypeWithId(String id) {
	return wrapConnectorType(create(connectorType.schema(), id));
}

public final SchemaTypeShortcuts_connectorType_default connectorType
	 = new SchemaTypeShortcuts_connectorType_default("connectorType_default");
public class SchemaTypeShortcuts_connectorType_default extends SchemaTypeShortcuts {
	protected SchemaTypeShortcuts_connectorType_default(String schemaCode) {
		super(schemaCode);
}

	public Metadata code() {
		return metadata("code");
	}

	public Metadata connectorClassName() {
		return metadata("connectorClassName");
	}

	public Metadata defaultAvailableProperties() {
		return metadata("defaultAvailableProperties");
	}

	public Metadata linkedSchema() {
		return metadata("linkedSchema");
	}
}
/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end
/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

}
