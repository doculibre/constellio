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
package com.constellio.app.modules.rm.reports.administration.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_FilingSpace;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_User;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportPresenter;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class AdministrativeUnitReportPresenterManualAcceptTest extends ReportBuilderTestFramework {

	RMTestRecords records = new RMTestRecords(zeCollection);
	AdministrativeUnitReportPresenter presenter;
	AdministrativeUnitReportModel_User referenceUserBob;
	AdministrativeUnitReportModel_User referenceUserCharles;
	AdministrativeUnitReportModel_User referenceUserGandalf;
	AdministrativeUnitReportModel_User referenceUserDakota;
	AdministrativeUnitReportModel_User referenceUserEdouard;
	AdministrativeUnitReportModel_User referenceUserChuck;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		presenter = new AdministrativeUnitReportPresenter(zeCollection, getModelLayerFactory());

		referenceUserBob = new AdministrativeUnitReportModel_User();
		referenceUserBob.setFirstName("Bob 'Elvis'");
		referenceUserBob.setLastName("Gratton");
		referenceUserBob.setUserName("bob");
		referenceUserBob.setEmail("bob@doculibre.com");

		referenceUserCharles = new AdministrativeUnitReportModel_User();
		referenceUserCharles.setFirstName("Charles-François");
		referenceUserCharles.setLastName("Xavier");
		referenceUserCharles.setUserName("charles");
		referenceUserCharles.setEmail("charles@doculibre.com");

		referenceUserGandalf = new AdministrativeUnitReportModel_User();
		referenceUserGandalf.setFirstName("Gandalf");
		referenceUserGandalf.setLastName("Leblanc");
		referenceUserGandalf.setUserName("gandalf");
		referenceUserGandalf.setEmail("gandalf@doculibre.com");

		referenceUserDakota = new AdministrativeUnitReportModel_User();
		referenceUserDakota.setFirstName("Dakota");
		referenceUserDakota.setLastName("L'Indien");
		referenceUserDakota.setUserName("dakota");
		referenceUserDakota.setEmail("dakota@doculibre.com");

		referenceUserEdouard = new AdministrativeUnitReportModel_User();
		referenceUserEdouard.setFirstName("Edouard");
		referenceUserEdouard.setLastName("Lechat");
		referenceUserEdouard.setUserName("edouard");
		referenceUserEdouard.setEmail("edouard@doculibre.com");

		referenceUserChuck = new AdministrativeUnitReportModel_User();
		referenceUserChuck.setFirstName("Chuck");
		referenceUserChuck.setLastName("Norris");
		referenceUserChuck.setUserName("chuck");
		referenceUserChuck.setEmail("chuck@doculibre.com");

	}

	@Test
	public void givenAdministrativeUnitsTreeWhenBuildingModelWithUsersThenGetAppropriateModel() {
		AdministrativeUnitReportModel model = presenter.build();
		assertThat(model.getAdministrativeUnits()).isNotNull();
		assertThat(model.getAdministrativeUnits().size()).isEqualTo(3);

		testAdministrativeUnit10(model);

		testAdministrativeUnit11(model);

		testAdministrativeUnit12(model);

		testAdministrativeUnit20(model);

		testAdministrativeUnit30(model);

		build(new AdministrativeUnitReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));

	}

	@Test
	public void givenAdministrativeUnitsTreeWhenBuildingModelWithoutUsersThenGetAppropriateModel() {
		presenter = new AdministrativeUnitReportPresenter(zeCollection, getModelLayerFactory(), false);
		AdministrativeUnitReportModel model = presenter.build();
		assertThat(model.getAdministrativeUnits()).isNotNull();

		build(new AdministrativeUnitReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));

	}

	private void testAdministrativeUnit10(AdministrativeUnitReportModel model) {
		assertThat(model.getAdministrativeUnits()).extracting("code").containsOnlyOnce("10");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");

		assertThat(administrativeUnit10.getLabel()).isEqualTo("Administrative unit with room A");
		assertThat(administrativeUnit10.getDescription()).isEmpty();
		assertThat(administrativeUnit10.getFilingSpaces().size()).isEqualTo(1);

		AdministrativeUnitReportModel_FilingSpace filingSpaceA = getFilingSpace(administrativeUnit10, "A");
		assertThat(filingSpaceA.getCode()).isEqualTo("A");
		assertThat(filingSpaceA.getLabel()).isEqualTo("Room A");
		assertThat(filingSpaceA.getDescription()).isEmpty();

		List<AdministrativeUnitReportModel_User> usersA = filingSpaceA.getUsers();
		assertThat(usersA.size()).isEqualTo(3);

		assertThat(usersA).extracting("firstName").containsOnlyOnce("Bob 'Elvis'");
		AdministrativeUnitReportModel_User userBob = getUser(usersA, "Bob 'Elvis'");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		assertThat(usersA).extracting("firstName").containsOnlyOnce("Charles-François");
		AdministrativeUnitReportModel_User userCharles = getUser(usersA, "Charles-François");
		assertThat(userCharles).isEqualToComparingFieldByField(referenceUserCharles);

		List<AdministrativeUnitReportModel_User> administratorsA = filingSpaceA.getAdministrators();
		assertThat(administratorsA.size()).isEqualTo(2);

		assertThat(administratorsA).extracting("firstName").containsOnlyOnce("Dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(administratorsA, "Dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		assertThat(administratorsA).extracting("firstName").containsOnlyOnce("Gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(administratorsA, "Gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);

	}

	private void testAdministrativeUnit11(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		assertThat(administrativeUnit10.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("11");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit11 = getUnit(administrativeUnit10, "11");
		assertThat(administrativeUnit11.getLabel()).isEqualTo("Administrative unit with room B");
		assertThat(administrativeUnit11.getDescription()).isEmpty();
		assertThat(administrativeUnit11.getFilingSpaces().size()).isEqualTo(1);

		AdministrativeUnitReportModel_FilingSpace filingSpaceB = getFilingSpace(administrativeUnit11, "B");
		assertThat(filingSpaceB.getCode()).isEqualTo("B");
		assertThat(filingSpaceB.getLabel()).isEqualTo("Room B");
		assertThat(filingSpaceB.getDescription()).isEmpty();

		List<AdministrativeUnitReportModel_User> usersB = filingSpaceB.getUsers();
		assertThat(usersB.size()).isEqualTo(2);

		assertThat(usersB).extracting("firstName").containsOnlyOnce("Dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersB, "Dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		List<AdministrativeUnitReportModel_User> administratorsB = filingSpaceB.getAdministrators();
		assertThat(administratorsB.size()).isEqualTo(2);

		assertThat(administratorsB).extracting("firstName").containsOnlyOnce("Edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(administratorsB, "Edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(administratorsB).extracting("firstName").containsOnlyOnce("Gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(administratorsB, "Gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);

	}

	private void testAdministrativeUnit12(AdministrativeUnitReportModel model) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit10 = getUnit(model, "10");
		assertThat(administrativeUnit10.getChildAdministrativeUnits()).extracting("code").containsOnlyOnce("12");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit12 = getUnit(administrativeUnit10, "12");
		assertThat(administrativeUnit12.getLabel()).isEqualTo("Administrative unit with room B and C");
		assertThat(administrativeUnit12.getDescription()).isEmpty();
		assertThat(administrativeUnit12.getFilingSpaces().size()).isEqualTo(2);

		AdministrativeUnitReportModel_FilingSpace filingSpaceB = getFilingSpace(administrativeUnit12, "B");
		assertThat(filingSpaceB.getCode()).isEqualTo("B");
		assertThat(filingSpaceB.getLabel()).isEqualTo("Room B");
		assertThat(filingSpaceB.getDescription()).isEmpty();

		List<AdministrativeUnitReportModel_User> usersB = filingSpaceB.getUsers();
		assertThat(usersB.size()).isEqualTo(2);

		assertThat(usersB).extracting("firstName").containsOnlyOnce("Dakota");
		AdministrativeUnitReportModel_User userDakota = getUser(usersB, "Dakota");
		assertThat(userDakota).isEqualToComparingFieldByField(referenceUserDakota);

		AdministrativeUnitReportModel_FilingSpace filingSpaceC = getFilingSpace(administrativeUnit12, "C");
		assertThat(filingSpaceC.getCode()).isEqualTo("C");
		assertThat(filingSpaceC.getLabel()).isEqualTo("Room C");
		assertThat(filingSpaceC.getDescription()).isEmpty();

		List<AdministrativeUnitReportModel_User> administratorsB = filingSpaceB.getAdministrators();
		assertThat(administratorsB.size()).isEqualTo(2);

		assertThat(administratorsB).extracting("firstName").containsOnlyOnce("Edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(administratorsB, "Edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(administratorsB).extracting("firstName").containsOnlyOnce("Gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(administratorsB, "Gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);

		List<AdministrativeUnitReportModel_User> usersC = filingSpaceC.getUsers();
		assertThat(usersC.size()).isEqualTo(3);

		assertThat(usersC).extracting("firstName").containsOnlyOnce("Edouard");
		userEdouard = getUser(usersC, "Edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersC).extracting("firstName").containsOnlyOnce("Bob 'Elvis'");
		AdministrativeUnitReportModel_User userBob = getUser(usersC, "Bob 'Elvis'");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		List<AdministrativeUnitReportModel_User> administratorsC = filingSpaceC.getAdministrators();
		assertThat(administratorsC.size()).isEqualTo(1);

		assertThat(administratorsC).extracting("firstName").containsOnlyOnce("Gandalf");
		userGandalf = getUser(administratorsC, "Gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);

	}

	private void testAdministrativeUnit20(AdministrativeUnitReportModel model) {
		assertThat(model.getAdministrativeUnits()).extracting("code").containsOnlyOnce("20");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit20 = getUnit(model, "20");
		assertThat(administrativeUnit20.getLabel()).isEqualTo("Administrative unit with room D");
		assertThat(administrativeUnit20.getDescription()).isEmpty();
		assertThat(administrativeUnit20.getFilingSpaces().size()).isEqualTo(2);

		AdministrativeUnitReportModel_FilingSpace filingSpaceD = getFilingSpace(administrativeUnit20, "D");
		assertThat(filingSpaceD.getCode()).isEqualTo("D");
		assertThat(filingSpaceD.getLabel()).isEqualTo("Room D");
		assertThat(filingSpaceD.getDescription()).isEmpty();

		List<AdministrativeUnitReportModel_User> usersD = filingSpaceD.getUsers();
		assertThat(usersD).isEmpty();

		List<AdministrativeUnitReportModel_User> administratorsD = filingSpaceD.getAdministrators();
		assertThat(administratorsD).isEmpty();

		AdministrativeUnitReportModel_FilingSpace filingSpaceE = getFilingSpace(administrativeUnit20, "E");
		assertThat(filingSpaceE.getCode()).isEqualTo("E");
		assertThat(filingSpaceE.getLabel()).isEqualTo("Room E");
		assertThat(filingSpaceE.getDescription()).isEmpty();

		List<AdministrativeUnitReportModel_User> usersE = filingSpaceE.getUsers();
		assertThat(usersE).isEmpty();

		List<AdministrativeUnitReportModel_User> administratorsE = filingSpaceE.getAdministrators();
		assertThat(administratorsE).isEmpty();

	}

	private void testAdministrativeUnit30(AdministrativeUnitReportModel model) {
		assertThat(model.getAdministrativeUnits()).extracting("code").containsOnlyOnce("30");
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit30 = getUnit(model, "30");
		assertThat(administrativeUnit30.getLabel()).isEqualTo("Administrative unit without filing spaces");
		assertThat(administrativeUnit30.getDescription()).isEmpty();

		AdministrativeUnitReportModel_FilingSpace filingSpaceC = getFilingSpace(administrativeUnit30, "C");
		assertThat(filingSpaceC.getCode()).isEqualTo("C");
		assertThat(filingSpaceC.getLabel()).isEqualTo("Room C");
		assertThat(filingSpaceC.getDescription()).isEmpty();

		List<AdministrativeUnitReportModel_User> usersC = filingSpaceC.getUsers();
		assertThat(usersC.size()).isEqualTo(3);

		assertThat(usersC).extracting("firstName").containsOnlyOnce("Edouard");
		AdministrativeUnitReportModel_User userEdouard = getUser(usersC, "Edouard");
		assertThat(userEdouard).isEqualToComparingFieldByField(referenceUserEdouard);

		assertThat(usersC).extracting("firstName").containsOnlyOnce("Bob 'Elvis'");
		AdministrativeUnitReportModel_User userBob = getUser(usersC, "Bob 'Elvis'");
		assertThat(userBob).isEqualToComparingFieldByField(referenceUserBob);

		List<AdministrativeUnitReportModel_User> administratorsC = filingSpaceC.getAdministrators();
		assertThat(administratorsC.size()).isEqualTo(1);

		assertThat(administratorsC).extracting("firstName").containsOnlyOnce("Gandalf");
		AdministrativeUnitReportModel_User userGandalf = getUser(administratorsC, "Gandalf");
		assertThat(userGandalf).isEqualToComparingFieldByField(referenceUserGandalf);

	}

	private AdministrativeUnitReportModel_AdministrativeUnit getUnit(AdministrativeUnitReportModel model, String code) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnitFound = null;
		for (AdministrativeUnitReportModel_AdministrativeUnit unit : model.getAdministrativeUnits()) {
			if (unit.getCode().equals(code)) {
				administrativeUnitFound = unit;
			}
		}
		if (administrativeUnitFound == null) {
			fail("Did not find administrative unit " + code);
		}

		return administrativeUnitFound;
	}

	private AdministrativeUnitReportModel_AdministrativeUnit getUnit(
			AdministrativeUnitReportModel_AdministrativeUnit parentUnit, String code) {
		AdministrativeUnitReportModel_AdministrativeUnit administrativeUnitFound = null;
		for (AdministrativeUnitReportModel_AdministrativeUnit unit : parentUnit.getChildAdministrativeUnits()) {
			if (unit.getCode().equals(code)) {
				administrativeUnitFound = unit;
			}
		}
		if (administrativeUnitFound == null) {
			fail("Did not find child administrative unit " + code);
		}

		return administrativeUnitFound;
	}

	private AdministrativeUnitReportModel_FilingSpace getFilingSpace(
			AdministrativeUnitReportModel_AdministrativeUnit unit, String code) {
		AdministrativeUnitReportModel_FilingSpace filingSpaceFound = null;
		for (AdministrativeUnitReportModel_FilingSpace filingSpace : unit.getFilingSpaces()) {
			if (filingSpace.getCode().equals(code)) {
				filingSpaceFound = filingSpace;
			}
		}
		if (filingSpaceFound == null) {
			fail("Did not find filling space " + code);
		}

		return filingSpaceFound;
	}

	private AdministrativeUnitReportModel_User getUser(List<AdministrativeUnitReportModel_User> users, String firstName) {
		AdministrativeUnitReportModel_User userFound = null;

		for (AdministrativeUnitReportModel_User user : users) {
			if (user.getFirstName().equals(firstName)) {
				userFound = user;
			}
		}

		if (userFound == null) {
			fail("Did not find user " + firstName);
		}

		return userFound;
	}
}