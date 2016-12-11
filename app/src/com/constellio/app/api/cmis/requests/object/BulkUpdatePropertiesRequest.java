package com.constellio.app.api.cmis.requests.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateObjectIdAndChangeTokenImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_InvalidArgumentNoObjectIdsProvided;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class BulkUpdatePropertiesRequest extends CmisCollectionRequest<List<BulkUpdateObjectIdAndChangeToken>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final UpdatePropertiesRequest updatePropertiesRequest;
	private final List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken;
	private final Properties properties;
	private final ObjectInfoHandler objectInfos;

	public BulkUpdatePropertiesRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			UpdatePropertiesRequest updatePropertiesRequest, CallContext context,
			List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken, Properties properties, ObjectInfoHandler objectInfos) {
		super(context, repository, appLayerFactory);
		this.updatePropertiesRequest = updatePropertiesRequest;
		this.objectIdAndChangeToken = objectIdAndChangeToken;
		this.properties = properties;
		this.objectInfos = objectInfos;
	}

	/**
	 * CMIS bulkUpdateProperties.
	 */
	@Override
	public List<BulkUpdateObjectIdAndChangeToken> process() {

		if (objectIdAndChangeToken == null) {
			throw new CmisExceptions_InvalidArgumentNoObjectIdsProvided();
		}

		List<BulkUpdateObjectIdAndChangeToken> result = new ArrayList<BulkUpdateObjectIdAndChangeToken>();

		for (BulkUpdateObjectIdAndChangeToken oid : objectIdAndChangeToken) {
			if (oid == null) {
				// ignore invalid ids
				continue;
			}
			try {
				Holder<String> oidHolder = new Holder<String>(oid.getId());
				updatePropertiesRequest.process();

				result.add(new BulkUpdateObjectIdAndChangeTokenImpl(oid.getId(), oidHolder.getValue(), null));
			} catch (CmisBaseException e) {
				// ignore exceptions - see specification
			}
		}

		return result;
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
