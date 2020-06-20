package com.constellio.app.modules.restapi.record.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.record.dto.MetadataDto;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;

import java.util.ArrayList;
import java.util.List;

public class RecordDao extends BaseDao {

	public MetadataDto getRecordMetadata(Record record, Metadata metadata) {
		//List<String> values = parseMetadataValue(metadata.getType(), getMetadataValue(record, metadataCode));
		//return MetadataDto.builder().code(metadata.getLocalCode()).values(values).build();

		return MetadataDto.builder().build();
	}

	public void setRecordMetadata(User user, Record record, Metadata metadata, MetadataDto metadataDto)
			throws Exception {
		List<Object> values = parseStringValues(metadata.getType(), metadataDto.getValues());

		if (values == null || values.isEmpty()) {
			record.set(metadata, null);
		} else {
			record.set(metadata, metadata.isMultivalue() ? values : values.get(0));
		}

		recordServices.update(record, user);
	}

	private List<Object> parseStringValues(MetadataValueType type, List<String> stringValues) {
		if (stringValues == null) {
			return null;
		}

		List<Object> values = new ArrayList<>(stringValues.size());
		for (String value : stringValues) {
			switch (type) {
				case STRING:
				case TEXT:
				case REFERENCE:
					values.add(value);
					break;
				case DATE:
					values.add(DateUtils.parseLocalDate(value, getDateFormat()));
					break;
				case DATE_TIME:
					values.add(DateUtils.parseLocalDateTime(value, getDateTimeFormat()));
					break;
				case NUMBER:
					values.add(Double.valueOf(value));
					break;
				case BOOLEAN:
					values.add(Boolean.valueOf(value));
					break;
				default:
					throw new UnsupportedMetadataTypeException(type.name());
			}
		}
		return values;
	}
}
