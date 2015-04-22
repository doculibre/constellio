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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDate;

import com.constellio.model.entities.security.AuthorizationDetails;

public class AuthorizationDetailsReader {

	private static final String COLLECTION = "collection";
	private static final String ROLES = "roles";
	private static final String END_DATE = "endDate";
	private static final String START_DATE = "startDate";
	private static final String ID = "id";
	private static final String SYNCED = "synced";
	Document document;

	public AuthorizationDetailsReader(Document document) {
		this.document = document;
	}

	public Map<String, AuthorizationDetails> readAll() {
		AuthorizationDetails authorizationDetail;
		Map<String, AuthorizationDetails> authorizationDetails = new HashMap<>();
		Element authorizationsElements = document.getRootElement();
		for (Element authorizationElement : authorizationsElements.getChildren()) {
			authorizationDetail = createAuthorizationObject(authorizationElement);
			authorizationDetails.put(authorizationDetail.getId(), authorizationDetail);
		}
		return authorizationDetails;
	}

	private AuthorizationDetails createAuthorizationObject(Element authorizationElement) {
		AuthorizationDetails authorizationDetails;
		String id = authorizationElement.getAttributeValue(ID);
		String collection = authorizationElement.getChildText(COLLECTION);
		String startDate = authorizationElement.getChildText(START_DATE);
		String endDate = authorizationElement.getChildText(END_DATE);
		boolean synced = "true".equals(authorizationElement.getChildText(SYNCED));
		Element rolesElements = authorizationElement.getChild(ROLES);
		List<String> roles = new ArrayList<>();
		for (Element roleElement : rolesElements.getChildren()) {
			roles.add(roleElement.getText());
		}
		LocalDate startDateDt = new LocalDate(startDate);
		LocalDate endDateDt = new LocalDate(endDate);
		if (startDateDt.isEqual(new LocalDate(Integer.MIN_VALUE))) {
			startDateDt = null;
		}
		if (endDateDt.isEqual(new LocalDate(Integer.MAX_VALUE))) {
			endDateDt = null;
		}
		authorizationDetails = new AuthorizationDetails(collection, id, roles, startDateDt, endDateDt, synced);
		return authorizationDetails;
	}
}
