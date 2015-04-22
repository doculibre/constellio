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
package com.constellio.app.api.cmis.requests.repository;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ObjectNotFound;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetRepositoryInfoRequest extends CmisCollectionRequest<RepositoryInfo> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final String repositoryId;
	private final ExtensionsData extension;
	private final CallContext callContext;

	public GetRepositoryInfoRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			String repositoryId, ExtensionsData extension, CallContext callContext) {
		super(repository, appLayerFactory);
		this.repositoryId = repositoryId;
		this.extension = extension;
		this.callContext = callContext;
	}

	@Override
	public RepositoryInfo process() {
		if (repository == null) {
			throw new CmisExceptions_ObjectNotFound("repository", repositoryId);
		} else {
			return repository.getRepositoryInfoManager().getRepositoryInfo(callContext);
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return "GetRepositoryInfoRequest{" +
				"repositoryId='" + repositoryId + '\'' +
				", repository='" + repository + '\'' +
				'}';
	}
}
