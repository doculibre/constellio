package com.constellio.app.api.cmis.requests.object;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.builders.object.AllowableActionsBuilder;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;

public class AllowableActionsRequest extends CmisCollectionRequest<AllowableActions> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final String objectId;

	public AllowableActionsRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, String objectId) {
		super(context, repository, appLayerFactory);
		this.objectId = objectId;
	}

	@Override
	public AllowableActions process() {
		if (objectId.startsWith("taxo_")) {
			return allowableActionsBuilder.buildTaxonomyActions(objectId.substring(5));
		} else {
			return allowableActionsBuilder.build(modelLayerFactory.newRecordServices().getDocumentById(objectId));
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
