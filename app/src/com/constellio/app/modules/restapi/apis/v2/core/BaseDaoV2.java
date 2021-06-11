package com.constellio.app.modules.restapi.apis.v2.core;

import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.ExpiredTokenException;
import com.constellio.app.modules.restapi.apis.v1.validation.exception.UnauthenticatedUserException;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.GetRecordOptions;
import com.constellio.model.services.records.RecordServicesRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BaseDaoV2 extends BaseDao {

	public Record getRecordById(String id, FilterMode filterMode) {
		return getRecordById(id, null, filterMode);
	}

	public Record getRecordById(String id, String eTag, FilterMode filterMode) {
		try {
			List<GetRecordOptions> options = new ArrayList<>();
			if (eTag != null) {
				options.add(GetRecordOptions.EXPECTING_VERSION_HIGHER_OR_EQUAL_TO(Long.valueOf(eTag)));
			}
			if (filterMode == FilterMode.SUMMARY) {
				options.add(GetRecordOptions.RETURNING_SUMMARY);
			}
			return recordServices.get(id, options.toArray(new GetRecordOptions[0]));
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			return null;
		}
	}

	public List<Record> getRecordsById(Set<String> ids, FilterMode filterMode) {
		if (filterMode == FilterMode.SUMMARY) {
			return recordServices.get(new ArrayList<>(ids), GetRecordOptions.RETURNING_SUMMARY);
		}
		return recordServices.get(new ArrayList<>(ids));
	}

	public User getUserByToken(String token, String collection) {
		UserCredential userCredential = modelLayerFactory.getUserCredentialTokenCacheHookRetriever().getUserByToken(token);
		if (userCredential == null) {
			throw new UnauthenticatedUserException();
		}
		if (userCredential.getAccessTokens().get(token).isBefore(TimeProvider.getLocalDateTime())) {
			throw new ExpiredTokenException();
		}
		return userServices.getUserInCollection(userCredential.getUsername(), collection);
	}

	public boolean isExistingCollection(String collectionCode) {
		try {
			recordServices.getDocumentById(collectionCode);
			return true;
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			return false;
		}
	}

	protected Locale getDefaultLocale() {
		return new Locale(modelLayerFactory.getConfiguration().getMainDataLanguage());
	}

	public Locale getPreferredLocale(List<Locale> acceptLanguages) {
		List<String> supportedLanguages = Language.getAvailableLanguageCodes();

		for (Locale locale : acceptLanguages) {
			if (supportedLanguages.contains(locale.getLanguage())) {
				return locale;
			}
		}
		return getDefaultLocale();
	}

	protected Metadata getGlobalMetadata(String code) {
		Metadata globalMetadata = null;
		if (!code.contains("_")) {
			globalMetadata = Schemas.getGlobalMetadata(code);
		}
		if (globalMetadata == null) {
			throw new MetadataNotFoundException(code);
		}
		return globalMetadata;
	}
}
