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
