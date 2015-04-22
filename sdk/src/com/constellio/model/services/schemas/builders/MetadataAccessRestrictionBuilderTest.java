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
package com.constellio.model.services.schemas.builders;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.sdk.tests.ConstellioTest;

public class MetadataAccessRestrictionBuilderTest extends ConstellioTest {

	MetadataAccessRestrictionBuilder builder;
	MetadataAccessRestriction restriction;

	@Before
	public void setUp()
			throws Exception {

		builder = MetadataAccessRestrictionBuilder.create();
	}

	@Test
	public void givenRequiredReadRolesWhenBuildingThenOK()
			throws Exception {
		builder.getRequiredReadRoles().add("role1");

		build();

		assertThat(restriction.getRequiredReadRoles()).containsOnly("role1");
	}

	@Test
	public void givenRequiredReadRolesWhenModifyingThenOK()
			throws Exception {
		builder.getRequiredReadRoles().add("role1");

		buildAndModify();

		assertThat(builder.getRequiredReadRoles()).containsOnly("role1");
	}

	@Test
	public void givenRequiredWriteRolesWhenBuildingThenOK()
			throws Exception {
		builder.getRequiredWriteRoles().add("role1");

		build();

		assertThat(restriction.getRequiredWriteRoles()).containsOnly("role1");
	}

	@Test
	public void givenRequiredWriteRolesWhenModifyingThenOK()
			throws Exception {
		builder.getRequiredWriteRoles().add("role1");

		buildAndModify();

		assertThat(builder.getRequiredWriteRoles()).containsOnly("role1");
	}

	@Test
	public void givenRequiredDeleteRolesWhenBuildingThenOK()
			throws Exception {
		builder.getRequiredDeleteRoles().add("role1");

		build();

		assertThat(restriction.getRequiredDeleteRoles()).containsOnly("role1");
	}

	@Test
	public void givenRequiredDeleteRolesWhenModifyingThenOK()
			throws Exception {
		builder.getRequiredDeleteRoles().add("role1");

		buildAndModify();

		assertThat(builder.getRequiredDeleteRoles()).containsOnly("role1");
	}

	@Test
	public void givenRequiredModificationRolesWhenModifyingThenOK()
			throws Exception {
		builder.getRequiredModificationRoles().add("role1");

		buildAndModify();

		assertThat(builder.getRequiredModificationRoles()).containsOnly("role1");
	}

	@Test
	public void givenRequiredModificationRolesWhenBuildingThenOK()
			throws Exception {
		builder.getRequiredModificationRoles().add("role1");

		build();

		assertThat(restriction.getRequiredModificationRoles()).containsOnly("role1");
	}

	@Test
	public void whenAddingSameElementsMultipleTimesThenOnlySetOnce()
			throws Exception {

		builder.getRequiredReadRoles().add("role1");
		builder.getRequiredReadRoles().add("role1");
		builder.getRequiredWriteRoles().add("role2");
		builder.getRequiredWriteRoles().add("role2");
		builder.getRequiredModificationRoles().add("role3");
		builder.getRequiredModificationRoles().add("role3");
		builder.getRequiredDeleteRoles().add("role4");
		builder.getRequiredDeleteRoles().add("role4");

		build();

		assertThat(restriction.getRequiredReadRoles()).hasSize(1);
		assertThat(restriction.getRequiredWriteRoles()).hasSize(1);
		assertThat(restriction.getRequiredModificationRoles()).hasSize(1);
		assertThat(restriction.getRequiredDeleteRoles()).hasSize(1);

	}

	@Test
	public void whenAddingElementWithAddMethodThenSet()
			throws Exception {

		builder.withRequiredReadRole("read").withRequiredWriteRole("write").withRequiredModificationRole("modifyTo")
				.withRequiredDeleteRole("delete").withRequiredWriteAndDeleteRole("write_and_delete").withRequiredRole("chuck");

		build();

		assertThat(restriction.getRequiredReadRoles()).containsOnly("read", "chuck");
		assertThat(restriction.getRequiredWriteRoles()).containsOnly("write", "write_and_delete", "chuck");
		assertThat(restriction.getRequiredModificationRoles()).containsOnly("modifyTo", "chuck");
		assertThat(restriction.getRequiredDeleteRoles()).containsOnly("delete", "write_and_delete", "chuck");

	}

	private void build() {
		restriction = builder.build();
	}

	private void buildAndModify() {
		restriction = builder.build();
		builder = MetadataAccessRestrictionBuilder.modify(restriction);
	}
}
