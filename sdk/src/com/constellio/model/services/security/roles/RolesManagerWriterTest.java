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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.security.Role;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class RolesManagerWriterTest extends ConstellioTest {

	@Mock SearchServices searchServices;
	private Role validRole;
	private RolesManagerWriter writer;
	private Document validNewDocument;
	private Document document;

	@Before
	public void setUp()
			throws Exception {
		validRole = new Role(zeCollection, "uniqueCode", "zeValidRole", asList("operation1", "operation2"));

		Element validNewXml = new Element("roles").addContent(new Element("role").setAttribute("code", "uniqueCode")
				.setAttribute("title", "zeValidRole").setAttribute("collection", zeCollection)
				.addContent(new Element("permission").setText("read")).addContent(new Element("permission").setText("write")));
		validNewDocument = new Document(validNewXml);

		document = new Document();
		writer = new RolesManagerWriter(document);
		writer.createEmptyRoles();

	}

	@Test
	public void whenCreateNewRoleThenItIsreated()
			throws Exception {

		assertThat(document.getRootElement().getChildren()).isEmpty();
		assertThat(document.getRootElement().getName()).isEqualTo("roles");
	}

	@Test
	public void givenCorrectRoleThenManagerSaveIt()
			throws RolesManagerRuntimeException {

		writer.addRole(validRole);

		assertThat(document.getRootElement().getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChild("role").getAttribute("collection").getValue()).isEqualTo(zeCollection);
		assertThat(document.getRootElement().getChild("role").getAttribute("code").getValue()).isEqualTo("uniqueCode");
		assertThat(document.getRootElement().getChild("role").getAttribute("title").getValue()).isEqualTo("zeValidRole");
		assertThat(document.getRootElement().getChild("role").getText()).isEqualTo("operation1,operation2");
	}

	@Test
	public void givenCorrectCodeAndUpdatedTitleThenTitleIsUpdated()
			throws RolesManagerRuntimeException {

		writer.addRole(validRole);

		writer.updateRole(validRole.withTitle("validTitle"));

		assertThat(document.getRootElement().getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChild("role").getAttribute("collection").getValue()).isEqualTo(zeCollection);
		assertThat(document.getRootElement().getChild("role").getAttribute("code").getValue()).isEqualTo("uniqueCode");
		assertThat(document.getRootElement().getChild("role").getAttribute("title").getValue()).isEqualTo("validTitle");
		assertThat(document.getRootElement().getChild("role").getText()).isEqualTo("operation1,operation2");
	}

	@Test
	public void givenCorrectCodeAndUpdatedPermissionsThenPermissionsIsUpdated()
			throws RolesManagerRuntimeException {

		writer.addRole(validRole);

		writer.updateRole(validRole.withPermissions(asList("operation3", "operation4")));

		assertThat(document.getRootElement().getChildren()).hasSize(1);
		assertThat(document.getRootElement().getChild("role").getAttribute("collection").getValue()).isEqualTo(zeCollection);
		assertThat(document.getRootElement().getChild("role").getAttribute("code").getValue()).isEqualTo("uniqueCode");
		assertThat(document.getRootElement().getChild("role").getAttribute("title").getValue()).isEqualTo("zeValidRole");
		assertThat(document.getRootElement().getChild("role").getText()).isEqualTo("operation3,operation4");
	}

	@Test
	public void givenCorrectCodeAndDeleteThenRoleDeleted()
			throws RolesManagerRuntimeException {

		writer.addRole(validRole);

		writer.deleteRole(validRole);

		assertThat(document.getRootElement().getChildren()).hasSize(0);
	}
}
