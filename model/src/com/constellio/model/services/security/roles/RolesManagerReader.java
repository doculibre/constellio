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
