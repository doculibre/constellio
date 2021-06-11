package com.constellio.model.services.records;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.records.wrappers.CapsuleLanguage;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Conversation;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.Source;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public abstract class GeneratedSchemasRecordsServices extends BaseSchemasRecordsServices {

	public GeneratedSchemasRecordsServices(String collection,
										   Factory<ModelLayerFactory> modelLayerFactoryFactory) {
		super(collection, modelLayerFactoryFactory);
	}

	public GeneratedSchemasRecordsServices(String collection,
										   Factory<ModelLayerFactory> modelLayerFactoryFactory, Locale locale) {
		super(collection, modelLayerFactoryFactory, locale);
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start

	/**
	 * * ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **
	 **/


	public Authorization wrapAuthorization(Record record) {
		return record == null ? null : new RecordAuthorization(record, getTypes());
	}

	public List<Authorization> wrapAuthorizations(List<Record> records) {
		List<Authorization> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new RecordAuthorization(record, getTypes()));
		}

		return wrapped;
	}

	public List<Authorization> searchAuthorizations(LogicalSearchQuery query) {
		return wrapAuthorizations(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Authorization> searchAuthorizations(LogicalSearchCondition condition) {
		MetadataSchemaType type = authorizationDetails.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapAuthorizations(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<Authorization> authorizationIterator() {
		return iterateFromCache(authorizationDetails.schemaType(), this::wrapAuthorization);
	}

	public Stream<Authorization> authorizationStream() {
		return streamFromCache(authorizationDetails.schemaType(), this::wrapAuthorization);
	}

	public Authorization getAuthorization(String id) {
		return wrapAuthorization(get(authorizationDetails.schemaType(), id));
	}

	public List<Authorization> getAuthorizations(List<String> ids) {
		return wrapAuthorizations(get(authorizationDetails.schemaType(), ids));
	}

	public Authorization getAuthorizationWithLegacyId(String legacyId) {
		return wrapAuthorization(getByLegacyId(authorizationDetails.schemaType(), legacyId));
	}

	public Authorization newAuthorization() {
		return wrapAuthorization(create(authorizationDetails.schema()));
	}

	public Authorization newAuthorizationWithId(String id) {
		return wrapAuthorization(create(authorizationDetails.schema(), id));
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

		public Metadata lastTokenRecalculate() {
			return metadata("lastTokenRecalculate");
		}

		public Metadata negative() {
			return metadata("negative");
		}

		public Metadata overrideInherited() {
			return metadata("overrideInherited");
		}

		public Metadata roles() {
			return metadata("roles");
		}

		public Metadata sharedBy() {
			return metadata("sharedBy");
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

		public Metadata principals() {return metadata("principals");}
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

	public Iterator<Capsule> capsuleIterator() {
		return iterateFromCache(capsule.schemaType(), this::wrapCapsule);
	}

	public Stream<Capsule> capsuleStream() {
		return streamFromCache(capsule.schemaType(), this::wrapCapsule);
	}

	public Capsule getCapsule(String id) {
		return wrapCapsule(get(capsule.schemaType(), id));
	}

	public List<Capsule> getCapsules(List<String> ids) {
		return wrapCapsules(get(capsule.schemaType(), ids));
	}

	public Capsule getCapsuleWithCode(String code) {
		return wrapCapsule(getByCode(capsule.schemaType(), code));
	}

	public Capsule getCapsuleWithLegacyId(String legacyId) {
		return wrapCapsule(getByLegacyId(capsule.schemaType(), legacyId));
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

		public Metadata images() {
			return metadata("images");
		}

		public Metadata keywords() {
			return metadata("keywords");
		}

		public Metadata language() {
			return metadata("language");
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

	public Iterator<Collection> collectionIterator() {
		return iterateFromCache(collection.schemaType(), this::wrapCollection);
	}

	public Stream<Collection> collectionStream() {
		return streamFromCache(collection.schemaType(), this::wrapCollection);
	}

	public Collection getCollection(String id) {
		return wrapCollection(get(collection.schemaType(), id));
	}

	public List<Collection> getCollections(List<String> ids) {
		return wrapCollections(get(collection.schemaType(), ids));
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
	public CapsuleLanguage wrapCapsuleLanguage(Record record) {
		return record == null ? null : new CapsuleLanguage(record, getTypes());
	}

	public List<CapsuleLanguage> wrapCapsuleLanguages(List<Record> records) {
		List<CapsuleLanguage> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new CapsuleLanguage(record, getTypes()));
		}

		return wrapped;
	}

	public List<CapsuleLanguage> searchCapsuleLanguages(LogicalSearchQuery query) {
		return wrapCapsuleLanguages(modelLayerFactory.newSearchServices().search(query));
	}

	public List<CapsuleLanguage> searchCapsuleLanguages(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvCapsuleLanguage.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapCapsuleLanguages(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<CapsuleLanguage> capsuleLanguageIterator() {
		return iterateFromCache(ddvCapsuleLanguage.schemaType(), this::wrapCapsuleLanguage);
	}

	public Stream<CapsuleLanguage> capsuleLanguageStream() {
		return streamFromCache(ddvCapsuleLanguage.schemaType(), this::wrapCapsuleLanguage);
	}

	public CapsuleLanguage getCapsuleLanguage(String id) {
		return wrapCapsuleLanguage(get(ddvCapsuleLanguage.schemaType(), id));
	}

	public List<CapsuleLanguage> getCapsuleLanguages(List<String> ids) {
		return wrapCapsuleLanguages(get(ddvCapsuleLanguage.schemaType(), ids));
	}

	public CapsuleLanguage getCapsuleLanguageWithCode(String code) {
		return wrapCapsuleLanguage(getByCode(ddvCapsuleLanguage.schemaType(), code));
	}

	public CapsuleLanguage getCapsuleLanguageWithLegacyId(String legacyId) {
		return wrapCapsuleLanguage(getByLegacyId(ddvCapsuleLanguage.schemaType(), legacyId));
	}

	public CapsuleLanguage newCapsuleLanguage() {
		return wrapCapsuleLanguage(create(ddvCapsuleLanguage.schema()));
	}

	public CapsuleLanguage newCapsuleLanguageWithId(String id) {
		return wrapCapsuleLanguage(create(ddvCapsuleLanguage.schema(), id));
	}

	public final SchemaTypeShortcuts_ddvCapsuleLanguage_default ddvCapsuleLanguage
			= new SchemaTypeShortcuts_ddvCapsuleLanguage_default("ddvCapsuleLanguage_default");

	public class SchemaTypeShortcuts_ddvCapsuleLanguage_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_ddvCapsuleLanguage_default(String schemaCode) {
			super(schemaCode);
		}
	}

	public Source wrapSource(Record record) {
		return record == null ? null : new Source(record, getTypes());
	}

	public List<Source> wrapSources(List<Record> records) {
		List<Source> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Source(record, getTypes()));
		}

		return wrapped;
	}

	public List<Source> searchSources(LogicalSearchQuery query) {
		return wrapSources(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Source> searchSources(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvSource.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapSources(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<Source> sourceIterator() {
		return iterateFromCache(ddvSource.schemaType(), this::wrapSource);
	}

	public Stream<Source> sourceStream() {
		return streamFromCache(ddvSource.schemaType(), this::wrapSource);
	}

	public Source getSource(String id) {
		return wrapSource(get(ddvSource.schemaType(), id));
	}

	public List<Source> getSources(List<String> ids) {
		return wrapSources(get(ddvSource.schemaType(), ids));
	}

	public Source getSourceWithCode(String code) {
		return wrapSource(getByCode(ddvSource.schemaType(), code));
	}

	public Source getSourceWithLegacyId(String legacyId) {
		return wrapSource(getByLegacyId(ddvSource.schemaType(), legacyId));
	}

	public Source newSource() {
		return wrapSource(create(ddvSource.schema()));
	}

	public Source newSourceWithId(String id) {
		return wrapSource(create(ddvSource.schema(), id));
	}

	public final SchemaTypeShortcuts_ddvSource_default ddvSource
			= new SchemaTypeShortcuts_ddvSource_default("ddvSource_default");

	public class SchemaTypeShortcuts_ddvSource_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_ddvSource_default(String schemaCode) {
			super(schemaCode);
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

	public Iterator<EmailToSend> emailToSendIterator(LogicalSearchCondition condition) {
		return searchIterator(from(emailToSend.schemaType()).whereAllConditions(asList(condition)), this::wrapEmailToSend);
	}

	public Stream<EmailToSend> emailToSendStream(LogicalSearchCondition condition) {
		return searchIterator(from(emailToSend.schemaType()).whereAllConditions(asList(condition)), this::wrapEmailToSend).stream();
	}

	public Iterator<EmailToSend> emailToSendIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapEmailToSend);
	}

	public Stream<EmailToSend> emailToSendStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapEmailToSend).stream();
	}

	public EmailToSend getEmailToSend(String id) {
		return wrapEmailToSend(get(emailToSend.schemaType(), id));
	}

	public List<EmailToSend> getEmailToSends(List<String> ids) {
		return wrapEmailToSends(get(emailToSend.schemaType(), ids));
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

		public Metadata body() {
			return metadata("body");
		}

		public Metadata error() {
			return metadata("error");
		}

		public Metadata from() {
			return metadata("from");
		}

		public Metadata linkedFiles() {
			return metadata("linkedFiles");
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

	public Iterator<Event> eventIterator(LogicalSearchCondition condition) {
		return searchIterator(from(event.schemaType()).whereAllConditions(asList(condition)), this::wrapEvent);
	}

	public Stream<Event> eventStream(LogicalSearchCondition condition) {
		return searchIterator(from(event.schemaType()).whereAllConditions(asList(condition)), this::wrapEvent).stream();
	}

	public Iterator<Event> eventIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapEvent);
	}

	public Stream<Event> eventStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapEvent).stream();
	}

	public Event getEvent(String id) {
		return wrapEvent(get(event.schemaType(), id));
	}

	public List<Event> getEvents(List<String> ids) {
		return wrapEvents(get(event.schemaType(), ids));
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

		public Metadata batchProcessIdentifier() {
			return metadata("batchProcessIdentifier");
		}

		public Metadata content() {
			return metadata("content");
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

		public Metadata negative() {
			return metadata("negative");
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

		public Metadata sharedBy() {
			return metadata("sharedBy");
		}

		public Metadata totalModifiedRecord() {
			return metadata("totalModifiedRecord");
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
	public ExternalAccessUrl wrapExternalAccessUrl(Record record) {
		return record == null ? null : new ExternalAccessUrl(record, getTypes());
	}

	public List<ExternalAccessUrl> wrapExternalAccessUrls(List<Record> records) {
		List<ExternalAccessUrl> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new ExternalAccessUrl(record, getTypes()));
		}

		return wrapped;
	}

	public List<ExternalAccessUrl> searchExternalAccessUrls(LogicalSearchQuery query) {
		return wrapExternalAccessUrls(modelLayerFactory.newSearchServices().search(query));
	}

	public List<ExternalAccessUrl> searchExternalAccessUrls(LogicalSearchCondition condition) {
		MetadataSchemaType type = externalAccessUrl.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapExternalAccessUrls(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<ExternalAccessUrl> externalAccessUrlIterator() {
		return iterateFromCache(externalAccessUrl.schemaType(), this::wrapExternalAccessUrl);
	}

	public Stream<ExternalAccessUrl> externalAccessUrlStream() {
		return streamFromCache(externalAccessUrl.schemaType(), this::wrapExternalAccessUrl);
	}

	public ExternalAccessUrl getExternalAccessUrl(String id) {
		return wrapExternalAccessUrl(get(externalAccessUrl.schemaType(), id));
	}

	public List<ExternalAccessUrl> getExternalAccessUrls(List<String> ids) {
		return wrapExternalAccessUrls(get(externalAccessUrl.schemaType(), ids));
	}

	public ExternalAccessUrl getExternalAccessUrlWithLegacyId(String legacyId) {
		return wrapExternalAccessUrl(getByLegacyId(externalAccessUrl.schemaType(), legacyId));
	}

	public ExternalAccessUrl newExternalAccessUrl() {
		return wrapExternalAccessUrl(create(externalAccessUrl.schema()));
	}

	public ExternalAccessUrl newExternalAccessUrlWithId(String id) {
		return wrapExternalAccessUrl(create(externalAccessUrl.schema(), id));
	}

	public final SchemaTypeShortcuts_externalAccessUrl_default externalAccessUrl
			= new SchemaTypeShortcuts_externalAccessUrl_default("externalAccessUrl_default");

	public class SchemaTypeShortcuts_externalAccessUrl_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_externalAccessUrl_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata accessRecord() {
			return metadata("accessRecord");
		}

		public Metadata email() {
			return metadata("email");
		}

		public Metadata expirationDate() {
			return metadata("expirationDate");
		}

		public Metadata fullname() {
			return metadata("fullname");
		}

		public Metadata roles() {
			return metadata("roles");
		}

		public Metadata status() {
			return metadata("status");
		}

		public Metadata token() {
			return metadata("token");
		}

		public Metadata user() {
			return metadata("user");
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

	public Iterator<Facet> facetIterator() {
		return iterateFromCache(facet.schemaType(), this::wrapFacet);
	}

	public Stream<Facet> facetStream() {
		return streamFromCache(facet.schemaType(), this::wrapFacet);
	}

	public Facet getFacet(String id) {
		return wrapFacet(get(facet.schemaType(), id));
	}

	public List<Facet> getFacets(List<String> ids) {
		return wrapFacets(get(facet.schemaType(), ids));
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

	public Iterator<Group> groupIterator() {
		return iterateFromCache(group.schemaType(), this::wrapGroup);
	}

	public Stream<Group> groupStream() {
		return streamFromCache(group.schemaType(), this::wrapGroup);
	}

	public Group getGroup(String id) {
		return wrapGroup(get(group.schemaType(), id));
	}

	public List<Group> getGroups(List<String> ids) {
		return wrapGroups(get(group.schemaType(), ids));
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

		public Metadata ancestors() {
			return metadata("ancestors");
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata hierarchy() {
			return metadata("hierarchy");
		}

		public Metadata isGlobal() {
			return metadata("isGlobal");
		}

		public Metadata locallyCreated() {
			return metadata("locallyCreated");
		}

		public Metadata parent() {
			return metadata("parent");
		}

		public Metadata roles() {
			return metadata("roles");
		}

		public Metadata status() {
			return metadata("status");
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

	public Iterator<Printable> printableIterator() {
		return iterateFromCache(printable.schemaType(), this::wrapPrintable);
	}

	public Stream<Printable> printableStream() {
		return streamFromCache(printable.schemaType(), this::wrapPrintable);
	}

	public Printable getPrintable(String id) {
		return wrapPrintable(get(printable.schemaType(), id));
	}

	public List<Printable> getPrintables(List<String> ids) {
		return wrapPrintables(get(printable.schemaType(), ids));
	}

	public Printable getPrintableWithLegacyId(String legacyId) {
		return wrapPrintable(getByLegacyId(printable.schemaType(), legacyId));
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
	public Report wrapReport(Record record) {
		return record == null ? null : new Report(record, getTypes());
	}

	public List<Report> wrapReports(List<Record> records) {
		List<Report> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Report(record, getTypes()));
		}

		return wrapped;
	}

	public List<Report> searchReports(LogicalSearchQuery query) {
		return wrapReports(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Report> searchReports(LogicalSearchCondition condition) {
		MetadataSchemaType type = report.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapReports(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<Report> reportIterator() {
		return iterateFromCache(report.schemaType(), this::wrapReport);
	}

	public Stream<Report> reportStream() {
		return streamFromCache(report.schemaType(), this::wrapReport);
	}

	public Report getReport(String id) {
		return wrapReport(get(report.schemaType(), id));
	}

	public List<Report> getReports(List<String> ids) {
		return wrapReports(get(report.schemaType(), ids));
	}

	public Report getReportWithLegacyId(String legacyId) {
		return wrapReport(getByLegacyId(report.schemaType(), legacyId));
	}

	public Report newReport() {
		return wrapReport(create(report.schema()));
	}

	public Report newReportWithId(String id) {
		return wrapReport(create(report.schema(), id));
	}

	public final SchemaTypeShortcuts_report_default report
			= new SchemaTypeShortcuts_report_default("report_default");
	public class SchemaTypeShortcuts_report_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_report_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata columnsCount() {
			return metadata("columnsCount");
		}

		public Metadata linesCount() {
			return metadata("linesCount");
		}

		public Metadata reportedMetadata() {
			return metadata("reportedMetadata");
		}

		public Metadata schemaTypeCode() {
			return metadata("schemaTypeCode");
		}

		public Metadata separator() {
			return metadata("separator");
		}

		public Metadata username() {
			return metadata("username");
		}
	}
	public SearchEvent wrapSearchEvent(Record record) {
		return record == null ? null : new SearchEvent(record, getTypes());
	}

	public List<SearchEvent> wrapSearchEvents(List<Record> records) {
		List<SearchEvent> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new SearchEvent(record, getTypes()));
		}

		return wrapped;
	}

	public List<SearchEvent> searchSearchEvents(LogicalSearchQuery query) {
		return wrapSearchEvents(modelLayerFactory.newSearchServices().search(query));
	}

	public List<SearchEvent> searchSearchEvents(LogicalSearchCondition condition) {
		MetadataSchemaType type = searchEvent.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapSearchEvents(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<SearchEvent> searchEventIterator(LogicalSearchCondition condition) {
		return searchIterator(from(searchEvent.schemaType()).whereAllConditions(asList(condition)), this::wrapSearchEvent);
	}

	public Stream<SearchEvent> searchEventStream(LogicalSearchCondition condition) {
		return searchIterator(from(searchEvent.schemaType()).whereAllConditions(asList(condition)), this::wrapSearchEvent).stream();
	}

	public Iterator<SearchEvent> searchEventIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapSearchEvent);
	}

	public Stream<SearchEvent> searchEventStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapSearchEvent).stream();
	}

	public SearchEvent getSearchEvent(String id) {
		return wrapSearchEvent(get(searchEvent.schemaType(), id));
	}

	public List<SearchEvent> getSearchEvents(List<String> ids) {
		return wrapSearchEvents(get(searchEvent.schemaType(), ids));
	}

	public SearchEvent getSearchEventWithLegacyId(String legacyId) {
		return wrapSearchEvent(getByLegacyId(searchEvent.schemaType(), legacyId));
	}

	public SearchEvent newSearchEvent() {
		return wrapSearchEvent(create(searchEvent.schema()));
	}

	public SearchEvent newSearchEventWithId(String id) {
		return wrapSearchEvent(create(searchEvent.schema(), id));
	}

	public final SchemaTypeShortcuts_searchEvent_default searchEvent
			= new SchemaTypeShortcuts_searchEvent_default("searchEvent_default");
	public class SchemaTypeShortcuts_searchEvent_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_searchEvent_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata capsule() {
			return metadata("capsule");
		}

		public Metadata clickCount() {
			return metadata("clickCount");
		}

		public Metadata clicks() {
			return metadata("clicks");
		}

		public Metadata dwellTime() {
			return metadata("dwellTime");
		}

		public Metadata lastPageNavigation() {
			return metadata("lastPageNavigation");
		}

		public Metadata numFound() {
			return metadata("numFound");
		}

		public Metadata originalQuery() {
			return metadata("originalQuery");
		}

		public Metadata pageNavigationCount() {
			return metadata("pageNavigationCount");
		}

		public Metadata params() {
			return metadata("params");
		}

		public Metadata qTime() {
			return metadata("qTime");
		}

		public Metadata query() {
			return metadata("query");
		}

		public Metadata username() {
			return metadata("username");
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

	public Iterator<TemporaryRecord> temporaryRecordIterator() {
		return iterateFromCache(temporaryRecord.schemaType(), this::wrapTemporaryRecord);
	}

	public Stream<TemporaryRecord> temporaryRecordStream() {
		return streamFromCache(temporaryRecord.schemaType(), this::wrapTemporaryRecord);
	}

	public TemporaryRecord getTemporaryRecord(String id) {
		return wrapTemporaryRecord(get(temporaryRecord.schemaType(), id));
	}

	public List<TemporaryRecord> getTemporaryRecords(List<String> ids) {
		return wrapTemporaryRecords(get(temporaryRecord.schemaType(), ids));
	}

	public TemporaryRecord getTemporaryRecordWithLegacyId(String legacyId) {
		return wrapTemporaryRecord(getByLegacyId(temporaryRecord.schemaType(), legacyId));
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
	public ThesaurusConfig wrapThesaurusConfig(Record record) {
		return record == null ? null : new ThesaurusConfig(record, getTypes());
	}

	public List<ThesaurusConfig> wrapThesaurusConfigs(List<Record> records) {
		List<ThesaurusConfig> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new ThesaurusConfig(record, getTypes()));
		}

		return wrapped;
	}

	public List<ThesaurusConfig> searchThesaurusConfigs(LogicalSearchQuery query) {
		return wrapThesaurusConfigs(modelLayerFactory.newSearchServices().search(query));
	}

	public List<ThesaurusConfig> searchThesaurusConfigs(LogicalSearchCondition condition) {
		MetadataSchemaType type = thesaurusConfig.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapThesaurusConfigs(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<ThesaurusConfig> thesaurusConfigIterator() {
		return iterateFromCache(thesaurusConfig.schemaType(), this::wrapThesaurusConfig);
	}

	public Stream<ThesaurusConfig> thesaurusConfigStream() {
		return streamFromCache(thesaurusConfig.schemaType(), this::wrapThesaurusConfig);
	}

	public ThesaurusConfig getThesaurusConfig(String id) {
		return wrapThesaurusConfig(get(thesaurusConfig.schemaType(), id));
	}

	public List<ThesaurusConfig> getThesaurusConfigs(List<String> ids) {
		return wrapThesaurusConfigs(get(thesaurusConfig.schemaType(), ids));
	}

	public ThesaurusConfig getThesaurusConfigWithLegacyId(String legacyId) {
		return wrapThesaurusConfig(getByLegacyId(thesaurusConfig.schemaType(), legacyId));
	}

	public ThesaurusConfig newThesaurusConfig() {
		return wrapThesaurusConfig(create(thesaurusConfig.schema()));
	}

	public ThesaurusConfig newThesaurusConfigWithId(String id) {
		return wrapThesaurusConfig(create(thesaurusConfig.schema(), id));
	}

	public final SchemaTypeShortcuts_thesaurusConfig_default thesaurusConfig
			= new SchemaTypeShortcuts_thesaurusConfig_default("thesaurusConfig_default");
	public class SchemaTypeShortcuts_thesaurusConfig_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_thesaurusConfig_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata content() {
			return metadata("content");
		}

		public Metadata deniedWord() {
			return metadata("deniedWord");
		}
	}
	public abstract User wrapUser(Record record);
	public abstract List<User> wrapUsers(List<Record> records);

	public List<User> searchUsers(LogicalSearchQuery query) {
		return wrapUsers(modelLayerFactory.newSearchServices().search(query));
	}

	public List<User> searchUsers(LogicalSearchCondition condition) {
		MetadataSchemaType type = user.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapUsers(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<User> userIterator() {
		return iterateFromCache(user.schemaType(), this::wrapUser);
	}

	public Stream<User> userStream() {
		return streamFromCache(user.schemaType(), this::wrapUser);
	}

	public User getUser(String id) {
		return wrapUser(get(user.schemaType(), id));
	}

	public List<User> getUsers(List<String> ids) {
		return wrapUsers(get(user.schemaType(), ids));
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

		public Metadata address() {
			return metadata("address");
		}

		public Metadata agentEnabled() {
			return metadata("agentEnabled");
		}

		public Metadata allroles() {
			return metadata("allroles");
		}

		public Metadata azureuser() {
			return metadata("azureuser");
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

		public Metadata doNotReceiveEmails() {
			return metadata("doNotReceiveEmails");
		}

		public Metadata domain() {
			return metadata("domain");
		}

		public Metadata email() {
			return metadata("email");
		}

		public Metadata enableFacetsApplyButton() {
			return metadata("enableFacetsApplyButton");
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

		public Metadata msExchangeDelegateList() {
			return metadata("msExchangeDelegateList");
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

		public Metadata taxonomyDisplayOrder() {
			return metadata("taxonomyDisplayOrder");
		}

		public Metadata userDocumentSizeSum() {
			return metadata("userDocumentSizeSum");
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

	public Iterator<UserDocument> userDocumentIterator(LogicalSearchCondition condition) {
		return searchIterator(from(userDocument.schemaType()).whereAllConditions(asList(condition)), this::wrapUserDocument);
	}

	public Stream<UserDocument> userDocumentStream(LogicalSearchCondition condition) {
		return searchIterator(from(userDocument.schemaType()).whereAllConditions(asList(condition)), this::wrapUserDocument).stream();
	}

	public Iterator<UserDocument> userDocumentIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapUserDocument);
	}

	public Stream<UserDocument> userDocumentStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapUserDocument).stream();
	}

	public UserDocument getUserDocument(String id) {
		return wrapUserDocument(get(userDocument.schemaType(), id));
	}

	public List<UserDocument> getUserDocuments(List<String> ids) {
		return wrapUserDocuments(get(userDocument.schemaType(), ids));
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

		public Metadata contentSize() {
			return metadata("contentSize");
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

		public Metadata contentHashes() {
			return metadata("contentHashes");
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

	public Iterator<UserFolder> userFolderIterator(LogicalSearchCondition condition) {
		return searchIterator(from(userFolder.schemaType()).whereAllConditions(asList(condition)), this::wrapUserFolder);
	}

	public Stream<UserFolder> userFolderStream(LogicalSearchCondition condition) {
		return searchIterator(from(userFolder.schemaType()).whereAllConditions(asList(condition)), this::wrapUserFolder).stream();
	}

	public Iterator<UserFolder> userFolderIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapUserFolder);
	}

	public Stream<UserFolder> userFolderStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapUserFolder).stream();
	}

	public UserFolder getUserFolder(String id) {
		return wrapUserFolder(get(userFolder.schemaType(), id));
	}

	public List<UserFolder> getUserFolders(List<String> ids) {
		return wrapUserFolders(get(userFolder.schemaType(), ids));
	}

	public UserFolder getUserFolderWithLegacyId(String legacyId) {
		return wrapUserFolder(getByLegacyId(userFolder.schemaType(), legacyId));
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

	public Message wrapMessage(Record record) {
		return record == null ? null : new Message(record, getTypes());
	}

	public List<Message> wrapMessages(List<Record> records) {
		List<Message> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Message(record, getTypes()));
		}

		return wrapped;
	}

	public List<Message> searchMessages(LogicalSearchQuery query) {
		return wrapMessages(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Message> searchMessages(LogicalSearchCondition condition) {
		MetadataSchemaType type = message.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapMessages(modelLayerFactory.newSearchServices().search(query));
	}

	public Message getMessage(String id) {
		return wrapMessage(get(message.schemaType(), id));
	}

	public List<Message> getMessages(List<String> ids) {
		return wrapMessages(get(message.schemaType(), ids));
	}

	public Message getMessageWithLegacyId(String legacyId) {
		return wrapMessage(getByLegacyId(message.schemaType(), legacyId));
	}

	public Message newMessage() {
		return wrapMessage(create(message.schema()));
	}

	public Message newMessageWithId(String id) {
		return wrapMessage(create(message.schema(), id));
	}

	public final SchemaTypeShortcuts_message_default message
			= new SchemaTypeShortcuts_message_default("message_default");

	public class SchemaTypeShortcuts_message_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_message_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata formCreatedOn() {
			return metadata("formCreatedOn");
		}

		public Metadata formModifiedOn() {
			return metadata("formModifiedOn");
		}

		public Metadata messageAuthor() {
			return metadata("messageAuthor");
		}

		public Metadata conversation() {
			return metadata("conversation");
		}

		public Metadata messageBody() {
			return metadata("messageBody");
		}

		public Metadata messageParent() {
			return metadata("messageParent");
		}
	}

	public Conversation wrapConversation(Record record) {
		return record == null ? null : new Conversation(record, getTypes());
	}

	public List<Conversation> wrapConversations(List<Record> records) {
		List<Conversation> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Conversation(record, getTypes()));
		}

		return wrapped;
	}

	public List<Conversation> searchConversations(LogicalSearchQuery query) {
		return wrapConversations(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Conversation> searchConversations(LogicalSearchCondition condition) {
		MetadataSchemaType type = conversation.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapConversations(modelLayerFactory.newSearchServices().search(query));
	}

	public Conversation getConversation(String id) {
		return wrapConversation(get(conversation.schemaType(), id));
	}

	public List<Conversation> getConversations(List<String> ids) {
		return wrapConversations(get(conversation.schemaType(), ids));
	}

	public Conversation getConversationWithLegacyId(String legacyId) {
		return wrapConversation(getByLegacyId(conversation.schemaType(), legacyId));
	}

	public Conversation newConversation() {
		return wrapConversation(create(conversation.schema()));
	}

	public Conversation newConversationWithId(String id) {
		return wrapConversation(create(conversation.schema(), id));
	}

	public final SchemaTypeShortcuts_conversation_default conversation
			= new SchemaTypeShortcuts_conversation_default("conversation_default");

	public class SchemaTypeShortcuts_conversation_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_conversation_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata formCreatedOn() {
			return metadata("formCreatedOn");
		}

		public Metadata formModifiedOn() {
			return metadata("formModifiedOn");
		}
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

}
