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
package com.constellio.app.modules.rm.extensions;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class AdminUnitAuthorizationSyncAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;

	Users users;

	FilingSpace filingSpace1WithUserAliceBob;
	FilingSpace filingSpace2WithUserBobCharles;
	FilingSpace filingSpace3WithAdminCharlesDakota;
	FilingSpace filingSpace4WithAdminDakotaEdouard;
	FilingSpace filingSpace5WithUserDakotaEdouardAndAdministratorGandalfChuckNorris;
	FilingSpace filingSpace6;
	FilingSpace filingSpace7;

	String alice, bob, charles, dakota, edouard, gandalf, chuckNorris;

	RecordServices recordServices;

	AdministrativeUnit administrativeUnit;

	String userAuth = "rw_U_ua42users";
	String adminAuth = "rwd_M_ua42admins";

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withAllTestUsers().withConstellioRMModule();
		users = new Users().setUp(getModelLayerFactory().newUserServices());
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		alice = users.aliceIn(zeCollection).getId();
		bob = users.bobIn(zeCollection).getId();
		charles = users.charlesIn(zeCollection).getId();
		dakota = users.dakotaIn(zeCollection).getId();
		edouard = users.edouardIn(zeCollection).getId();
		gandalf = users.gandalfIn(zeCollection).getId();
		chuckNorris = users.chuckNorrisIn(zeCollection).getId();

		Transaction transaction = new Transaction();
		filingSpace1WithUserAliceBob = transaction.add(rm.newFilingSpace()
				.setCode("1").setTitle("Filing space 1").setUsers(asList(alice, bob)));

		filingSpace2WithUserBobCharles = transaction.add(rm.newFilingSpace()
				.setCode("2").setTitle("Filing space 2").setUsers(asList(bob, charles)));

		filingSpace3WithAdminCharlesDakota = transaction.add(rm.newFilingSpace()
				.setCode("3").setTitle("Filing space 3").setAdministrators(asList(charles, dakota)));

		filingSpace4WithAdminDakotaEdouard = transaction.add(rm.newFilingSpace()
				.setCode("4").setTitle("Filing space 4").setAdministrators(asList(dakota, edouard)));

		filingSpace5WithUserDakotaEdouardAndAdministratorGandalfChuckNorris = transaction.add(rm.newFilingSpace()
				.setCode("5").setTitle("Filing space 5")
				.setUsers(asList(dakota, edouard)).setAdministrators(asList(gandalf, chuckNorris)));

		filingSpace6 = transaction.add(rm.newFilingSpace()
				.setCode("6").setTitle("Filing space 6"));

		filingSpace7 = transaction.add(rm.newFilingSpace()
				.setCode("7").setTitle("Filing space 7"));

		recordServices = getModelLayerFactory().newRecordServices();
		recordServices.execute(transaction);

		administrativeUnit = rm.newAdministrativeUnitWithId("42").setCode("Ze 42").setTitle("Ze administrative unit");
	}

	@Test
	public void givenAnAdministrativeUnitWithoutUsersWhenAssignNewFilingSpaceWithUserThenAuthCreated()
			throws Exception {

		//		getDataLayerFactory().getDataLayerLogger().monitor("idx_rfc_00000000007");
		//		getDataLayerFactory().getDataLayerLogger().monitor("00000000007");

		recordServices.add(administrativeUnit);
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targettingNoPrincipals());
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targettingNoPrincipals());

		waitForBatchProcess();
		recordServices.refresh(administrativeUnit);
		recordServices.update(administrativeUnit.setFilingSpaces(asList(filingSpace1WithUserAliceBob)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targetting(alice, bob));
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targettingNoPrincipals());

		waitForBatchProcess();
		recordServices.refresh(administrativeUnit);
		recordServices.update(
				administrativeUnit.setFilingSpaces(asList(filingSpace1WithUserAliceBob, filingSpace2WithUserBobCharles)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targetting(alice, bob, charles));
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targettingNoPrincipals());

		waitForBatchProcess();
	}

	@Test
	public void givenAnAdministrativeWithUsersWhenNoMoreUserThenAuthDeleted()
			throws Exception {

		recordServices.add(administrativeUnit
				.setFilingSpaces(asList(filingSpace1WithUserAliceBob, filingSpace3WithAdminCharlesDakota)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targetting(alice, bob));
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targetting(charles, dakota));

		recordServices.update(administrativeUnit.setFilingSpaces(asList(filingSpace3WithAdminCharlesDakota)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targettingNoPrincipals());
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targetting(charles, dakota));
	}

	@Test
	public void givenAnAdministrativeUnitWithoutAdministratorsWhenAssignNewFilingSpaceWithUserThenAuthCreated()
			throws Exception {
		recordServices.add(administrativeUnit);
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targettingNoPrincipals());
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targettingNoPrincipals());

		recordServices.refresh(administrativeUnit);
		recordServices.update(administrativeUnit.setFilingSpaces(asList(filingSpace3WithAdminCharlesDakota)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targettingNoPrincipals());
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targetting(charles, dakota));

		recordServices.refresh(administrativeUnit);
		recordServices.update(administrativeUnit
				.setFilingSpaces(asList(filingSpace3WithAdminCharlesDakota, filingSpace4WithAdminDakotaEdouard)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targettingNoPrincipals());
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targetting(charles, dakota, edouard));
	}

	@Test
	public void givenAnAdministrativeWithAdministratorsWhenNoMoreUserThenAuthDeleted()
			throws Exception {

		recordServices.add(administrativeUnit
				.setFilingSpaces(asList(filingSpace1WithUserAliceBob, filingSpace3WithAdminCharlesDakota)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targetting(alice, bob));
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targetting(charles, dakota));

		recordServices.refresh(administrativeUnit);
		recordServices.update(administrativeUnit.setFilingSpaces(asList(filingSpace1WithUserAliceBob)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targetting(alice, bob));
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targettingNoPrincipals());
	}

	@Test
	public void whenPhysicallyDeletingAnAdministrativeUnitWithUsersAndManagersThenBothAuthorizationsDeleted()
			throws Exception {
		recordServices.add(administrativeUnit
				.setFilingSpaces(asList(filingSpace1WithUserAliceBob, filingSpace3WithAdminCharlesDakota)));
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targetting(alice, bob));
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targetting(charles, dakota));

		recordServices.logicallyDelete(administrativeUnit.getWrappedRecord(), User.GOD);
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targetting(alice, bob));
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targetting(charles, dakota));

		recordServices.physicallyDelete(administrativeUnit.getWrappedRecord(), User.GOD);
		assertThat(authorization(userAuth)).isNull();
		assertThat(authorization(adminAuth)).isNull();
	}

	@Test
	public void givenAnAdministrativeUnitWithoutUsersWhenAssignUsersAndManagersToItsFilingspacesThenBothAuthCreated()
			throws Exception {

		recordServices.add(administrativeUnit.setFilingSpaces(asList(filingSpace6, filingSpace7)));

		recordServices.update(filingSpace6.setAdministrators(asList(alice, bob)));
		recordServices.update(filingSpace7.setUsers(asList(bob, charles)));

		waitForBatchProcess();
		assertThat(authorization(userAuth)).has(userRoleAndWriteAccess()).is(targetting(bob, charles));
		assertThat(authorization(adminAuth)).has(managerRoleAndWriteDeleteAccess()).is(targetting(alice, bob));

	}

	// ---------------------------------------------

	private Authorization authorization(String id) {
		try {
			return getModelLayerFactory().newAuthorizationsServices().getAuthorization(zeCollection, id);
		} catch (NoSuchAuthorizationWithId e) {
			return null;
		}
	}

	private Condition<? super Authorization> userRoleAndWriteAccess() {
		return new Condition<Authorization>() {
			@Override
			public boolean matches(Authorization authorization) {
				assertThat(authorization).isNotNull();
				assertThat(authorization.getDetail().getRoles()).containsOnly(RMRoles.USER, Role.WRITE);
				return true;
			}
		};
	}

	private Condition<? super Authorization> managerRoleAndWriteDeleteAccess() {
		return new Condition<Authorization>() {
			@Override
			public boolean matches(Authorization authorization) {
				assertThat(authorization).isNotNull();
				assertThat(authorization.getDetail().getRoles())
						.containsOnly(RMRoles.MANAGER, Role.WRITE, Role.DELETE);
				return true;
			}
		};
	}

	private Condition<? super Authorization> targetting(final String... users) {
		return new Condition<Authorization>() {
			@Override
			public boolean matches(Authorization authorization) {
				assertThat(authorization).isNotNull();
				assertThat(authorization.getGrantedToPrincipals()).containsOnly(users);
				return true;
			}
		};
	}

	private Condition<? super Authorization> targettingNoPrincipals() {
		return new Condition<Authorization>() {
			@Override
			public boolean matches(Authorization authorization) {
				assertThat(authorization).isNotNull();
				assertThat(authorization.getGrantedToPrincipals()).isEmpty();
				return true;
			}
		};
	}

}
