package com.constellio.app.api.cmis.requests.navigation;

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetFolderParentRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final GetObjectParentsRequest getObjectParentsRequest;

	public GetFolderParentRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			GetObjectParentsRequest getObjectParentsRequest, CallContext context) {
		super(context, repository, appLayerFactory);
		this.getObjectParentsRequest = getObjectParentsRequest;
	}

	@Override
	public ObjectData process()
			throws ConstellioCmisException {
		List<ObjectParentData> allParentsResponse = getObjectParentsRequest.process();

		return allParentsResponse.isEmpty() ? null : allParentsResponse.get(0).getObject();
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
