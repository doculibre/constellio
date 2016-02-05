package com.constellio.app.services.schemas.bulkImport.authorization;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationPrincipal;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationTarget;

public class ImportedAuthorizationReader {

	private static final String ROLES = "roles";
	private static final String ACCESS = "access";
	private static final String ID = "id";
	private static final String TARGET = "targets";
	private static final String PRINCIPAL = "principals";
	private static final String TYPE = "type";
	private static final String LEGACY_ID = "legacyId";
	private static final java.lang.String USERNAME = "username";
	private static final java.lang.String GROUP_CODE = "groupCode";
	Document document;

	public ImportedAuthorizationReader(Document document) {
		this.document = document;
	}

	public List<ImportedAuthorization> readAll() {
		List<ImportedAuthorization> returnList = new ArrayList<>();
		Element authorizationsElements = document.getRootElement();
		for (Element authorizationElement : authorizationsElements.getChildren()) {
			returnList.add(readAuthorization(authorizationElement));
		}
		return returnList;
	}

	private ImportedAuthorization readAuthorization(Element authorizationElement) {
		return new ImportedAuthorization().setId(readId(authorizationElement)).setAccess(
				readAccess(authorizationElement)).setRoles(readRoles(authorizationElement))
				.setPrincipals(readPrincipals(authorizationElement))
				.setTargets(readTargets(authorizationElement));
	}

	private List<ImportedAuthorizationTarget> readTargets(Element authorizationElement) {
		Element targetsElements = authorizationElement.getChild(TARGET);
		List<ImportedAuthorizationTarget> targets = new ArrayList<>();
		if (targetsElements != null) {
			for (Element targetElement : targetsElements.getChildren()) {
				String type = targetElement.getAttributeValue(TYPE);
				String legacyId = targetElement.getAttributeValue(LEGACY_ID);
				targets.add(new ImportedAuthorizationTarget(type, legacyId));
			}
		}
		return targets;
	}

	private List<ImportedAuthorizationPrincipal> readPrincipals(Element authorizationElement) {
		Element principalsElements = authorizationElement.getChild(PRINCIPAL);
		List<ImportedAuthorizationPrincipal> principals = new ArrayList<>();
		if (principalsElements != null) {
			for (Element principalElement : principalsElements.getChildren()) {
				String type = principalElement.getAttributeValue(TYPE);
				String principalId = principalElement.getAttributeValue(USERNAME);
				if (principalId == null) {
					principalId = principalElement.getAttributeValue(GROUP_CODE);
				}
				principals.add(new ImportedAuthorizationPrincipal(type, principalId));
			}
		}
		return principals;
	}

	private List<String> readRoles(Element authorizationElement) {
		Element rolesElements = authorizationElement.getChild(ROLES);
		List<String> roles = new ArrayList<>();
		if (rolesElements != null) {
			for (Element roleElement : rolesElements.getChildren()) {
				roles.add(roleElement.getText());
			}
		}
		return roles;
	}

	private String readAccess(Element authorizationElement) {
		Element accessElement = authorizationElement.getChild(ACCESS);
		if (accessElement != null) {
			return accessElement.getText().toLowerCase().trim();
		}
		return null;
	}

	private String readId(Element authorizationElement) {
		return authorizationElement.getAttributeValue(ID);
	}
}
