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

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_CannotCreateCollection;
import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_CannotCreateTaxonomy;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.builders.object.RecordBuilder;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.api.cmis.utils.CmisRecordUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServicesException;

public class CreateFolderRequest extends CmisCollectionRequest<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final Properties properties;
	private final String folderId;
	private CallContext context;

	public CreateFolderRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, Properties properties, String folderId) {
		super(repository, appLayerFactory);
		this.context = context;
		this.properties = properties;
		this.folderId = folderId;
	}

	@Override
	public String process() {
		String objectType = properties.getProperties().get(PropertyIds.OBJECT_TYPE_ID).getFirstValue().toString();
		if ("collection_default".equals(objectType)) {
			throw new CmisExceptions_CannotCreateCollection();
		} else if ("taxonomy".equals(objectType)) {
			throw new CmisExceptions_CannotCreateTaxonomy();
		} else {
			return saveRecordFromProperties(objectType);
		}
	}

	public String saveRecordFromProperties(String objectType) {
		String collection = context.get(ConstellioCmisContextParameters.COLLECTION).toString();
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(collection).getSchema(objectType);
		Record newRecord = modelLayerFactory.newRecordServices().newRecordWithSchema(schema);
		new RecordBuilder(properties, context, modelLayerFactory).setMetadataFromProperties(newRecord);
		Record parentRecord = modelLayerFactory.newRecordServices().getDocumentById(folderId);
		new CmisRecordUtils(modelLayerFactory).setParentOfRecord(newRecord, parentRecord, schema);
		try {
			modelLayerFactory.newRecordServices().execute(new Transaction(newRecord));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return newRecord.getId();
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
