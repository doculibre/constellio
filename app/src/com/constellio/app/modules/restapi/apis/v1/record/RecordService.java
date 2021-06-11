package com.constellio.app.modules.restapi.apis.v1.record;

import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.restapi.apis.v1.core.BaseService;
import com.constellio.app.modules.restapi.apis.v1.record.dao.RecordDao;
import com.constellio.app.modules.restapi.apis.v1.record.dto.MetadataDto;
import com.constellio.app.modules.restapi.apis.v1.validation.ValidationService;
import com.constellio.app.modules.restapi.apis.v1.validation.dao.ValidationDao;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthorizedAccessException;
import com.constellio.app.modules.restapi.core.exception.InvalidMetadataValueException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotManualException;
import com.constellio.app.modules.restapi.core.exception.MetadataNotMultivalueException;
import com.constellio.app.modules.restapi.core.exception.MetadataReferenceNotAllowedException;
import com.constellio.app.modules.restapi.core.exception.UnsupportedMetadataTypeException;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.core.util.StringUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.security.global.UserCredential;

import javax.inject.Inject;
import java.util.List;

public class RecordService extends BaseService {

	@Inject
	private RecordDao recordDao;

	@Inject
	private ValidationDao validationDao;

	@Inject
	private ValidationService validationService;

	@Override
	protected BaseDao getDao() {
		return recordDao;
	}

	public MetadataDto getRecordMetadata(String host, String token, String serviceKey, String recordId,
										 String metadataCode) {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		Record record = getRecord(recordId, true);
		if (record.isOfSchemaType(UserCredential.SCHEMA_TYPE)) {
			Record userCredentials = recordDao.getUserCredentialByServiceKey(serviceKey);
			validateCredential(recordId, userCredentials.getId());
		} else {
			User user = getUserByServiceKey(serviceKey, record.getCollection());
			validationService.validateUserAccess(user, record, HttpMethods.GET);
		}

		Metadata metadata = getMetadata(record, metadataCode);
		return recordDao.getRecordMetadata(record, metadata);
	}

	public void setRecordMetadata(String host, String token, String serviceKey, String recordId,
								  MetadataDto metadataDto)
			throws Exception {
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);

		User user = null;
		Record record = getRecord(recordId, true);
		if (record.isOfSchemaType(UserCredential.SCHEMA_TYPE)) {
			Record userCredentials = recordDao.getUserCredentialByServiceKey(serviceKey);
			validateCredential(recordId, userCredentials.getId());
		} else {
			user = getUserByServiceKey(serviceKey, record.getCollection());
			validationService.validateUserAccess(user, record, HttpMethods.GET);
		}

		Metadata metadata = getMetadata(record, metadataDto.getCode());
		validateMetadata(metadata, metadataDto.getValues());

		recordDao.setRecordMetadata(user, record, metadata, metadataDto);
	}

	private void validateCredential(String requestedCredentialId, String authenticatedCredentialId) {
		if (!authenticatedCredentialId.equals(requestedCredentialId)) {
			throw new UnauthorizedAccessException();
		}
	}

	private Metadata getMetadata(Record record, String metadataCode) {

		Metadata metadata;
		try {
			MetadataSchema schema = recordDao.getMetadataSchema(record);
			metadata = schema.getMetadata(metadataCode);
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			throw new MetadataNotFoundException(metadataCode);
		}

		switch (metadata.getType()) {
			case REFERENCE:
			case DATE:
			case DATE_TIME:
			case NUMBER:
			case BOOLEAN:
			case STRING:
			case TEXT:
				break;
			default:
				throw new UnsupportedMetadataTypeException(metadata.getType().name());
		}

		return metadata;
	}

	protected void validateMetadata(Metadata metadata, List<String> values) {
		if (metadata.getDataEntry().getType() != DataEntryType.MANUAL) {
			throw new MetadataNotManualException(metadata.getCode());
		}

		if (values == null) {
			return;
		}

		if (!metadata.isMultivalue() && values.size() != 1) {
			throw new MetadataNotMultivalueException(metadata.getCode());
		}

		String dateFormat = getDao().getDateFormat();
		String dateTimeFormat = getDao().getDateTimeFormat();

		for (String value : values) {
			switch (metadata.getType()) {
				case REFERENCE:
					Record ref = getRecord(value, true);
					if (!metadata.getAllowedReferences().getAllowedSchemaType().equals(ref.getTypeCode())) {
						throw new MetadataReferenceNotAllowedException(ref.getTypeCode(), metadata.getCode());
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
