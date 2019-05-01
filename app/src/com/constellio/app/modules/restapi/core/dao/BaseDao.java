package com.constellio.app.modules.restapi.core.dao;

import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

public abstract class BaseDao {

	protected ContentManager contentManager;
	protected MetadataSchemasManager metadataSchemasManager;
	protected SystemConfigurationsManager systemConfigurationsManager;
	protected RestApiConfigs restApiConfigs;

	protected RecordServices recordServices;
	protected SearchServices searchServices;
	protected UserServices userServices;
	protected SchemasRecordsServices schemas;

	@PostConstruct
	protected void init() {
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		contentManager = modelLayerFactory.getContentManager();
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		restApiConfigs = new RestApiConfigs(modelLayerFactory);

		recordServices = modelLayerFactory.newRecordServices();
		searchServices = modelLayerFactory.newSearchServices();
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
		try {
			return recordServices.realtimeGetRecordById(id);
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

	public MetadataSchema getMetadataSchema(Record record) {
		return metadataSchemasManager.getSchemaOf(record);
	}

	public List<String> getAllowedHosts() {
		List<String> restApiUrls = restApiConfigs.getRestApiUrls();
		if (restApiUrls.isEmpty()) {
			return Collections.singletonList(getServerHost());
		}
		return restApiUrls;
	}

	public String getServerPath() {
		String url = getUrl();
		return url.concat(!url.endsWith("/") ? "/" : "");
	}

	public String getDateFormat() {
		return systemConfigurationsManager.getValue(ConstellioEIMConfigs.DATE_FORMAT);
	}

	public String getDateTimeFormat() {
		return systemConfigurationsManager.getValue(ConstellioEIMConfigs.DATE_TIME_FORMAT);
	}

	public Record getUserByUsername(String username, String collection) {
		return recordServices.getRecordsCaches().getCache(collection).getByMetadata(schemas.user.username(), username);
	}

	public Record getGroupByCode(String groupCode, String collection) {
		return recordServices.getRecordsCaches().getCache(collection).getByMetadata(schemas.group.code(), groupCode);
	}

	protected Record getRecordByMetadata(Metadata metadata, String value) {
		return recordServices.getRecordByMetadata(metadata, value);
	}

	protected MetadataSchema getMetadataSchema(String collection, String metadataSchemaType) {
		return getMetadataSchema(collection, metadataSchemaType, null);
	}

	protected MetadataSchema getMetadataSchema(String collection, String metadataSchemaType,
											   String metadataSchemaCode) {
		List<MetadataSchemaType> metadataSchemaTypes =
				metadataSchemasManager.getSchemaTypes(collection, Collections.singletonList(metadataSchemaType));
		if (metadataSchemaTypes.isEmpty()) {
			return null;
		}

		if (metadataSchemaCode == null) {
			return metadataSchemaTypes.get(0).getDefaultSchema();
		}
		return metadataSchemaTypes.get(0).getSchema(metadataSchemaCode);
	}

	protected <T> void updateMetadataValue(Record record, MetadataSchema schema, String metadataCode, T value) {
		record.set(schema.getMetadata(metadataCode), value);
	}

	protected void clearCustomMetadataValues(Record record, MetadataSchema schema) {
		for (Metadata metadata : schema.getMetadatas().onlyUSR()) {
			record.set(metadata, null);
		}
	}

	protected String getUrl() {
		return systemConfigurationsManager.getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
	}

	protected Transaction buildTransaction(String flushMode) {
		return buildTransaction(flushMode, null);
	}

	protected Transaction buildTransaction(String flushMode, User user) {
		return new Transaction().setRecordFlushing(toRecordFlushing(flushMode)).setUser(user);
	}

	private RecordsFlushing toRecordFlushing(String value) {
		if (value.equals("NOW")) {
			return RecordsFlushing.NOW();
		}
		if (value.equals("LATER")) {
			return RecordsFlushing.LATER();
		}

		return RecordsFlushing.WITHIN_SECONDS(Integer.valueOf(value.split("_")[1]));
	}

	private String getServerHost() {
		return getUrl().split("/")[2].split(":")[0];
	}
}
