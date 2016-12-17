package com.constellio.app.api.cmis.requests.versioning;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class GetObjectOfLatestVersionUnsupportedRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetObjectOfLatestVersionUnsupportedRequest.class);

	String objectId;
	String versionSeriesId;
	Boolean major;
	String filter;
	Boolean includeAllowableActions;
	IncludeRelationships includeRelationships;
	String renditionFilter;
	Boolean includePolicyIds;
	Boolean includeAcl;
	ExtensionsData extension;
	ObjectInfoHandler objectInfos;

	public GetObjectOfLatestVersionUnsupportedRequest(ConstellioCollectionRepository repository,
			AppLayerFactory appLayerFactory,
			CallContext callContext, String objectId, String versionSeriesId, Boolean major, String filter,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension, ObjectInfoHandler objectInfos) {
		super(callContext, repository, appLayerFactory);
		this.objectId = objectId;
		this.versionSeriesId = versionSeriesId;
		this.major = major;
		this.filter = filter;
		this.includeAllowableActions = includeAllowableActions;
		this.includeRelationships = includeRelationships;
		this.renditionFilter = renditionFilter;
		this.includePolicyIds = includePolicyIds;
		this.includeAcl = includeAcl;
		this.extension = extension;
		this.objectInfos = objectInfos;
	}

	@Override
	protected ObjectData process()
			throws ConstellioCmisException {
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
