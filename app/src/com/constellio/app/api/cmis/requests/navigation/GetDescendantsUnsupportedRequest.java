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

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetDescendantsUnsupportedRequest extends CmisCollectionRequest<List<ObjectInFolderContainer>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final CallContext context;
	private final String folderId;
	private final String filter;
	private final boolean includeAllowableActions;
	private final boolean includePathSegment;
	private final BigInteger depth;
	private final ObjectInfoHandler objectInfos;
	private final boolean foldersOnly;

	public GetDescendantsUnsupportedRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
			Boolean includePathSegment, ObjectInfoHandler objectInfos, boolean foldersOnly) {
		super(repository, appLayerFactory);
		this.context = context;
		this.folderId = folderId;
		this.filter = filter;
		this.includeAllowableActions = includeAllowableActions;
		this.includePathSegment = includePathSegment;
		this.depth = depth;
		this.objectInfos = objectInfos;
		this.foldersOnly = foldersOnly;
	}

	@Override
	public List<ObjectInFolderContainer> process() {

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
