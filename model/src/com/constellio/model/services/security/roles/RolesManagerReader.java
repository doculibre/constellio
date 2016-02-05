package com.constellio.model.services.security.roles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.security.Role;

public class RolesManagerReader {

	private static final String TITLE = "title";
	private static final String CODE = "code";
	private static final String COLLECTION = "collection";
	private Document document;

	public RolesManagerReader(Document document) {
		this.document = document;
	}

	public List<Role> getAllRoles() {
		List<Role> roles = new ArrayList<>();

		Element root = document.getRootElement();
		for (Element child : root.getChildren()) {
			roles.add(getRoleFromElement(child));
		}

		return roles;
	}

	private Role getRoleFromElement(Element element) {

		List<String> operationPermissions = Arrays.asList(element.getText().split(","));

		return new Role(element.getAttributeValue(COLLECTION), element.getAttributeValue(CODE), element.getAttributeValue(TITLE),
				operationPermissions);
	}

}
