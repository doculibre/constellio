package com.constellio.app.modules.restapi.apis.v1.record.dao;

import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.restapi.apis.v1.record.dto.MetadataDto;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class RecordDao extends BaseDao {

	public MetadataDto getRecordMetadata(Record record, Metadata metadata) {
		List<String> values;
		if (metadata.isMultivalue()) {
			values = parseMetadataValue(metadata.getType(), getMetadataValue(record, metadata.getCode()));
		} else {
			values = parseMetadataValue(metadata.getType(), singletonList(getMetadataValue(record, metadata.getCode())));
		}
		return MetadataDto.builder().code(metadata.getLocalCode()).values(values).build();
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

	public Record getUserCredentialByServiceKey(String serviceKey) {
		return getRecordByMetadata(schemas.credentialServiceKey(), serviceKey);
	}

	private List<String> parseMetadataValue(MetadataValueType type, List<Object> objectValues) {
		if (objectValues == null) {
			return null;
		}

		List<String> values = new ArrayList<>(objectValues.size());
		for (Object value : objectValues) {
			switch (type) {
				case STRING:
				case TEXT:
				case REFERENCE:
					values.add((String) value);
					break;
				case DATE:
					values.add(DateUtils.format((LocalDate) value, getDateFormat()));
					break;
				case DATE_TIME:
					values.add(DateUtils.format((LocalDateTime) value, getDateTimeFormat()));
					break;
				case NUMBER:
				case BOOLEAN:
					values.add(String.valueOf(value));
					break;
				default:
					throw new UnsupportedMetadataTypeException(type.name());
			}
		}
		return values;
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
