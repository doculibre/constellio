package com.constellio.app.modules.restapi.resource.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.document.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.resource.dto.ResourceTypeDto;
import com.constellio.app.modules.restapi.resource.exception.ResourceTypeNotFoundException;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;

public abstract class ResourceDao extends BaseDao {

	protected abstract String getSchemaType();

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
			MetadataSchema schema = getMetadataSchema(collection, getSchemaType());
			Metadata metadata = getMetadata(schema, DocumentType.CODE);
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
			return getMetadataSchema(collection, getSchemaType(), linkedSchemaCode);
		}
		return getMetadataSchema(collection, getSchemaType());
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

}
