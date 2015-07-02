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
package com.constellio.app.modules.rm.reports.search.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.administration.plan.UserReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_FilingSpace;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_User;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportPresenter;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.model.services.users.UserServices;

public class StatsReportPresenterManualAcceptTest extends ReportBuilderTestFramework {
	private String[] filingSpaceFields = { "firstName", "lastName", "userName" };

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private UserReportPresenter presenter;

	private UserReportModel_User referenceUserBob;
	private UserReportModel_User referenceUserCharles;
	private UserReportModel_User referenceUserGandalf;
	private UserReportModel_User referenceUserDakota;
	private UserReportModel_User referenceUserEdouard;
	private UserReportModel_User referenceUserChuck;
	private UserReportModel_User referenceUserAdmin;

	private UserReportModel_FilingSpace referenceFilingSpaceA;
	private UserReportModel_FilingSpace referenceFilingSpaceB;
	private UserReportModel_FilingSpace referenceFilingSpaceC;
	private UserReportModel_FilingSpace referenceFilingSpaceD;
	private UserReportModel_FilingSpace referenceFilingSpaceE;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
		);

		presenter = new UserReportPresenter(zeCollection, getModelLayerFactory());

		UserServices userServices = getModelLayerFactory().newUserServices();
		userServices.removeUserFromCollection(userServices.getUserCredential(aliceWonderland), zeCollection);

		setupUsers();

		setupFilingSpaces();

	}

	private void setupUsers() {
		referenceUserBob = new UserReportModel_User();
		referenceUserBob.setFirstName("Bob 'Elvis'");
		referenceUserBob.setLastName("Gratton");
		referenceUserBob.setUserName("bob");
		referenceUserBob.setUserId(records.getBob_userInAC().getId());

		referenceUserCharles = new UserReportModel_User();
		referenceUserCharles.setFirstName("Charles-François");
		referenceUserCharles.setLastName("Xavier");
		referenceUserCharles.setUserName("charles");
		referenceUserCharles.setUserId(records.getCharles_userInA().getId());

		referenceUserGandalf = new UserReportModel_User();
		referenceUserGandalf.setFirstName("Gandalf");
		referenceUserGandalf.setLastName("Leblanc");
		referenceUserGandalf.setUserName("gandalf");
		referenceUserGandalf.setUserId(records.getGandalf_managerInABC().getId());

		referenceUserDakota = new UserReportModel_User();
		referenceUserDakota.setFirstName("Dakota");
		referenceUserDakota.setLastName("L'Indien");
		referenceUserDakota.setUserName("dakota");
		referenceUserDakota.setUserId(records.getDakota_managerInA_userInB().getId());

		referenceUserEdouard = new UserReportModel_User();
		referenceUserEdouard.setFirstName("Edouard");
		referenceUserEdouard.setLastName("Lechat");
		referenceUserEdouard.setUserName("edouard");
		referenceUserEdouard.setUserId(records.getEdouard_managerInB_userInC().getId());

		referenceUserChuck = new UserReportModel_User();
		referenceUserChuck.setFirstName("Chuck");
		referenceUserChuck.setLastName("Norris");
		referenceUserChuck.setUserName("chuck");
		referenceUserChuck.setUserId(records.getChuckNorris().getId());

		referenceUserAdmin = new UserReportModel_User();
		referenceUserAdmin.setFirstName("System");
		referenceUserAdmin.setLastName("Admin");
		referenceUserAdmin.setUserName("admin");
		referenceUserAdmin.setUserId(records.getAdmin().getId());
	}

	private void setupFilingSpaces() {

		referenceFilingSpaceA = new UserReportModel_FilingSpace();
		referenceFilingSpaceA.setCode("A");
		referenceFilingSpaceA.setLabel("Room A");
		referenceFilingSpaceA.setDescription("");

		referenceFilingSpaceB = new UserReportModel_FilingSpace();
		referenceFilingSpaceB.setCode("B");
		referenceFilingSpaceB.setLabel("Room B");
		referenceFilingSpaceB.setDescription("");

		referenceFilingSpaceC = new UserReportModel_FilingSpace();
		referenceFilingSpaceC.setCode("C");
		referenceFilingSpaceC.setLabel("Room C");
		referenceFilingSpaceC.setDescription("");

		referenceFilingSpaceD = new UserReportModel_FilingSpace();
		referenceFilingSpaceD.setCode("D");
		referenceFilingSpaceD.setLabel("Room D");
		referenceFilingSpaceD.setDescription("");

		referenceFilingSpaceE = new UserReportModel_FilingSpace();
		referenceFilingSpaceE.setCode("E");
		referenceFilingSpaceE.setLabel("Room E");
		referenceFilingSpaceE.setDescription("");
	}

	@Test
	public void givenWhenBuildingModelThenGetAppropriateModel() {
		UserReportModel model = presenter.build();

		assertThat(model.getUsers().size()).isEqualTo(7);

		verifyChuck(model);

		verifyBob(model);

		verifyGandlaf(model);

		verifyCharles(model);

		verifyEdoudard(model);

		verifyDakota(model);

		verifyAdmin(model);

		build(new UserReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));

	}

	private void verifyChuck(UserReportModel model) {
		UserReportModel_User userChuck = getUser(model, "Chuck");
		assertThat(userChuck).isEqualToComparingOnlyGivenFields(referenceUserChuck, filingSpaceFields);

		assertThat(userChuck.getFilingSpaces()).isEmpty();
	}

	private void verifyBob(UserReportModel model) {
		UserReportModel_User userBob = getUser(model, "Bob 'Elvis'");
		assertThat(userBob).isEqualToComparingOnlyGivenFields(referenceUserBob, filingSpaceFields);

		assertThat(userBob.getFilingSpaces().size()).isEqualTo(2);
		UserReportModel_FilingSpace filingSpaceA = getFilingSpace(userBob, "A");
		assertThat(filingSpaceA).isEqualToComparingFieldByField(referenceFilingSpaceA);

		UserReportModel_FilingSpace filingSpaceC = getFilingSpace(userBob, "C");
		assertThat(filingSpaceC).isEqualToComparingFieldByField(referenceFilingSpaceC);

	}

	private void verifyGandlaf(UserReportModel model) {
		UserReportModel_User userGandalf = getUser(model, "Gandalf");
		assertThat(userGandalf).isEqualToComparingOnlyGivenFields(referenceUserGandalf, filingSpaceFields);

		assertThat(userGandalf.getFilingSpaces()).isNotNull();
	}

	private void verifyCharles(UserReportModel model) {
		UserReportModel_User userCharles = getUser(model, "Charles-François");
		assertThat(userCharles).isEqualToComparingOnlyGivenFields(referenceUserCharles, filingSpaceFields);

		assertThat(userCharles.getFilingSpaces().size()).isEqualTo(1);
		UserReportModel_FilingSpace filingSpaceA = getFilingSpace(userCharles, "A");
		assertThat(filingSpaceA).isEqualToComparingFieldByField(referenceFilingSpaceA);
	}

	private void verifyEdoudard(UserReportModel model) {
		UserReportModel_User userEdouard = getUser(model, "Edouard");
		assertThat(userEdouard).isEqualToComparingOnlyGivenFields(referenceUserEdouard, filingSpaceFields);

		assertThat(userEdouard.getFilingSpaces().size()).isEqualTo(2);
		UserReportModel_FilingSpace filingSpaceC = getFilingSpace(userEdouard, "C");
		assertThat(filingSpaceC).isEqualToComparingFieldByField(referenceFilingSpaceC);

	}

	private void verifyDakota(UserReportModel model) {
		UserReportModel_User userDakota = getUser(model, "Dakota");
		assertThat(userDakota).isEqualToComparingOnlyGivenFields(referenceUserDakota, filingSpaceFields);

		assertThat(userDakota.getFilingSpaces().size()).isEqualTo(2);
		UserReportModel_FilingSpace filingSpaceB = getFilingSpace(userDakota, "B");
		assertThat(filingSpaceB).isEqualToComparingFieldByField(referenceFilingSpaceB);
	}

	private void verifyAdmin(UserReportModel model) {
		UserReportModel_User userAdmin = getUser(model, "System");
		assertThat(userAdmin).isEqualToComparingOnlyGivenFields(referenceUserAdmin, filingSpaceFields);

		assertThat(userAdmin.getFilingSpaces().size()).isEqualTo(3);

	}

	private UserReportModel_User getUser(UserReportModel model, String firstName) {
		UserReportModel_User userFound = null;
		for (UserReportModel_User user : model.getUsers()) {
			if (user.getFirstName().equals(firstName))
				userFound = user;
		}

		if (userFound == null) {
			fail("Did not find user " + firstName);
		}
		return userFound;
	}

	private UserReportModel_FilingSpace getFilingSpace(UserReportModel_User user, String code) {
		UserReportModel_FilingSpace filingSpaceFound = null;
		for (UserReportModel_FilingSpace filingSpace : user.getFilingSpaces()) {
			if (filingSpace.getCode().equals(code)) {
				filingSpaceFound = filingSpace;
			}
		}

		if (filingSpaceFound == null) {
			fail("Did not find filing space " + code);
		}
		return filingSpaceFound;
	}
}