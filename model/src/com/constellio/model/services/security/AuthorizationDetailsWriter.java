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
package com.constellio.model.services.security;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDate;

import com.constellio.model.entities.security.AuthorizationDetails;

public class AuthorizationDetailsWriter {

	private static final String ID = "id";
	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String AUTHORIZATION = "authorization";
	private static final String ROLE = "role";
	private static final String ROLES = "roles";
	private static final String SYNCED = "synced";
	private static final String COLLECTION = "collection";
	private static final String AUTHORIZATIONS = "authorizations";
	Document document;

	public AuthorizationDetailsWriter(Document document) {
		this.document = document;
	}

	public void createEmptyAuthorizations() {
		Element authorizations = new Element(AUTHORIZATIONS);
		document.setRootElement(authorizations);
	}

	public void add(AuthorizationDetails authorizationDetails) {
		Element rolesElements = new Element(ROLES);
		for (String role : authorizationDetails.getRoles()) {
			Element roleElement = new Element(ROLE).setText(role);
			rolesElements.addContent(roleElement);
		}
		LocalDate startDate = authorizationDetails.getStartDate();
		LocalDate endDate = authorizationDetails.getEndDate();
		if (startDate == null) {
			startDate = new LocalDate(Integer.MIN_VALUE);
		}
		if (endDate == null) {
			endDate = new LocalDate(Integer.MAX_VALUE);
		}
		Element collectionElement = new Element(COLLECTION).setText(authorizationDetails.getCollection());
		Element startDateElement = new Element(START_DATE).setText(startDate.toString());
		Element endDateElement = new Element(END_DATE).setText(endDate.toString());
		Element authorizationElement = new Element(AUTHORIZATION).setAttribute(ID, authorizationDetails.getId());
		Element syncedElement = new Element(SYNCED).setText(authorizationDetails.isSynced() ? "true" : "false");
		authorizationElement.addContent(startDateElement);
		authorizationElement.addContent(endDateElement);
		authorizationElement.addContent(collectionElement);
		authorizationElement.addContent(rolesElements);
		authorizationElement.addContent(syncedElement);
		Element authorizationsElement = document.getRootElement();
		authorizationsElement.addContent(authorizationElement);
	}

	public void modifyEndDate(String id, LocalDate endDate) {
		Element authorizationsElement = document.getRootElement();

		for (Element authorizationElement : authorizationsElement.getChildren()) {
			if (authorizationElement.getAttributeValue(ID).equals(id)) {
				Element endDateElement = authorizationElement.getChild(END_DATE);
				endDateElement.setText(endDate.toString());
			}
		}
	}

	public void remove(String id) {
		Element authorizationsElement = document.getRootElement();
		Element elementToRemove = null;
		for (Element authorizationElement : authorizationsElement.getChildren()) {
			if (authorizationElement.getAttributeValue(ID).equals(id)) {
				elementToRemove = authorizationElement;
			}
		}
		if (elementToRemove != null) {
			elementToRemove.detach();
		}
		//document.setRootElement(authorizationsElement);
	}

	public void clearAuthorizations(List<String> finishedAuthorizationIds) {
		for (String finishedAuthorizationId : finishedAuthorizationIds) {
			remove(finishedAuthorizationId);
		}
	}
}
