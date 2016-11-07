package com.constellio.app.api.cmis.requests.objectType;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetTypeDescendantsRequest extends CmisCollectionRequest<List<TypeDefinitionContainer>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private String typeId;
	private BigInteger depth;
	private Boolean includePropertiesDefinition;
	private CallContext context;

	public GetTypeDescendantsRequest(ConstellioCollectionRepository repository, CallContext context, String typeId,
			BigInteger depth, Boolean includePropertiesDefinition, AppLayerFactory appLayerFactory) {
		super(context, repository, appLayerFactory);
		this.context = context;
		this.typeId = typeId;
		this.depth = depth;
		this.includePropertiesDefinition = includePropertiesDefinition;
	}

	@Override
	protected List<TypeDefinitionContainer> process()
			throws ConstellioCmisException {
		return repository.getTypeDefinitionsManager().getTypeDescendants(context, typeId, depth, includePropertiesDefinition);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return "GetTypeDescendantsRequest{" +
				"includePropertiesDefinition=" + includePropertiesDefinition +
				", depth=" + depth +
				", typeId='" + typeId + '\'' +
				", repository='" + repository + '\'' +
				'}';
	}
}
