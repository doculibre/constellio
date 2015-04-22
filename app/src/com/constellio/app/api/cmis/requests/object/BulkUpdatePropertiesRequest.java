/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
	private final CallContext context;
	private final List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken;
	private final Properties properties;
	private final ObjectInfoHandler objectInfos;

	public BulkUpdatePropertiesRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			UpdatePropertiesRequest updatePropertiesRequest, CallContext context,
			List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken, Properties properties, ObjectInfoHandler objectInfos) {
		super(repository, appLayerFactory);
		this.updatePropertiesRequest = updatePropertiesRequest;
		this.context = context;
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
