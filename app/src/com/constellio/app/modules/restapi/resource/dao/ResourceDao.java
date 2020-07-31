package com.constellio.app.modules.restapi.resource.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.restapi.resource.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.resource.dto.ResourceTypeDto;
import com.constellio.app.modules.restapi.resource.exception.ResourceTypeNotFoundException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

public abstract class ResourceDao extends BaseDao {

	@PostConstruct
	public void init() {
		super.init();
	}

	protected abstract String getResourceSchemaType();

	protected abstract String getResourceTypeSchemaType();

	protected Record getResourceTypeRecord(ResourceTypeDto resourceType, String collection) {
		if (resourceType == null) {
			return null;
		}

		Record record;
		if (!Strings.isNullOrEmpty(resourceType.getId())) {
			record = getRecordById(resourceType.getId());

			if (record == null) {
				throw new ResourceTypeNotFoundException("id", resourceType.getId());
			}
		} else {
			MetadataSchema schema = getMetadataSchema(collection, getResourceTypeSchemaType());
			Metadata metadata = getMetadata(schema, Schemas.CODE.getCode());
			record = getRecordByMetadata(metadata, resourceType.getCode());

			if (record == null) {
				throw new ResourceTypeNotFoundException("code", resourceType.getCode());
			}
		}
		return record;
	}

	public MetadataSchema getLinkedMetadataSchema(ResourceTypeDto resourceType, String collection) {
		Record documentTypeRecord = getResourceTypeRecord(resourceType, collection);
		return getResourceMetadataSchema(documentTypeRecord, collection);
	}

	public MetadataSchema getResourceMetadataSchema(Record resourceTypeRecord, String collection) {
		if (resourceTypeRecord != null) {
			String linkedSchemaCode = getMetadataValue(resourceTypeRecord, "linkedSchema");
			return getMetadataSchema(collection, getResourceSchemaType(), linkedSchemaCode);
		}
		return getMetadataSchema(collection, getResourceSchemaType());
	}

	public List<ExtendedAttributeDto> getExtendedAttributes(MetadataSchema schema, Record record) {
		List<ExtendedAttributeDto> extendedAttributes = Lists.newArrayList();

		for (Metadata metadata : schema.getMetadatas().onlyUSR()) {
			List<String> values = Lists.newArrayList();
			for (Object value : record.getValues(metadata)) {
				if (metadata.getType() == MetadataValueType.DATE) {
					values.add(value != null ? DateUtils.format((LocalDate) value, getDateFormat()) : null);
				} else if (metadata.getType() == MetadataValueType.DATE_TIME) {
					values.add(value != null ? DateUtils.format((LocalDateTime) value, getDateTimeFormat()) : null);
				} else {
					values.add(value != null ? String.valueOf(value) : null);
				}
			}

			extendedAttributes.add(ExtendedAttributeDto.builder().key(metadata.getLocalCode()).values(values).build());
		}
		return extendedAttributes;
	}

	protected void updateCustomMetadataValues(Record resourceRecord, MetadataSchema schema,
											  List<ExtendedAttributeDto> attributes, boolean partial) {
		if (!partial || attributes != null) {
			clearCustomMetadataValues(resourceRecord, schema);
		}

		for (ExtendedAttributeDto attribute : ListUtils.nullToEmpty(attributes)) {
			Metadata metadata = schema.getMetadata(attribute.getKey());

			List<Object> values = new ArrayList<>(attribute.getValues().size());
			for (String value : attribute.getValues()) {
				switch (metadata.getType()) {
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
						throw new UnsupportedMetadataTypeException(metadata.getType().name());
				}
			}
			resourceRecord.set(metadata, metadata.isMultivalue() ? values : values.get(0));
		}
	}

	protected <T> void updateDocumentMetadataValue(Record resourceRecord, MetadataSchema schema, String metadataCode,
												   T value, boolean ignoreNull) {
		if (ignoreNull && value == null) {
			return;
		}
		updateMetadataValue(resourceRecord, schema, metadataCode, value);
	}

}
