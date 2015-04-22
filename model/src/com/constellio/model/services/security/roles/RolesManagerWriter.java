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
package com.constellio.model.services.security.roles;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.security.Role;

public class RolesManagerWriter {

	private static final String TITLE = "title";
	private static final String CODE = "code";
	private static final String COLLECTION = "collection";
	private static final String ROLES = "roles";
	private Document document;

	public RolesManagerWriter(Document document) {
		this.document = document;
	}

	public void createEmptyRoles() {
		Element roles = new Element(ROLES);
		document.setRootElement(roles);
	}

	public void addRole(final Role role) {
		Element rootElement = document.getRootElement();
		Element roleElement = new Element("role");
		roleElement.setAttribute(TITLE, role.getTitle());
		roleElement.setAttribute(CODE, role.getCode());
		roleElement.setAttribute(COLLECTION, role.getCollection());
		roleElement.setText(StringUtils.join(role.getOperationPermissions(), ","));

		rootElement.addContent(roleElement);
	}

	public void updateRole(final Role role) {
		Element rootElement = document.getRootElement();
		for (Element child : rootElement.getChildren()) {
			if (child.getAttribute(CODE).getValue().equals(role.getCode()) && child.getAttribute(COLLECTION).getValue()
					.equals(role.getCollection())) {
				child.getAttribute(TITLE).setValue(role.getTitle());
				child.setText(StringUtils.join(role.getOperationPermissions(), ","));
				break;
			}
		}
	}

	public void deleteRole(final Role role) {
		Element rootElement = document.getRootElement();
		Iterator<Element> iterator = rootElement.getChildren().listIterator();

		while (iterator.hasNext()) {
			Element child = iterator.next();
			if (child.getAttribute(CODE).getValue().equals(role.getCode())
					&& child.getAttribute(COLLECTION).getValue().equals(role.getCollection())) {
				iterator.remove();
				break;
			}
		}
	}
}
