package com.constellio.model.services.security;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDate;

import com.constellio.model.entities.security.XMLAuthorizationDetails;

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

	public void add(XMLAuthorizationDetails xmlAuthorizationDetails) {
		Element rolesElements = new Element(ROLES);
		for (String role : xmlAuthorizationDetails.getRoles()) {
			Element roleElement = new Element(ROLE).setText(role);
			rolesElements.addContent(roleElement);
		}
		LocalDate startDate = xmlAuthorizationDetails.getStartDate();
		LocalDate endDate = xmlAuthorizationDetails.getEndDate();
		if (startDate == null) {
			startDate = new LocalDate(Integer.MIN_VALUE);
		}
		if (endDate == null) {
			endDate = new LocalDate(Integer.MAX_VALUE);
		}
		Element collectionElement = new Element(COLLECTION).setText(xmlAuthorizationDetails.getCollection());
		Element startDateElement = new Element(START_DATE).setText(startDate.toString());
		Element endDateElement = new Element(END_DATE).setText(endDate.toString());
		Element authorizationElement = new Element(AUTHORIZATION).setAttribute(ID, xmlAuthorizationDetails.getId());
		Element syncedElement = new Element(SYNCED).setText(xmlAuthorizationDetails.isSynced() ? "true" : "false");
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
