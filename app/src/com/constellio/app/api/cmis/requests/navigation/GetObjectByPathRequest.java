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

import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class GetObjectByPathRequest extends CmisCollectionRequest<ObjectData> {

	private static final String TAXONOMY_PREFIX = "taxo_";

	private static final Logger LOGGER = LoggerFactory.getLogger(GetObjectByPathRequest.class);

	private final CallContext context;
	private final String folderPath;
	private final Set<String> filter;
	private final boolean includeAllowableActions;
	private final boolean includeACL;
	private final ObjectInfoHandler objectInfos;

	public GetObjectByPathRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, String folderPath, String filter, boolean includeAllowableActions, boolean includeACL,
			ObjectInfoHandler objectInfo) {
		super(repository, appLayerFactory);
		this.context = context;
		this.folderPath = folderPath;
		if (filter != null) {
			this.filter = CmisUtils.splitFilter(filter);
		} else {
			this.filter = null;
		}
		this.includeAllowableActions = includeAllowableActions;
		this.includeACL = includeACL;
		this.objectInfos = objectInfo;
	}

	@Override
	public ObjectData process()
			throws ConstellioCmisException {

		if (folderPath.equals("/")) {
			return collectionObjectData();
		} else {
			String[] pathPart = folderPath.split("/");
			String lastPathPart = pathPart[pathPart.length - 1];
			if (lastPathPart.startsWith(TAXONOMY_PREFIX)) {
				return taxoObjectData(lastPathPart.substring(TAXONOMY_PREFIX.length()));
			} else {
				return recordObjectData(lastPathPart);
			}
		}
	}

	private ObjectData recordObjectData(String id) {
		User user = (User) context.get(ConstellioCmisContextParameters.USER);
		Record record = modelLayerFactory.newRecordServices().getDocumentById(id, user);
		return recordObjectData(record);
	}

	private ObjectData taxoObjectData(String taxonomyCode) {
		Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager().getEnabledTaxonomyWithCode(repository.getCollection(),
				taxonomyCode);
		return newTaxonomyObjectBuilder().build(context, taxonomy, objectInfos);
	}

	private ObjectData collectionObjectData() {
		Record collection = appLayerFactory.getCollectionsManager().getCollection(repository.getCollection())
				.getWrappedRecord();
		return recordObjectData(collection);
	}

	private ObjectData recordObjectData(Record record) {
		return newObjectDataBuilder().build(context, record, filter, includeAllowableActions, includeACL, objectInfos);
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
