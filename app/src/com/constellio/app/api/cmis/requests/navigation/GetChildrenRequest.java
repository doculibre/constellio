package com.constellio.app.api.cmis.requests.navigation;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;

public class GetChildrenRequest extends CmisCollectionRequest<ObjectInFolderList> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final CallContext context;
	private final String folderId;
	private final Set<String> filter;
	private final boolean includeAllowableActions;
	private final boolean includePathSegment;
	private final BigInteger maxItems;
	private final BigInteger skipCount;
	private final ObjectInfoHandler objectInfo;

	public GetChildrenRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, String folderId, String filter, Boolean includeAllowableActions, Boolean includePathSegment,
			BigInteger maxItems, BigInteger skipCount, ObjectInfoHandler objectInfo) {
		super(repository, appLayerFactory);
		this.context = context;
		this.folderId = folderId;
		if (filter != null) {
			this.filter = CmisUtils.splitFilter(filter);
		} else {
			this.filter = null;
		}
		this.includeAllowableActions = includeAllowableActions;
		this.includePathSegment = includePathSegment;
		this.maxItems = maxItems;
		this.skipCount = skipCount;
		this.objectInfo = objectInfo;
	}

	@Override
	public ObjectInFolderList process() {

		ObjectInFolderList children = new ObjectInFolderListImpl();
		TaxonomiesSearchServices searchServices = modelLayerFactory.newTaxonomiesSearchService();
		TaxonomiesSearchOptions taxonomiesSearchOptions = new TaxonomiesSearchOptions(maxItems.intValue(), skipCount.intValue(),
				StatusFilter.ACTIVES);

		String collection = context.get(ConstellioCmisContextParameters.COLLECTION).toString();
		User user = (User) context.get(ConstellioCmisContextParameters.USER);
		List<Record> childRecords;
		if (collection.equals(folderId)) {
			List<Taxonomy> taxonomies = modelLayerFactory.getTaxonomiesManager().getEnabledTaxonomies(collection);
			for (Taxonomy taxonomy : taxonomies) {
				ObjectData object = newTaxonomyObjectBuilder().build(context, taxonomy, objectInfo);
				children.getObjects().add(new ObjectInFolderDataImpl(object));
			}
		} else if (folderId.startsWith("taxo_")) {
			childRecords = searchServices.getRootConcept(collection, folderId.substring(5), taxonomiesSearchOptions);
			addFoldersToChildren(children, childRecords);
		} else {
			Record record = modelLayerFactory.newRecordServices().getDocumentById(folderId, user);
			childRecords = searchServices.getChildConcept(record, taxonomiesSearchOptions);
			addFoldersToChildren(children, childRecords);
			addDocumentsToChildren(children, record, collection, user);
		}
		return children;
	}

	private void addFoldersToChildren(ObjectInFolderList children, List<Record> childRecords) {
		for (Record childRecord : childRecords) {
			ObjectData object = newObjectDataBuilder().build(context, childRecord, filter, includeAllowableActions, false,
					objectInfo);
			children.getObjects().add(new ObjectInFolderDataImpl(object));
		}
	}

	private void addDocumentsToChildren(ObjectInFolderList children, Record record, String collection, User user) {
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(record.getSchemaCode());
		for (Metadata metadata : schema.getMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
			addDocumentsForMetadata(children, record, user, metadata);
		}
	}

	private void addDocumentsForMetadata(ObjectInFolderList children, Record record, User user, Metadata metadata) {
		if (record.get(metadata) != null) {
			if (metadata.isMultivalue() == true) {
				List<Content> contents = record.getList(metadata);
				for (Content content : contents) {
					addDocumentToChildren(children, record, user, metadata, content);
				}
			} else {
				Content content = record.get(metadata);
				addDocumentToChildren(children, record, user, metadata, content);
			}
		}
	}

	private void addDocumentToChildren(ObjectInFolderList children, Record record, User user, Metadata metadata,
			Content content) {
		ContentCmisDocument contentDocument = ContentCmisDocument
				.createForVersionSeenBy(content, record, metadata.getLocalCode(), user);
		ObjectData contentObject = newContentObjectDataBuilder()
				.build(appLayerFactory, context, contentDocument, filter, includeAllowableActions, false, objectInfo);
		children.getObjects().add(new ObjectInFolderDataImpl(contentObject));
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
