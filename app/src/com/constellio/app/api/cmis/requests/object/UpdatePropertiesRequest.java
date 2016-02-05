package com.constellio.app.api.cmis.requests.object;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
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
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServicesException;

public class UpdatePropertiesRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final CallContext context;
	private final Holder<String> objectId;
	private final Properties properties;
	private final ObjectInfoHandler objectInfos;

	public UpdatePropertiesRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CallContext context, Holder<String> objectId, Properties properties, ObjectInfoHandler objectInfos) {
		super(repository, appLayerFactory);
		this.context = context;
		this.objectId = objectId;
		this.properties = properties;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process() {

		Record updatedRecord = modelLayerFactory.newRecordServices()
				.getDocumentById(objectId.getValue(), (User) context.get(ConstellioCmisContextParameters.USER));
		String collection = context.get(ConstellioCmisContextParameters.COLLECTION).toString();
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(updatedRecord.getSchemaCode());
		if ("collection_default".equals(schema.getCode())) {
			throw new CmisExceptions_CannotUpdateCollection();
		} else {
			Object changeToken = properties.getProperties().get(PropertyIds.CHANGE_TOKEN);
			return updateRecordFromProperties(updatedRecord, schema);
		}

	}

	public ObjectData updateRecordFromProperties(Record updatedRecord, MetadataSchema schema) {
		new RecordBuilder(properties, context, appLayerFactory).setMetadataFromProperties(updatedRecord);
		try {
			modelLayerFactory.newRecordServices().execute(new Transaction(updatedRecord));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		modelLayerFactory.newRecordServices().refresh(updatedRecord);
		return newObjectDataBuilder().build(context, updatedRecord, null, false, false, objectInfos);
	}
	//
	//	private void setMetadataFromProperty(Record updatedRecord, Metadata metadata) {
	//		PropertyData<?> propertyData = properties.getProperties().get(metadata.getCode());
	//		if (propertyData != null) {
	//			if (metadata.isMultivalue()) {
	//				setMultivalueMetadata(updatedRecord, metadata, propertyData);
	//			} else {
	//				setSingleValueMetadata(updatedRecord, metadata, propertyData);
	//			}
	//		} else {
	//			if (metadata.getLocalCode().equals(Schemas.MODIFIED_BY.getLocalCode())) {
	//				updatedRecord.set(metadata, ((User) context.get(ConstellioCmisContextParameters.USER)).getId());
	//			}
	//		}
	//	}
	//
	//	private void setMultivalueMetadata(Record updatedRecord, Metadata metadata, PropertyData<?> propertyData) {
	//		if (metadata.getType() == MetadataValueType.DATE_TIME) {
	//			List<GregorianCalendar> calendars = (List<GregorianCalendar>) propertyData.getValues();
	//			List<LocalDateTime> convertedDates = new ArrayList<>();
	//			for (GregorianCalendar calendar : calendars) {
	//				convertedDates.add(new LocalDateTime(calendar));
	//			}
	//			updatedRecord.set(metadata, convertedDates);
	//		} else {
	//			updatedRecord.set(metadata, propertyData.getValues());
	//		}
	//	}
	//
	//	private void setSingleValueMetadata(Record updatedRecord, Metadata metadata, PropertyData<?> propertyData) {
	//		if (metadata.getType() == MetadataValueType.DATE_TIME) {
	//			LocalDateTime convertedDate = new LocalDateTime((GregorianCalendar) propertyData.getFirstValue());
	//			if (metadata.getLocalCode().equals(Schemas.MODIFIED_ON)) {
	//				convertedDate = new LocalDateTime();
	//			}
	//			updatedRecord.set(metadata, convertedDate);
	//		}
	//		updatedRecord.set(metadata, propertyData.getFirstValue());
	//	}

	/**
	 * Checks a property set for an update.
	 */
	//	private void checkUpdateProperties(Properties properties, String typeId) {
	//		// check properties
	//		if (properties == null || properties.getProperties() == null) {
	//			throw new CmisExceptions_InvalidArgumentObjectNotSetted("Properties");
	//		}
	//
	//		// check the name
	//		String name = CmisUtils.getStringProperty(properties, PropertyIds.NAME);
	//		if (name != null) {
	//			if (!FileShareUtils.isValidName(name)) {
	//				throw new CmisExceptions_ConstraintViolationName();
	//			}
	//		}
	//
	//		// check type properties
	//		TypeDefinition typeDefinition = repository.getTypeDefinitionsManager().getInternalTypeDefinition(typeId);
	//		CmisUtils.checkTypeProperties(typeDefinition, properties, typeId, false);
	//	}
	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
