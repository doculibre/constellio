package com.constellio.app.api.cmis.requests.object;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.Action;
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
import com.constellio.app.extensions.api.cmis.params.CreateDocumentParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServicesException;

public class CreateFolderRequest extends CmisCollectionRequest<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final Properties properties;
	private final String folderId;

	public CreateFolderRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, Properties properties, String folderId) {
		super(context, repository, appLayerFactory);
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
		MetadataSchema schema = types().getSchema(objectType);
		Record newRecord = recordServices.newRecordWithSchema(schema);

		new RecordBuilder(properties, callContext, appLayerFactory).setMetadataFromProperties(newRecord);

		Record parentRecord = null;
		if (!folderId.startsWith("taxo")) {
			parentRecord = recordServices.getDocumentById(folderId);
			ensureUserHasAllowableActionsOnRecord(parentRecord, Action.CAN_CREATE_FOLDER);
		}
		new CmisRecordUtils(modelLayerFactory).setParentOfRecord(newRecord, parentRecord, schema);
		try {
			recordServices.execute(new Transaction(newRecord).setUser(user));
//			CreateDocumentParams params = new CreateDocumentParams(user, newRecord);
			//			appLayerFactory.getExtensions().forCollection(collection).onCreateCMISDocument(params);
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
