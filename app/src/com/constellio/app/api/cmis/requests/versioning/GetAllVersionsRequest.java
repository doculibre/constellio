package com.constellio.app.api.cmis.requests.versioning;

import static com.constellio.app.api.cmis.binding.utils.CmisContentUtils.getAllVersions;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_ALL_VERSIONS;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;

public class GetAllVersionsRequest extends CmisCollectionRequest<List<ObjectData>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetAllVersionsRequest.class);

	String repositoryId;
	String objectId;
	String versionSeriesId;
	String filter;
	Boolean includeAllowableActions;
	ExtensionsData extension;

	ObjectInfoHandler objectInfos;

	public GetAllVersionsRequest(ConstellioCollectionRepository repository,
			AppLayerFactory appLayerFactory, CallContext callContext, String repositoryId,
			String objectId, String versionSeriesId, String filter, Boolean includeAllowableActions,
			ExtensionsData extension, ObjectInfoHandler objectInfos) {
		super(callContext, repository, appLayerFactory);
		this.repositoryId = repositoryId;
		this.objectId = objectId;
		this.versionSeriesId = versionSeriesId;
		this.filter = filter;
		this.includeAllowableActions = includeAllowableActions;
		this.extension = extension;
		this.objectInfos = objectInfos;
	}

	@Override
	protected List<ObjectData> process()
			throws ConstellioCmisException {

		List<ObjectData> versions = new ArrayList<>();

		List<ContentCmisDocument> contentCmisDocuments = getAllVersions(objectId, recordServices, types(), user);
		if (!contentCmisDocuments.isEmpty()) {
			ensureUserHasAllowableActionsOnRecord(contentCmisDocuments.get(0).getRecord(), CAN_GET_ALL_VERSIONS);

			for (ContentCmisDocument version : contentCmisDocuments) {
				versions.add(newContentObjectDataBuilder().build(version, null, includeAllowableActions, false, objectInfos));
			}
		}

		return versions;
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
