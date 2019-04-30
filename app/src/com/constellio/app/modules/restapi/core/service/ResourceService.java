package com.constellio.app.modules.restapi.core.service;

import com.constellio.app.modules.restapi.core.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;

import javax.inject.Inject;
import java.util.Set;

public abstract class ResourceService extends BaseService {

	@Inject
	protected ValidationService validationService;

	abstract protected String getSchemaType();

	abstract protected ResourceAdaptor<?> getAdaptor();

	protected void validateParameters(String host, String id, String serviceKey, String method, String date,
									  int expiration, String version, Boolean physical, String signature)
			throws Exception {
		validationService.validateHost(host);
		validationService.validateUrl(date, expiration);
		validationService.validateSignature(host, id, serviceKey, getSchemaType(), method, date,
				expiration, version, physical, signature);
	}

	@SuppressWarnings("unchecked")
	public <T> T getResource(String host, String id, String serviceKey, String method, String date, int expiration,
							 String signature, Set<String> filters) throws Exception {
		validateParameters(host, id, serviceKey, method, date, expiration, null, null, signature);

		Record record = getRecord(id, false);
		User user = getUser(serviceKey, record.getCollection());
		validationService.validateUserAccess(user, record, method);

		MetadataSchema schema = getDao().getMetadataSchema(record);

		return (T) getAdaptor().adapt(null, record, schema, false, filters);
	}
}
