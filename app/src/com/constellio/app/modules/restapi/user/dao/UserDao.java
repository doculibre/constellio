package com.constellio.app.modules.restapi.user.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.exception.MetadataNotFoundException;
import com.constellio.app.modules.restapi.user.dto.UserConfigDto;
import com.constellio.app.modules.restapi.user.dto.UserSignatureContentDto;
import com.constellio.app.modules.restapi.user.exception.SignatureContentNotFoundException;
import com.constellio.app.modules.restapi.user.exception.SignatureInvalidContentException;
import com.constellio.app.modules.restapi.user.exception.SignatureNoContentException;
import com.constellio.app.modules.restapi.user.exception.UserConfigNoContentException;
import com.constellio.app.modules.restapi.user.exception.UserConfigNotSupportedException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentImplRuntimeException;
import com.constellio.model.services.contents.ContentManager.ContentVersionDataSummaryResponse;
import com.constellio.model.services.contents.ContentManagerRuntimeException;
import com.constellio.model.services.contents.ContentVersionDataSummary;

import java.io.InputStream;
import java.util.List;

public class UserDao extends BaseDao {

	public UserSignatureContentDto getContent(String username, String metadataCode) {
		try {
			UserCredential userCredentials = userServices.getUser(username);

			if (!userCredentials.hasValue(metadataCode)) {
				throw new SignatureNoContentException();
			}

			Content content = getMetadataValue(userCredentials.getWrappedRecord(), metadataCode);
			if (content == null) {
				throw new SignatureNoContentException();
			}

			ContentVersion contentVersion = content.getCurrentVersion();
			InputStream stream = contentManager.getContentInputStream(contentVersion.getHash(), contentVersion.getFilename());
			String mimeType = contentVersion.getMimetype();

			return UserSignatureContentDto.builder().content(stream).mimeType(mimeType).filename(contentVersion.getFilename()).build();
		} catch (ContentImplRuntimeException.ContentImplRuntimeException_NoSuchVersion |
				ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent e) {
			throw new SignatureContentNotFoundException();
		}
	}

	public void setContent(String username, String metadataCode, String filename, InputStream fileStream) {
		ContentVersionDataSummaryResponse response = contentManager.upload(fileStream, filename);
		if (response == null) {
			throw new SignatureInvalidContentException();
		}

		ContentVersionDataSummary versionDataSummary = response.getContentVersionDataSummary();
		if (versionDataSummary == null) {
			throw new SignatureInvalidContentException();
		}

		if (!versionDataSummary.getMimetype().contains("image")) {
			throw new SignatureInvalidContentException();
		}

		Content content = contentManager.createSystemContent(filename, versionDataSummary);
		UserCredential userCredentials = userServices.getUser(username);
		userCredentials.set(metadataCode, content);
		userServices.addUpdateUserCredential(userCredentials);
	}

	public void deleteContent(String username, String metadataCode) {
		UserCredential userCredentials = userServices.getUser(username);
		userCredentials.set(metadataCode, null);
		userServices.addUpdateUserCredential(userCredentials);
	}

	public UserConfigDto getConfig(String username, String metadataCode) {
		UserCredential userCredentials = userServices.getUser(username);

		MetadataSchema schema = getMetadataSchema(userCredentials.getWrappedRecord());
		if (!schema.hasMetadataWithCode(metadataCode)) {
			throw new MetadataNotFoundException(metadataCode);
		}

		List<String> value;
		try {
			value = getMetadataValue(userCredentials.getWrappedRecord(), metadataCode);
		} catch (ClassCastException e) {
			throw new UserConfigNotSupportedException();
		}

		if (value == null || value.isEmpty()) {
			throw new UserConfigNoContentException();
		}

		return UserConfigDto.builder().localCode(metadataCode).value(value).build();
	}

	public void setConfig(String username, String metadataCode, UserConfigDto config) {
		UserCredential userCredentials = userServices.getUser(username);

		MetadataSchema schema = getMetadataSchema(userCredentials.getWrappedRecord());
		if (!schema.hasMetadataWithCode(metadataCode)) {
			throw new MetadataNotFoundException(metadataCode);
		}

		try {
			userCredentials.set(metadataCode, config.getValue());
		} catch (IllegalArgumentException e) {
			throw new UserConfigNotSupportedException();
		}

		userServices.addUpdateUserCredential(userCredentials);
	}
}
