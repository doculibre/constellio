package com.constellio.app.api.cmis.requests.acl;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetAclUnsupportedRequest extends CmisCollectionRequest<Acl> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final String objectId;

	public GetAclUnsupportedRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			String objectId) {
		super(repository, appLayerFactory);
		this.objectId = objectId;
	}

	/**
	 * CMIS getACL.
	 */
	@Override
	public Acl process() {
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
