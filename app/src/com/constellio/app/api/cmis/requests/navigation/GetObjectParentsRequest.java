package com.constellio.app.api.cmis.requests.navigation;

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
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public class GetObjectParentsRequest extends CmisCollectionRequest<List<ObjectParentData>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final CallContext context;
	private final Set<String> filter;
	private final String objectId;
	private final Boolean includeAllowableActions;
	private final Boolean includeRelativePathSegment;
	private final ObjectInfoHandler objectInfo;

	public GetObjectParentsRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, String objectId, String filter, Boolean includeAllowableActions,
			Boolean includeRelativePathSegment, ObjectInfoHandler objectInfo) {
		super(repository, appLayerFactory);
		this.context = context;
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

		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(repository.getCollection());
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		User user = (User) context.get(ConstellioCmisContextParameters.USER);
		if (objectId.startsWith("content_")) {
			ObjectData objectData = getObjectDataDocument(types);
			return Collections.<ObjectParentData>singletonList(new ObjectParentDataImpl(objectData));
		} else {
			ObjectData objectData = getObjectDataFolder(types, recordServices, user);
			return Collections.<ObjectParentData>singletonList(new ObjectParentDataImpl(objectData));
		}
	}

	private ObjectData getObjectDataDocument(MetadataSchemaTypes types) {
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		ContentCmisDocument content = CmisContentUtils.getContent(objectId, recordServices, types);

		return newObjectDataBuilder().build(context, content.getRecord(), filter, false, false, objectInfo);
	}

	private ObjectData getObjectDataFolder(MetadataSchemaTypes types, RecordServices recordServices, User user) {
		Record record = recordServices.getDocumentById(objectId, user);
		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		List<Metadata> parentReferencesMetadatas = schema.getParentReferences();
		List<Metadata> referencesMetadatas = schema.getTaxonomyRelationshipReferences(Arrays.asList(modelLayerFactory
				.getTaxonomiesManager().getPrincipalTaxonomy(record.getCollection())));

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
			//return newObjectDataBuilder().build(context, record, filter, false, false, objectInfo);
		} else {
			return newObjectDataBuilder().build(context, parentRecord, filter, false, false, objectInfo);
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
