package com.constellio.app.api.cmis.requests.objectType;

import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetTypeChildrenRequest extends CmisCollectionRequest<TypeDefinitionList> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private CallContext callContext;
	private String typeId;
	private BigInteger maxItems;
	private Boolean includePropertyDefinitions;
	private BigInteger skipCount;

	public GetTypeChildrenRequest(ConstellioCollectionRepository constellioCollectionRepository,
			CallContext callContext, String typeId, BigInteger maxItems, Boolean includePropertyDefinitions, BigInteger skipCount,
			AppLayerFactory appLayerFactory) {
		super(constellioCollectionRepository, appLayerFactory);

		this.callContext = callContext;
		this.typeId = typeId;
		this.maxItems = maxItems;
		this.includePropertyDefinitions = includePropertyDefinitions;
		this.skipCount = skipCount;
	}

	@Override
	protected TypeDefinitionList process()
			throws ConstellioCmisException {
		return repository.getTypeDefinitionsManager().getTypeChildren(callContext, typeId, includePropertyDefinitions, maxItems,
				skipCount);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return "GetTypeChildrenRequest{" +
				"typeId='" + typeId + '\'' +
				", maxItems=" + maxItems +
				", includePropertyDefinitions=" + includePropertyDefinitions +
				", skipCount=" + skipCount +
				", repository='" + repository + '\'' +
				'}';
	}
}
