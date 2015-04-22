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
package com.constellio.app.api.cmis.requests.navigation;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetFolderParentUnsupportedRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final GetObjectParentsRequest getObjectParentsRequest;
	private final CallContext context;
	private final String folderId;
	private final String filter;
	private final ObjectInfoHandler objectInfos;

	public GetFolderParentUnsupportedRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			GetObjectParentsRequest getObjectParentsRequest, CallContext context, String folderId, String filter,
			ObjectInfoHandler objectInfos) {
		super(repository, appLayerFactory);
		this.getObjectParentsRequest = getObjectParentsRequest;
		this.context = context;
		this.folderId = folderId;
		this.filter = filter;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process() {
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
