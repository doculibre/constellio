package com.constellio.app.servlet;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import org.joda.time.LocalDateTime;

import java.util.Collections;
import java.util.Map;

public abstract class BaseServletDao {

	protected AppLayerFactory appLayerFactory;
	protected ModelLayerFactory modelLayerFactory;
	protected MetadataSchemasManager metadataSchemasManager;
	protected SystemConfigurationsManager systemConfigurationsManager;

	protected RecordServices recordServices;
	protected UserServices userServices;
	protected SchemasRecordsServices schemas;

	public BaseServletDao() {
		appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();

		recordServices = modelLayerFactory.newRecordServices();
		userServices = modelLayerFactory.newUserServices();
		schemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	public String getUsernameByServiceKey(String serviceKey) {
		Record userCredential = getRecordByMetadata(schemas.credentialServiceKey(), serviceKey);
		if (userCredential == null) {
			return null;
		}

		return getMetadataValue(userCredential, UserCredential.USERNAME);
	}

	public User getUser(String serviceKey, String collection) {
		String username = getUsernameByServiceKey(serviceKey);

		return userServices.getUserInCollection(username, collection);
	}

	public Record getRecordById(String id) {
		return getRecordById(id, null);
	}

	public Record getRecordById(String id, String eTag) {
		try {
			Long recordVersion = eTag != null ? Long.valueOf(eTag) : null;
			return recordServices.realtimeGetRecordById(id, recordVersion);
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			return null;
		}
	}

	public <T> T getMetadataValue(Record record, String metadataCode) {
		MetadataSchema metadataSchema = metadataSchemasManager.getSchemaOf(record);
		return record.get(metadataSchema.getMetadata(metadataCode));
	}

	public Metadata getMetadata(MetadataSchema schema, String metadataCode) {
		return schema.getMetadata(metadataCode);
	}

	public String getDateFormat() {
		return systemConfigurationsManager.getValue(ConstellioEIMConfigs.DATE_FORMAT);
	}

	public String getDateTimeFormat() {
		return systemConfigurationsManager.getValue(ConstellioEIMConfigs.DATE_TIME_FORMAT);
	}

	protected Record getRecordByMetadata(Metadata metadata, String value) {
		return recordServices.getRecordByMetadata(metadata, value);
	}

	protected String getUrl() {
		return systemConfigurationsManager.getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
	}

	public User getUserByUsername(String username, String collection) {
		return userServices.getUserInCollection(username, collection);
	}

	public Map<String, LocalDateTime> getUserAccessTokens(String serviceKey) {
		String username = getUsernameByServiceKey(serviceKey);
		if (username == null) {
			return Collections.emptyMap();
		}

		SystemWideUserInfos userCredential = userServices.getUserInfos(username);
		if (userCredential == null) {
			return Collections.emptyMap();
		}

		return userCredential.getAccessTokens();
	}
}
