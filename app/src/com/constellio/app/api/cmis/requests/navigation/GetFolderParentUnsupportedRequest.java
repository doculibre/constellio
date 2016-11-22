package com.constellio.app.api.cmis.requests.navigation;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetFolderParentUnsupportedRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final GetObjectParentsRequest getObjectParentsRequest;
	private final String folderId;
	private final String filter;
	private final ObjectInfoHandler objectInfos;

	public GetFolderParentUnsupportedRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			GetObjectParentsRequest getObjectParentsRequest, CallContext context, String folderId, String filter,
			ObjectInfoHandler objectInfos) {
		super(context, repository, appLayerFactory);
		this.getObjectParentsRequest = getObjectParentsRequest;
		this.folderId = folderId;
		this.filter = filter;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process() {
		throw new UnsupportedOperationException();
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
