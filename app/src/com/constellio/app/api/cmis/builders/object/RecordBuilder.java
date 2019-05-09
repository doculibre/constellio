package com.constellio.app.api.cmis.builders.object;

import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.api.cmis.params.BuildConstellioRecordFromCmisObjectParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class RecordBuilder {

	private final Properties properties;

	private final CallContext context;

	private final AppLayerFactory appLayerFactory;
	private final ModelLayerFactory modelLayerFactory;

	public RecordBuilder(Properties properties, CallContext context, AppLayerFactory appLayerFactory) {
		this.properties = properties;
		this.context = context;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
	}

	public void setMetadataFromProperties(Record record) {
		String collection = context.get(ConstellioCmisContextParameters.COLLECTION).toString();
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(collection).getSchema(record.getSchemaCode());
		for (Metadata metadata : schema.getMetadatas()) {
			boolean writeAllowed = false;
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL) {
				writeAllowed = true;
			}
			if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
				writeAllowed = ((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().hasEvaluator();
			}

			if (writeAllowed) {
				setMetadataFromProperty(record, metadata);
			}
		}

		callExtensions(record, properties, context);
	}

	private void callExtensions(Record record, Properties properties, CallContext context) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(record);
		extensions
				.buildConstellioRecordFromCmisObject(new BuildConstellioRecordFromCmisObjectParams(record, properties, context));
	}

	private void setMetadataFromProperty(Record newRecord, Metadata metadata) {
		String metadataCode = metadata.getCode();
		String propertyCode = new SchemaUtils().getLocalCodeFromMetadataCode(metadataCode);
		PropertyData<?> propertyData = properties.getProperties().get(propertyCode);
		if (propertyData != null) {
			if (metadata.isMultivalue()) {
				setMultivalueMetadata(newRecord, metadata, propertyData);
			} else {
				setSingleValueMetadata(newRecord, metadata, propertyData);
			}
		} else {
			if (metadata.getLocalCode().equals(Schemas.CREATED_BY.getLocalCode())) {
				newRecord.set(metadata, ((User) context.get(ConstellioCmisContextParameters.USER)).getId());
			}
		}
	}

	private void setMultivalueMetadata(Record newRecord, Metadata metadata, PropertyData<?> propertyData) {
		if (metadata.getType() == MetadataValueType.DATE_TIME) {
			List<GregorianCalendar> calendars = (List<GregorianCalendar>) propertyData.getValues();
			List<LocalDateTime> convertedDates = new ArrayList<>();
			for (GregorianCalendar calendar : calendars) {
				convertedDates.add(new LocalDateTime(calendar));
			}
			newRecord.set(metadata, convertedDates);

		} else if (metadata.getType() == MetadataValueType.DATE) {
			List<GregorianCalendar> calendars = (List<GregorianCalendar>) propertyData.getValues();
			List<LocalDate> convertedDates = new ArrayList<>();
			for (GregorianCalendar calendar : calendars) {
				convertedDates.add(new LocalDate(calendar));
			}
			newRecord.set(metadata, convertedDates);

		} else if (metadata.getType() == MetadataValueType.ENUM) {
			List<String> codes = (List<String>) propertyData.getValues();
			List<Enum<?>> values = new ArrayList<>();
			for (String code : codes) {
				values.add(EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), code));
			}
			newRecord.set(metadata, values);

		} else {
			newRecord.set(metadata, propertyData.getValues());
		}
	}

	private void setSingleValueMetadata(Record newRecord, Metadata metadata, PropertyData<?> propertyData) {
		if (metadata.getType() == MetadataValueType.DATE_TIME) {
			LocalDateTime convertedDate = new LocalDateTime(propertyData.getFirstValue());
			if (metadata.getLocalCode().equals(Schemas.CREATED_ON)) {
				convertedDate = new LocalDateTime();
			}
			newRecord.set(metadata, convertedDate);

		} else if (metadata.getType() == MetadataValueType.DATE) {
			Calendar calendar = (Calendar) propertyData.getFirstValue();
			LocalDate convertedDate = new LocalDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
					calendar.get(Calendar.DAY_OF_MONTH));
			newRecord.set(metadata, convertedDate);

		} else if (metadata.getType() == MetadataValueType.ENUM) {
			String code = (String) propertyData.getFirstValue();
			if (code == null) {
				newRecord.set(metadata, null);
			} else {
				newRecord.set(metadata, EnumWithSmallCodeUtils.toEnum(metadata.getEnumClass(), code));
			}

		} else {
			newRecord.set(metadata, propertyData.getFirstValue());
		}
	}

}
