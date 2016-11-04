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

	public GetRepositoryInfoRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			String repositoryId, ExtensionsData extension, CallContext callContext) {
		super(callContext, repository, appLayerFactory);
		this.repositoryId = repositoryId;
		this.extension = extension;
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
