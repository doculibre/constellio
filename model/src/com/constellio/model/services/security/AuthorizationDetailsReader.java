package com.constellio.model.services.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDate;

import com.constellio.model.entities.security.XMLAuthorizationDetails;
import com.constellio.model.entities.security.global.AuthorizationDetails;

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
		XMLAuthorizationDetails xmlAuthorizationDetails;
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
		xmlAuthorizationDetails = new XMLAuthorizationDetails(collection, id, roles, startDateDt, endDateDt, synced);
		return xmlAuthorizationDetails;
	}
}
