package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.*;
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
import com.constellio.model.services.factories.ModelLayerFactoryWithRequestCacheImpl;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.roles.Roles;

public class SchemasRecordsServices extends GeneratedSchemasRecordsServices {

	public SchemasRecordsServices(String collection,
			final ModelLayerFactory modelLayerFactory) {
		super(collection, toModelLayerFactoryFactory(modelLayerFactory));
	}

	private static Factory<ModelLayerFactory> toModelLayerFactoryFactory(final ModelLayerFactory modelLayerFactory) {
		if (modelLayerFactory instanceof ModelLayerFactoryWithRequestCacheImpl) {
			return modelLayerFactory.getModelLayerFactoryFactory();
		} else {
			return new Factory<ModelLayerFactory>() {
				@Override
				public ModelLayerFactory get() {
					return modelLayerFactory;
				}
			};
		}
	}

	private SchemasRecordsServices(String collection,
			final Factory<ModelLayerFactory> modelLayerFactoryFactory) {
		super(collection, modelLayerFactoryFactory);
	}

	public static SchemasRecordsServices usingMainModelLayerFactory(String collection,
			final ModelLayerFactory modelLayerFactory) {

		return new SchemasRecordsServices(collection, new Factory<ModelLayerFactory>() {

			@Override
			public ModelLayerFactory get() {
				return modelLayerFactory;
			}
		});
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
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION);
		return types.getSchemaType(SolrGlobalGroup.SCHEMA_TYPE);
	}

	public MetadataSchema globalGroupSchema() {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION);
		return types.getSchema(SolrGlobalGroup.DEFAULT_SCHEMA);
	}

	public Metadata globalGroupCode() {
		return globalGroupSchema().getMetadata(SolrGlobalGroup.CODE);
	}

	public Metadata globalGroupHierarchy() {
		return globalGroupSchema().getMetadata(SolrGlobalGroup.HIERARCHY);
	}

	public Metadata globalGroupStatus() {
		return globalGroupSchema().getMetadata(SolrGlobalGroup.STATUS);
	}

	public Metadata globalGroupCollections() {
		return globalGroupSchema().getMetadata(SolrGlobalGroup.COLLECTIONS);
	}

	public GlobalGroup newGlobalGroup() {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION);
		return new SolrGlobalGroup(create(globalGroupSchema()), types);
	}

	public GlobalGroup wrapGlobalGroup(Record record) {
		MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION);
		return new SolrGlobalGroup(record, types);
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

	public List<Event> searchEvents(LogicalSearchQuery query) {
		return wrapEvents(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Event> searchEvents(LogicalSearchCondition condition) {
		MetadataSchemaType type = eventSchemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapEvents(modelLayerFactory.newSearchServices().search(query));
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

	public MetadataSchemaType temporaryRecordSchemaType() {
		return getTypes().getSchemaType(TemporaryRecord.SCHEMA_TYPE);
	}

	public MetadataSchema temporaryRecord() {
		return getTypes().getSchema(TemporaryRecord.DEFAULT_SCHEMA);
	}

	public TemporaryRecord newTemporaryRecord() {
		return new TemporaryRecord(create(defaultSchema(TemporaryRecord.SCHEMA_TYPE)), getTypes());
	}

	public ImportAudit newImportAudit() {
		return new ImportAudit(create(getTypes().getSchema(ImportAudit.FULL_SCHEMA)), getTypes());
	}

	public ExportAudit newExportAudit() {
		return new ExportAudit(create(getTypes().getSchema(ExportAudit.FULL_SCHEMA)), getTypes());
	}

	public BatchProcessReport newBatchProcessReport() {
		return new BatchProcessReport(create(getTypes().getSchema(BatchProcessReport.FULL_SCHEMA)), getTypes());
	}

	public VaultScanReport newVaultScanReport() {
		return new VaultScanReport(create(getTypes().getSchema(VaultScanReport.FULL_SCHEMA)), getTypes());
	}

	//Groups

	public MetadataSchema groupSchema() {
		return getTypes().getSchema(Group.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType groupSchemaType() {
		return getTypes().getSchemaType(Group.SCHEMA_TYPE);
	}

	public Group wrapGroup(Record record) {
		return record == null ? null : new Group(record, getTypes());
	}

	public List<Group> wrapGroups(List<Record> records) {
		List<Group> users = new ArrayList<>();
		for (Record record : records) {
			users.add(new Group(record, getTypes()));
		}
		return users;
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

	public UserCredential wrapUserCredential(Record record) {
		return new SolrUserCredential(record, getTypes());
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

	public User newUser() {
		return new User(create(userSchema()), getTypes(), getRoles());
	}

	public User newUserWithId(String id) {
		return new User(create(userSchema(), id), getTypes(), getRoles());
	}

	private Roles getRoles() {
		return modelLayerFactory.getRolesManager().getCollectionRoles(getCollection(), modelLayerFactory);
	}

	public void executeTransaction(Transaction tx)
			throws RecordServicesException {
		getModelLayerFactory().newRecordServices().execute(tx);
	}

	public void executeTransactionHandlingImpactsAsync(Transaction tx)
			throws RecordServicesException {
		getModelLayerFactory().newRecordServices().executeHandlingImpactsAsync(tx);
	}

	public void executeTransactionWithoutImpactHandling(Transaction tx)
			throws RecordServicesException {
		getModelLayerFactory().newRecordServices().executeWithoutImpactHandling(tx);
	}

	public List<SolrAuthorizationDetails> getAllAuthorizations() {
		return wrapSolrAuthorizationDetailss(
				getModelLayerFactory().newSearchServices().getAllRecordsInUnmodifiableState(authorizationDetails.schemaType()));
	}

	public List<SolrAuthorizationDetails> getAllAuthorizationsInUnmodifiableState() {
		return wrapSolrAuthorizationDetailss(
				getModelLayerFactory().newSearchServices().getAllRecordsInUnmodifiableState(authorizationDetails.schemaType()));
	}

	public List<User> getAllUsers() {
		return wrapUsers(getModelLayerFactory().newSearchServices().getAllRecordsInUnmodifiableState(user.schemaType()));
	}

	public List<User> getAllUsersInUnmodifiableState() {
		return wrapUsers(getModelLayerFactory().newSearchServices().getAllRecordsInUnmodifiableState(user.schemaType()));
	}

	public List<Group> getAllGroups() {
		return wrapGroups(getModelLayerFactory().newSearchServices().getAllRecordsInUnmodifiableState(group.schemaType()));
	}

	public List<Report> getAllReports() {
		return wrapReports(getModelLayerFactory().newSearchServices().getAllRecords(report.schemaType()));
	}

	public SolrAuthorizationDetails getSolrAuthorizationDetails(String id) {
		return wrapSolrAuthorizationDetails(get(id));
	}

	public User getUser(String id) {
		return wrapUser(get(id));
	}

	public Group getGroup(String id) {
		return wrapGroup(get(id));
	}

	public GlobalGroup getGlobalGroup(String id) {
		return wrapGlobalGroup(get(id));
	}

	public GlobalGroup getGlobalGroupWithCode(String code) {
		return modelLayerFactory.newUserServices().getGroup(code);
	}

	public List<Capsule> getAllCapsules() {
		return wrapCapsules(getModelLayerFactory().newSearchServices().getAllRecords(capsule.schemaType()));
	}

	public BatchProcessReport wrapBatchProcessReport(Record record) {
		return record == null ? null : new BatchProcessReport(record, getTypes());
	}

	public List<BatchProcessReport> wrapBatchProcessReports(List<Record> records) {
		List<BatchProcessReport> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new BatchProcessReport(record, getTypes()));
		}

		return wrapped;
	}

	public boolean isGroupActive(String aGroup) {
		return modelLayerFactory.newUserServices().isGroupActive(aGroup);
	}

	public boolean isGroupActive(Group aGroup) {
		return modelLayerFactory.newUserServices().isGroupActive(aGroup);
	}

	public List<User> getAllUsersInGroup(Group group, boolean includeGroupInheritance, boolean onlyActiveUsersAndGroups) {
		return modelLayerFactory.newUserServices().getAllUsersInGroup(group, includeGroupInheritance, onlyActiveUsersAndGroups);
	}
}
