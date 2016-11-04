package com.constellio.app.api.cmis.requests.acl;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.builders.object.AclBuilder;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;

public class GetAclRequest extends CmisCollectionRequest<Acl> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetAclRequest.class);
	private final String objectId;

	public GetAclRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext callContext, String objectId) {
		super(callContext, repository, appLayerFactory);
		this.objectId = objectId;
	}

	/**
	 * CMIS getACL.
	 */
	@Override
	public Acl process() {

		Record record = modelLayerFactory.newRecordServices().getDocumentById(objectId);
		ensureUserHasAllowableActionsOnRecord(record, Action.CAN_GET_ACL);
		return new AclBuilder(repository, modelLayerFactory).build(record);
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
