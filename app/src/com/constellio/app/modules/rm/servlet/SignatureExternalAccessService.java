package com.constellio.app.modules.rm.servlet;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

public class SignatureExternalAccessService {
	private static final String UNAUTHORIZED = "Unauthorized";

	private AppLayerFactory appLayerFactory;
	private UserServices userServices;
	private RecordServices recordServices;
	private MetadataSchemasManager metadataSchemasManager;
	private SchemasRecordsServices schemas;

	public SignatureExternalAccessService() {
		appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		userServices = appLayerFactory.getModelLayerFactory().newUserServices();

		metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		schemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, appLayerFactory.getModelLayerFactory());
	}

	public String accessExternalSignature(String accessId, String token, String language, String ipAddress)
			throws SignatureExternalAccessServiceException {

		SignatureExternalAccessDao dao = new SignatureExternalAccessDao(appLayerFactory);
		return dao.accessExternalSignature(accessId, token, language, ipAddress);
	}

	public String createExternalSignatureUrl(String authorization, String serviceKey, String documentId,
											 String externalUserFullname, String externalUserEmail,
											 String expirationDate, String language)
			throws SignatureExternalAccessServiceException {

		validateAuth(authorization, serviceKey);

		SignatureExternalAccessDao dao = new SignatureExternalAccessDao(appLayerFactory);
		return dao.createExternalSignatureUrl(getUsernameByServiceKey(serviceKey), documentId, externalUserFullname,
				externalUserEmail, expirationDate, language);
	}

	private void validateAuth(String authorization, String serviceKey)
			throws SignatureExternalAccessServiceException {

		if (StringUtils.isBlank(authorization)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		String[] autorizationStrings = authorization.split(" ", 2);
		if (autorizationStrings.length != 2 || !autorizationStrings[0].equals("Bearer")) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		String token = autorizationStrings[1];
		if (StringUtils.isBlank(token) || StringUtils.isBlank(serviceKey)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		Map<String, LocalDateTime> tokens = getUserAccessTokens(serviceKey);
		if (!tokens.containsKey(token)) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}

		if (tokens.get(token).isBefore(TimeProvider.getLocalDateTime())) {
			throw new SignatureExternalAccessServiceException(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED);
		}
	}

	private Map<String, LocalDateTime> getUserAccessTokens(String serviceKey) {
		String username = getUsernameByServiceKey(serviceKey);
		if (username == null) {
			return Collections.emptyMap();
		}

		UserCredential userCredential = userServices.getUser(username);
		if (userCredential == null) {
			return Collections.emptyMap();
		}

		return userCredential.getAccessTokens();
	}

	private String getUsernameByServiceKey(String serviceKey) {
		Record userCredential = getRecordByMetadata(schemas.credentialServiceKey(), serviceKey);
		if (userCredential == null) {
			return null;
		}

		return getMetadataValue(userCredential, UserCredential.USERNAME);
	}

	private Record getRecordByMetadata(Metadata metadata, String value) {
		return recordServices.getRecordByMetadata(metadata, value);
	}

	private <T> T getMetadataValue(Record record, String metadataCode) {
		MetadataSchema metadataSchema = metadataSchemasManager.getSchemaOf(record);
		return record.get(metadataSchema.getMetadata(metadataCode));
	}
}
