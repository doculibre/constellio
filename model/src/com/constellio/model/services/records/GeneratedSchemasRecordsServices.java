package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.*;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class GeneratedSchemasRecordsServices extends BaseSchemasRecordsServices {
	public GeneratedSchemasRecordsServices(String collection,
			ModelLayerFactory modelLayerFactory) {
		super(collection, modelLayerFactory);
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

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
		return wrapCollection(getByLegacyId(collection.schemaType(), legacyId));
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

		public Metadata languages() {
			return metadata("languages");
		}

		public Metadata name() {
			return metadata("name");
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
		return wrapEmailToSend(getByLegacyId(emailToSend.schemaType(), legacyId));
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
		return wrapEvent(getByLegacyId(event.schemaType(), legacyId));
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
		return wrapFacet(getByLegacyId(facet.schemaType(), legacyId));
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
		return wrapGroup(getByLegacyId(group.schemaType(), legacyId));
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

	public User wrapUser(Record record) {
		return record == null ? null : wrapUser(record);
	}

	public List<User> wrapUsers(List<Record> records) {
		List<User> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(wrapUser(record));
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
		return wrapUser(getByLegacyId(user.schemaType(), legacyId));
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

		public Metadata defaultTabInFolderDisplay() {
			return metadata("defaultTabInFolderDisplay");
		}

		public Metadata defaultTaxonomy() {
			return metadata("defaultTaxonomy");
		}

		public Metadata email() {
			return metadata("email");
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
		return wrapUserDocument(getByLegacyId(userDocument.schemaType(), legacyId));
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

		public Metadata user() {
			return metadata("user");
		}
	}

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
		MetadataSchemaType type = autorizationDetail.schemaType();
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
		return wrapSolrAuthorizationDetails(getByLegacyId(autorizationDetail.schemaType(),  legacyId));
	}

	public SolrAuthorizationDetails newSolrAuthorizationDetails() {
		return wrapSolrAuthorizationDetails(create(autorizationDetail.schema()));
	}

	public SolrAuthorizationDetails newSolrAuthorizationDetailsWithId(String id) {
		return wrapSolrAuthorizationDetails(create(autorizationDetail.schema(), id));
	}

	public final SchemaTypeShortcuts_autorizationDetail_default autorizationDetail
			= new SchemaTypeShortcuts_autorizationDetail_default("autorizationDetail_default");
	public class SchemaTypeShortcuts_autorizationDetail_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_autorizationDetail_default(String schemaCode) {
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
	}
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
}
