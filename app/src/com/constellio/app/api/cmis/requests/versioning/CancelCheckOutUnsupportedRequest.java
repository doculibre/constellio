package com.constellio.app.api.cmis.requests.versioning;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class CancelCheckOutUnsupportedRequest extends CmisCollectionRequest<Boolean> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CancelCheckOutUnsupportedRequest.class);
	String repositoryId;
	String objectId;
	ExtensionsData extension;

	public CancelCheckOutUnsupportedRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			String repositoryId, String objectId,
			ExtensionsData extension) {
		super(repository, appLayerFactory);
		this.repositoryId = repositoryId;
		this.objectId = objectId;
		this.extension = extension;
	}

	@Override
	protected Boolean process()
			throws ConstellioCmisException {
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
