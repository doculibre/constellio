/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.users;

import static com.constellio.model.services.users.UserUtils.toCacheKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.utils.EnumWithSmallCodeUtils;

public class UserCredentialsReader {

	private static final String COLLECTIONS = "collections";
	private static final String GLOBAL_GROUPS = "globalGroups";
	private static final String EMAIL = "email";
	private static final String LAST_NAME = "lastName";
	private static final String FIRST_NAME = "firstName";
	private static final String USERNAME = "username";
	private static final String SERVICE_KEY = "serviceKey";
	private static final String TOKENS = "tokens";
	private static final String TOKEN_ID = "tokenId";
	private static final String TOKEN_END_DATE = "tokenEndDate";
	private static final String SYSTEM_ADMIN = "systemAdmin";
	public static final String STATUS = "status";
	public static final String DOMAIN = "domain";
	Document document;

	public UserCredentialsReader(Document document) {
		this.document = document;
	}

	public Map<String, UserCredential> readAll() {
		UserCredential userCredential;
		Map<String, UserCredential> usersCredentials = new HashMap<>();
		Element usersCredentialsElements = document.getRootElement();
		for (Element userCredentialElement : usersCredentialsElements.getChildren()) {
			userCredential = createUserCredentialObject(userCredentialElement);

			usersCredentials.put(toCacheKey(userCredential.getUsername()), userCredential);
		}
		return usersCredentials;
	}

	private UserCredential createUserCredentialObject(Element userCredentialElement) {
		UserCredential userCredential;
		String username = userCredentialElement.getAttributeValue(USERNAME);
		String firstName = userCredentialElement.getChildText(FIRST_NAME);
		String lastName = userCredentialElement.getChildText(LAST_NAME);
		String email = userCredentialElement.getChildText(EMAIL);
		String serviceKey = userCredentialElement.getChildText(SERVICE_KEY);
		if ("null".equals(serviceKey)) {
			serviceKey = null;
		}
		Map<String, LocalDateTime> tokens = new HashMap<>();
		Element tokensElements = userCredentialElement.getChild(TOKENS);
		for (Element tokenElement : tokensElements.getChildren()) {
			String tokenId = tokenElement.getChildText(TOKEN_ID);
			String tokenEndDate = tokenElement.getChildText(TOKEN_END_DATE);
			LocalDateTime endDateTime = LocalDateTime.parse(tokenEndDate);
			tokens.put(tokenId, endDateTime);
		}
		boolean systemAdmin = "true".equals(userCredentialElement.getChildText(SYSTEM_ADMIN));
		Element globalGroupsElements = userCredentialElement.getChild(GLOBAL_GROUPS);
		List<String> globalGroups = new ArrayList<>();
		for (Element globalGroupElement : globalGroupsElements.getChildren()) {
			globalGroups.add(globalGroupElement.getText());
		}
		Element collectionsElements = userCredentialElement.getChild(COLLECTIONS);
		List<String> collections = new ArrayList<>();
		for (Element collectionElement : collectionsElements.getChildren()) {
			collections.add(collectionElement.getText());
		}
		UserCredentialStatus status;
		String statusStr = userCredentialElement.getChildText(STATUS);
		if (statusStr != null) {
			status = (UserCredentialStatus) EnumWithSmallCodeUtils.toEnumWithSmallCode(UserCredentialStatus.class, statusStr);
		} else {
			status = UserCredentialStatus.ACTIVE;
		}
		String domain = userCredentialElement.getChildText(DOMAIN);
		userCredential = new UserCredential(username, firstName, lastName, email, serviceKey, systemAdmin, globalGroups,
				collections, tokens, status, domain);
		return userCredential;
	}
}
