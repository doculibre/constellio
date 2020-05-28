package com.constellio.app.modules.restapi.user.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.user.dto.UserSignatureContentDto;
import com.constellio.app.modules.restapi.user.exception.SignatureContentNotFoundException;
import com.constellio.app.modules.restapi.user.exception.SignatureInvalidContentException;
import com.constellio.app.modules.restapi.user.exception.SignatureNoContentException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentImplRuntimeException;
import com.constellio.model.services.contents.ContentManager.ContentVersionDataSummaryResponse;
import com.constellio.model.services.contents.ContentManagerRuntimeException;
import com.constellio.model.services.contents.ContentVersionDataSummary;

import java.io.InputStream;

public class UserDao extends BaseDao {

	public UserSignatureContentDto getContent(String username, String metadataCode) {
		try {
			UserCredential userCredentials = userServices.getUser(username);
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

		Content content = contentManager.createSystemContent(filename, versionDataSummary);
		UserCredential userCredentials = userServices.getUser(username);
		userCredentials.set(metadataCode, content);
		userServices.addUpdateUserCredential(userCredentials);
	}
}
