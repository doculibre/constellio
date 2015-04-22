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

import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_InvalidArgumentObjectNotSetted;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.CmisContentUtils;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;

public class GetObjectRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final CallContext context;
	private final String objectId;
	private final String versionRequestsId;
	private final String filter;
	private final Boolean includeAllowableActions;
	private final Boolean includeAcl;
	private final ObjectInfoHandler objectInfos;

	public GetObjectRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context,
			String objectId, String versionRequestsId, String filter, Boolean includeAllowableActions, Boolean includeAcl,
			ObjectInfoHandler objectInfos) {
		super(repository, appLayerFactory);
		this.context = context;
		if (objectId == null) {
			// this works only because there are no versions in a file system
			// and the object id and version series id are the same
			this.objectId = versionRequestsId;
		} else {
			this.objectId = objectId;
		}
		this.versionRequestsId = versionRequestsId;
		this.filter = filter;
		this.includeAllowableActions = includeAllowableActions;
		this.includeAcl = includeAcl;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process() {

		if (objectId == null && versionRequestsId == null) {
			throw new CmisExceptions_InvalidArgumentObjectNotSetted();
		}

		boolean includeAllowableActionsValue = CmisUtils.getBooleanParameter(includeAllowableActions, false);
		boolean includeAclValue = CmisUtils.getBooleanParameter(includeAcl, false);

		Set<String> filterCollection = CmisUtils.splitFilter(filter);
		User user = (User) context.get(ConstellioCmisContextParameters.USER);

		if ("@root@".equals(objectId)) {
			Record collection = appLayerFactory.getCollectionsManager().getCollection(repository.getCollection())
					.getWrappedRecord();
			return newObjectDataBuilder()
					.build(context, collection, filterCollection, includeAllowableActionsValue, includeAclValue, objectInfos);
		} else if (objectId.startsWith("taxo_")) {
			Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager()
					.getEnabledTaxonomyWithCode(repository.getCollection(), objectId.split("_")[1]);
			return newTaxonomyObjectBuilder().build(context, taxonomy, objectInfos);
		} else if (objectId.startsWith("content_")) {
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager()
					.getSchemaTypes(repository.getCollection());
			ContentCmisDocument content = CmisContentUtils.getContent(objectId, recordServices, types);
			return newContentObjectDataBuilder()
					.build(context, content, filterCollection, includeAllowableActionsValue, includeAclValue, objectInfos);
		} else {
			Record record = modelLayerFactory.newRecordServices().getDocumentById(objectId, user);
			return newObjectDataBuilder()
					.build(context, record, filterCollection, includeAllowableActionsValue, includeAclValue, objectInfos);
		}

	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return "getObject(objectId=" + objectId + ", versionRequestsId=" + versionRequestsId + ", filter=" + filter
				+ ", includeAllowableActions=" + includeAllowableActions + ", includeAcl=" + includeAcl + ")";
	}
}
