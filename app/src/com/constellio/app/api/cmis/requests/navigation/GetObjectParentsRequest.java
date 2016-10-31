package com.constellio.app.api.cmis.requests.navigation;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class GetObjectParentsRequest extends CmisCollectionRequest<List<ObjectParentData>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final Set<String> filter;
	private final String objectId;
	private final Boolean includeAllowableActions;
	private final Boolean includeRelativePathSegment;
	private final ObjectInfoHandler objectInfo;

	public GetObjectParentsRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, String objectId, String filter, Boolean includeAllowableActions,
			Boolean includeRelativePathSegment, ObjectInfoHandler objectInfo) {
		super(context, repository, appLayerFactory);
		if (filter != null) {
			this.filter = CmisUtils.splitFilter(filter);
		} else {
			this.filter = null;
		}
		this.objectId = objectId;
		this.includeAllowableActions = includeAllowableActions;
		this.includeRelativePathSegment = includeRelativePathSegment;
		this.objectInfo = objectInfo;
	}

	@Override
	protected List<ObjectParentData> process()
			throws ConstellioCmisException {

		ObjectParentData parent;

		if (objectId.startsWith("content_")) {
			parent = new ObjectParentDataImpl(buildContentObjectData());

		} else if (objectId.startsWith("taxo_")) {
			Record record = appLayerFactory.getCollectionsManager().getCollection(collection).getWrappedRecord();
			parent = new ObjectParentDataImpl(newObjectDataBuilder().build(record, filter, false, false, objectInfo));

		} else {
			Record record = recordServices.getDocumentById(objectId, user);
			parent = new ObjectParentDataImpl(buildRecordObjectData(record));
		}
		return Collections.<ObjectParentData>singletonList(parent);
	}

	private ObjectData buildContentObjectData() {
		ContentCmisDocument content = CmisContentUtils.getContent(objectId, recordServices, types());
		return newObjectDataBuilder().build(content.getRecord(), filter, false, false, objectInfo);
	}

	private ObjectData buildRecordObjectData(Record record) {

		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(collection);
		MetadataSchema schema = types().getSchema(record.getSchemaCode());
		List<Metadata> parentReferencesMetadatas = schema.getParentReferences();
		List<Metadata> referencesMetadatas = schema.getTaxonomyRelationshipReferences(asList(principalTaxonomy));
		List<Metadata> allReferencesMetadatas = new ArrayList<>();
		allReferencesMetadatas.addAll(parentReferencesMetadatas);
		allReferencesMetadatas.addAll(referencesMetadatas);

		Record parentRecord = null;
		for (Metadata referenceMetadata : allReferencesMetadatas) {
			if (record.get(referenceMetadata) != null) {
				String parentId = record.get(referenceMetadata);
				parentRecord = recordServices.getDocumentById(parentId, user);
				break;
			}
		}
		if (parentRecord == null) {
			return null;
		} else {
			return newObjectDataBuilder().build(parentRecord, filter, false, false, objectInfo);
		}

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
