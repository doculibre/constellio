package com.constellio.app.modules.rm.reports.administration.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.builders.administration.plan.UserReportBuilder;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_User;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportPresenter;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.model.services.users.UserServices;

public class UserReportPresenterManualAcceptTest extends ReportBuilderTestFramework {
	//	private String[] filingSpaceFields = { "firstName", "lastName", "userName" };

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private UserReportPresenter presenter;

	private UserReportModel_User referenceUserBob;
	private UserReportModel_User referenceUserCharles;
	private UserReportModel_User referenceUserGandalf;
	private UserReportModel_User referenceUserDakota;
	private UserReportModel_User referenceUserEdouard;
	private UserReportModel_User referenceUserChuck;
	private UserReportModel_User referenceUserAdmin;

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
		assertThat(userChuck).isEqualToComparingOnlyGivenFields(referenceUserChuck);
	}

	private void verifyBob(UserReportModel model) {
		UserReportModel_User userBob = getUser(model, "Bob 'Elvis'");
		assertThat(userBob).isEqualToComparingOnlyGivenFields(referenceUserBob);

	}

	private void verifyGandlaf(UserReportModel model) {
		UserReportModel_User userGandalf = getUser(model, "Gandalf");
		assertThat(userGandalf).isEqualToComparingOnlyGivenFields(referenceUserGandalf);
	}

	private void verifyCharles(UserReportModel model) {
		UserReportModel_User userCharles = getUser(model, "Charles-François");
		assertThat(userCharles).isEqualToComparingOnlyGivenFields(referenceUserCharles);
	}

	private void verifyEdoudard(UserReportModel model) {
		UserReportModel_User userEdouard = getUser(model, "Edouard");
		assertThat(userEdouard).isEqualToComparingOnlyGivenFields(referenceUserEdouard);

	}

	private void verifyDakota(UserReportModel model) {
		UserReportModel_User userDakota = getUser(model, "Dakota");
		assertThat(userDakota).isEqualToComparingOnlyGivenFields(referenceUserDakota);
	}

	private void verifyAdmin(UserReportModel model) {
		UserReportModel_User userAdmin = getUser(model, "System");
		assertThat(userAdmin).isEqualToComparingOnlyGivenFields(referenceUserAdmin);

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
}