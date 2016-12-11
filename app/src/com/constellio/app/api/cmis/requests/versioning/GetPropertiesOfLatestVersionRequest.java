package com.constellio.app.api.cmis.requests.versioning;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetPropertiesOfLatestVersionRequest extends CmisCollectionRequest<Properties> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetObjectOfLatestVersionUnsupportedRequest.class);

	String objectId;
	String versionSeriesId;
	Boolean major;
	String filter;
	ExtensionsData extension;

	public GetPropertiesOfLatestVersionRequest(ConstellioCollectionRepository repository,
			AppLayerFactory appLayerFactory,
			CallContext callContext, String objectId, String versionSeriesId, Boolean major, String filter,
			ExtensionsData extension) {
		super(callContext, repository, appLayerFactory);
		this.objectId = objectId;
		this.versionSeriesId = versionSeriesId;
		this.major = major;
		this.filter = filter;
		this.extension = extension;
	}

	@Override
	protected Properties process()
			throws ConstellioCmisException {
		return null;
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
