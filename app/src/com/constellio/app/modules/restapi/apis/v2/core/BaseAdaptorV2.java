package com.constellio.app.modules.restapi.apis.v2.core;

import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordDtoV2;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.google.common.collect.Sets;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;

public abstract class BaseAdaptorV2 {

	private static final Set<String> IGNORED_KEYS = Sets.newHashSet("id", "schema_s");

	public abstract BaseDaoV2 getDao();

	public RecordDtoV2 adaptRecord(Record record) {
		MetadataSchema schema = getDao().getMetadataSchema(record);

		return RecordDtoV2.builder()
				.id(record.getId())
				.schemaType(record.getTypeCode())
				.version(String.valueOf(record.getVersion()))
				.metadatas(record.getRecordDTO().getFields().entrySet().stream()
						.filter(e -> e.getValue() != null && !IGNORED_KEYS.contains(e.getKey()) &&
									 schema.getMetadataByDatastoreCode(e.getKey()) != null &&
									 schema.getMetadataByDatastoreCode(e.getKey()).getType() != STRUCTURE)
						.collect(Collectors.toMap(
								e -> schema.getMetadataByDatastoreCode(e.getKey()).getLocalCode(),
								e -> parseMetadataValue(record, schema.getMetadataByDatastoreCode(e.getKey())))))
				.build();
	}

	public List<RecordDtoV2> adaptRecords(List<Record> records) {
		return records.stream().map(this::adaptRecord).collect(Collectors.toList());
	}

	protected <T> T getRecordMetadataValue(Record record, Metadata metadata) {
		try {
			return record.get(metadata);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	protected <T> T getRecordMetadataValue(Record record, List<Metadata> metadatas) {
		for (Metadata metadata : metadatas) {
			T value = getRecordMetadataValue(record, metadata);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	// FIXME code dupliqué avec v1
	protected List<String> parseMetadataValue(Record record, Metadata metadata) {
		List<Object> objectValues = record.getValues(metadata);

		List<String> values = new ArrayList<>(objectValues.size());
		for (Object value : objectValues) {
			if (value == null) {
				continue;
			}

			switch (metadata.getType()) {
				case STRING:
				case TEXT:
				case REFERENCE:
					values.add((String) value);
					break;
				case DATE:
					values.add(DateUtils.format((LocalDate) value, getDao().getDateFormat()));
					break;
				case DATE_TIME:
					values.add(DateUtils.format((LocalDateTime) value, getDao().getDateTimeFormat()));
					break;
				case INTEGER:
				case NUMBER:
				case BOOLEAN:
					values.add(String.valueOf(value));
					break;
				default:
					values.add(value.toString());
			}
		}
		return values;
	}
}
