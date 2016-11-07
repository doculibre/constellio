package com.constellio.app.api.cmis.requests.object;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_CannotUpdateCollection;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.api.cmis.builders.object.RecordBuilder;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.extensions.api.cmis.params.UpdateFolderParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServicesException;

public class UpdatePropertiesRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final Holder<String> objectId;
	private final Properties properties;
	private final ObjectInfoHandler objectInfos;

	public UpdatePropertiesRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, Holder<String> objectId, Properties properties, ObjectInfoHandler objectInfos) {
		super(context, repository, appLayerFactory);
		this.objectId = objectId;
		this.properties = properties;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process() {

		Record updatedRecord = modelLayerFactory.newRecordServices().getDocumentById(objectId.getValue(), user);
		ensureUserHasAllowableActionsOnRecord(updatedRecord, Action.CAN_UPDATE_PROPERTIES);
		MetadataSchema schema = types().getSchema(updatedRecord.getSchemaCode());
		if ("collection_default".equals(schema.getCode())) {
			throw new CmisExceptions_CannotUpdateCollection();
		} else {
			Object changeToken = properties.getProperties().get(PropertyIds.CHANGE_TOKEN);
			return updateRecordFromProperties(updatedRecord, schema);
		}

	}

	public ObjectData updateRecordFromProperties(Record updatedRecord, MetadataSchema schema) {
		new RecordBuilder(properties, callContext, appLayerFactory).setMetadataFromProperties(updatedRecord);
		try {
			recordServices.execute(new Transaction(updatedRecord).setUser(user));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
//		UpdateFolderParams updateFolderParams = new UpdateFolderParams(user, updatedRecord);
		//		appLayerFactory.getExtensions().forCollection(collection).onUpdateCMISFolder(updateFolderParams);
		recordServices.refresh(updatedRecord);
		return newObjectDataBuilder().build(updatedRecord, null, false, false, objectInfos);
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
