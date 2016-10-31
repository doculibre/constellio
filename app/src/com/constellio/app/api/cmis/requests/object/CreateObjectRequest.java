package com.constellio.app.api.cmis.requests.object;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ObjectNotFound;
import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;

public class CreateObjectRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final Properties properties;
	private final ObjectInfoHandler objectInfos;
	CreateFolderRequest createFolderRequest;
	CreateDocumentRequest createDocumentRequest;

	public CreateObjectRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CreateFolderRequest createFolderRequest, CreateDocumentRequest createDocumentRequest, CallContext context,
			Properties properties, String folderId, ContentStream contentStream, VersioningState versioningState,
			ObjectInfoHandler objectInfos) {
		super(context, repository, appLayerFactory);
		this.createFolderRequest = createFolderRequest;
		this.createDocumentRequest = createDocumentRequest;
		this.properties = properties;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process()
			throws ConstellioCmisException {

		boolean userReadOnly = false;

		String typeId = CmisUtils.getObjectTypeId(properties);
		TypeDefinition type = repository.getTypeDefinitionsManager().getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new CmisExceptions_ObjectNotFound("Type", typeId);
		}

		if (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
			ContentCmisDocument contentCmisDocument = createDocumentRequest.process();
			ensureUserHasAllowableActionsOnRecord(contentCmisDocument.getRecord(), Action.CAN_CREATE_DOCUMENT);
			return newContentObjectDataBuilder().build(contentCmisDocument, null, false, userReadOnly, objectInfos);

		} else if (type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
			String objectId = createFolderRequest.process();
			Record record = recordServices.getDocumentById(objectId);
			ensureUserHasAllowableActionsOnRecord(record, Action.CAN_CREATE_FOLDER);
			return newObjectDataBuilder().build(record, null, false, userReadOnly, objectInfos);

		} else {
			throw new CmisExceptions_ObjectNotFound("Type", typeId);
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
