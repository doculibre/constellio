package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.*;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class GeneratedSchemasRecordsServices extends BaseSchemasRecordsServices {
	public GeneratedSchemasRecordsServices(String collection,
			ModelLayerFactory modelLayerFactory) {
		super(collection, modelLayerFactory);
	}

/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/



	public SolrAuthorizationDetails wrapSolrAuthorizationDetails(Record record) {
		return record == null ? null : new SolrAuthorizationDetails(record, getTypes());
	}

	public List<SolrAuthorizationDetails> wrapSolrAuthorizationDetailss(List<Record> records) {
		List<SolrAuthorizationDetails> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new SolrAuthorizationDetails(record, getTypes()));
		}

		return wrapped;
	}

	public List<SolrAuthorizationDetails> searchSolrAuthorizationDetailss(LogicalSearchQuery query) {
		return wrapSolrAuthorizationDetailss(modelLayerFactory.newSearchServices().search(query));
	}

	public List<SolrAuthorizationDetails> searchSolrAuthorizationDetailss(LogicalSearchCondition condition) {
		MetadataSchemaType type = authorizationDetails.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapSolrAuthorizationDetailss(modelLayerFactory.newSearchServices().search(query));
	}

	public SolrAuthorizationDetails getSolrAuthorizationDetails(String id) {
		return wrapSolrAuthorizationDetails(get(id));
	}

	public List<SolrAuthorizationDetails> getSolrAuthorizationDetailss(List<String> ids) {
		return wrapSolrAuthorizationDetailss(get(ids));
	}

	public SolrAuthorizationDetails getSolrAuthorizationDetailsWithLegacyId(String legacyId) {
		return wrapSolrAuthorizationDetails(getByLegacyId(authorizationDetails.schemaType(),  legacyId));
	}

	public SolrAuthorizationDetails newSolrAuthorizationDetails() {
		return wrapSolrAuthorizationDetails(create(authorizationDetails.schema()));
	}

	public SolrAuthorizationDetails newSolrAuthorizationDetailsWithId(String id) {
		return wrapSolrAuthorizationDetails(create(authorizationDetails.schema(), id));
	}

	public final SchemaTypeShortcuts_authorizationDetails_default authorizationDetails
			= new SchemaTypeShortcuts_authorizationDetails_default("authorizationDetails_default");
	public class SchemaTypeShortcuts_authorizationDetails_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_authorizationDetails_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata endDate() {
			return metadata("endDate");
		}

		public Metadata roles() {
			return metadata("roles");
		}

		public Metadata startDate() {
			return metadata("startDate");
		}

		public Metadata synced() {
			return metadata("synced");
		}

		public Metadata target() {
			return metadata("target");
		}

		public Metadata targetSchemaType() {
			return metadata("targetSchemaType");
		}
	}
	public Capsule wrapCapsule(Record record) {
		return record == null ? null : new Capsule(record, getTypes());
	}

	public List<Capsule> wrapCapsules(List<Record> records) {
		List<Capsule> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Capsule(record, getTypes()));
		}

		return wrapped;
	}

	public List<Capsule> searchCapsules(LogicalSearchQuery query) {
		return wrapCapsules(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Capsule> searchCapsules(LogicalSearchCondition condition) {
		MetadataSchemaType type = capsule.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapCapsules(modelLayerFactory.newSearchServices().search(query));
	}

	public Capsule getCapsule(String id) {
		return wrapCapsule(get(id));
	}

	public List<Capsule> getCapsules(List<String> ids) {
		return wrapCapsules(get(ids));
	}

	public Capsule getCapsuleWithCode(String code) {
		return wrapCapsule(getByCode(capsule.schemaType(), code));
	}

	public Capsule getCapsuleWithLegacyId(String legacyId) {
		return wrapCapsule(getByLegacyId(capsule.schemaType(),  legacyId));
	}

	public Capsule newCapsule() {
		return wrapCapsule(create(capsule.schema()));
	}

	public Capsule newCapsuleWithId(String id) {
		return wrapCapsule(create(capsule.schema(), id));
	}

	public final SchemaTypeShortcuts_capsule_default capsule
			= new SchemaTypeShortcuts_capsule_default("capsule_default");
	public class SchemaTypeShortcuts_capsule_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_capsule_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata html() {
			return metadata("html");
		}

		public Metadata keywords() {
			return metadata("keywords");
		}
	}
	public Collection wrapCollection(Record record) {
		return record == null ? null : new Collection(record, getTypes());
	}

	public List<Collection> wrapCollections(List<Record> records) {
		List<Collection> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Collection(record, getTypes()));
		}

		return wrapped;
	}

	public List<Collection> searchCollections(LogicalSearchQuery query) {
		return wrapCollections(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Collection> searchCollections(LogicalSearchCondition condition) {
		MetadataSchemaType type = collection.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapCollections(modelLayerFactory.newSearchServices().search(query));
	}

	public Collection getCollection(String id) {
		return wrapCollection(get(id));
	}

	public List<Collection> getCollections(List<String> ids) {
		return wrapCollections(get(ids));
	}

	public Collection getCollectionWithCode(String code) {
		return wrapCollection(getByCode(collection.schemaType(), code));
	}

	public Collection getCollectionWithLegacyId(String legacyId) {
		return wrapCollection(getByLegacyId(collection.schemaType(),  legacyId));
	}

	public Collection newCollection() {
		return wrapCollection(create(collection.schema()));
	}

	public Collection newCollectionWithId(String id) {
		return wrapCollection(create(collection.schema(), id));
	}

	public final SchemaTypeShortcuts_collection_default collection
			= new SchemaTypeShortcuts_collection_default("collection_default");
	public class SchemaTypeShortcuts_collection_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_collection_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata conservationCalendarNumber() {
			return metadata("conservationCalendarNumber");
		}

		public Metadata languages() {
			return metadata("languages");
		}

		public Metadata name() {
			return metadata("name");
		}

		public Metadata organizationNumber() {
			return metadata("organizationNumber");
		}
	}
	public EmailToSend wrapEmailToSend(Record record) {
		return record == null ? null : new EmailToSend(record, getTypes());
	}

	public List<EmailToSend> wrapEmailToSends(List<Record> records) {
		List<EmailToSend> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new EmailToSend(record, getTypes()));
		}

		return wrapped;
	}

	public List<EmailToSend> searchEmailToSends(LogicalSearchQuery query) {
		return wrapEmailToSends(modelLayerFactory.newSearchServices().search(query));
	}

	public List<EmailToSend> searchEmailToSends(LogicalSearchCondition condition) {
		MetadataSchemaType type = emailToSend.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapEmailToSends(modelLayerFactory.newSearchServices().search(query));
	}

	public EmailToSend getEmailToSend(String id) {
		return wrapEmailToSend(get(id));
	}

	public List<EmailToSend> getEmailToSends(List<String> ids) {
		return wrapEmailToSends(get(ids));
	}

	public EmailToSend getEmailToSendWithLegacyId(String legacyId) {
		return wrapEmailToSend(getByLegacyId(emailToSend.schemaType(),  legacyId));
	}

	public EmailToSend newEmailToSend() {
		return wrapEmailToSend(create(emailToSend.schema()));
	}

	public EmailToSend newEmailToSendWithId(String id) {
		return wrapEmailToSend(create(emailToSend.schema(), id));
	}

	public final SchemaTypeShortcuts_emailToSend_default emailToSend
			= new SchemaTypeShortcuts_emailToSend_default("emailToSend_default");
	public class SchemaTypeShortcuts_emailToSend_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_emailToSend_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata BCC() {
			return metadata("BCC");
		}

		public Metadata CC() {
			return metadata("CC");
		}

		public Metadata error() {
			return metadata("error");
		}

		public Metadata from() {
			return metadata("from");
		}

		public Metadata parameters() {
			return metadata("parameters");
		}

		public Metadata sendOn() {
			return metadata("sendOn");
		}

		public Metadata subject() {
			return metadata("subject");
		}

		public Metadata template() {
			return metadata("template");
		}

		public Metadata to() {
			return metadata("to");
		}

		public Metadata tryingCount() {
			return metadata("tryingCount");
		}
	}
	public Event wrapEvent(Record record) {
		return record == null ? null : new Event(record, getTypes());
	}

	public List<Event> wrapEvents(List<Record> records) {
		List<Event> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Event(record, getTypes()));
		}

		return wrapped;
	}

	public List<Event> searchEvents(LogicalSearchQuery query) {
		return wrapEvents(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Event> searchEvents(LogicalSearchCondition condition) {
		MetadataSchemaType type = event.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapEvents(modelLayerFactory.newSearchServices().search(query));
	}

	public Event getEvent(String id) {
		return wrapEvent(get(id));
	}

	public List<Event> getEvents(List<String> ids) {
		return wrapEvents(get(ids));
	}

	public Event getEventWithLegacyId(String legacyId) {
		return wrapEvent(getByLegacyId(event.schemaType(),  legacyId));
	}

	public Event newEvent() {
		return wrapEvent(create(event.schema()));
	}

	public Event newEventWithId(String id) {
		return wrapEvent(create(event.schema(), id));
	}

	public final SchemaTypeShortcuts_event_default event
			= new SchemaTypeShortcuts_event_default("event_default");
	public class SchemaTypeShortcuts_event_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_event_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata delta() {
			return metadata("delta");
		}

		public Metadata eventPrincipalPath() {
			return metadata("eventPrincipalPath");
		}

		public Metadata ip() {
			return metadata("ip");
		}

		public Metadata permissionDateRange() {
			return metadata("permissionDateRange");
		}

		public Metadata permissionRoles() {
			return metadata("permissionRoles");
		}

		public Metadata permissionUsers() {
			return metadata("permissionUsers");
		}

		public Metadata reason() {
			return metadata("reason");
		}

		public Metadata recordIdentifier() {
			return metadata("recordIdentifier");
		}

		public Metadata recordVersion() {
			return metadata("recordVersion");
		}

		public Metadata type() {
			return metadata("type");
		}

		public Metadata userRoles() {
			return metadata("userRoles");
		}

		public Metadata username() {
			return metadata("username");
		}
	}
	public Facet wrapFacet(Record record) {
		return record == null ? null : new Facet(record, getTypes());
	}

	public List<Facet> wrapFacets(List<Record> records) {
		List<Facet> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Facet(record, getTypes()));
		}

		return wrapped;
	}

	public List<Facet> searchFacets(LogicalSearchQuery query) {
		return wrapFacets(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Facet> searchFacets(LogicalSearchCondition condition) {
		MetadataSchemaType type = facet.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapFacets(modelLayerFactory.newSearchServices().search(query));
	}

	public Facet getFacet(String id) {
		return wrapFacet(get(id));
	}

	public List<Facet> getFacets(List<String> ids) {
		return wrapFacets(get(ids));
	}

	public Facet getFacetWithLegacyId(String legacyId) {
		return wrapFacet(getByLegacyId(facet.schemaType(),  legacyId));
	}

	public Facet newFacet() {
		return wrapFacet(create(facet.schema()));
	}

	public Facet newFacetWithId(String id) {
		return wrapFacet(create(facet.schema(), id));
	}

	public final SchemaTypeShortcuts_facet_default facet
			= new SchemaTypeShortcuts_facet_default("facet_default");
	public class SchemaTypeShortcuts_facet_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_facet_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata active() {
			return metadata("active");
		}

		public Metadata elementPerPage() {
			return metadata("elementPerPage");
		}

		public Metadata facetType() {
			return metadata("facetType");
		}

		public Metadata fieldDatastoreCode() {
			return metadata("fieldDatastoreCode");
		}

		public Metadata openByDefault() {
			return metadata("openByDefault");
		}

		public Metadata order() {
			return metadata("order");
		}

		public Metadata orderResult() {
			return metadata("orderResult");
		}

		public Metadata pages() {
			return metadata("pages");
		}

		public Metadata usedByModule() {
			return metadata("usedByModule");
		}
	}
	public Group wrapGroup(Record record) {
		return record == null ? null : new Group(record, getTypes());
	}

	public List<Group> wrapGroups(List<Record> records) {
		List<Group> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Group(record, getTypes()));
		}

		return wrapped;
	}

	public List<Group> searchGroups(LogicalSearchQuery query) {
		return wrapGroups(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Group> searchGroups(LogicalSearchCondition condition) {
		MetadataSchemaType type = group.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapGroups(modelLayerFactory.newSearchServices().search(query));
	}

	public Group getGroup(String id) {
		return wrapGroup(get(id));
	}

	public List<Group> getGroups(List<String> ids) {
		return wrapGroups(get(ids));
	}

	public Group getGroupWithCode(String code) {
		return wrapGroup(getByCode(group.schemaType(), code));
	}

	public Group getGroupWithLegacyId(String legacyId) {
		return wrapGroup(getByLegacyId(group.schemaType(),  legacyId));
	}

	public Group newGroup() {
		return wrapGroup(create(group.schema()));
	}

	public Group newGroupWithId(String id) {
		return wrapGroup(create(group.schema(), id));
	}

	public final SchemaTypeShortcuts_group_default group
			= new SchemaTypeShortcuts_group_default("group_default");
	public class SchemaTypeShortcuts_group_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_group_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata allauthorizations() {
			return metadata("allauthorizations");
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata isGlobal() {
			return metadata("isGlobal");
		}

		public Metadata parent() {
			return metadata("parent");
		}

		public Metadata roles() {
			return metadata("roles");
		}

		public Metadata title() {
			return metadata("title");
		}
	}
	public Printable wrapPrintable(Record record) {
		return record == null ? null : new Printable(record, getTypes());
	}

	public List<Printable> wrapPrintables(List<Record> records) {
		List<Printable> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Printable(record, getTypes()));
		}

		return wrapped;
	}

	public List<Printable> searchPrintables(LogicalSearchQuery query) {
		return wrapPrintables(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Printable> searchPrintables(LogicalSearchCondition condition) {
		MetadataSchemaType type = printable.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapPrintables(modelLayerFactory.newSearchServices().search(query));
	}

	public Printable getPrintable(String id) {
		return wrapPrintable(get(id));
	}

	public List<Printable> getPrintables(List<String> ids) {
		return wrapPrintables(get(ids));
	}

	public Printable getPrintableWithLegacyId(String legacyId) {
		return wrapPrintable(getByLegacyId(printable.schemaType(),  legacyId));
	}

	public Printable newPrintable() {
		return wrapPrintable(create(printable.schema()));
	}

	public Printable newPrintableWithId(String id) {
		return wrapPrintable(create(printable.schema(), id));
	}

	public final SchemaTypeShortcuts_printable_default printable
			= new SchemaTypeShortcuts_printable_default("printable_default");
	public class SchemaTypeShortcuts_printable_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_printable_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata isdeletable() {
			return metadata("isdeletable");
		}

		public Metadata jasperfile() {
			return metadata("jasperfile");
		}
	}
	public TemporaryRecord wrapTemporaryRecord(Record record) {
		return record == null ? null : new TemporaryRecord(record, getTypes());
	}

	public List<TemporaryRecord> wrapTemporaryRecords(List<Record> records) {
		List<TemporaryRecord> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new TemporaryRecord(record, getTypes()));
		}

		return wrapped;
	}

	public List<TemporaryRecord> searchTemporaryRecords(LogicalSearchQuery query) {
		return wrapTemporaryRecords(modelLayerFactory.newSearchServices().search(query));
	}

	public List<TemporaryRecord> searchTemporaryRecords(LogicalSearchCondition condition) {
		MetadataSchemaType type = temporaryRecord.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapTemporaryRecords(modelLayerFactory.newSearchServices().search(query));
	}

	public TemporaryRecord getTemporaryRecord(String id) {
		return wrapTemporaryRecord(get(id));
	}

	public List<TemporaryRecord> getTemporaryRecords(List<String> ids) {
		return wrapTemporaryRecords(get(ids));
	}

	public TemporaryRecord getTemporaryRecordWithLegacyId(String legacyId) {
		return wrapTemporaryRecord(getByLegacyId(temporaryRecord.schemaType(),  legacyId));
	}

	public TemporaryRecord newTemporaryRecord() {
		return wrapTemporaryRecord(create(temporaryRecord.schema()));
	}

	public TemporaryRecord newTemporaryRecordWithId(String id) {
		return wrapTemporaryRecord(create(temporaryRecord.schema(), id));
	}

	public final SchemaTypeShortcuts_temporaryRecord_default temporaryRecord
			= new SchemaTypeShortcuts_temporaryRecord_default("temporaryRecord_default");
	public class SchemaTypeShortcuts_temporaryRecord_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_temporaryRecord_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata content() {
			return metadata("content");
		}

		public Metadata daysBeforeDestruction() {
			return metadata("daysBeforeDestruction");
		}

		public Metadata destructionDate() {
			return metadata("destructionDate");
		}

		public Metadata title() {
			return metadata("title");
		}
	}
	public User wrapUser(Record record) {
		return record == null ? null : new User(record, getTypes(), null);
	}

	public List<User> wrapUsers(List<Record> records) {
		List<User> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new User(record, getTypes(), null));
		}

		return wrapped;
	}

	public List<User> searchUsers(LogicalSearchQuery query) {
		return wrapUsers(modelLayerFactory.newSearchServices().search(query));
	}

	public List<User> searchUsers(LogicalSearchCondition condition) {
		MetadataSchemaType type = user.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapUsers(modelLayerFactory.newSearchServices().search(query));
	}

	public User getUser(String id) {
		return wrapUser(get(id));
	}

	public List<User> getUsers(List<String> ids) {
		return wrapUsers(get(ids));
	}

	public User getUserWithLegacyId(String legacyId) {
		return wrapUser(getByLegacyId(user.schemaType(),  legacyId));
	}

	public User newUser() {
		return wrapUser(create(user.schema()));
	}

	public User newUserWithId(String id) {
		return wrapUser(create(user.schema(), id));
	}

	public final SchemaTypeShortcuts_user_default user
			= new SchemaTypeShortcuts_user_default("user_default");
	public class SchemaTypeShortcuts_user_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_user_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata address() {
			return metadata("address");
		}

		public Metadata agentEnabled() {
			return metadata("agentEnabled");
		}

		public Metadata allroles() {
			return metadata("allroles");
		}

		public Metadata alluserauthorizations() {
			return metadata("alluserauthorizations");
		}

		public Metadata collectionDeleteAccess() {
			return metadata("collectionDeleteAccess");
		}

		public Metadata collectionReadAccess() {
			return metadata("collectionReadAccess");
		}

		public Metadata collectionWriteAccess() {
			return metadata("collectionWriteAccess");
		}

		public Metadata defaultPageLength() {
			return metadata("defaultPageLength");
		}

		public Metadata defaultTabInFolderDisplay() {
			return metadata("defaultTabInFolderDisplay");
		}

		public Metadata defaultTaxonomy() {
			return metadata("defaultTaxonomy");
		}

		public Metadata email() {
			return metadata("email");
		}

		public Metadata fax() {
			return metadata("fax");
		}

		public Metadata firstname() {
			return metadata("firstname");
		}

		public Metadata groups() {
			return metadata("groups");
		}

		public Metadata groupsauthorizations() {
			return metadata("groupsauthorizations");
		}

		public Metadata jobTitle() {
			return metadata("jobTitle");
		}

		public Metadata lastIPAddress() {
			return metadata("lastIPAddress");
		}

		public Metadata lastLogin() {
			return metadata("lastLogin");
		}

		public Metadata lastname() {
			return metadata("lastname");
		}

		public Metadata loginLanguageCode() {
			return metadata("loginLanguageCode");
		}

		public Metadata personalEmails() {
			return metadata("personalEmails");
		}

		public Metadata phone() {
			return metadata("phone");
		}

		public Metadata signature() {
			return metadata("signature");
		}

		public Metadata startTab() {
			return metadata("startTab");
		}

		public Metadata status() {
			return metadata("status");
		}

		public Metadata systemAdmin() {
			return metadata("systemAdmin");
		}

		public Metadata username() {
			return metadata("username");
		}

		public Metadata userroles() {
			return metadata("userroles");
		}

		public Metadata usertokens() {
			return metadata("usertokens");
		}

		public Metadata visibleTableColumns() {
			return metadata("visibleTableColumns");
		}
	}
	public UserDocument wrapUserDocument(Record record) {
		return record == null ? null : new UserDocument(record, getTypes());
	}

	public List<UserDocument> wrapUserDocuments(List<Record> records) {
		List<UserDocument> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new UserDocument(record, getTypes()));
		}

		return wrapped;
	}

	public List<UserDocument> searchUserDocuments(LogicalSearchQuery query) {
		return wrapUserDocuments(modelLayerFactory.newSearchServices().search(query));
	}

	public List<UserDocument> searchUserDocuments(LogicalSearchCondition condition) {
		MetadataSchemaType type = userDocument.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapUserDocuments(modelLayerFactory.newSearchServices().search(query));
	}

	public UserDocument getUserDocument(String id) {
		return wrapUserDocument(get(id));
	}

	public List<UserDocument> getUserDocuments(List<String> ids) {
		return wrapUserDocuments(get(ids));
	}

	public UserDocument getUserDocumentWithLegacyId(String legacyId) {
		return wrapUserDocument(getByLegacyId(userDocument.schemaType(),  legacyId));
	}

	public UserDocument newUserDocument() {
		return wrapUserDocument(create(userDocument.schema()));
	}

	public UserDocument newUserDocumentWithId(String id) {
		return wrapUserDocument(create(userDocument.schema(), id));
	}

	public final SchemaTypeShortcuts_userDocument_default userDocument
			= new SchemaTypeShortcuts_userDocument_default("userDocument_default");
	public class SchemaTypeShortcuts_userDocument_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_userDocument_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata content() {
			return metadata("content");
		}

		public Metadata formCreatedOn() {
			return metadata("formCreatedOn");
		}

		public Metadata formModifiedOn() {
			return metadata("formModifiedOn");
		}

		public Metadata user() {
			return metadata("user");
		}

		public Metadata userFolder() {
			return metadata("userFolder");
		}
	}
	public UserFolder wrapUserFolder(Record record) {
		return record == null ? null : new UserFolder(record, getTypes());
	}

	public List<UserFolder> wrapUserFolders(List<Record> records) {
		List<UserFolder> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new UserFolder(record, getTypes()));
		}

		return wrapped;
	}

	public List<UserFolder> searchUserFolders(LogicalSearchQuery query) {
		return wrapUserFolders(modelLayerFactory.newSearchServices().search(query));
	}

	public List<UserFolder> searchUserFolders(LogicalSearchCondition condition) {
		MetadataSchemaType type = userFolder.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapUserFolders(modelLayerFactory.newSearchServices().search(query));
	}

	public UserFolder getUserFolder(String id) {
		return wrapUserFolder(get(id));
	}

	public List<UserFolder> getUserFolders(List<String> ids) {
		return wrapUserFolders(get(ids));
	}

	public UserFolder getUserFolderWithLegacyId(String legacyId) {
		return wrapUserFolder(getByLegacyId(userFolder.schemaType(),  legacyId));
	}

	public UserFolder newUserFolder() {
		return wrapUserFolder(create(userFolder.schema()));
	}

	public UserFolder newUserFolderWithId(String id) {
		return wrapUserFolder(create(userFolder.schema(), id));
	}

	public final SchemaTypeShortcuts_userFolder_default userFolder
			= new SchemaTypeShortcuts_userFolder_default("userFolder_default");
	public class SchemaTypeShortcuts_userFolder_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_userFolder_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata formCreatedOn() {
			return metadata("formCreatedOn");
		}

		public Metadata formModifiedOn() {
			return metadata("formModifiedOn");
		}

		public Metadata parentUserFolder() {
			return metadata("parentUserFolder");
		}

		public Metadata user() {
			return metadata("user");
		}
	}
/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end
/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
}
