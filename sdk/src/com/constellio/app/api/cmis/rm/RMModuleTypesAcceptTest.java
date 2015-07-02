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
package com.constellio.app.api.cmis.rm;

import static org.apache.chemistry.opencmis.commons.enums.PropertyType.DATETIME;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.STRING;
import static org.apache.chemistry.opencmis.commons.enums.Updatability.READONLY;
import static org.apache.chemistry.opencmis.commons.enums.Updatability.READWRITE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.sdk.tests.ConstellioTest;

public class RMModuleTypesAcceptTest extends ConstellioTest {

	Session cmisSession;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule()
		);

		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();
		authenticationService.changePassword(chuckNorris, "1qaz2wsx");

	}

	@Test
	public void validateFolderType()
			throws Exception {
		cmisSession = newCmisSessionBuilder().authenticatedBy(chuckNorris, "1qaz2wsx").onCollection(zeCollection).build();

		ObjectType baseFolderType = cmisSession.getTypeDefinition("cmis:folder");
		Iterator<ObjectType> iterator = baseFolderType.getChildren().iterator();

		Map<String, PropertyDefinition<?>> folderTypeMetadatas = getFolderType("folder_default").getPropertyDefinitions();
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.OPENING_DATE))
				.has(propertyType(DATETIME)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.ACTUAL_TRANSFER_DATE))
				.has(propertyType(DATETIME)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.RETENTION_RULE_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.CATEGORY_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.ADMINISTRATIVE_UNIT_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.FILING_SPACE_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.COPY_STATUS_ENTERED))
				.has(propertyType(STRING)).has(updatability(READWRITE));

		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.EXPECTED_TRANSFER_DATE))
				.has(propertyType(DATETIME)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.RETENTION_RULE))
				.has(propertyType(STRING)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.CATEGORY))
				.has(propertyType(STRING)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.ADMINISTRATIVE_UNIT))
				.has(propertyType(STRING)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.FILING_SPACE))
				.has(propertyType(STRING)).has(updatability(READONLY));
		assertThat(folderTypeMetadatas.get("folder_default_" + Folder.COPY_STATUS))
				.has(propertyType(STRING)).has(updatability(READONLY));

	}

	private Condition<? super PropertyDefinition<?>> updatability(final Updatability expectedValue) {
		return new Condition<PropertyDefinition<?>>() {
			@Override
			public boolean matches(PropertyDefinition<?> value) {
				assertThat(value.getUpdatability()).isEqualTo(expectedValue);
				return true;
			}
		};
	}

	private Condition<? super PropertyDefinition<?>> propertyType(final PropertyType expectedValue) {
		return new Condition<PropertyDefinition<?>>() {
			@Override
			public boolean matches(PropertyDefinition<?> value) {
				assertThat(value.getPropertyType().value()).isEqualTo(expectedValue.value());
				return true;
			}
		};
	}

	private ObjectType getFolderType(String id) {
		ObjectType baseFolderType = cmisSession.getTypeDefinition("cmis:folder");
		Iterator<ObjectType> iterator = baseFolderType.getChildren().iterator();
		while (iterator.hasNext()) {
			ObjectType objectType = iterator.next();
			if (id.equals(objectType.getId())) {
				return objectType;
			}
		}

		throw new RuntimeException("No such object type with id : " + id);
	}
}
