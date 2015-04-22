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
