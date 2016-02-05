package com.constellio.app.modules.es.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
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

	public Metadata charset() {
		return metadata("charset");
	}

	public Metadata connector() {
		return metadata("connector");
	}

	public Metadata connectorType() {
		return metadata("connectorType");
	}

	public Metadata contentType() {
		return metadata("contentType");
	}

	public Metadata copyOf() {
		return metadata("copyOf");
	}

	public Metadata digest() {
		return metadata("digest");
	}

	public Metadata downloadTime() {
		return metadata("downloadTime");
	}

	public Metadata errorCode() {
		return metadata("errorCode");
	}

	public Metadata errorMessage() {
		return metadata("errorMessage");
	}

	public Metadata errorStackTrace() {
		return metadata("errorStackTrace");
	}

	public Metadata errorsCount() {
		return metadata("errorsCount");
	}

	public Metadata fetchDelay() {
		return metadata("fetchDelay");
	}

	public Metadata fetched() {
		return metadata("fetched");
	}

	public Metadata fetchedDateTime() {
		return metadata("fetchedDateTime");
	}

	public Metadata frequency() {
		return metadata("frequency");
	}

	public Metadata inlinks() {
		return metadata("inlinks");
	}

	public Metadata level() {
		return metadata("level");
	}

	public Metadata neverFetch() {
		return metadata("neverFetch");
	}

	public Metadata nextFetch() {
		return metadata("nextFetch");
	}

	public Metadata onDemand() {
		return metadata("onDemand");
	}

	public Metadata outlinks() {
		return metadata("outlinks");
	}

	public Metadata parsedContent() {
		return metadata("parsedContent");
	}

	public Metadata priority() {
		return metadata("priority");
	}

	public Metadata searchable() {
		return metadata("searchable");
	}

	public Metadata status() {
		return metadata("status");
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

	public Metadata availableFields() {
		return metadata("availableFields");
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

	public Metadata lastTraversalOn() {
		return metadata("lastTraversalOn");
	}

	public Metadata propertiesMapping() {
		return metadata("propertiesMapping");
	}

	public Metadata traversalCode() {
		return metadata("traversalCode");
	}

		public Metadata traversalSchedule() {
			return metadata("traversalSchedule");
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

	public Metadata authenticationScheme() {
		return metadata("authenticationScheme");
	}

	public Metadata daysBeforeRefetching() {
		return metadata("daysBeforeRefetching");
	}

	public Metadata documentsPerJobs() {
		return metadata("documentsPerJobs");
	}

	public Metadata domain() {
		return metadata("domain");
	}

	public Metadata excludePatterns() {
		return metadata("excludePatterns");
	}

	public Metadata includePatterns() {
		return metadata("includePatterns");
	}

	public Metadata jobsInParallel() {
		return metadata("jobsInParallel");
	}

	public Metadata maxLevel() {
		return metadata("maxLevel");
	}

	public Metadata onDemands() {
		return metadata("onDemands");
	}

	public Metadata password() {
		return metadata("password");
	}

	public Metadata seeds() {
		return metadata("seeds");
	}

	public Metadata username() {
		return metadata("username");
	}
}
public ConnectorLDAPInstance wrapConnectorLDAPInstance(Record record) {
	return record == null ? null : new ConnectorLDAPInstance(record, getTypes());
}

public List<ConnectorLDAPInstance> wrapConnectorLDAPInstances(List<Record> records) {
	List<ConnectorLDAPInstance> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorLDAPInstance(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorLDAPInstance> searchConnectorLDAPInstances(LogicalSearchQuery query) {
	return wrapConnectorLDAPInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorLDAPInstance> searchConnectorLDAPInstances(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorInstance.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorLDAPInstances(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorLDAPInstance getConnectorLDAPInstance(String id) {
	return wrapConnectorLDAPInstance(get(id));
}

public List<ConnectorLDAPInstance> getConnectorLDAPInstances(List<String> ids) {
	return wrapConnectorLDAPInstances(get(ids));
}

public ConnectorLDAPInstance getConnectorLDAPInstanceWithCode(String code) {
	return wrapConnectorLDAPInstance(getByCode(connectorInstance.schemaType(), code));
}

public ConnectorLDAPInstance getConnectorLDAPInstanceWithLegacyId(String legacyId) {
	return wrapConnectorLDAPInstance(getByLegacyId(connectorInstance.schemaType(),  legacyId));
}

public ConnectorLDAPInstance newConnectorLDAPInstance() {
	return wrapConnectorLDAPInstance(create(connectorInstance_ldap.schema()));
}

public ConnectorLDAPInstance newConnectorLDAPInstanceWithId(String id) {
	return wrapConnectorLDAPInstance(create(connectorInstance_ldap.schema(), id));
}

public final SchemaTypeShortcuts_connectorInstance_ldap connectorInstance_ldap
	 = new SchemaTypeShortcuts_connectorInstance_ldap("connectorInstance_ldap");
public class SchemaTypeShortcuts_connectorInstance_ldap extends SchemaTypeShortcuts_connectorInstance_default {
	protected SchemaTypeShortcuts_connectorInstance_ldap(String schemaCode) {
		super(schemaCode);
}

	public Metadata address() {
		return metadata("address");
	}

	public Metadata company() {
		return metadata("company");
	}

	public Metadata connectionUsername() {
		return metadata("connectionUsername");
	}

	public Metadata department() {
		return metadata("department");
	}

	public Metadata directoryType() {
		return metadata("directoryType");
	}

	public Metadata displayName() {
		return metadata("displayName");
	}

	public Metadata dn() {
		return metadata("dn");
	}

	public Metadata documentsPerJob() {
		return metadata("documentsPerJob");
	}

	public Metadata email() {
		return metadata("email");
	}

	public Metadata excludeRegex() {
		return metadata("excludeRegex");
	}

	public Metadata fetchComputers() {
		return metadata("fetchComputers");
	}

	public Metadata fetchGroups() {
		return metadata("fetchGroups");
	}

	public Metadata fetchUsers() {
		return metadata("fetchUsers");
	}

	public Metadata firstName() {
		return metadata("firstName");
	}

	public Metadata followReferences() {
		return metadata("followReferences");
	}

	public Metadata includeRegex() {
		return metadata("includeRegex");
	}

	public Metadata jobTitle() {
		return metadata("jobTitle");
	}

	public Metadata jobsInParallel() {
		return metadata("jobsInParallel");
	}

	public Metadata lastName() {
		return metadata("lastName");
	}

	public Metadata manager() {
		return metadata("manager");
	}

	public Metadata password() {
		return metadata("password");
	}

	public Metadata telephone() {
		return metadata("telephone");
	}

	public Metadata url() {
		return metadata("url");
	}

	public Metadata username() {
		return metadata("username");
	}

		public Metadata usersBaseContextList() {
			return metadata("usersBaseContextList");
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

	public Metadata resumeUrl() {
		return metadata("resumeUrl");
	}

	public Metadata smbSeeds() {
		return metadata("smbSeeds");
	}

	public Metadata username() {
		return metadata("username");
	}
}
public ConnectorLDAPUserDocument wrapConnectorLDAPUserDocument(Record record) {
	return record == null ? null : new ConnectorLDAPUserDocument(record, getTypes());
}

public List<ConnectorLDAPUserDocument> wrapConnectorLDAPUserDocuments(List<Record> records) {
	List<ConnectorLDAPUserDocument> wrapped = new ArrayList<>();
	for (Record record : records) {
		wrapped.add(new ConnectorLDAPUserDocument(record, getTypes()));
	}

	return wrapped;
}

public List<ConnectorLDAPUserDocument> searchConnectorLDAPUserDocuments(LogicalSearchQuery query) {
	return wrapConnectorLDAPUserDocuments(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public List<ConnectorLDAPUserDocument> searchConnectorLDAPUserDocuments(LogicalSearchCondition condition) {
	MetadataSchemaType type = connectorLdapUserDocument.schemaType();
	LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
	return wrapConnectorLDAPUserDocuments(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
}

public ConnectorLDAPUserDocument getConnectorLDAPUserDocument(String id) {
	return wrapConnectorLDAPUserDocument(get(id));
}

public List<ConnectorLDAPUserDocument> getConnectorLDAPUserDocuments(List<String> ids) {
	return wrapConnectorLDAPUserDocuments(get(ids));
}

public ConnectorLDAPUserDocument getConnectorLDAPUserDocumentWithLegacyId(String legacyId) {
	return wrapConnectorLDAPUserDocument(getByLegacyId(connectorLdapUserDocument.schemaType(),  legacyId));
}

public ConnectorLDAPUserDocument newConnectorLDAPUserDocument() {
	return wrapConnectorLDAPUserDocument(create(connectorLdapUserDocument.schema()));
}

public ConnectorLDAPUserDocument newConnectorLDAPUserDocumentWithId(String id) {
	return wrapConnectorLDAPUserDocument(create(connectorLdapUserDocument.schema(), id));
}

public final SchemaTypeShortcuts_connectorLdapUserDocument_default connectorLdapUserDocument
	 = new SchemaTypeShortcuts_connectorLdapUserDocument_default("connectorLdapUserDocument_default");
public class SchemaTypeShortcuts_connectorLdapUserDocument_default extends SchemaTypeShortcuts {
	protected SchemaTypeShortcuts_connectorLdapUserDocument_default(String schemaCode) {
		super(schemaCode);
}

	public Metadata address() {
		return metadata("address");
	}

	public Metadata company() {
		return metadata("company");
	}

	public Metadata connector() {
		return metadata("connector");
	}

	public Metadata connectorType() {
		return metadata("connectorType");
	}

	public Metadata department() {
		return metadata("department");
	}

	public Metadata displayName() {
		return metadata("displayName");
	}

	public Metadata distinguishedName() {
		return metadata("distinguishedName");
	}

	public Metadata email() {
		return metadata("email");
	}

	public Metadata enabled() {
		return metadata("enabled");
	}

	public Metadata errorCode() {
		return metadata("errorCode");
	}

	public Metadata errorMessage() {
		return metadata("errorMessage");
	}

	public Metadata errorStackTrace() {
		return metadata("errorStackTrace");
	}

	public Metadata errorsCount() {
		return metadata("errorsCount");
	}

	public Metadata fetchDelay() {
		return metadata("fetchDelay");
	}

	public Metadata fetched() {
		return metadata("fetched");
	}

	public Metadata fetchedDateTime() {
		return metadata("fetchedDateTime");
	}

	public Metadata firstName() {
		return metadata("firstName");
	}

	public Metadata frequency() {
		return metadata("frequency");
	}

	public Metadata lastName() {
		return metadata("lastName");
	}

	public Metadata manager() {
		return metadata("manager");
	}

	public Metadata neverFetch() {
		return metadata("neverFetch");
	}

	public Metadata nextFetch() {
		return metadata("nextFetch");
	}

	public Metadata searchable() {
		return metadata("searchable");
	}

	public Metadata status() {
		return metadata("status");
	}

	public Metadata telephone() {
		return metadata("telephone");
	}

	public Metadata url() {
		return metadata("url");
	}

	public Metadata username() {
		return metadata("username");
	}

		public Metadata workTitle() {
			return metadata("workTitle");
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

	public Metadata lastFetchAttempt() {
		return metadata("lastFetchAttempt");
	}

	public Metadata lastFetchAttemptDetails() {
		return metadata("lastFetchAttemptDetails");
	}

	public Metadata lastFetchAttemptStatus() {
		return metadata("lastFetchAttemptStatus");
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

	public Metadata lastFetchAttempt() {
		return metadata("lastFetchAttempt");
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

	public Metadata defaultAvailableFields() {
		return metadata("defaultAvailableFields");
	}

	public Metadata linkedSchema() {
		return metadata("linkedSchema");
	}
}
/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end
/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
}
