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

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_CmisRuntimeCannotUpdateRecord;
import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_InvalidArgument;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.api.cmis.utils.CmisRecordUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public class MoveObjectRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final CallContext context;
	private final Holder<String> objectId;
	private final String targetFolderId;
	private final ObjectInfoHandler objectInfos;

	public MoveObjectRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context,
			Holder<String> objectId, String targetFolderId, ObjectInfoHandler objectInfos) {
		super(repository, appLayerFactory);
		this.context = context;
		this.objectId = objectId;
		this.targetFolderId = targetFolderId;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process() {
		if (objectId == null) {
			throw new CmisExceptions_InvalidArgument("Id");
		}

		MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(repository.getCollection());
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		User user = (User) context.get(ConstellioCmisContextParameters.USER);
		Record record = recordServices.getDocumentById(objectId.getValue(), user);
		Record targetRecord = recordServices.getDocumentById(targetFolderId, user);
		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		new CmisRecordUtils(modelLayerFactory).setParentOfRecord(record, targetRecord, schema);
		try {
			recordServices.update(record);
		} catch (RecordServicesException e) {
			throw new CmisExceptions_CmisRuntimeCannotUpdateRecord(record.getId(), e);
		}
		return newObjectDataBuilder().build(context, record, null, false, false, objectInfos);
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
