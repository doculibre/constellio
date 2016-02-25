package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.FacetType;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.roles.Roles;

public class SchemasRecordsServices {

	protected String collection;

	protected ModelLayerFactory modelLayerFactory;

	public SchemasRecordsServices(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
	}

	public String getCollection() {
		return collection;
	}

	//

	//Generic

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public MetadataSchemaTypes getTypes() {
		return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public MetadataSchema defaultSchema(String code) {
		return getTypes().getSchema(code + "_default");
	}

	public MetadataSchema schema(String code) {
		return getTypes().getSchema(code);
	}

	public MetadataSchemaType schemaType(String code) {
		return getTypes().getSchemaType(code);
	}

	public Record get(String id) {
		return modelLayerFactory.newRecordServices().getDocumentById(id);
	}

	public Record getByLegacyId(MetadataSchemaType schemaType, String id) {
		LogicalSearchCondition condition = from(schemaType).where(Schemas.LEGACY_ID).isEqualTo(id);
		return modelLayerFactory.newSearchServices().searchSingleResult(condition);
	}

	public Record getByLegacyId(String schemaTypeCode, String id) {
		LogicalSearchCondition condition = from(schemaType(schemaTypeCode)).where(Schemas.LEGACY_ID).isEqualTo(id);
		return modelLayerFactory.newSearchServices().searchSingleResult(condition);
	}

	public List<Record> get(List<String> id) {
		return modelLayerFactory.newRecordServices().getRecordsById(collection, id);
	}

	public Record create(MetadataSchema schema) {
		return modelLayerFactory.newRecordServices().newRecordWithSchema(schema);
	}

	public Record create(MetadataSchema schema, String id) {
		return modelLayerFactory.newRecordServices().newRecordWithSchema(schema, id);
	}

	public Record getByCode(MetadataSchemaType schemaType, String code) {
		Metadata metadata = schemaType.getDefaultSchema().getMetadata(Schemas.CODE.getLocalCode());
		return modelLayerFactory.newRecordServices().getRecordByMetadata(metadata, code);
	}

	public MetadataSchemaType collectionSchemaType() {
		return getTypes().getSchemaType(Collection.SCHEMA_TYPE);
	}

	//

	// User Credentials

	public MetadataSchemaType credentialSchemaType() {
		return getTypes().getSchemaType(SolrUserCredential.SCHEMA_TYPE);
	}

	public MetadataSchema credentialSchema() {
		return getTypes().getSchema(SolrUserCredential.DEFAULT_SCHEMA);
	}

	public Metadata credentialUsername() {
		return credentialSchema().getMetadata(SolrUserCredential.USERNAME);
	}

	public Metadata credentialStatus() {
		return credentialSchema().getMetadata(SolrUserCredential.STATUS);
	}

	public Metadata credentialCollections() {
		return credentialSchema().getMetadata(SolrUserCredential.COLLECTIONS);
	}

	public Metadata credentialTokenKeys() {
		return credentialSchema().getMetadata(SolrUserCredential.TOKEN_KEYS);
	}

	public Metadata credentialTokenExpirations() {
		return credentialSchema().getMetadata(SolrUserCredential.TOKEN_EXPIRATIONS);
	}

	public Metadata credentialGroups() {
		return credentialSchema().getMetadata(SolrUserCredential.GLOBAL_GROUPS);
	}

	public Metadata credentialServiceKey() {
		return credentialSchema().getMetadata(SolrUserCredential.SERVICE_KEY);
	}

	public UserCredential newCredential() {
		return new SolrUserCredential(create(credentialSchema()), getTypes());
	}

	public UserCredential wrapCredential(Record record) {
		return new SolrUserCredential(record, getTypes());
	}

	public List<UserCredential> wrapCredentials(List<Record> records) {
		List<UserCredential> result = new ArrayList<>(records.size());
		for (Record record : records) {
			result.add(wrapCredential(record));
		}
		return result;
	}

	// Global Groups

	public MetadataSchemaType globalGroupSchemaType() {
		return getTypes().getSchemaType(SolrGlobalGroup.SCHEMA_TYPE);
	}

	public MetadataSchema globalGroupSchema() {
		return getTypes().getSchema(SolrGlobalGroup.DEFAULT_SCHEMA);
	}

	public Metadata globalGroupCode() {
		return globalGroupSchema().getMetadata(SolrGlobalGroup.CODE);
	}

	public Metadata globalGroupPath() {
		return globalGroupSchema().getMetadata(SolrGlobalGroup.PATH);
	}

	public GlobalGroup newGlobalGroup() {
		return new SolrGlobalGroup(create(globalGroupSchema()), getTypes());
	}

	public GlobalGroup wrapGlobalGroup(Record record) {
		return new SolrGlobalGroup(record, getTypes());
	}

	public List<GlobalGroup> wrapGlobalGroups(List<Record> records) {
		List<GlobalGroup> result = new ArrayList<>(records.size());
		for (Record record : records) {
			result.add(wrapGlobalGroup(record));
		}
		return result;
	}

	//Events

	public MetadataSchema eventSchema() {
		return getTypes().getSchema(Event.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType eventSchemaType() {
		return getTypes().getSchemaType(Event.SCHEMA_TYPE);
	}

	public Metadata eventType() {
		return eventSchema().getMetadata(Event.TYPE);
	}

	public Metadata eventUsername() {
		return eventSchema().getMetadata(Event.USERNAME);
	}

	public Metadata eventCreation() {
		return eventSchema().getMetadata(Schemas.CREATED_ON.getLocalCode());
	}

	public Event wrapEvent(Record record) {
		return new Event(record, getTypes());
	}

	public List<Event> wrapEvents(List<Record> records) {
		List<Event> events = new ArrayList<>();
		for (Record record : records) {
			events.add(new Event(record, getTypes()));
		}
		return events;
	}

	public Event getEvent(String id) {
		return new Event(get(id), getTypes());
	}

	public Event newEvent() {
		return new Event(create(defaultSchema(Event.SCHEMA_TYPE)), getTypes());
	}

	//EmailToSend
	public MetadataSchema emailToSend() {
		return getTypes().getSchema(EmailToSend.DEFAULT_SCHEMA);
	}

	public EmailToSend wrapEmailToSend(Record record) {
		return new EmailToSend(record, getTypes());
	}

	public EmailToSend newEmailToSend() {
		return new EmailToSend(create(defaultSchema(EmailToSend.SCHEMA_TYPE)), getTypes());
	}

	//Groups

	public MetadataSchema groupSchema() {
		return getTypes().getSchema(Group.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType groupSchemaType() {
		return getTypes().getSchemaType(Group.SCHEMA_TYPE);
	}

	public Group wrapGroup(Record record) {
		return new Group(record, getTypes());
	}

	public List<Group> wrapGroups(List<Record> records) {
		List<Group> users = new ArrayList<>();
		for (Record record : records) {
			users.add(new Group(record, getTypes()));
		}
		return users;
	}

	public Group getGroup(String id) {
		return new Group(get(id), getTypes());
	}

	public Group newGroup() {
		return new Group(create(groupSchema()), getTypes());
	}

	public Group newGroupWithId(String id) {
		return new Group(create(groupSchema(), id), getTypes());
	}

	//

	//Facet
	public MetadataSchema facetFieldSchema() {
		return getTypes().getSchema(Facet.FIELD_SCHEMA);
	}

	public MetadataSchemaType facetSchemaType() {
		return getTypes().getSchemaType(Facet.SCHEMA_TYPE);
	}

	public MetadataSchema defaultFacet() {
		return getTypes().getSchema(Facet.DEFAULT_SCHEMA);
	}

	public Metadata facetOrder() {
		return defaultFacet().getMetadata(Facet.ORDER);
	}

	public Metadata facetActive() {
		return defaultFacet().getMetadata(Facet.ACTIVE);
	}

	public MetadataSchema facetQuerySchema() {
		return getTypes().getSchema(Facet.QUERY_SCHEMA);
	}

	public Facet newFacetField(String id) {
		return new Facet(create(facetFieldSchema(), id), getTypes()).setFacetType(FacetType.FIELD);
	}

	public Facet newFacetField() {
		return new Facet(create(facetFieldSchema()), getTypes()).setFacetType(FacetType.FIELD);
	}

	public Facet newFacetQuery(String id) {
		return new Facet(create(facetQuerySchema(), id), getTypes()).setFacetType(FacetType.QUERY);
	}

	public Facet newFacetQuery() {
		return new Facet(create(facetQuerySchema()), getTypes()).setFacetType(FacetType.QUERY);
	}

	public Facet newFacetDefault() {
		return new Facet(create(defaultFacet()), getTypes()).setFacetType(null);
	}

	public Facet getFacet(String id) {
		return new Facet(get(id), getTypes());
	}

	public Facet wrapFacet(Record record) {
		return record == null ? null : new Facet(record, getTypes());
	}

	public List<Facet> wrapFacets(List<Record> records) {
		List<Facet> wrappers = new ArrayList<>();
		for (Record record : records) {
			wrappers.add(new Facet(record, getTypes()));
		}
		return wrappers;
	}

	//Collection

	public Collection wrapCollection(Record record) {
		return new Collection(record, getTypes());
	}

	//Users

	public MetadataSchema userSchema() {
		return getTypes().getSchema(User.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType userSchemaType() {
		return getTypes().getSchemaType(User.SCHEMA_TYPE);
	}

	public Metadata userUsername() {
		return userSchema().getMetadata(User.USERNAME);
	}

	public Metadata userEmail() {
		return userSchema().getMetadata(User.EMAIL);
	}

	public User wrapUser(Record record) {
		return new User(record, getTypes(), getRoles());
	}

	public List<User> wrapUsers(List<Record> records) {
		List<User> users = new ArrayList<>();
		for (Record record : records) {
			users.add(new User(record, getTypes(), getRoles()));
		}
		return users;
	}

	public List<User> getUsers(List<String> ids) {
		List<User> users = new ArrayList<>();
		for (String id : ids) {
			users.add(new User(get(id), getTypes(), getRoles()));
		}
		return users;
	}

	public User getUser(String id) {
		return new User(get(id), getTypes(), getRoles());
	}

	public User newUser() {
		return new User(create(userSchema()), getTypes(), getRoles());
	}

	public User newUserWithId(String id) {
		return new User(create(userSchema(), id), getTypes(), getRoles());
	}

	private Roles getRoles() {
		return modelLayerFactory.getRolesManager().getCollectionRoles(collection);
	}

	public class SchemaTypeShortcuts extends AbstractSchemaTypeShortcuts {

		protected SchemaTypeShortcuts(String schemaCode) {
			super(schemaCode);
		}

		@Override
		protected MetadataSchemaTypes types() {
			return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		}
	}

	public static abstract class AbstractSchemaTypeShortcuts {

		String schemaTypeCode;
		String schemaCode;

		protected AbstractSchemaTypeShortcuts(String schemaCode) {
			this.schemaCode = schemaCode;
			this.schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		}

		public MetadataSchemaType schemaType() {
			return types().getSchemaType(schemaTypeCode);
		}

		public MetadataSchema schema() {
			return types().getSchema(schemaCode);
		}

		protected abstract MetadataSchemaTypes types();

		public Metadata title() {
			return metadata(Schemas.TITLE.getLocalCode());
		}

		public Metadata createdOn() {
			return metadata(Schemas.CREATED_ON.getLocalCode());
		}

		public Metadata createdBy() {
			return metadata(Schemas.CREATED_BY.getLocalCode());
		}

		public Metadata modifiedOn() {
			return metadata(Schemas.MODIFIED_ON.getLocalCode());
		}

		public Metadata modifiedBy() {
			return metadata(Schemas.MODIFIED_BY.getLocalCode());
		}

		public Metadata legacyId() {
			return metadata(Schemas.LEGACY_ID.getLocalCode());
		}

		protected Metadata metadata(String code) {
			return schema().getMetadata(schemaCode + "_" + code);
		}

/*		MetadataSchemaTypes types() {
			return modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		}*/

	}

}
