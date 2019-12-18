package com.constellio.app.api.cmis.requests.navigation;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.constellio.model.services.search.StatusFilter.ACTIVES;
import static com.constellio.model.services.taxonomies.TaxonomiesSearchOptions.HasChildrenFlagCalculated.NEVER;

public class GetChildrenRequest extends CmisCollectionRequest<ObjectInFolderList> {

	public static final String LEGACY_ROOT_ID = "@root@";

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final String folderId;
	private final Set<String> filter;
	private final boolean includeAllowableActions;
	private final boolean includePathSegment;
	private final BigInteger maxItems;
	private final BigInteger skipCount;
	private final ObjectInfoHandler objectInfo;

	public GetChildrenRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
							  CallContext context, String folderId, String filter, Boolean includeAllowableActions,
							  Boolean includePathSegment,
							  BigInteger maxItems, BigInteger skipCount, ObjectInfoHandler objectInfo) {
		super(context, repository, appLayerFactory);
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

		TaxonomiesSearchOptions options = new TaxonomiesSearchOptions(maxItems.intValue(), skipCount.intValue(), ACTIVES)
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true)
				.setShowInvisibleRecordsInLinkingMode(true)
				.setHasChildrenFlagCalculated(NEVER)
				.setLinkableFlagCalculated(false);

		List<Record> childRecords;
		if (collection.equals(folderId) || folderId.equals(LEGACY_ROOT_ID)) {
			List<Taxonomy> taxonomies = taxonomiesManager.getEnabledTaxonomies(collection);
			for (Taxonomy taxonomy : taxonomies) {
				ObjectData object = newTaxonomyObjectBuilder().build(taxonomy, objectInfo, Language.withCode(modelLayerFactory.getCollectionsListManager().getMainDataLanguage()));
				children.getObjects().add(new ObjectInFolderDataImpl(object));
			}
		} else if (folderId.startsWith("taxo_")) {
			String taxonomyCode = folderId.substring(5);

			childRecords = new ArrayList<>();
			for (TaxonomySearchRecord record : searchServices.getVisibleRootConcept(user, collection, taxonomyCode, options)) {
				if (repository.getTypeDefinitionsManager().hasTypeDefinition(record.getRecord().getSchemaCode())) {
					childRecords.add(record.getRecord());
				}
			}
			addFoldersToChildren(children, childRecords);
		} else {
			Record record = recordServices.getDocumentById(folderId);
			ensureUserHasAllowableActionsOnRecord(record, Action.CAN_GET_CHILDREN);
			childRecords = new ArrayList<>();

			for (TaxonomySearchRecord child : searchServices.getVisibleChildConcept(user, record, options)) {
				if (repository.getTypeDefinitionsManager().hasTypeDefinition(child.getRecord().getSchemaCode())) {
					childRecords.add(child.getRecord());
				}

			}

			addFoldersToChildren(children, childRecords);
			addDocumentsToChildren(children, record);
		}
		return children;
	}

	private void addFoldersToChildren(ObjectInFolderList children, List<Record> childRecords) {
		for (Record childRecord : childRecords) {
			ObjectData object = newObjectDataBuilder().build(childRecord, filter, includeAllowableActions, false, objectInfo);
			children.getObjects().add(new ObjectInFolderDataImpl(object));
		}
	}

	private void addDocumentsToChildren(ObjectInFolderList children, Record record) {
		MetadataSchema schema = types().getSchema(record.getSchemaCode());
		for (Metadata metadata : schema.getMetadatas().onlyWithType(MetadataValueType.CONTENT)) {
			addDocumentsForMetadata(children, record, metadata);
		}
	}

	private void addDocumentsForMetadata(ObjectInFolderList children, Record record, Metadata metadata) {
		if (record.get(metadata) != null) {
			if (metadata.isMultivalue() == true) {
				List<Content> contents = record.getList(metadata);
				for (Content content : contents) {
					addDocumentToChildren(children, record, metadata, content);
				}
			} else {
				Content content = record.get(metadata);
				addDocumentToChildren(children, record, metadata, content);
			}
		}
	}

	private void addDocumentToChildren(ObjectInFolderList children, Record record, Metadata metadata, Content content) {
		ContentCmisDocument contentDocument = ContentCmisDocument
				.createForVersionSeenBy(content, record, metadata.getLocalCode(), user);
		ObjectData contentObject = newContentObjectDataBuilder()
				.build(contentDocument, filter, includeAllowableActions, false, objectInfo);
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
