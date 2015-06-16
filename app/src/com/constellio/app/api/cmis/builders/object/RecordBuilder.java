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
package com.constellio.app.api.cmis.builders.object;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class RecordBuilder {

	private final Properties properties;

	private final CallContext context;

	private final ModelLayerFactory modelLayerFactory;

	public RecordBuilder(Properties properties, CallContext context, ModelLayerFactory modelLayerFactory) {
		this.properties = properties;
		this.context = context;
		this.modelLayerFactory = modelLayerFactory;
	}

	public void setMetadataFromProperties(Record record) {
		String collection = context.get(ConstellioCmisContextParameters.COLLECTION).toString();
		MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager()
				.getSchemaTypes(collection).getSchema(record.getSchemaCode());
		for (Metadata metadata : schema.getMetadatas()) {
			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL) {
				setMetadataFromProperty(record, metadata);
			}
		}
	}

	public void setMetadataFromProperty(Record newRecord, Metadata metadata) {
		PropertyData<?> propertyData = properties.getProperties().get(metadata.getCode());
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
