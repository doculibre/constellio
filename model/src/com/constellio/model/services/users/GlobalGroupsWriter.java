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

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import org.apache.commons.collections.IteratorUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalGroupsWriter {

	private static final String CODE = "code";
	private static final String GLOBAL_GROUP = "globalGroup";
	private static final String NAME = "name";
	private static final String USER_AUTOMATICALLY_ADDED_TO_COLLECTIONS = "userAutomaticallyAddedToCollections";
	private static final String USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS = "usersAutomaticallyAddedToCollections";
	private static final String GLOBAL_GROUPS = "globalGroups";
	public static final String PARENT = "parent";
	public static final String STATUS = "status";
	Document document;

	public GlobalGroupsWriter(Document document) {
		this.document = document;
	}

	public void createEmptyGlobalGroups() {
		Element globalGroupsElement = new Element(GLOBAL_GROUPS);
		document.setRootElement(globalGroupsElement);
	}

	public void addUpdate(GlobalGroup globalGroup) {
		Element root = document.getRootElement();
		removeIfExists(globalGroup.getCode(), root);
		add(globalGroup);
	}

	public void logicallyRemove(GlobalGroup globalGroup) {
		logicallyRemove(Arrays.asList(globalGroup));
	}

	public void logicallyRemove(List<GlobalGroup> globalGroups) {
		for (GlobalGroup globalGroup : globalGroups) {
			globalGroup = globalGroup.withStatus(GlobalGroupStatus.INACTIVE);
			addUpdate(globalGroup);
		}
	}

	private void add(GlobalGroup globalGroup) {
		Element usersAutomaticallyAddedToCollectionsElements = new Element(USERS_AUTOMATICALLY_ADDED_TO_COLLECTIONS);
		for (String userAutomaticallyAddedToCollections : globalGroup.getUsersAutomaticallyAddedToCollections()) {
			Element userAutomaticallyAddedToCollectionsElement = new Element(USER_AUTOMATICALLY_ADDED_TO_COLLECTIONS)
					.setText(userAutomaticallyAddedToCollections);
			usersAutomaticallyAddedToCollectionsElements.addContent(userAutomaticallyAddedToCollectionsElement);
		}
		Element nameElement = new Element(NAME).setText(globalGroup.getName());
		Element parentElement = new Element(PARENT).setText(globalGroup.getParent());
		Element statusElement = new Element(STATUS).setText(globalGroup.getStatus().name());
		Element globalGroupElement = new Element(GLOBAL_GROUP).setAttribute(CODE, globalGroup.getCode());

		globalGroupElement.addContent(nameElement);
		globalGroupElement.addContent(parentElement);
		globalGroupElement.addContent(statusElement);
		globalGroupElement.addContent(usersAutomaticallyAddedToCollectionsElements);
		document.getRootElement().addContent(globalGroupElement);
	}

	private void removeIfExists(String globalGroupCode, Element root) {
		Element elementToRemove = null;
		for (Element element : root.getChildren()) {
			if (element.getAttributeValue(CODE).equals(globalGroupCode)) {
				elementToRemove = element;
				break;
			}
		}
		if (elementToRemove != null) {
			elementToRemove.detach();
		}
	}

	public void removeCollection(String collection) {
		List<Element> elementsToRemove = getCollectionsElementsToRemove(collection);
		for (Element element : elementsToRemove) {
			element.detach();
		}
	}

	private List<Element> getCollectionsElementsToRemove(String collection) {
		List<Element> elementsToRemove = new ArrayList<>();
		Filter<Element> collectionFilter = Filters.element(USER_AUTOMATICALLY_ADDED_TO_COLLECTIONS);
		IteratorIterable<Element> collectionElements = document.getRootElement().getDescendants(collectionFilter);
		List<Element> collectionElementsList = IteratorUtils.toList(collectionElements);
		for (Element collectionElement : collectionElementsList) {
			if (collectionElement.getText().equals(collection)) {
				elementsToRemove.add(collectionElement);
			}
		}
		return elementsToRemove;
	}
}
