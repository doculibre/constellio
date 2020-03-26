package com.constellio.app.modules.restapi.resource.service;

import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotManualException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.service.BaseService;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.core.util.StringUtils;
import com.constellio.app.modules.restapi.resource.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.resource.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.entries.DataEntryType;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public abstract class ResourceService extends BaseService {

	@Inject
	private ValidationService validationService;

	abstract protected SchemaTypes getSchemaType();

	abstract protected ResourceAdaptor<?> getAdaptor();

	public <T> T getResource(String host, String id, String serviceKey, String method, String date, int expiration,
							 String signature, Set<String> filters) throws Exception {
		return getResource(host, id, serviceKey, method, date, expiration, signature, filters, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getResource(String host, String id, String serviceKey, String method, String date, int expiration,
							 String signature, Set<String> filters, String eTag) throws Exception {
		validateParameters(host, id, serviceKey, method, date, expiration, null, null, null, signature);

		Record record = getRecord(id, eTag, false);
		User user = getUser(serviceKey, record.getCollection());
		validationService.validateUserAccess(user, record, method);

		MetadataSchema schema = getDao().getMetadataSchema(record);

		return (T) getAdaptor().adapt(null, record, schema, false, filters);
	}

	protected void validateUserAccess(User user, Record resourceRecord, String method) {
		validationService.validateUserAccess(user, resourceRecord, method);
	}

	protected void validateUserDeleteAccessOnHierarchy(User user, Record resourceRecord) {
		validationService.validateUserDeleteAccessOnHierarchy(user, resourceRecord);
	}

	protected void validateAuthorizations(List<AceDto> authorizations, String collection) {
		validationService.validateAuthorizations(authorizations, collection);
	}

	protected void validateETag(String recordId, String eTag, long recordVersion) {
		validationService.validateETag(recordId, eTag, recordVersion);
	}

	protected void validateParameters(String host, String id, String serviceKey, String method, String date,
									  int expiration, String version, Boolean physical, String copySourceId,
									  String signature) throws Exception {
		validateParameters(host, id, serviceKey, method, date, expiration, version, physical, copySourceId,
				signature, false);
	}

	protected void validateParameters(String host, String id, String serviceKey, String method, String date,
									  int expiration, String version, Boolean physical, String copySourceId,
									  String signature, boolean urlValidated) throws Exception {
		validationService.validateHost(host);
		if (!urlValidated) {
			validationService.validateUrl(date, expiration);
		}
		validationService.validateSignature(host, id, serviceKey, getSchemaType().name(), method, date,
				expiration, version, physical, copySourceId, signature);
	}

	protected void validateExtendedAttributes(List<ExtendedAttributeDto> extendedAttributes, MetadataSchema schema) {
		if (ListUtils.isNullOrEmpty(extendedAttributes)) {
			return;
		}

		String dateFormat = getDao().getDateFormat();
		String dateTimeFormat = getDao().getDateTimeFormat();

		for (ExtendedAttributeDto attribute : extendedAttributes) {
			Metadata metadata;
			try {
				metadata = schema.getMetadata(attribute.getKey());
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				throw new MetadataNotFoundException(attribute.getKey());
			}

			if (!metadata.isMultivalue() && attribute.getValues().size() != 1) {
				throw new MetadataNotMultivalueException(attribute.getKey());
			}

			if (metadata.getDataEntry().getType() != DataEntryType.MANUAL) {
				throw new MetadataNotManualException(attribute.getKey());
			}

			for (String value : attribute.getValues()) {
				switch (metadata.getType()) {
					case REFERENCE:
						Record record = getRecord(value, true);
						if (!metadata.getAllowedReferences().getAllowedSchemaType().equals(record.getTypeCode())) {
							throw new MetadataReferenceNotAllowedException(record.getTypeCode(), attribute.getKey());
						}
						break;
					case DATE:
						DateUtils.validateLocalDate(value, dateFormat);
						break;
					case DATE_TIME:
						DateUtils.validateLocalDateTime(value, dateTimeFormat);
						break;
					case NUMBER:
						if (!StringUtils.isUnsignedDouble(value)) {
							throw new InvalidMetadataValueException(metadata.getType().name(), value);
						}
						break;
					case BOOLEAN:
						if (!value.equals("true") && !value.equals("false")) {
							throw new InvalidMetadataValueException(metadata.getType().name(), value);
						}
						break;
					case STRING:
					case TEXT:
						break;
					default:
						throw new UnsupportedMetadataTypeException(metadata.getType().name());
				}
			}
		}
	}
}
