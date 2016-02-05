package com.constellio.model.entities.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.constellio.model.entities.security.AuthorizationDetailsRuntimeException.AuthorizationDetailsRuntimeException_RoleRequired;
import com.constellio.sdk.tests.ConstellioTest;

public class AuthorizationDetailsTest extends ConstellioTest {

	Role roleWithNothing = new Role("zeUltimeCollection", "roleWithNothing", "a", Arrays.asList("operation"));

	@Test
	public void whenCreateAuthorizationDetailsThenHasCorrectCollection()
			throws Exception {
		AuthorizationDetails authorizationDetails = AuthorizationDetails
				.create(aString(), Arrays.asList(Role.READ), null, null, "zeUltimeCollection");
		assertThat(authorizationDetails.getCollection()).isEqualTo("zeUltimeCollection");
	}

	@Test
	public void whenCreateAuthorizationDetailsWithoutDatesThenHasNoDates()
			throws Exception {
		AuthorizationDetails authorizationDetails = AuthorizationDetails
				.create(aString(), Arrays.asList(Role.READ), null, null, zeCollection);
		assertThat(authorizationDetails.getStartDate()).isNull();
		assertThat(authorizationDetails.getEndDate()).isNull();
	}

	@Test
	public void whenCreateAuthorizationDetailsWithDatesThenHasNoDates()
			throws Exception {

		LocalDate start = new LocalDate();
		LocalDate end = start.plusYears(1);

		AuthorizationDetails authorizationDetails = AuthorizationDetails
				.create(aString(), Arrays.asList(Role.READ), start, end, zeCollection);
		assertThat(authorizationDetails.getStartDate()).isEqualTo(start);
		assertThat(authorizationDetails.getEndDate()).isEqualTo(end);
	}

	@Test // TODO Decide how to deal with that (expected = AuthorizationDetailsRuntimeException_SameCollectionRequired.class)
	public void givenRolesOfDifferentCollectionsThenExceptions()
			throws Exception {
		AuthorizationDetails.create(aString(), Arrays.asList("a", "b"), null, null, zeCollection);
	}

	@Test(expected = AuthorizationDetailsRuntimeException_RoleRequired.class)
	public void givenNoRolesThenExceptions()
			throws Exception {
		AuthorizationDetails.create(aString(), new ArrayList<String>(), null, null, zeCollection);
	}

	@Test
	public void givenMutlipleRolesThenRoleCodesInAuthorizationDetails()
			throws Exception {

		assertThat(AuthorizationDetails
				.create(aString(), Arrays.asList(Role.READ, roleWithNothing.getCode()), null, null, zeCollection)
				.getRoles())
				.containsOnly("READ", "roleWithNothing");
	}

	@Test
	public void givenMutlipleRolesWithNoAccessThenStartWithUnderscore()
			throws Exception {

		assertThat(getNewAuthorizationId(roleWithNothing.getCode())).startsWith("_");
	}

	@Test
	public void givenMutlipleRolesWithReadOnlyThenStartWithR()
			throws Exception {

		assertThat(getNewAuthorizationId(roleWithNothing.getCode(), Role.READ)).startsWith("r_");
	}

	@Test
	public void givenMutlipleRolesWithWriteAndReadOnlyThenStartWithRW()
			throws Exception {

		assertThat(getNewAuthorizationId(Role.WRITE)).startsWith("rw_");
	}

	@Test
	public void givenMutlipleRolesWithWriteDeleteAndReadOnlyThenStartWithRWD()
			throws Exception {

		assertThat(getNewAuthorizationId(Role.READ, Role.WRITE, Role.DELETE)).startsWith("rwd_");
	}

	@Test
	public void givenMutlipleRolesWithDeleteAndReadOnlyThenStartWithRWD()
			throws Exception {

		assertThat(getNewAuthorizationId(Role.DELETE)).startsWith("rwd_");
	}

	String getNewAuthorizationId(String... roles) {
		return AuthorizationDetails.create(aString(), Arrays.asList(roles), null, null, zeCollection).getId();
	}
}
