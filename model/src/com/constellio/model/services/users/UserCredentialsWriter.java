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

import com.constellio.model.entities.security.global.UserCredential;
import org.apache.commons.collections.IteratorUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserCredentialsWriter {

	private static final String GLOBAL_GROUP = "globalGroup";
	private static final String GLOBAL_GROUPS = "globalGroups";
	private static final String COLLECTIONS = "collections";
	private static final String COLLECTION = "collection";
	private static final String USERNAME = "username";
	private static final String EMAIL = "email";
	private static final String SERVICE_KEY = "serviceKey";
	private static final String TOKENS = "tokens";
	private static final String TOKEN = "token";
	private static final String TOKEN_ID = "tokenId";
	private static final String TOKEN_END_DATE = "tokenEndDate";
	private static final String SYSTEM_ADMIN = "systemAdmin";
	private static final String LAST_NAME = "lastName";
	private static final String FIRST_NAME = "firstName";
	private static final String USER_CREDENTIAL = "userCredential";
	private static final String USERS_CREDENTIALS = "usersCredentials";
	public static final String STATUS = "status";
	public static final String DOMAIN = "domain";
	Document document;

	public UserCredentialsWriter(Document document) {
		this.document = document;
	}

	public void createEmptyUserCredentials() {
		Element usersCredentials = new Element(USERS_CREDENTIALS);
		document.setRootElement(usersCredentials);
	}

	public void addUpdate(UserCredential userCredential) {
		Element root = document.getRootElement();
		removeIfExists(userCredential, root);
		add(userCredential);
	}

	public void removeCollection(String collection) {
		List<Element> elementsToRemove = getCollectionsElementsToRemove(collection);
		for (Element element : elementsToRemove) {
			element.detach();
		}
	}

	public void removeToken(String token) {
		List<Element> elementsToRemove = getTokenElementsToRemove(token);
		for (Element element : elementsToRemove) {
			element.detach();
		}
	}

	public void removeUserFromCollection(UserCredential userCredential, String collection) {
		List<Element> elementsToRemove = getCollectionElementToRemoveFromUser(userCredential, collection);
		for (Element element : elementsToRemove) {
			element.detach();
		}
	}

	public void removeGroup(String groupCode) {
		List<Element> elementsToRemove = getGroupElementToRemove(groupCode);
		for (Element element : elementsToRemove) {
			element.detach();
		}
	}

	//

	private void removeIfExists(UserCredential userCredential, Element root) {
		Element elementToRemove = null;
		for (Element element : root.getChildren()) {
			if (element.getAttributeValue(USERNAME).equals(userCredential.getUsername())) {
				elementToRemove = element;
				break;
			}
		}
		if (elementToRemove != null) {
			elementToRemove.detach();
		}
	}

	private List<Element> getTokenElementsToRemove(String token) {
		List<Element> elementsToRemove = new ArrayList<>();
		Filter<Element> tokenFilter = Filters.element(TOKEN);
		IteratorIterable<Element> tokenElements = document.getRootElement().getDescendants(tokenFilter);
		List<Element> tokenElementsList = IteratorUtils.toList(tokenElements);
		for (Element tokenElement : tokenElementsList) {
			if (tokenElement.getChildText(TOKEN_ID).equals(token)) {
				elementsToRemove.add(tokenElement);
			}
		}
		return elementsToRemove;
	}

	private List<Element> getCollectionsElementsToRemove(String collection) {
		List<Element> elementsToRemove = new ArrayList<>();
		Filter<Element> collectionFilter = Filters.element(COLLECTION);
		IteratorIterable<Element> collectionElements = document.getRootElement().getDescendants(collectionFilter);
		List<Element> collectionElementsList = IteratorUtils.toList(collectionElements);
		for (Element collectionElement : collectionElementsList) {
			if (collectionElement.getText().equals(collection)) {
				elementsToRemove.add(collectionElement);
			}
		}
		return elementsToRemove;
	}

	private List<Element> getCollectionElementToRemoveFromUser(UserCredential userCredential, String collection) {
		List<Element> elementsToRemove = new ArrayList<>();
		Filter<Element> userCredentialFilter = Filters.element(USER_CREDENTIAL);
		IteratorIterable<Element> userCredentialElements = document.getRootElement().getDescendants(userCredentialFilter);
		List<Element> userCredentialElementsList = IteratorUtils.toList(userCredentialElements);
		for (Element userCredentialElement : userCredentialElementsList) {
			if (userCredentialElement.getAttributeValue(USERNAME).equals(userCredential.getUsername())) {
				for (Element collectionElement : userCredentialElement.getChild(COLLECTIONS).getChildren()) {
					if (collectionElement.getText().equals(collection)) {
						elementsToRemove.add(collectionElement);
					}
				}
				break;
			}
		}
		return elementsToRemove;
	}

	private List<Element> getGroupElementToRemove(String groupCode) {
		List<Element> elementsToRemove = new ArrayList<>();
		Filter<Element> globalGroupFilter = Filters.element(GLOBAL_GROUP);
		IteratorIterable<Element> globalGroupElements = document.getRootElement().getDescendants(globalGroupFilter);
		List<Element> globalGroupElementsList = IteratorUtils.toList(globalGroupElements);
		for (Element globalGroupElement : globalGroupElementsList) {
			if (globalGroupElement.getText().equals(groupCode)) {
				elementsToRemove.add(globalGroupElement);
			}
		}
		return elementsToRemove;
	}

	private void add(UserCredential userCredential) {
		Element globalGroupsElement = new Element(GLOBAL_GROUPS);
		for (String group : userCredential.getGlobalGroups()) {
			Element globalGroupElement = new Element(GLOBAL_GROUP).setText(group);
			globalGroupsElement.addContent(globalGroupElement);
		}
		Element collectionsElement = new Element(COLLECTIONS);
		for (String collection : userCredential.getCollections()) {
			Element collectionElement = new Element(COLLECTION).setText(collection);
			collectionsElement.addContent(collectionElement);
		}
		Element tokensElement = new Element(TOKENS);
		if (userCredential.getTokens() != null) {
			for (Map.Entry<String, LocalDateTime> token : userCredential.getTokens().entrySet()) {
				Element tokenElement = new Element(TOKEN);
				Element tokenIdElement = new Element(TOKEN_ID).setText(token.getKey());
				Element tokenEndDateElement = new Element(TOKEN_END_DATE).setText(token.getValue().toString());
				tokenElement.addContent(tokenIdElement);
				tokenElement.addContent(tokenEndDateElement);
				tokensElement.addContent(tokenElement);
			}
		}
		Element firstNameElement = new Element(FIRST_NAME).setText(userCredential.getFirstName());
		Element lastNameElement = new Element(LAST_NAME).setText(userCredential.getLastName());
		Element emailElement = new Element(EMAIL).setText(userCredential.getEmail());
		String serviceKey = userCredential.getServiceKey() == null ? "null" : userCredential.getServiceKey();
		Element serviceKeyElement = new Element(SERVICE_KEY).setText(serviceKey);
		Element statusElement = new Element(STATUS).setText(userCredential.getStatus().getCode());
		Element domainElement = new Element(DOMAIN).setText(userCredential.getDomain());
		Element systemAdminPermissions = new Element(SYSTEM_ADMIN).setText(userCredential.isSystemAdmin() ? "true" : "false");
		Element userCredentialElement = new Element(USER_CREDENTIAL).setAttribute(USERNAME, userCredential.getUsername());
		userCredentialElement.addContent(firstNameElement);
		userCredentialElement.addContent(lastNameElement);
		userCredentialElement.addContent(emailElement);
		userCredentialElement.addContent(serviceKeyElement);
		userCredentialElement.addContent(tokensElement);
		userCredentialElement.addContent(systemAdminPermissions);
		userCredentialElement.addContent(globalGroupsElement);
		userCredentialElement.addContent(collectionsElement);
		userCredentialElement.addContent(statusElement);
		userCredentialElement.addContent(domainElement);
		document.getRootElement().addContent(userCredentialElement);
	}
}
