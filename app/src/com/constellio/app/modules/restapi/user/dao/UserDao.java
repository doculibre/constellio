package com.constellio.app.modules.restapi.user.dao;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.user.dto.UserCredentialsContentDto;
import com.constellio.app.modules.restapi.user.dto.UserInCollectionDto;
import com.constellio.app.modules.restapi.user.dto.UsersByCollectionDto;
import com.constellio.app.modules.restapi.user.exception.SignatureContentNotFoundException;
import com.constellio.app.modules.restapi.user.exception.SignatureInvalidContentException;
import com.constellio.app.modules.restapi.user.exception.SignatureNoContentException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentImplRuntimeException;
import com.constellio.model.services.contents.ContentManager.ContentVersionDataSummaryResponse;
import com.constellio.model.services.contents.ContentManagerRuntimeException;
import com.constellio.model.services.contents.ContentVersionDataSummary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserDao extends BaseDao {

	public UsersByCollectionDto getUsersByCollection(String username) {
		UserCredential userCredentials = userServices.getUser(username);
		List<String> codes = userCredentials.getCollections();

		List<UserInCollectionDto> usersByCollection = new ArrayList<>();
		for (String code : codes) {
			User user = getUserByUsername(username, code);
			Collection collection = appLayerFactory.getCollectionsManager().getCollection(code);

			UserInCollectionDto dto = UserInCollectionDto.builder()
					.userId(user.getId())
					.collectionCode(code)
					.collectionTitle(collection.getTitle())
					.build();

			usersByCollection.add(dto);
		}

		return UsersByCollectionDto.builder().usersByCollection(usersByCollection).build();
	}

	public UserCredentialsContentDto getContent(String username, String metadataCode) {
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

			return UserCredentialsContentDto.builder().content(stream).mimeType(mimeType).filename(contentVersion.getFilename()).build();
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
}
