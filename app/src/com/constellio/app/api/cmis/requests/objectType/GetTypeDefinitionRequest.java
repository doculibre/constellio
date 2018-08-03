package com.constellio.app.api.cmis.requests.objectType;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTypeDefinitionRequest extends CmisCollectionRequest<TypeDefinition> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private String typeId;

	public GetTypeDefinitionRequest(ConstellioCollectionRepository constellioCollectionRepository,
									AppLayerFactory appLayerFactory, CallContext callContext, String typeId) {
		super(callContext, constellioCollectionRepository, appLayerFactory);

		this.typeId = typeId;
	}

	@Override
	protected TypeDefinition process()
			throws ConstellioCmisException {
		return repository.getTypeDefinitionsManager().getTypeDefinition(callContext, typeId);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return "GetTypeDefinitionRequest{" +
			   "typeId='" + typeId + '\'' +
			   ", repository='" + repository + '\'' +
			   '}';
	}
}
